package com.gelonggld.db2bkg.dialogs

import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.model.ModelField
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.DialogUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import com.gelonggld.db2bkg.model.TableData
import com.gelonggld.db2bkg.utils.JFXUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import javax.swing.*
import java.awt.event.*
import java.sql.Connection
import java.sql.SQLException
import java.util.*
@Suppress("unused")
class DBCompareDialog(private val dbTable: String, dbName: String, private val project: Project, private val virtualFile: VirtualFile,
                      private val connection: Connection) : JDialog() {
    lateinit var contentPane: JPanel
    lateinit var buttonOK: JButton
    lateinit var buttonCancel: JButton
    lateinit var content: JPanel
    lateinit var errorInfo: JLabel
    private var jfxPanel: JFXPanel

    private var noMatchDBField: List<DBField>? = null
    private var noMatchPsiField: MutableList<ModelField>? = null
    private var matchs: MutableMap<ModelField, DBField>? = null
    private var tableDatas: ArrayList<TableData>


    private fun compareBeanWithDB(tableName: String, connection: Connection, dbName: String) {
        val dbFields = SqlUtil.getDBFields(tableName, connection, dbName) ?: return
        matchs = HashMap()
        noMatchDBField = ArrayList()
        noMatchPsiField = ArrayList()
        val classFields = FileDispatch.allField(virtualFile) ?: let {
            return
        }
        for (field in classFields) {
            val modelField = FileDispatch.createModelField(field)
            val popDBField = popDBFieldByName(modelField, dbFields)
            if (popDBField != null) {
                matchs!![modelField] = popDBField
            } else {
                noMatchPsiField!!.add(modelField)
            }
        }
        noMatchDBField = dbFields
    }


    private fun popDBFieldByName(modelField: ModelField, dbFields: MutableList<DBField>): DBField? {
        var checked: DBField? = null
        for (dbField in dbFields) {
            if (DBConvertUtil.beanField2DB(modelField.name!!) == dbField.name) {
                checked = dbField
                break
            }
        }
        if (checked != null) {
            dbFields.remove(checked)
        }
        return checked
    }

    @Suppress("unused")
    private fun error(s: String) {
        errorInfo.text = s
    }

    private fun createTableView(): TableView<TableData> {
        val tableView = TableView<TableData>()
        tableView.minWidth = 1580.0
        createTableTitle(tableView)
        return tableView
    }

    private fun createTableTitle(tableView: TableView<TableData>) {
        val mField = createTableColumn("模型字段", 120, PropertyValueFactory("mName"))
        val mType = createTableColumn("模型类型", 120, PropertyValueFactory("mType"))
        val mNote = createTableColumn("模型注释", 120, PropertyValueFactory("mNote"))

        val mExt = TableColumn<TableData, Any>("字段附加信息")
        val fieldRangle = createTableColumn("字段长度", 100, PropertyValueFactory("fieldRang"))
        val defaultValue = createTableColumn("默认值", 100, PropertyValueFactory("defaultValue"))
        val canNull = createTableColumn("可空", 100, PropertyValueFactory("canNone"))
        mExt.columns.addAll(fieldRangle, defaultValue, canNull)

        val opt = TableColumn<TableData, Any>("操作")
        val deleteOpt = createTableColumn("删除", 171, PropertyValueFactory("deleteOpt"))
        val addOpt = createTableColumn("添加", 171, PropertyValueFactory("addOpt"))
        val doNone = createTableColumn("无操作", 100, PropertyValueFactory("doNone"))
        opt.columns.addAll(deleteOpt, addOpt, doNone)

        val dField = createTableColumn("数据库字段", 120, PropertyValueFactory("dName"))
        val dType = createTableColumn("数据库类型", 120, PropertyValueFactory("dType"))
        val dNote = createTableColumn("数据库注解", 120, PropertyValueFactory("dNote"))
        val dExt = createTableColumn("模型附加信息", 120, PropertyValueFactory("dExt"))

        tableView.columns.addAll(mField, mType, mNote, mExt, opt, dField, dType, dNote, dExt)
        val datas = FXCollections.observableArrayList(tableDatas)
        tableView.items = datas
    }

    private fun createTableColumn(columnName: String, minWidth: Int, mName: PropertyValueFactory<TableData, Any>): TableColumn<TableData, Any> {
        val mField = TableColumn<TableData, Any>(columnName)
        mField.minWidth = minWidth.toDouble()
        mField.cellValueFactory = mName
        return mField
    }

    private fun buildTableDatas(): ArrayList<TableData> {
        tableDatas = ArrayList()
        for (dbField in noMatchDBField!!) {
            tableDatas.add(TableData(dbField,FileDispatch.ifKt(virtualFile)))
        }
        for (modelField in noMatchPsiField!!) {
            tableDatas.add(TableData(modelField,FileDispatch.ifKt(virtualFile)))
        }
        for (entry in matchs!!.entries) {
            tableDatas.add(TableData(entry,FileDispatch.ifKt(virtualFile)))
        }
        return tableDatas
    }


    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK

        buttonOK.addActionListener { onOK() }

        buttonCancel.addActionListener { onCancel() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction({ onCancel() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        DialogUtil.centSelf(this, 1600, 900)
        jfxPanel = JFXPanel()
        content.add(jfxPanel)
    }

    private fun onOK() {
        // add your code here
        val addToModels = ArrayList<TableData>()
        val deleteFromModels = ArrayList<TableData>()
        val addCommitToModels = ArrayList<TableData>()
        val addToDBs = ArrayList<TableData>()
        val deleteFromDBs = ArrayList<TableData>()
        val addCommitToDBs = ArrayList<TableData>()

        for (tableData in tableDatas) {
            val select = tableData.selectRadio ?: continue
            when (select.text) {
                TableData.ADD_TO_MODEL -> addToModels.add(tableData)
                TableData.DELETE_FROM_MODEL -> deleteFromModels.add(tableData)
                TableData.ADD_COMMIT_TO_MODEL -> addCommitToModels.add(tableData)
                TableData.ADD_TO_DB -> addToDBs.add(tableData)
                TableData.DELETE_FROM_DB -> deleteFromDBs.add(tableData)
                TableData.ADD_COMMIT_TO_DB -> addCommitToDBs.add(tableData)
            }
        }


        motifyModel(addToModels, deleteFromModels, addCommitToModels)
        SqlUtil.motifyDBTable(addToDBs, deleteFromDBs, addCommitToDBs, dbTable, FileDispatch.ifKt(virtualFile),connection)
        recycleConnection()
        dispose()
    }


    private fun motifyModel(addToModels: List<TableData>, deleteFromModel: List<TableData>, addCommitToModel: List<TableData>) {
        val selectClass = FileDispatch.findClass(virtualFile)?:let {
            error("没有查找出对应的class ${virtualFile.name}")
            return
        }
        val runnable = {

            for (tableData in addToModels) {
                FileDispatch.addFieldAndGetSet( DBConvertUtil.dBField2Bean(tableData.getdName()!!),
                        DBConvertUtil.getDB2BeanMapType(tableData.getdType()!!,FileDispatch.ifKt(virtualFile)), tableData.getdNote()!!, false,selectClass)
            }

            for (tableData in deleteFromModel) {
                    FileDispatch.deleteFieldAndGetSet(tableData.getmName()!!, selectClass)
            }
            for (tableData in addCommitToModel) {
                val field = FileDispatch.findFieldByName(tableData.getmName()!!,selectClass)
                if(field != null){
                    FileDispatch.addCommentToField( tableData.getdNote()!!,field, selectClass)
                }else{
                    error("模型没有字段${tableData.getmName()}")
                }
            }
            FileDispatch.formatClass(selectClass)
        }

        val writeAction = { ApplicationManager.getApplication().runWriteAction(runnable) }

        com.intellij.openapi.command.CommandProcessor.getInstance().executeCommand(project, writeAction, "GenerDbBean", "GenerDbBean")
    }


    private fun recycleConnection() {
        try {
            connection.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun onCancel() {
        // add your code here if necessary
        recycleConnection()
        dispose()
    }


    init {
        compareBeanWithDB(dbTable, connection, dbName)
        tableDatas = buildTableDatas()
        JFXUtil.addTable(jfxPanel,createTableView())
    }
}

package com.gelonggld.db2bkg.dialogs

import com.gelonggld.db2bkg.model.TableCreate
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.DialogUtil
import com.gelonggld.db2bkg.utils.JFXUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiField
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javax.swing.*
import java.awt.event.*
import java.sql.Connection

class CreateTableDialog() : JDialog() {
    lateinit var contentPane: JPanel
    lateinit var buttonOK: JButton
    lateinit var buttonCancel: JButton
    lateinit var content: JPanel
    lateinit var errorInfo: JLabel
    private var jfxPanel: JFXPanel
    private lateinit var conn: Connection
    private lateinit var tableCreates: List<TableCreate>
    private lateinit var tableView: TableView<TableCreate>
    lateinit var tableName: String


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

    constructor(conn: Connection, selectFile: VirtualFile, tableName: String) : this() {
        this.conn = conn
        this.tableName = tableName
        val psiFields  = FileDispatch.allField(selectFile)
        this.tableCreates = psiFields?.map { TableCreate(it,FileDispatch.ifKt(selectFile)) } ?: ArrayList()
        JFXUtil.addTable(jfxPanel,createTableView())
    }



    private fun createTableView(): TableView<*> {
        tableView = TableView()
        tableView.minWidth = 1580.0
        createTableTitle(tableView)
        return tableView
    }

    private fun createTableTitle(tableView: TableView<TableCreate>) {
        val isPri = createTableColumn("主键", 150, PropertyValueFactory("isPri"))
        val fieldName = createTableColumn("模型字段名", 150, PropertyValueFactory("fieldName"))
        val fieldType = createTableColumn("字段类型", 150, PropertyValueFactory("fieldType"))
        val fieldComment = createTableColumn("字段注释", 150, PropertyValueFactory("fieldComment"))
        val fieldRang = createTableColumn("长度", 150, PropertyValueFactory("fieldRang"))
        val defaultValue = createTableColumn("默认值", 150, PropertyValueFactory("defaultValue"))
        val canNull = createTableColumn("可空", 150, PropertyValueFactory("canNull"))
        val autoIncrement = createTableColumn("自增长", 150, PropertyValueFactory("autoIncrement"))
        val dbFieldName = createTableColumn("数据库字段名", 150, PropertyValueFactory("dbFieldName"))
        val dbFieldType = createTableColumn("数据库字段类型", 150, PropertyValueFactory("dbFieldType"))
        tableView.columns.addAll(isPri, fieldName, fieldType, fieldComment, fieldRang, defaultValue, canNull, autoIncrement, dbFieldName, dbFieldType)
        val observableList = FXCollections.observableList(tableCreates)
        tableView.items = observableList
    }


    private fun createTableColumn(columnName: String, minWidth: Int, mName: PropertyValueFactory<TableCreate, Any>): TableColumn<TableCreate, *> {
        val mField = TableColumn<TableCreate,Any>(columnName)
        mField.minWidth = minWidth.toDouble()
        mField.cellValueFactory = mName
        return mField
    }


    private fun onOK() {
        // add your code here
        val priTableCreates = tableCreates.filter { it.isPri() }
        when {
            priTableCreates.isEmpty() -> {
                error("选择主键")
                return
            }
            priTableCreates.size > 1 -> {
                error("只能选择一个主键")
                return
            }
            else -> SqlUtil.createTable(tableCreates, priTableCreates[0], tableName, conn)
        }
        dispose()
    }


    private fun error(text: String) {
        errorInfo.text = text
    }


    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val dialog = CreateTableDialog()
            dialog.pack()
            dialog.isVisible = true
            System.exit(0)
        }
    }
}

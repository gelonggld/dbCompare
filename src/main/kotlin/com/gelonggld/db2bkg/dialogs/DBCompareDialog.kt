package com.gelonggld.db2bkg.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.model.ModelField
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.DialogUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.gelonggld.db2bkg.model.TableData
import com.gelonggld.db2bkg.model.TableData.Companion.ADD_COMMIT_TO_DB
import com.gelonggld.db2bkg.model.TableData.Companion.ADD_COMMIT_TO_MODEL
import com.gelonggld.db2bkg.model.TableData.Companion.ADD_TO_DB
import com.gelonggld.db2bkg.model.TableData.Companion.ADD_TO_MODEL
import com.gelonggld.db2bkg.model.TableData.Companion.DELETE_FROM_DB
import com.gelonggld.db2bkg.model.TableData.Companion.DELETE_FROM_MODEL
import com.gelonggld.db2bkg.model.TableData.Companion.NO_NONE
import com.gelonggld.db2bkg.model.TableData.Companion.OPT_ADD
import com.gelonggld.db2bkg.model.TableData.Companion.OPT_DELETE
import com.gelonggld.db2bkg.model.TableData.Companion.OPT_NONE
import com.gelonggld.db2bkg.utils.ViewComponent.topBar
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import javax.swing.*
import java.awt.event.*
import java.sql.Connection
import java.sql.SQLException
import java.util.*
@Suppress("unused")
class DBCompareDialog(private val dbTable: String, dbName: String, private val project: Project, private val virtualFile: VirtualFile,
                      private val connection: Connection) : JDialog() {

    private var noMatchDBField: List<DBField>? = null
    private var noMatchPsiField: MutableList<ModelField>? = null
    private var matchs: MutableMap<ModelField, DBField>? = null
    private var tableDatas: ArrayList<TableData>? = null
    private val error = mutableStateOf<String?>(null)


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
            if (DBConvertUtil.beanField2DB(modelField.name) == dbField.name) {
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
        error.value = s
    }

    @Composable
    private fun RowScope.tableHead() {
        Text("模型字段",Modifier.weight(1F))
        Text("模型类型",Modifier.weight(1F))
        Text("模型注释",Modifier.weight(1F))

        Column (modifier = Modifier.weight(3f)){
            Text("字段附加值",Modifier.fillMaxWidth())
            Row {
                Text("字段长度",Modifier.weight(1F))
                Text("默认值",Modifier.weight(1F))
                Text("可空",Modifier.weight(1F))
            }
        }

        Column (modifier = Modifier.weight(3f)){
            Text("操作",Modifier.fillMaxWidth())
            Row {
                Text("删除",Modifier.weight(1F))
                Text("添加",Modifier.weight(1F))
                Text("无操作",Modifier.weight(1F))
            }
        }

        Text("数据库字段",Modifier.weight(1F))
        Text("数据库类型",Modifier.weight(1F))
        Text("数据库注解",Modifier.weight(1F))
        Text("模型附加信息",Modifier.weight(1F))
    }

    @Composable
    private fun RowScope.tableData(data: TableData) {
        Text(data.mName.value?:"", Modifier.weight(1F))
        Text(data.mType.value?:"", Modifier.weight(1F))
        Text(data.mNote.value?:"", Modifier.weight(1F))
        TextField(data.fieldRang.value.toString(), { data.fieldRang.value = it.toInt()},Modifier.weight(1F))
        TextField(data.defaultValue.value,{data.defaultValue.value = it},Modifier.weight(1F))
        Checkbox(data.canNone.value,{data.canNone.value = it}, Modifier.weight(1F))
        when {
            data.mName.value == null -> opt(DELETE_FROM_DB, ADD_TO_MODEL,data)
            data.dName.value == null -> opt(DELETE_FROM_MODEL,ADD_TO_DB,data)
            !data.matchType() -> Text("字段类型不匹配，无法操作", Modifier.weight(1F))
            data.mNote.value != data.dNote.value -> opt(ADD_COMMIT_TO_DB,ADD_COMMIT_TO_MODEL,data)
            else -> {}
        }
    }

    @Composable
    private fun RowScope.opt(deleteDetail: String,addDetail: String,data: TableData) {
        Row(Modifier.Companion.weight(1F)) {
            Text(deleteDetail, Modifier.weight(1F))
            RadioButton(data.radioState.value == OPT_DELETE, { data.radioState.value = OPT_DELETE })
        }
        Row(Modifier.Companion.weight(1F)) {
            Text(addDetail, Modifier.weight(1F))
            RadioButton(data.radioState.value == OPT_ADD, { data.radioState.value = OPT_ADD })
        }
        Row(Modifier.Companion.weight(1F)) {
            Text(NO_NONE, Modifier.weight(1F))
            RadioButton(data.radioState.value == OPT_NONE, { data.radioState.value = OPT_NONE })
        }
    }



    private fun buildTableDatas(): ArrayList<TableData> {
        tableDatas = ArrayList()
        for (dbField in noMatchDBField!!) {
            tableDatas!!.add(TableData(dbField,null,FileDispatch.ifKt(virtualFile)))
        }
        for (modelField in noMatchPsiField!!) {
            tableDatas!!.add(TableData(null,modelField,FileDispatch.ifKt(virtualFile)))
        }
        for (entry in matchs!!.entries) {
            tableDatas!!.add(TableData(entry.value,entry.key,FileDispatch.ifKt(virtualFile)))
        }
        return tableDatas!!
    }


    init {
        isModal = true
        DialogUtil.centSelf(this, 1600, 900)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })
        val composePanel = ComposePanel()
        contentPane = composePanel
        composePanel.setContent { content() }
        compareBeanWithDB(dbTable, connection, dbName)
        tableDatas = buildTableDatas()

    }

    @Composable
    fun content() {
        Scaffold (
            topBar = { topBar("对象比较")},
            floatingActionButton = { okButton() }
        ){
            Column (modifier = Modifier.fillMaxSize()){
                Row (modifier = Modifier.fillMaxWidth()){ tableHead() }
                error.value?.let { Text(color = Color.Red, text = it) }
                tableDatas?.let { datas ->
                    datas.forEach { Row(Modifier.fillMaxWidth()) {tableData(it) } }
                }
            }
        }
    }

    @Composable
    private fun okButton() {
        FloatingActionButton(onClick = { onOK() }) {
            Text(text = "确定")
        }
    }



    private fun onOK() {
        // add your code here
        val addToModels = ArrayList<TableData>()
        val deleteFromModels = ArrayList<TableData>()
        val addCommitToModels = ArrayList<TableData>()
        val addToDBs = ArrayList<TableData>()
        val deleteFromDBs = ArrayList<TableData>()
        val addCommitToDBs = ArrayList<TableData>()

        tableDatas?.filter { it.matchType() && it.radioState.value != OPT_NONE}
            ?.forEach {
                when {
                    it.mName.value == null && it.radioState.value == OPT_ADD ->addToModels.add(it)
                    it.mName.value == null && it.radioState.value == OPT_DELETE ->deleteFromDBs.add(it)
                    it.dName.value == null && it.radioState.value == OPT_ADD ->addToDBs.add(it)
                    it.dName.value == null && it.radioState.value == OPT_DELETE ->deleteFromModels.add(it)
                    it.radioState.value == OPT_ADD -> addCommitToModels.add(it)
                    it.radioState.value == OPT_DELETE -> addCommitToDBs.add(it)
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
                FileDispatch.addFieldAndGetSet( DBConvertUtil.dBField2Bean(tableData.dName.value!!),
                        DBConvertUtil.getDB2BeanMapType(tableData.dType.value!!,FileDispatch.ifKt(virtualFile)), tableData.dNote.value!!, false,selectClass)
            }

            for (tableData in deleteFromModel) {
                    FileDispatch.deleteFieldAndGetSet(tableData.mName.value!!, selectClass)
            }
            for (tableData in addCommitToModel) {
                val field = FileDispatch.findFieldByName(tableData.mName.value!!,selectClass)
                if(field != null){
                    FileDispatch.addCommentToField( tableData.dNote.value!!,field, selectClass)
                }else{
                    error("模型没有字段${tableData.mName.value}")
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


}

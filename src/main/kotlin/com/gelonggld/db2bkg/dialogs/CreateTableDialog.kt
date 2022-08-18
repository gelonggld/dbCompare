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
import com.gelonggld.db2bkg.model.TableCreate
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.DialogUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.*
import java.awt.event.*
import java.sql.Connection

class CreateTableDialog(
    private val conn: Connection,
    val tableName: String,
    selectFile: VirtualFile
) : JDialog() {
    private var tableCreates = FileDispatch.allField(selectFile)?.map { TableCreate(it,FileDispatch.ifKt(selectFile)) } ?: ArrayList()
    private val error = mutableStateOf<String?>(null)
    init {
        isModal = true
        DialogUtil.centSelf(this, 1600, 900)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        val composePanel = ComposePanel()
        contentPane.add(composePanel)
        composePanel.setContent { content() }

    }

    @Composable
    fun content() {
        Scaffold (
            topBar = { topBar()},
            floatingActionButton = { okButton() }
                ){
            Column (modifier = Modifier.fillMaxSize()){
                Row (modifier = Modifier.fillMaxWidth()){ tableHead() }
                error.value?.let { Text(color = Color.Red, text = it) }
                tableCreates.forEach { Row (modifier = Modifier.fillMaxWidth()){createItem(it)} }
            }
        }
    }

    @Composable
    fun createItem(tableCreate: TableCreate) {
        Checkbox(tableCreate.isPri.value,onCheckedChange = {
            tableCreate.isPri.value = it
            if(it) tableCreate.canNull.value = false
        } )
        Text(tableCreate.fieldName.value)
        Text(tableCreate.fieldType.value)
        Text(tableCreate.fieldComment.value?:"")
        Text(tableCreate.fieldRang.value.toString())
        Text(tableCreate.defaultValue.value)
        Checkbox(tableCreate.canNull.value,onCheckedChange = {tableCreate.canNull.value = it})
        Checkbox(tableCreate.autoIncrement.value,onCheckedChange = {tableCreate.autoIncrement.value = it})
        Text(tableCreate.dbFieldName.value)
        Text(tableCreate.dbFieldType.value)
        Checkbox(tableCreate.isKt.value,onCheckedChange = { tableCreate.isKt.value = it})
    }

    @Composable
    fun topBar() {
        TopAppBar(
            title = { Text("创建表") },
            navigationIcon = {
                IconButton(onClick = { dispose() }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null
                    )
                }
            }
        )
    }

    @Composable
    private fun okButton() {
        FloatingActionButton(onClick = { onOK() }) {
            Text(text = "确定")
        }
    }

    @Composable
    private fun RowScope.tableHead() {
        Text(modifier = Modifier.weight(1F), text = "主键")
        Text(modifier = Modifier.weight(1F), text = "模型字段名")
        Text(modifier = Modifier.weight(1F), text = "字段类型")
        Text(modifier = Modifier.weight(1F), text = "字段注释")
        Text(modifier = Modifier.weight(1F), text = "长度")
        Text(modifier = Modifier.weight(1F), text = "默认值")
        Text(modifier = Modifier.weight(1F), text = "可空")
        Text(modifier = Modifier.weight(1F), text = "自增长")
        Text(modifier = Modifier.weight(1F), text = "数据库字段名")
        Text(modifier = Modifier.weight(1F), text = "数据库字段类型")
    }

    private fun onOK() {
        // add your code here
        val priTableCreates = tableCreates.filter { it.isPri.value }
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
        error.value = text
    }


    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

}

package com.gelonggld.db2bkg.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.gelonggld.db2bkg.Application
import com.gelonggld.db2bkg.constants.StrConstant
import com.gelonggld.db2bkg.project
import com.gelonggld.db2bkg.rootFile
import com.gelonggld.db2bkg.utils.ProperUtil
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.ViewComponent
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.gelonggld.db2bkg.utils.db.bean.MariadbBean
import com.gelonggld.db2bkg.utils.db.bean.MysqlBean
import com.gelonggld.db2bkg.utils.db.bean.OracleSqlBean
import com.gelonggld.db2bkg.utilsBean.ConnectBean
import com.intellij.openapi.vfs.VirtualFile
import com.openhtmltopdf.css.parser.property.PrimitivePropertyBuilders.VerticalAlign
import java.sql.*
import java.util.ArrayList

class CheckDialog() {
    private var jdbcUrl = mutableStateOf(ProperUtil.readPath(StrConstant.CONN_URL))
    private var jUsername = mutableStateOf(ProperUtil.readPath(StrConstant.CONN_USERNAME))
    private var jPassword = mutableStateOf(ProperUtil.readPath(StrConstant.CONN_PASSWORD))
    private var jDriver = mutableStateOf(ProperUtil.readPath(StrConstant.CONN_DRIVER))
    private var errorInfo = mutableStateOf<String?>(null)

    private var targetDbTable: String? = null
    private var dbTableNames: ArrayList<String>? = null

    private lateinit var connection: Connection


    @Composable
    fun content() {
        MaterialTheme {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxHeight().width(700.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(jdbcUrl.value, { jdbcUrl.value = it }, Modifier.fillMaxWidth(), label = { Text("jodbUrl") })
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(100.dp)) {
                        TextField(
                            jUsername.value,
                            { jUsername.value = it },
                            Modifier.weight(1F),
                            label = { Text("username") })
                        TextField(
                            jPassword.value,
                            { jPassword.value = it },
                            Modifier.weight(1F),
                            label = { Text("password") })
                    }
                    TextField(jDriver.value, { jDriver.value = it }, Modifier.fillMaxWidth(), label = { Text("driver") })
                    Button(onClick = { onOK() }, Modifier.fillMaxWidth()) {
                        Text(text = "确定")
                    }
                }
            }
        }
    }

    private fun processDBConnent(connectBean: ConnectBean): Boolean {
        connection = connectDb(connectBean)
        connectBean.databaseName = SqlUtil.selectDBName(connection) ?: let {
            error("没查询出数据库名称")
            SqlUtil.recycleConn(connection)
            return false
        }
        try {
            dbTableNames = SqlUtil.allDBTable(connection)
        } catch (ex: Exception) {
            error(ex.message)
            ex.printStackTrace()
            SqlUtil.recycleConn(connection)
            return false
        }
        if (rootFile.isDirectory) {
            showCreateBeanDialog(connectBean.databaseName)
            return true
        } else {
            targetDbTable = FileDispatch.dispatchFindTargetTable(rootFile, "Table", "name") ?: let {
                error("没有找到表注解")
                return false
            }
            val targetDBT = targetDbTable ?: return false

            return if (dbTableNames!!.any { it == targetDbTable }) {
                showCompareDialog(targetDBT, connectBean.databaseName)
                true
            } else {
                showCreateTableDialog(connection, targetDBT)
                true
            }
        }
    }

    private fun showCompareDialog(targetDbTable: String, dbName: String) {
        val dbCompareDialog = DBCompareDialog(targetDbTable, dbName, project, rootFile, connection)
        dbCompareDialog.pack()
        dbCompareDialog.isVisible = true
    }

    private fun showCreateBeanDialog(dbName: String) {
        @Suppress("DEPRECATION")
        val javaTableName = allTables(project.baseDir, ArrayList<String>())
        dbTableNames!!.removeAll(javaTableName.toSet())
        val createBeanDialog = CreateBeanDialog(dbTableNames!!, dbName, connection, rootFile)
        createBeanDialog.pack()
        createBeanDialog.isVisible = true
    }


    private fun showCreateTableDialog(connection: Connection, tableName: String) {
        val createTableDialog = CreateTableDialog(connection, tableName, rootFile)
        createTableDialog.pack()
        createTableDialog.isVisible = true
    }


    private fun error(text: String?): Boolean {
        text?.let {
            errorInfo.value = text
            return true
        }
        return false
    }


    private fun connectDb(connectBean: ConnectBean): Connection {
        Class.forName(connectBean.drive)
        return DriverManager.getConnection(connectBean.url, connectBean.username, connectBean.password)
    }

    private fun onOK() {
        // add your code here
        FileDispatch.assemb()
        saveProp()
        val connectBean = validateInfo() ?: return
        processDBConnent(connectBean)
    }

    private fun saveProp() {
        ProperUtil.savePath(StrConstant.CONN_URL, jdbcUrl.value)
        ProperUtil.savePath(StrConstant.CONN_DRIVER, jDriver.value)
        ProperUtil.savePath(StrConstant.CONN_USERNAME, jUsername.value)
        ProperUtil.savePath(StrConstant.CONN_PASSWORD, jPassword.value)
    }

    private fun validateInfo(): ConnectBean? {
        val url = jdbcUrl.value
        val drive = jDriver.value
        val username = jUsername.value
        val password = jPassword.value

        val connectBean = ConnectBean(url, drive, username, password)
        when {
            connectBean.drive.contains("oracle") -> SqlUtil.sqlBean = OracleSqlBean()
            connectBean.drive.contains("mysql") -> SqlUtil.sqlBean = MysqlBean()
            connectBean.drive.contains("mariadb") -> SqlUtil.sqlBean = MariadbBean()
            else -> {
                error("不识别的driver类型")
                return null
            }
        }
        return connectBean
    }

    private fun allTables(virtualFile: VirtualFile, tableNames: MutableList<String>): List<String> {
        if (!virtualFile.isDirectory) {
            return tableNames
        }
        val children = virtualFile.children
        children.forEach { proessSingleFile(it, tableNames) }

        return tableNames
    }

    private fun proessSingleFile(it: VirtualFile, tableNames: MutableList<String>) {
        if (it.isDirectory) {
            allTables(it, tableNames)
        } else {
            FileDispatch.dispatchFindTargetTable(it, "Table", "name")?.let {
                tableNames.add(it)
            }
        }
    }


}

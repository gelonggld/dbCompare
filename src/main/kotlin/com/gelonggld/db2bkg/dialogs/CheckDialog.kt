package com.gelonggld.db2bkg.dialogs

import com.gelonggld.db2bkg.constants.StrConstant
import com.gelonggld.db2bkg.utils.ProperUtil
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.DialogUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.gelonggld.db2bkg.utils.db.bean.MariadbBean
import com.gelonggld.db2bkg.utils.db.bean.MysqlBean
import com.gelonggld.db2bkg.utils.db.bean.OracleSqlBean
import com.gelonggld.db2bkg.utilsBean.ConnectBean
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.*
import java.awt.event.*
import java.sql.*
import java.util.ArrayList

class CheckDialog(private val project: Project, private val virtualFile: VirtualFile) : JDialog() {
    lateinit var contentPane: JPanel
    lateinit var buttonOK: JButton
    lateinit var buttonCancel: JButton
    lateinit var jUrl: JTextField
    lateinit var jUsername: JTextField
    lateinit var jPassword: JTextField
    lateinit var jDriver: JTextField
    lateinit var errorInfo: JLabel

    private var targetDbTable: String? = null
    private var dbTableNames: ArrayList<String>? = null

    private lateinit var connection: Connection

    init {
        setPanel()
        setLocal()
        changeFocus()
        initPanel()
    }

    private fun initPanel() {
        initOk()
        initCancel()
        initInput()
        initSelf()
        initContentPane()
    }

    private fun initInput() {
        jUrl.text = ProperUtil.readPath(StrConstant.CONN_URL, project)
        jUsername.text = ProperUtil.readPath(StrConstant.CONN_USERNAME, project)
        jPassword.text = ProperUtil.readPath(StrConstant.CONN_PASSWORD, project)
        jDriver.text = ProperUtil.readPath(StrConstant.CONN_DRIVER, project)
    }

    private fun initSelf() {
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })
    }

    private fun initContentPane() {
        contentPane.registerKeyboardAction(
            { onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
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
        if (virtualFile.isDirectory) {
            showCreateBeanDialog(connectBean.databaseName)
            return true
        } else {
            targetDbTable = FileDispatch.dispatchFindTargetTable(virtualFile, "Table", "name") ?: let {
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
        val dbCompareDialog = DBCompareDialog(targetDbTable, dbName, project, virtualFile, connection)
        dbCompareDialog.pack()
        dbCompareDialog.isVisible = true
    }

    private fun showCreateBeanDialog(dbName: String) {
        @Suppress("DEPRECATION")
        val javaTableName = allTables(project.baseDir, ArrayList<String>())
        dbTableNames!!.removeAll(javaTableName)
        val createBeanDialog = CreateBeanDialog(project, dbTableNames!!, dbName, connection, virtualFile)
        createBeanDialog.pack()
        createBeanDialog.isVisible = true
    }


    private fun showCreateTableDialog(connection: Connection, tableName: String) {
        val createTableDialog = CreateTableDialog(connection, virtualFile, tableName)
        createTableDialog.pack()
        createTableDialog.isVisible = true
    }


    private fun error(text: String?): Boolean {
        text?.let {
            errorInfo.text = text
            return true
        }
        return false
    }


    private fun connectDb(connectBean: ConnectBean): Connection {
        Class.forName(connectBean.drive)
        return DriverManager.getConnection(connectBean.url, connectBean.username, connectBean.password)
    }


    private fun initCancel() {
        buttonCancel.addActionListener { onCancel() }
    }

    private fun initOk() {
        buttonOK.addActionListener { onOK() }
    }

    private fun changeFocus() {
        contentPane.isFocusable = true
        contentPane.requestFocus()
    }


    private fun setLocal() {
        DialogUtil.centSelf(this, 296, 119)
    }


    private fun setPanel() {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
    }


    private fun onOK() {
        // add your code here
        FileDispatch.assemb(project)
        saveProp()
        val connectBean = validateInfo() ?: return
        if (processDBConnent(connectBean)) {
            dispose()
        }
    }

    private fun saveProp() {
        ProperUtil.savePath(StrConstant.CONN_URL, jUrl.text, project)
        ProperUtil.savePath(StrConstant.CONN_DRIVER, jDriver.text, project)
        ProperUtil.savePath(StrConstant.CONN_USERNAME, jUsername.text, project)
        ProperUtil.savePath(StrConstant.CONN_PASSWORD, jPassword.text, project)
    }

    private fun validateInfo(): ConnectBean? {
        val url = jUrl.text ?: let {
            error("url 不能为空")
            return null
        }
        val drive = jDriver.text ?: let {
            error("url 不能为空")
            return null
        }
        val username = jUsername.text ?: let {
            error("url 不能为空")
            return null
        }
        val password: String = jPassword.text ?: let {
            error("url 不能为空")
            return null
        }

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

    /**
     * 递归方法
     */
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


    private fun onCancel() {
        dispose()
    }


}

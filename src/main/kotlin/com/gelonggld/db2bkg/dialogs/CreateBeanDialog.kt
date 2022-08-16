package com.gelonggld.db2bkg.dialogs

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import com.gelonggld.db2bkg.constants.StrConstant
import com.gelonggld.db2bkg.exceptions.CreateFileException
import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.model.DataBox
import com.gelonggld.db2bkg.model.TableList
import com.gelonggld.db2bkg.tail
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.ProperUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.intellij.ide.util.ClassFilter
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import javax.swing.*
import java.awt.event.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess

class CreateBeanDialog() : JDialog() {
    private var date: String? = null
    private var username: String? = null
    var dbName: String? = null
    private lateinit var dir: VirtualFile


    lateinit var contentPane: JPanel
    lateinit var content: JPanel
    lateinit var errorInfo: JLabel
    private var jfxPanel: JFXPanel
    private lateinit var tableView: TableView<TableList>

    private lateinit var project: Project
    private var dbTableNames: List<String>? = null
    private lateinit var connection: Connection
    private lateinit var tableLists: List<TableList>
    private var ySpan = 30
    private var xSpan = 600
    private var xSpan1 = 1200

    val beanbase = mutableStateOf(ProperUtil.readPath(StrConstant.BEAN_PATH))
    val mappterbase = mutableStateOf(ProperUtil.readPath(StrConstant.MAPPER_PARENT_PATH))
    val servicebase = mutableStateOf(ProperUtil.readPath(StrConstant.SERVICE_PARENT_PATH))
    val serviceImplbase = mutableStateOf(ProperUtil.readPath(StrConstant.SERVICEIMPL_PARENT_PATH))
    val jumpFirstLine = mutableStateOf(ProperUtil.readPath(StrConstant.AUTO_PASS_LINE))
    val genKtFile =  mutableStateOf(ProperUtil.readPath(StrConstant.GEN_KT_FILE))


    private val currentDate: String
        get() {
            val simpleDateFormat = SimpleDateFormat("yyy/MM/dd")
            return simpleDateFormat.format(Date())
        }

    init {
        isModal = true
        val composePanel = ComposePanel()
        contentPane.add(composePanel)
        contentPane.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                dispose()
            }
        })


        // call onCancel() when cross is clicked


        // call onCancel() on ESCAPE

        jfxPanel = JFXPanel()
        content.add(jfxPanel)
    }


    @Preview
    @Composable
    fun content() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("创建实例") },
                    navigationIcon = {
                        IconButton(onClick = { dispose() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { onOK() }) {
                    Text(text = "确定")
                }
            }
        ) {
            Column (modifier = Modifier.fillMaxSize()){
                Row (modifier = Modifier.fillMaxWidth()){
                    textPanel("bean继承",beanbase,StrConstant.BEAN_PATH)
                    textPanel("mapper继承",mappterbase,StrConstant.MAPPER_PARENT_PATH)
                    textPanel("service继承",servicebase,StrConstant.SERVICE_PARENT_PATH)
                    textPanel("serviceImpl继承",serviceImplbase,StrConstant.SERVICEIMPL_PARENT_PATH)
                    checkPanel("跳过第一个下划线",jumpFirstLine,StrConstant.AUTO_PASS_LINE)
                    checkPanel("kotlin",genKtFile,StrConstant.GEN_KT_FILE)
                }
            }
        }
    }


    constructor (
        project: Project,
        dbTableNames: List<String>, dbName: String, connection: Connection, dir: VirtualFile
    ) : this() {
        this.project = project
        this.dbTableNames = dbTableNames
        this.connection = connection
        this.dir = dir
        this.dbName = dbName
        this.username = System.getProperty("user.name")
        this.date = currentDate
        tableLists = dbTableNames.map { TableList.Build(it) }
        addTable()
    }

    private fun addTable() {
        Platform.setImplicitExit(false)
        Platform.runLater {
            val root = Group()
            val scene = Scene(root, javafx.scene.paint.Color.ALICEBLUE)
            beanInput = allToPanel("bean继承", StrConstant.BEAN_PATH, 0, 0, root)
            mapperInput = allToPanel("mapper继承", StrConstant.MAPPER_PARENT_PATH, xSpan, 0, root)
            serviceInput = allToPanel("service继承", StrConstant.SERVICE_PARENT_PATH, xSpan1, 0, root)
            serviceImplInput = allToPanel("serviceImpl继承", StrConstant.SERVICEIMPL_PARENT_PATH, 0, ySpan, root)
            jumpFirstLine = addToPanel("跳过第一个下划线", StrConstant.AUTO_PASS_LINE, xSpan, ySpan, root)
            genKtFile = addToPanel("kotlin", StrConstant.GEN_KT_FILE, xSpan1, ySpan, root)
            val tableView = createTableView(0, ySpan * 2)
            root.children.add(tableView)
            jfxPanel.scene = scene
        }
    }

    @Composable
    fun RowScope.textPanel(label: String,state: MutableState<String>,key:String) {
        Text(state.value)
        Button(onClick = {selectFilePath(project, state, key)}) {
            Text(label)
        }
    }

    @Composable
    fun RowScope.checkPanel(label: String,state: MutableState<String>,key: String) {
        Text(label)
        Checkbox(state.value == "1",onCheckedChange = {ProperUtil.savePath(key,state.value)})
    }



    private fun selectFilePath(project: Project, state: MutableState<String>, key: String) {
        SwingUtilities.invokeLater {
            val chooser = TreeClassChooserFactory.getInstance(project)
                .createNoInnerClassesScopeChooser(
                    "BaseActivity",
                    GlobalSearchScope.allScope(project),
                    ClassFilter.ALL,
                    null
                )
            chooser.showDialog()
            val parent = chooser.selected ?: return@invokeLater
            state.value = parent.qualifiedName!!
            ProperUtil.savePath(key, parent.qualifiedName!!)
        }
    }


    private fun allToPanel(labelName: String, propName: String, x: Int, y: Int, root: Group): TextField {
        val label = Label(labelName)
        val textField = TextField()
        textField.text = ProperUtil.readPath(propName, project)
        val button = Button("选取class")
        button.setOnAction { selectFilePath(project, textField, propName) }
        val hb = HBox()
        hb.children.addAll(label, textField, button)
        hb.spacing = 10.0
        hb.layoutX = x.toDouble()
        hb.layoutY = y.toDouble()
        root.children.add(hb)
        return textField
    }


    private fun addToPanel(labelName: String, propName: String, x: Int, y: Int, root: Group): CheckBox {
        val checkBox = CheckBox(labelName)
        checkBox.isSelected = ProperUtil.readPath(propName, project) == "Y"
        checkBox.layoutX = x.toDouble()
        checkBox.layoutY = y.toDouble()
        checkBox.selectedProperty().addListener { _, _, newValue ->
            val c = if (newValue) "Y" else "N"
            ProperUtil.savePath(propName, c, project)
        }
        root.children.add(checkBox)
        return checkBox
    }


    @Suppress("SameParameterValue")
    private fun createTableView(x: Int, y: Int): TableView<*> {
        tableView = TableView()
        tableView.layoutX = x.toDouble()
        tableView.layoutY = y.toDouble()
        tableView.minWidth = 1580.0
        createTableTitle(tableView)
        return tableView
    }

    private fun onOK() {
        // add your code here
        createBean(
            tableLists.filter { it.getGenerator().isSelected },
            ProperUtil.readPath(StrConstant.AUTO_PASS_LINE, project) == "Y"
        )
        SqlUtil.recycleConn(connection)
        dispose()
    }

    private fun createBean(collect: List<TableList>, passLine: Boolean) {
        WriteCommandAction.writeCommandAction(project).run<Throwable> {
            for (tableList in collect) {
                val dbFields = SqlUtil.getDBFields(tableList.getTableName(), connection, dbName!!)
                val beanName = DBConvertUtil.dBTableName2Bean(tableList.getTableName(), passLine)
                try {
                    val beanClass =
                        createBeanClass(beanName, dbFields, tableList.getTableName(), "import ${beanInput.text}")
                    if (tableList.getMapper().isSelected) {
                        val mapperClass = createMaperClass(beanClass, mapperInput.text)
                        createMapperXml(mapperClass.qualifiedName, beanClass.name)
                        if (tableList.getService().isSelected) {
                            val dataBox = bindService(beanClass, serviceInput.text)
                            val serviceDir = findCreateDir(dir, "service")
                            val serviceVFile = serviceDir.createChildData(
                                null,
                                "${beanClass.name!!}Service.${FileDispatch.tail()}"
                            )
                            writeFileFromTemplet(serviceVFile, "${FileDispatch.pre()}/interface.mvpd", dataBox)
                            val serviceClass = FileDispatch.findClass(serviceVFile)
                                ?: throw CreateFileException("在创建${beanClass.name!!}Service.${FileDispatch.tail()} 时发生异常")
                            createServiceImplClass(
                                beanClass,
                                serviceImplInput.text,
                                serviceClass,
                                serviceDir,
                                mapperClass
                            )
                        }
                    }
                } catch (e: IOException) {
                    errorL(e.message)
                }

            }
        }
//        object : WriteCommandAction.Simple<Any>(project, PsiManager.getInstance(project).findFile(dir)) {
//            @Throws(Throwable::class)
//            override fun run() {
//                for (tableList in collect) {
//                    val dbFields = SqlUtil.getDBFields(tableList.getTableName(), connection, dbName!!)
//                    val beanName = DBConvertUtil.dBTableName2Bean(tableList.getTableName(), passLine)
//                    try {
//                        val beanClass = createBeanClass(beanName, dbFields, tableList.getTableName(),"import ${beanInput.text}")
//                        if (tableList.getMapper().isSelected) {
//                            val mapperClass = createMaperClass(beanClass, mapperInput.text)
//                            createMapperXml(mapperClass.qualifiedName, beanClass.name)
//                            if (tableList.getService().isSelected) {
//                                val dataBox = bindService(beanClass, serviceInput.text)
//                                val serviceDir = findCreateDir(dir, "service")
//                                val serviceVFile = serviceDir.createChildData(null, "${beanClass.name!!}Service.${FileDispatch.tail()}")
//                                writeFileFromTemplet(serviceVFile, "${FileDispatch.pre()}/interface.mvpd", dataBox)
//                                val serviceClass = FileDispatch.findClass(serviceVFile) ?: throw CreateFileException("在创建${beanClass.name!!}Service.${FileDispatch.tail()} 时发生异常")
//                                createServiceImplClass(beanClass, serviceImplInput.text, serviceClass, serviceDir, mapperClass)
//                            }
//                        }
//                    } catch (e: IOException) {
//                        errorL(e.message)
//                        return
//                    }
//
//                }
//            }
//        }.execute()
    }


    private fun errorL(message: String?) {
        message?.let {
            errorInfo.text = message
        }
    }


    @Throws(IOException::class)
    private fun createServiceImplClass(
        beanClass: PsiClass,
        basePath: String,
        serviceClass: PsiClass,
        serviceVir: VirtualFile,
        mapperClass: PsiClass
    ): PsiClass {
        val dataBox = bindServiceImpl(beanClass, basePath, serviceClass)
        val virtualFile = createFromTemplete(
            serviceVir, "impl", "${beanClass.name!!}ServiceImpl.${FileDispatch.tail()}",
            "${FileDispatch.pre()}/classExtInteface.mvpd", dataBox
        )
        val psiClass = FileDispatch.findClass(virtualFile)
            ?: throw CreateFileException("在创建--${beanClass.name}ServiceImpl 时发生错误")
        FileDispatch.addAnnotationToClass("Service", psiClass, "org.springframework.stereotype.Service")
        val field = FileDispatch.addFieldtoClass(
            DBConvertUtil.firstLow(mapperClass.name!!),
            mapperClass.qualifiedName!!,
            psiClass,
            null,
            true
        )
        FileDispatch.addAnnotationToField(
            "Autowired",
            field,
            psiClass,
            "org.springframework.beans.factory.annotation.Autowired"
        )
        return psiClass

    }


    private fun createMapperXml(qualifiedName: String?, beanName: String?) {
        val dataBox = bindXml(qualifiedName)
        createFromTemplete(dir, "dao", "${beanName!!}Mapper.xml", "${FileDispatch.pre()}/xml.mvpd", dataBox)

    }

    private fun createFromTemplete(
        base: VirtualFile,
        dirname: String,
        fileName: String,
        templeteName: String,
        dataBox: DataBox
    ): VirtualFile {
        val mapperDir = findCreateDir(base, dirname)
        val child = mapperDir.createChildData(null, fileName)
        writeFileFromTemplet(child, templeteName, dataBox)
        return child
    }


    private fun createMaperClass(beanClass: PsiClass, basePath: String): PsiClass {
        val mapperDir = findCreateDir(dir, "dao")
        val child = mapperDir.createChildData(null, "${beanClass.name!!}Mapper.${FileDispatch.tail()}")
        val dataBox = bindMapper(mapperDir, basePath, beanClass)
        writeFileFromTemplet(child, "${FileDispatch.pre()}/interface.mvpd", dataBox)
        return FileDispatch.findClass(child)
            ?: throw CreateFileException("在创建--${beanClass.name}Mapper.${FileDispatch.tail()} 时发生错误")
    }

    private fun createBeanClass(
        beanName: String,
        dbFields: List<DBField>?,
        tableName: String,
        importBaseModel: String
    ): PsiClass {
        val modelDir = findCreateDir(dir, "model")
        val child = modelDir.createChildData(null, "$beanName.${FileDispatch.tail()}")
        val dataBox = buildBean(modelDir, beanName, importBaseModel)
        writeFileFromTemplet(child, "${FileDispatch.pre()}/class.mvpd", dataBox)
        val beanClass = FileDispatch.findClass(child) ?: throw CreateFileException("无法创建--$beanName")
        FileDispatch.addAnnotationToClass("Table(name = \"$tableName\")", beanClass, "javax.persistence.Table")
        dbFields!!.forEach { gener(it, beanClass) }
        FileDispatch.formatClass(beanClass)
        return beanClass
    }

    private fun gener(dbField: DBField, psiClass: PsiClass) {
        FileDispatch.addFieldAndGetSet(
            DBConvertUtil.dBField2Bean(dbField.name!!),
            DBConvertUtil.getDB2BeanMapType(dbField.type!!, FileDispatch.kt()),
            dbField.comment!!,
            dbField.isPri,
            psiClass
        )
    }


    @Throws(IOException::class)
    private fun writeFileFromTemplet(vir: VirtualFile, resourceFileName: String, dataBox: DataBox) {
        val inputStream = javaClass.classLoader
            .getResourceAsStream(resourceFileName)
            ?: throw RuntimeException("没有找到resource $resourceFileName")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val strStream = StringBuffer()
        var tmpString: String? = bufferedReader.readLine()
        while (tmpString != null) {
            strStream.append(checkBeanStr(tmpString, dataBox)).append("\n")
            tmpString = bufferedReader.readLine()
        }
        vir.setBinaryContent(strStream.toString().toByteArray(charset("UTF-8")))
        inputStream.close()
        bufferedReader.close()
    }


    private fun bindXml(qualifiedName: String?): DataBox {
        val dataBox = DataBox()
        dataBox.mapperPath = qualifiedName
        return dataBox
    }

    private fun bindService(beanClass: PsiClass, basePath: String): DataBox {
        val dataBox = DataBox()
        dataBox.packageInfo = "${getPackName(dir)!!}.service"
        bindBaseData(dataBox, basePath, beanClass)
        return dataBox
    }

    private fun bindBaseData(
        dataBox: DataBox,
        basePath: String,
        beanClass: PsiClass
    ) {
        dataBox.basePath = basePath
        dataBox.beanPath = beanClass.qualifiedName
        dataBox.auth = username
        dataBox.createTime = date
        dataBox.className = "${beanClass.name!!}Service"
        dataBox.baseName = nameFromPath(basePath)
        dataBox.beanName = beanClass.name
    }

    private fun bindServiceImpl(beanClass: PsiClass, basePath: String, serviceClass: PsiClass): DataBox {
        val dataBox = DataBox()
        dataBox.packageInfo = "${packageFromPath(serviceClass.qualifiedName!!)}.impl"
        dataBox.basePath = basePath
        dataBox.interfacePath = serviceClass.qualifiedName
        dataBox.beanPath = beanClass.qualifiedName
        dataBox.auth = username
        dataBox.createTime = date
        dataBox.className = "${beanClass.name!!}ServiceImpl"
        dataBox.baseName = nameFromPath(basePath)
        dataBox.beanName = nameFromPath(beanClass.name!!)
        dataBox.interfaceName = serviceClass.name
        return dataBox
    }


    private fun bindMapper(mapperDir: VirtualFile, basePath: String, beanClass: PsiClass): DataBox {
        val dataBox = DataBox()
        dataBox.packageInfo = getPackName(mapperDir)
        bindBaseData(dataBox, basePath, beanClass)
        return dataBox
    }


    private fun buildBean(modelDir: VirtualFile, beanName: String, importBaseModel: String): DataBox {
        val dataBox = DataBox()
        dataBox.packageInfo = getPackName(modelDir)
        dataBox.auth = username
        dataBox.createTime = date
        dataBox.className = beanName
        dataBox.importBaseModel = importBaseModel
        dataBox.baseModel = importBaseModel.tail()
        return dataBox
    }


    private fun packageFromPath(path: String): String {
        return path.substring(0, path.lastIndexOf("."))
    }

    private fun nameFromPath(path: String): String {
        return path.substring(path.lastIndexOf(".") + 1, path.length)
    }

    private fun getPackName(presenterPackage: VirtualFile): String? {
        val psiDirectory = PsiManager.getInstance(project).findDirectory(presenterPackage)
        val itemPresentation = psiDirectory!!.presentation
        return itemPresentation!!.locationString
    }


    private fun checkBeanStr(tmpString: String, dataBox: DataBox): String {
        return dataBox.replaceAll(tmpString)
    }


    @Throws(IOException::class)
    fun findCreateDir(dir: VirtualFile, name: String): VirtualFile {
        var childDir = dir.findChild(name)
        if (childDir == null) {
            childDir = dir.createChildDirectory(null, name)
        }
        if (!childDir.isDirectory) {
            throw CreateFileException("无法在--${dir.name} 目录下创建名为--$name  的目录,有重名文件")
        }
        return childDir
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }

    private fun createTableTitle(tableView: TableView<TableList>) {
        val tableName = createTableColumn("表名", 500, PropertyValueFactory("tableName"))
        val button = createTableColumn("创建", 150, PropertyValueFactory("generator"))
        val mapper = createTableColumn("生成mapper", 150, PropertyValueFactory("mapper"))
        val service = createTableColumn("生成service", 150, PropertyValueFactory("service"))
        tableView.columns.addAll(tableName, button, mapper, service)
        val observableList = FXCollections.observableList(tableLists)
        tableView.items = observableList
    }


    private fun createTableColumn(
        columnName: String,
        minWidth: Int,
        mName: PropertyValueFactory<TableList, Any>
    ): TableColumn<TableList, Any> {
        val mField = TableColumn<TableList, Any>(columnName)
        mField.minWidth = minWidth.toDouble()
        mField.cellValueFactory = mName
        return mField
    }

    companion object {


        @JvmStatic
        fun main(args: Array<String>) {
            val dialog = CreateBeanDialog()
            dialog.pack()
            dialog.isVisible = true
            exitProcess(0)
        }
    }
}

package com.gelonggld.db2bkg.dialogs

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import com.gelonggld.db2bkg.constants.StrConstant
import com.gelonggld.db2bkg.exceptions.CreateFileException
import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.model.DataBox
import com.gelonggld.db2bkg.model.TableCreateBeanData
import com.gelonggld.db2bkg.project
import com.gelonggld.db2bkg.tail
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.ProperUtil
import com.gelonggld.db2bkg.utils.TimeUtil
import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.intellij.ide.util.ClassFilter
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.command.WriteCommandAction
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
import java.time.LocalDateTime

class CreateBeanDialog(
    dbTableNames: List<String>,
    dbName: String,
    private var connection: Connection,
    private var dir: VirtualFile
) : JDialog() {
    private var auth: String = System.getProperty("user.name")
    private var dbName: String? = dbName
    private var tableItems = dbTableNames.map { TableCreateBeanData(it) }

    private val beanBase = mutableStateOf(ProperUtil.readPath(StrConstant.BEAN_PATH))
    private val mapperBase = mutableStateOf(ProperUtil.readPath(StrConstant.MAPPER_PARENT_PATH))
    private val serviceBase = mutableStateOf(ProperUtil.readPath(StrConstant.SERVICE_PARENT_PATH))
    private val serviceImplBase = mutableStateOf(ProperUtil.readPath(StrConstant.SERVICEIMPL_PARENT_PATH))
    private val jumpFirstLine = mutableStateOf(ProperUtil.readPath(StrConstant.AUTO_PASS_LINE))
    private val genKtFile =  mutableStateOf(ProperUtil.readPath(StrConstant.GEN_KT_FILE))



    private val error = mutableStateOf<String?>(null)
    @Preview
    @Composable
    fun content() {
        Scaffold(
            topBar = { topBar() },
            floatingActionButton = { okButton() }
        ) {
            Column (modifier = Modifier.fillMaxSize()){
                configPanel()
                tableHead()
                error.value?.let { Text(color = Color.Red, text = it) }
                tableItems.forEach { dataRow(it) }
            }
        }
    }
    @Composable
    private fun topBar() {
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
    }
    @Composable
    private fun okButton() {
        FloatingActionButton(onClick = { onOK() }) {
            Text(text = "确定")
        }
    }

    @Composable
    private fun dataRow(tableCreateBeanData: TableCreateBeanData) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(modifier = Modifier.weight(3F), text = tableCreateBeanData.tableName)
            itemCheck(tableCreateBeanData.generator)
            itemCheck(tableCreateBeanData.mapper, tableCreateBeanData.generator)
            itemCheck(tableCreateBeanData.service, tableCreateBeanData.mapper, tableCreateBeanData.generator)
        }
    }

    @Composable
    private fun RowScope.itemCheck(state: MutableState<Boolean>,vararg linkState : MutableState<Boolean>) =
        Checkbox(
            modifier = Modifier.weight(1F),
            checked = state.value,
            onCheckedChange = {check ->
                if(check) {
                    linkState.forEach { it.value = true }
                }
            }
        )


    @Composable
    private fun configPanel() {
        Row(modifier = Modifier.fillMaxWidth()) {
            textPanel("bean继承", beanBase, StrConstant.BEAN_PATH)
            textPanel("mapper继承", mapperBase, StrConstant.MAPPER_PARENT_PATH)
            textPanel("service继承", serviceBase, StrConstant.SERVICE_PARENT_PATH)
            textPanel("serviceImpl继承", serviceImplBase, StrConstant.SERVICEIMPL_PARENT_PATH)
            checkPanel("跳过第一个下划线", jumpFirstLine, StrConstant.AUTO_PASS_LINE)
            checkPanel("kotlin", genKtFile, StrConstant.GEN_KT_FILE)
        }
    }



    @Composable
    private fun tableHead() {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(modifier = Modifier.weight(3F), text = "表名")
            Text(modifier = Modifier.weight(1F), text = "创建")
            Text(modifier = Modifier.weight(1F), text = "生成mapper")
            Text(modifier = Modifier.weight(1F), text = "生成service")
        }
    }

    init {
        isModal = true
        val composePanel = ComposePanel()
        contentPane.add(composePanel)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                dispose()
            }
        })
        composePanel.setContent { content() }
    }



    @Composable
    fun textPanel(label: String,state: MutableState<String>,key:String) {
        Text(state.value)
        Button(onClick = {selectFilePath(state, key)}) {
            Text(label)
        }
    }

    @Composable
    fun checkPanel(label: String,state: MutableState<String>,key: String) {
        Text(label)
        Checkbox(state.value == "1",onCheckedChange = {ProperUtil.savePath(key,state.value)})
    }



    private fun selectFilePath( state: MutableState<String>, key: String) {
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


    private fun onOK() {
        // add your code here
        createBean(
            tableItems.filter { it.generator.value },
            ProperUtil.readPath(StrConstant.AUTO_PASS_LINE) == "Y"
        )
        SqlUtil.recycleConn(connection)
        dispose()
    }

    private fun createBean(collect: List<TableCreateBeanData>, passLine: Boolean) {
        WriteCommandAction.writeCommandAction(project).run<Throwable> {
            for (tableList in collect) {
                val dbFields = SqlUtil.getDBFields(tableList.tableName, connection, dbName!!)
                val beanName = DBConvertUtil.dBTableName2Bean(tableList.tableName, passLine)
                try {
                    val beanClass =
                        createBeanClass(beanName, dbFields, tableList.tableName, "import ${beanBase.value}")
                    if (tableList.mapper.value) {
                        val mapperClass = createMaperClass(beanClass, mapperBase.value)
                        createMapperXml(mapperClass.qualifiedName, beanClass.name)
                        if (tableList.service.value) {
                            val dataBox = bindService(beanClass, serviceBase.value)
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
                                serviceImplBase.value,
                                serviceClass,
                                serviceDir,
                                mapperClass
                            )
                        }
                    }
                } catch (e: IOException) {
                    errorL(e.message?:return@run)
                }

            }
        }
    }

    private fun errorL(message: String) {
        error.value = message
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
        dataBox.auth = auth
        dataBox.createTime = LocalDateTime.now().format(TimeUtil.y_s_normal)
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
        dataBox.auth = auth
        dataBox.createTime = LocalDateTime.now().format(TimeUtil.y_s_normal)
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
        dataBox.auth = auth
        dataBox.createTime = LocalDateTime.now().format(TimeUtil.y_s_normal)
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

}

package com.gelonggld.db2bkg.utils.codeparse

import com.gelonggld.db2bkg.constants.StrConstant
import com.gelonggld.db2bkg.model.ModelField
import com.gelonggld.db2bkg.removeBase
import com.gelonggld.db2bkg.tail
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.elements.FakeFileForLightClass
import org.jetbrains.kotlin.asJava.elements.KtLightField
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtValueArgumentName
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName
import org.jetbrains.kotlin.resolve.ImportPath

class KtWorker(val project: Project) {

    companion object {
        val logger = Logger.getInstance(KtWorker::class.java)
    }

    fun ktF() = KtPsiFactory(FileDispatch.project, false)


    fun addAnno2Class(annoStr: String, psiClass: KtLightClass, vararg import: String) {
        psiClass.kotlinOrigin!!.addAnnotationEntry(ktF().createAnnotationEntry("@$annoStr"))
        import.forEach { addImport(it, psiClass.containingFile) }
    }

    private fun findLastField(psiClass: KtLightClass): PsiField? {
        return if (psiClass.fields.isEmpty()) {
            null
        } else psiClass.fields[psiClass.fields.size - 1]
    }


    fun addFieldtoClass(mFieldName: String, mFieldType: String, psiClass: KtLightClass, commit: String?, lateinte: Boolean): PsiElement {
        val anchor: PsiElement
        if(mFieldType.contains('.')){
            addImport(mFieldType,psiClass.containingFile)
        }
        val field = if (lateinte) {
            ktF().createProperty("lateinit var $mFieldName : ${mFieldType.tail()}")
        } else {
            ktF().createProperty("var $mFieldName : ${mFieldType.tail()}? = null")
        }
        anchor = if (findLastField(psiClass) == null) {
            psiClass.kotlinOrigin!!.getBody()!!.addBefore(field, psiClass.kotlinOrigin!!.getBody()!!.rBrace)
        } else {
            psiClass.kotlinOrigin!!.getBody()!!.addBefore(field, psiClass.kotlinOrigin!!.getBody()!!.rBrace)
        }
        commit?.let {
            addCommentToField(commit, anchor, psiClass)
        }
        return anchor
    }

    fun addCommentToField(commit: String, anchor: PsiElement, psiClass: KtLightClass) {
        val psiComment = ktF().createComment(StrConstant.COMMENT_HEAD + commit + StrConstant.COMMENT_TAIL)
        (anchor as KtProperty).docComment?.delete()
        psiClass.kotlinOrigin!!.addBefore(psiComment, anchor)
    }


    fun getSet(mFieldName: String, mFileType: String, psiClass: KtLightClass, anchor: PsiElement) {
        val psiGetMethod = createGetMethod(mFieldName, mFileType, psiClass)
        val psiSetMethod = createSetMethod(mFieldName, mFileType, psiClass)
        val addedGetMethod: PsiElement
        if (findLastMethod(psiClass) == null) {
            addedGetMethod = psiClass.addAfter(psiGetMethod, anchor)
        } else {
            addedGetMethod = psiClass.addAfter(psiGetMethod, findLastMethod(psiClass))
        }
        psiClass.kotlinOrigin!!.addAfter(psiSetMethod, addedGetMethod)
    }


    private fun createGetMethod(mFieldName: String, mFileType: String, psiClass: KtLightClass): PsiElement {
        val getText = StringBuffer()
        getText.append("public ")
                .append(DBConvertUtil.simpleJavaType(mFileType))
                .append(" get")
                .append(DBConvertUtil.firstBig(mFieldName))
                .append("() {\n")
                .append("return this.")
                .append(mFieldName)
                .append(";\n")
                .append("}")
        val ktfun = ktF().createFunction(getText.toString())
        return psiClass.kotlinOrigin!!.addBefore(ktfun, psiClass.kotlinOrigin!!)
    }

    private fun createSetMethod(mFieldName: String, mFileType: String, psiClass: KtLightClass): PsiElement {
        val setText = StringBuffer()
        setText.append("public ")
                .append(psiClass.name)
                .append(" set")
                .append(DBConvertUtil.firstBig(mFieldName))
                .append("(")
                .append(DBConvertUtil.simpleJavaType(mFileType))
                .append(" ")
                .append(mFieldName)
                .append(") {\n")
                .append("this.")
                .append(mFieldName)
                .append("=")
                .append(mFieldName)
                .append(";\n")
                .append("return this;\n")
                .append("}")
        val ktfun = ktF().createFunction(setText.toString())
        return psiClass.kotlinOrigin!!.addBefore(ktfun, psiClass.kotlinOrigin!!)
    }


    private fun findLastMethod(psiClass: KtLightClass): PsiMethod? {
        return if (psiClass.methods.isEmpty()) {
            null
        } else psiClass.methods[psiClass.methods.size - 1]
    }

    fun addAnnotationToField(annoStr: String, element: PsiElement, psiClass: KtLightClass, vararg import: String) {
        val anno = ktF().createAnnotationEntry("@$annoStr")
        import.forEach { addImport(it, psiClass.containingFile) }
        psiClass.kotlinOrigin!!.addBefore(anno, element)
    }

    private fun addImport(importStr: String, containingFile: PsiFile?) {
        val ktFile = (containingFile as FakeFileForLightClass).navigationElement
        ktFile.let {
            val import = ktF().createImportDirective(ImportPath(FqName(importStr), false))
            if (ktFile.importDirectives.none { it.importPath == import.importPath }) {
                ktFile.importList?.add(import)
            }
        }
    }


    fun formatClass(psiClass: KtLightClass) {
//        val codeStyleManager = CodeStyleManager.getInstance(project)
//        codeStyleManager.reformat(psiClass)
    }

    fun findTargetTable(psiclass: KtLightClass,annoStr:String,tableNameSign:String): String? {
        val anno = psiclass.kotlinOrigin?.annotationEntries?.firstOrNull { it.typeReference?.text?.tail() == annoStr }?:return null
        val valueArg = anno.valueArguments.firstOrNull { (it.getArgumentName() as KtValueArgumentName).text == tableNameSign }?:return null
        return valueArg.getArgumentExpression()?.text?.replace("\"", "")
    }

    fun findFieldByName(fieldName: String, psiClass: KtLightClass): PsiElement? {
        return psiClass.kotlinOrigin?.findPropertyByName(fieldName)?.originalElement
    }

    fun deleteField(fieldName: String, psiClass: KtLightClass) {
        val field = psiClass.kotlinOrigin?.findPropertyByName(fieldName)?.originalElement
        field?.delete()
    }

    fun getNoteFromField(field: KtLightField): String {
        return field.kotlinOrigin?.docComment?.getDefaultSection()?.text
                ?.replaceFirst("*", "")
                ?.replaceFirst(" ", "")
                ?.replace("[\\s]+".toRegex(), " ")
                ?.trim { it <= ' ' } ?: ""
    }

    fun createModelField(field: KtLightField): ModelField {
        val modelField = ModelField()
        modelField.type = field.type.canonicalText.removeBase()
        modelField.name = field.name
        modelField.comment = FileDispatch.getNoteFromField(field)
        return modelField
    }


}

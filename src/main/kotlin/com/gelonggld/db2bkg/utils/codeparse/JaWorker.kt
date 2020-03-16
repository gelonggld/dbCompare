package com.gelonggld.db2bkg.utils.codeparse

import com.gelonggld.db2bkg.constants.StrConstant
import com.gelonggld.db2bkg.model.ModelField
import com.gelonggld.db2bkg.tail
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.DBConvertUtil.firstBig
import com.gelonggld.db2bkg.utils.DBConvertUtil.simpleJavaType
import com.gelonggld.db2bkg.utils.StrUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import org.intellij.lang.annotations.Language


class JaWorker(val project: Project) {
    fun addAnno2Class(annoStr: String, psiClass: PsiClass, vararg import: String) {
        psiClass.modifierList!!.addAnnotation(annoStr)
    }

    fun eleF(): PsiElementFactory {
        return JavaPsiFacade.getElementFactory(project)
    }


    fun addFieldtoClass(mFieldName: String, mFileType: String, psiClass: PsiClass, commit: String?): PsiElement {
        val elementFactory = eleF()
        val field = elementFactory.createField(mFieldName, elementFactory.createTypeByFQClassName(mFileType))
        val anchor: PsiElement
        if (findLastField(psiClass) == null) {
            anchor = psiClass.add(field)
        } else {
            anchor = psiClass.addAfter(field, findLastField(psiClass))
        }
        commit?.let {
            addCommentToField(commit, anchor, psiClass)
        }
        return anchor
    }


    fun addCommentToField(commit: String, anchor: PsiElement, psiClass: PsiClass) {
        val psiComment =
            eleF().createCommentFromText(StrConstant.COMMENT_HEAD + commit + StrConstant.COMMENT_TAIL, anchor)
        psiClass.addBefore(psiComment, anchor)
    }


    private fun findLastField(psiClass: PsiClass): PsiField? {
        return if (psiClass.fields.isEmpty()) {
            null
        } else psiClass.fields[psiClass.fields.size - 1]
    }


    fun getSet(mFieldName: String, mFileType: String, psiClass: PsiClass, anchor: PsiElement) {
        val psiGetMethod = createGetMethod(mFieldName, mFileType, psiClass)
        val psiSetMethod = createSetMethod(mFieldName, mFileType, psiClass)
        val addedGetMethod: PsiElement
        if (findLastMethod(psiClass) == null) {
            addedGetMethod = psiClass.addAfter(psiGetMethod, anchor)
        } else {
            addedGetMethod = psiClass.addAfter(psiGetMethod, findLastMethod(psiClass))
        }
        psiClass.addAfter(psiSetMethod, addedGetMethod)
    }


    private fun createGetMethod(mFieldName: String, mFileType: String, psiClass: PsiClass): PsiMethod {
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
        return eleF().createMethodFromText(getText.toString(), psiClass)
    }

    private fun createSetMethod(mFieldName: String, mFileType: String, psiClass: PsiClass): PsiMethod {
        @Language("JAVA")
        val setText = """public set${firstBig(mFieldName)}(${simpleJavaType(mFileType)} $mFieldName) {
            |   this.$mFieldName = $mFieldName;
            |   return this;
            |}
        """.trimMargin()
        return eleF().createMethodFromText(setText, psiClass)
    }


    fun formatClass(psiClass: PsiClass) {
        val codeStyleManager = CodeStyleManager.getInstance(project)
        codeStyleManager.reformat(psiClass)
    }


    private fun findLastMethod(psiClass: PsiClass): PsiMethod? {
        return if (psiClass.methods.isEmpty()) {
            null
        } else psiClass.methods[psiClass.methods.size - 1]
    }

    fun addAnnotationToField(annoStr: String, element: PsiElement, psiClass: PsiClass, vararg import: String) {
        val psiAnnotation = eleF().createAnnotationFromText(annoStr, null)
        psiClass.addBefore(psiAnnotation, element)
    }


    fun findTargetTable(psiClass: PsiClass, annoStr: String, tableNameSign: String): String? {
        val modifierList = psiClass.modifierList ?: return null
        val annotation = modifierList.annotations.firstOrNull { it.qualifiedName?.tail() == annoStr }
        return annotation?.parameterList?.attributes?.firstOrNull { it.name == tableNameSign }?.value?.text?.replace(
            "\"".toRegex(),
            ""
        )
    }

    fun findFieldByName(fieldName: String, psiClass: PsiClass): PsiElement? {
        return psiClass.findFieldByName(fieldName, true)
    }

    fun deleteFieldAndGetSet(fieldName: String, psiClass: PsiClass) {
        val field = psiClass.findFieldByName(fieldName, true)
        val getMethods = psiClass.findMethodsByName("get" + DBConvertUtil.firstBig(fieldName), true)
        val setMethods = psiClass.findMethodsByName("set" + DBConvertUtil.firstBig(fieldName), true)
        field!!.delete()
        getMethods.forEach { it.delete() }
        setMethods.forEach { it.delete() }

    }

    fun getNoteFromField(field: PsiField): String {
        val sb = StringBuffer()
        val docComm = field.docComment ?: return ""
        val psiElements = docComm.descriptionElements
        for (psiElement in psiElements) {
            val noteInfo = psiElement.text.replace("[\\s]+".toRegex(), " ").trim { it <= ' ' }
            if (!StrUtil.isEmpty(noteInfo)) {
                sb.append(noteInfo)
            }
        }
        return sb.toString()
    }

    fun createModelField(field: PsiField): ModelField {
        val modelField = ModelField()
        modelField.type = field.type.canonicalText
        modelField.name = field.name
        modelField.comment = FileDispatch.getNoteFromField(field)
        return modelField
    }


}

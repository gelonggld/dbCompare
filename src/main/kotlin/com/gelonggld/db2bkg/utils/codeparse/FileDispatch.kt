package com.gelonggld.db2bkg.utils.codeparse

import com.gelonggld.db2bkg.constants.StrConstant
import com.gelonggld.db2bkg.model.ModelField
import com.gelonggld.db2bkg.psi
import com.gelonggld.db2bkg.tail
import com.gelonggld.db2bkg.utils.ProperUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.elements.KtLightField


object FileDispatch {

    lateinit var ktWorker: KtWorker
    lateinit var jaWorker: JaWorker

    fun assemb() {
        ktWorker = KtWorker()
        jaWorker = JaWorker()
    }


    fun dispatchFindTargetTable(virtualFile: VirtualFile,annoStr:String,tableNameSign:String): String? {
        val psiclass = findClass(virtualFile) ?: return null
        if (psiclass is KtLightClass) {
            return ktWorker.findTargetTable(psiclass,annoStr,tableNameSign)
        }else{
            return jaWorker.findTargetTable(psiclass,annoStr,tableNameSign)
        }
    }


    fun findClass(virtualFile: VirtualFile): PsiClass? {

        val psiFile = virtualFile.psi()
        if (psiFile is PsiClassOwner) {
            if (psiFile.classes.isEmpty()) {
                return null
            }
            return psiFile.classes[0]
        }
        return null
    }


    fun addAnnotationToClass(annoStr: String, psiClass: PsiClass,vararg import: String) {
        if (psiClass is KtLightClass) {
            ktWorker.addAnno2Class(annoStr,  psiClass,*import)
        } else {
            jaWorker.addAnno2Class(annoStr, psiClass, *import)
        }
    }


    fun findFieldByName(fieldName:String,psiClass: PsiClass):PsiElement?{
        if (psiClass is KtLightClass) {
            return ktWorker.findFieldByName(fieldName,  psiClass)
        } else {
            return jaWorker.findFieldByName(fieldName,psiClass)
        }
    }


    fun allField(selectFile: VirtualFile): List<PsiField>? {
        val psiclass = findClass(selectFile) ?: return null
        return psiclass.allFields.filter { false == it.modifierList?.annotations?.any { "Transient" == it.qualifiedName?.tail() } }
    }


    fun addAnnotationToField(annoStr: String, element: PsiElement, psiClass: PsiClass, vararg import: String) {
        if (psiClass is KtLightClass) {
            ktWorker.addAnnotationToField(annoStr, element, psiClass, *import)
        } else {
            jaWorker.addAnnotationToField(annoStr, element, psiClass, *import)
        }
    }


    fun addFieldAndGetSet(mFieldName: String, mFileType: String, commit: String, isPri: Boolean, psiClass: PsiClass) {
        val anchor = addFieldtoClass(mFieldName, mFileType, psiClass, commit, false)
        if (psiClass is KtLightClass) {
        } else {
            jaWorker.getSet(mFieldName, mFileType, psiClass, anchor)
        }
        if (isPri) {
            addAnnotationToField("Id",anchor,psiClass,"javax.persistence.Id")
            addAnnotationToField("GeneratedValue(strategy = GenerationType.IDENTITY)",anchor,psiClass,"javax.persistence.GeneratedValue","javax.persistence.GenerationType")
        }

    }


    fun createModelField(field: PsiField): ModelField {
        if(field is KtLightField){
            return ktWorker.createModelField(field)
        }else {
            return jaWorker.createModelField(field)
        }
    }


    fun addFieldtoClass(mFieldName: String, mFileType: String, psiClass: PsiClass, commit: String? = null, lateinit: Boolean): PsiElement {
        if (psiClass is KtLightClass) {
            return ktWorker.addFieldtoClass(mFieldName, mFileType, psiClass, commit, lateinit)
        } else {
            return jaWorker.addFieldtoClass(mFieldName, mFileType, psiClass, commit)
        }
    }

    fun addCommentToField(commit: String, anchor: PsiElement, psiClass: PsiClass){
        if (psiClass is KtLightClass) {
            return ktWorker.addCommentToField(commit, anchor, psiClass)
        } else {
            return jaWorker.addCommentToField(commit, anchor, psiClass)
        }
    }

    fun deleteFieldAndGetSet(fieldName: String, psiClass: PsiClass) {
        if (psiClass is KtLightClass) {
            ktWorker.deleteField(fieldName, psiClass)
        } else {
            jaWorker.deleteFieldAndGetSet(fieldName, psiClass)
        }
    }

    fun getNoteFromField(field: PsiField): String{
        if (field is KtLightField) {
            return ktWorker.getNoteFromField(field)
        } else {
            return jaWorker.getNoteFromField(field)
        }
    }


    fun formatClass(psiClass: PsiClass) {
        if (psiClass is KtLightClass) {
            return ktWorker.formatClass(psiClass)
        } else {
            return jaWorker.formatClass(psiClass)
        }
    }



    fun kt() = ProperUtil.readPath(StrConstant.GEN_KT_FILE) == "Y"

    fun pre() = if (kt()) "ktt" else "jaa"

    fun tail() = if (kt()) "kt" else "java"

    fun ifKt(virtualFile: VirtualFile):Boolean {
        val psiClass = findClass(virtualFile)?:return false
        return psiClass is KtLightClass
    }

}

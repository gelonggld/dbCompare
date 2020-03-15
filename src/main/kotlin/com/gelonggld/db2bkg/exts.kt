package com.gelonggld.db2bkg

import com.gelonggld.db2bkg.utils.codeparse.FileDispatch
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class exts

fun VirtualFile.psi(project: Project) = PsiManager.getInstance(FileDispatch.project).findFile(this)

fun String.tail() :String{
    if(this.contains('.')){
        return this.substring(this.lastIndexOf('.') + 1,this.length)
    }
    return this
}

fun String.removeBase():String{
    if(this.startsWith("java.lang.")){
        return this.replaceFirst("java.lang.","").replace("?","")
    }else if(this.startsWith("kotlin.")){
        return this.replaceFirst("kotlin.","").replace("?","")
    }
    return this.replace("?","")
}

fun String.convertInt():String{
    return this.replace("Integer","Int")
}

fun String.ifNoNullReplace(key:String,value:String?)
    = if (value != null) {
        this.replace(key, value)
    } else this


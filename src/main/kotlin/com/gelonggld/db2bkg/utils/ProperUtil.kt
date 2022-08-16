package com.gelonggld.db2bkg.utils

import com.gelonggld.db2bkg.project
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

/**
 * Created by gelon on 2017/10/25.
 */
object ProperUtil {

    fun savePath(key: String, path: String) {
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setValue(key, path)
    }

    fun readPath(key: String,defaultValue:String = ""): String {
        val propertiesComponent = PropertiesComponent.getInstance(project)
        return propertiesComponent.getValue(key, "")
    }


}

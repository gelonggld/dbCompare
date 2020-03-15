package com.gelonggld.db2bkg.utils

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

/**
 * Created by gelon on 2017/10/25.
 */
object ProperUtil {

    fun savePath(key: String, path: String, project: Project) {
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setValue(key, path)
    }

    fun readPath(key: String, project: Project): String {
        val propertiesComponent = PropertiesComponent.getInstance(project)
        return propertiesComponent.getValue(key, "")
    }


}

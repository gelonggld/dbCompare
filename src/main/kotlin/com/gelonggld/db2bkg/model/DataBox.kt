package com.gelonggld.db2bkg.model

import com.gelonggld.db2bkg.ifNoNullReplace


/**
 * Created by gelon on 2017/10/25.
 */
class DataBox {


    var packageInfo: String? = null
    var auth: String? = null
    var createTime: String? = null
    var className: String? = null
    var basePath: String? = null
    var beanPath: String? = null
    var baseName: String? = null
    var beanName: String? = null
    var mapperPath: String? = null
    var interfaceName: String? = null
    var interfacePath: String? = null
    var importBaseModel:String? = null
    var baseModel:String? = null


    fun replaceAll(baseStr: String): String {
        return baseStr
                .ifNoNullReplace("\$packageInfo$", packageInfo)
                .ifNoNullReplace("\$auth$", auth)
                .ifNoNullReplace("\$createTime$", createTime)
                .ifNoNullReplace("\$className$", className)
                .ifNoNullReplace("\$basePath$", basePath)
                .ifNoNullReplace("\$beanPath$", beanPath)
                .ifNoNullReplace("\$baseName$", baseName)
                .ifNoNullReplace("\$beanName$", beanName)
                .ifNoNullReplace("\$mapperPath$", mapperPath)
                .ifNoNullReplace("\$interfaceName$", interfaceName)
                .ifNoNullReplace("\$interfacePath$", interfacePath)
                .ifNoNullReplace("\$importBaseModel$",importBaseModel)
                .ifNoNullReplace("\$BaseModel$",baseModel)

    }


    fun ifNoNullReplace(key: String, value: String?, baseStr: String): String {
        return if (value != null) {
            baseStr.replace(key, value)
        } else baseStr
    }

}

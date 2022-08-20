package com.gelonggld.db2bkg.utils

import com.gelonggld.db2bkg.constants.TypeConstanJa
import com.gelonggld.db2bkg.convertInt
import com.gelonggld.db2bkg.utils.db.SqlUtil

/**
 * Created by gelon on 2017/9/28.
 */
object DBConvertUtil {


    fun beanField2DB(beanName: String): String {
        val sb = StringBuffer()
        val charAyyay = beanName.toCharArray()
        for (c in charAyyay) {
            if (Character.isUpperCase(c)) {
                sb.append("_")
                sb.append(c)
            } else if (Character.isLowerCase(c)) {
                sb.append(Character.toUpperCase(c))
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }


    fun dBTableName2Bean(name: String, passFirst: Boolean): String {
        var name = name
        if (passFirst) {
            name = name.substring(name.indexOf("_") + 1, name.length)
        }
        val sb = StringBuffer()
        var nextUp = false
        for (c in name.toCharArray()) {
            if (sb.isEmpty()) {
                sb.append(Character.toUpperCase(c))
            } else if (c == '_') {
                nextUp = true
            } else if (nextUp) {
                sb.append(Character.toUpperCase(c))
                nextUp = false
            } else {
                sb.append(Character.toLowerCase(c))
            }
        }
        return sb.toString()
    }


    fun dBField2Bean(dbName: String): String {
        val sb = StringBuffer()
        val chars = dbName.toCharArray()
        var nextUp = false
        for (c in chars) {
            if (c == '_') {
                nextUp = true
            } else if (nextUp) {
                sb.append(Character.toUpperCase(c))
                nextUp = false
            } else {
                sb.append(Character.toLowerCase(c))
            }
        }
        return sb.toString()
    }


    fun getDB2BeanMapType(dbType: String, kt: Boolean): String {
        return SqlUtil.getDb2BeanMapGet(dbType.uppercase(), kt)
    }

    fun getBean2DBMapType(beanType: String?, kt: Boolean) = SqlUtil.getBean2DBMapGet(converBaseType(beanType!!, kt), kt)

    fun match(beanType: String, dbType: String, kt: Boolean): Boolean {
        var convertbeanType = converBaseType(beanType, kt)
        if(kt){
            convertbeanType = convertbeanType.convertInt()
        }
        return SqlUtil.getDb2BeanMapGet(dbType.uppercase(), kt) == convertbeanType || SqlUtil.getBean2DBMapGet(convertbeanType, kt) == dbType
    }

    private fun converBaseType(beanType: String, kt: Boolean): String {
        if (true == TypeConstanJa.getTypeMap()?.inverse()?.containsKey(beanType)) {
            return if (kt) {
                TypeConstanJa.getTypeMap()!!.inverse()[beanType]?.convertInt() ?: throw TypeCastException("没有找到基本类型$beanType")
            } else {
                TypeConstanJa.getTypeMap()!!.inverse()[beanType] ?: throw TypeCastException("没有找到基本类型$beanType")
            }
        }
        return beanType
    }

    fun firstBig(text: String): String {
        val sb = StringBuffer()
        return sb.append(Character.toUpperCase(text[0])).append(text.substring(1, text.length)).toString()
    }

    fun simpleJavaType(type: String): String {
        return type.substring(type.lastIndexOf('.') + 1, type.length)
    }


    fun converEmptyComment(comment: String?): String {
        return if (comment == null || comment == "") {
            " "
        } else comment
    }


    fun firstLow(str: String): String {
        val upFir = Character.toLowerCase(str[0])
        return StringBuffer().append(upFir).append(str.substring(1, str.length)).toString()
    }


}

package com.gelonggld.db2bkg.utils.db.typemapping

import com.gelonggld.db2bkg.exceptions.TypeMapException

import java.util.HashMap

/**
 * Created by gelon on 2017/10/10.
 */
object MariadbTypeMappingJa {


    private var db2BeanMap: MutableMap<String, String>? = null
    private var bean2DBMap: MutableMap<String, String>? = null


    fun init() {
        db2BeanMap = HashMap()
        bean2DBMap = HashMap()

        put("String", "VARCHAR", "CHAR", "TEXT")
        put("Byte[]", "BLOB","LONGBLOB")
        put("Integer", "INTEGER", "TINYINT", "SMALLINT", "MEDIUMINT", "BOOLEAN", "INT")
        bean2DBMap!!["Boolean"] = "TINYINT"
        put("java.math.BigInteger", "BIGINT")
        put("Float", "FLOAT")
        put("Double", "DOUBLE")
        put("java.math.BigDecimal", "DECIMAL")
        put("java.util.Date", "DATE", "YEAR", "TIME", "TIMESTAMP", "DATETIME")
    }


    fun getDb2BeanMapGet(dbType: String): String {
        if (db2BeanMap == null) {
            init()
        }
        return db2BeanMap!![dbType] ?: throw TypeMapException("错误的db类型--$dbType")
    }

    fun getBean2DBMapGet(beanType: String): String {
        if (bean2DBMap == null) {
            init()
        }
        return bean2DBMap!![beanType] ?: throw TypeMapException("错误的bean类型--$beanType")
    }

    private fun put(onlyValue: String, vararg values: String) {
        bean2DBMap!![onlyValue] = values[0]
        for (value in values) {
            db2BeanMap!![value] = onlyValue
        }
    }


}

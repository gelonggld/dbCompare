package com.gelonggld.db2bkg.constants

/**
 * Created by gelon on 2017/6/7.
 */
object RexConstant {

    val DRIVE = "mysql.driverClass=([\\w.]+)"
    val URL = "mysql.jdbcUrl=(\\S+)"
    val USERNAME = "mysql.name=(\\S+)"
    val PASSWORD = "mysql.password=(\\S+)"

    val MYSQL_DRIVE = "com.mysql.jdbc.Driver"
    val ORACLE_DRIVE = "oracle.jdbc.driver.OracleDriver"


}

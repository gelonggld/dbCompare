package com.gelonggld.db2bkg.utils.db.sql

/**
 * Created by gelon on 2017/10/27.
 */
object Mariadb {


    val TABLE_NAME = "{tableName}"
    val COLUMN_CONTENT = " {ColumnContent}"
    val NAME = "{name}"
    val TYPE = "{type}"
    val TYPE_LENGTH = "{typeLength}"
    val IF_NULL = "{ifNull}"
    val AUTO_INCREMENT = "{AUTO_INCREMENT}"
    val COMMENT = "{comment}"
    val DEFAULT = "{default}"
    val DBNAME = "{dbName}"
    val SPIT = ","


    val NULL = "NULL"
    val NOT_NULL = "NOT NULL"
    val AUTO_INCREMENT_STR = "AUTO_INCREMENT"
    val DEFAULT_STR = fm("DEFAULT %s", DEFAULT)
    val LEFT_BRACKET = "("
    val RIGHT_BRACKET = ")"
    val DATE_STR = "DATE"
    val SHOW_DATABASE = "SELECT DATABASE()"
    val SHOW_TABLES = "SHOW TABLES"

    val USER_INFORMATION = "USE information_schema"


    val USE_DB = fm("USE %s", DBNAME)
    val DROP_FIELD = "ALTER TABLE `%s` DROP COLUMN `%s`"
    val ADD_FIELD = "ALTER TABLE `%s` ADD COLUMN `%s`  %s%s %s %s COMMENT '%s'"
    val ADD_COMMENT = "ALTER TABLE `%s` MODIFY COLUMN `%s` %s(%s) COMMENT '%s'"
    val CREATE_TABLE = fm("CREATE TABLE `%s` ( %s)", TABLE_NAME, COLUMN_CONTENT)
    val CREATE_TABLE_COLUMN = fm("`%s`  %s%s %s %s %s COMMENT '%s'", NAME, TYPE, TYPE_LENGTH, IF_NULL, AUTO_INCREMENT, DEFAULT, COMMENT)
    val CREATE_TABLE_PRI = fm("PRIMARY KEY (`%s`)", NAME)
    val SELECT_TABLE_INFO = fm("SELECT COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_KEY,COLUMN_DEFAULT,EXTRA,COLUMN_COMMENT FROM information_schema.COLUMNS " + "WHERE table_schema = '%s' AND table_name = '%s'", DBNAME, TABLE_NAME)


    fun fm(sql: String, vararg params: String): String {
        return String.format(sql, *params as Array<Any>)
    }

}

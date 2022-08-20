package com.gelonggld.db2bkg.utils.db.sql

/**
 * Created by gelon on 2017/10/27.
 */
object Mysql {


    const val TABLE_NAME = "{tableName}"
    const val COLUMN_CONTENT = " {ColumnContent}"
    const val NAME = "{name}"
    const val TYPE = "{type}"
    const val TYPE_LENGTH = "{typeLength}"
    const val IF_NULL = "{ifNull}"
    const val AUTO_INCREMENT = "{AUTO_INCREMENT}"
    const val COMMENT = "{comment}"
    const val DEFAULT = "{default}"
    const val DBNAME = "{dbName}"
    const val SPIT = ","


    const val NULL = "NULL"
    const val NOT_NULL = "NOT NULL"
    const val AUTO_INCREMENT_STR = "AUTO_INCREMENT"
    val DEFAULT_STR = fm("DEFAULT %s", DEFAULT)
    const val LEFT_BRACKET = "("
    const val RIGHT_BRACKET = ")"
    const val DATE_STR = "DATE"
    const val SHOW_DATABASE = "SELECT DATABASE()"
    const val SHOW_TABLES = "SHOW TABLES"

    const val USER_INFORMATION = "USE information_schema"


    val USE_DB = fm("USE %s", DBNAME)
    const val DROP_FIELD = "ALTER TABLE `%s` DROP COLUMN `%s`"
    const val ADD_FIELD = "ALTER TABLE `%s` ADD COLUMN `%s`  %s%s %s %s COMMENT '%s'"
    const val ADD_COMMENT = "ALTER TABLE `%s` MODIFY COLUMN `%s` %s(%s) COMMENT '%s'"
    val CREATE_TABLE = fm("CREATE TABLE `%s` ( %s)", TABLE_NAME, COLUMN_CONTENT)
    val CREATE_TABLE_COLUMN = fm("`%s`  %s%s %s %s %s COMMENT '%s'", NAME, TYPE, TYPE_LENGTH, IF_NULL, AUTO_INCREMENT, DEFAULT, COMMENT)
    val CREATE_TABLE_PRI = fm("PRIMARY KEY (`%s`)", NAME)
    val SELECT_TABLE_INFO = fm("SELECT COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_KEY,COLUMN_DEFAULT,EXTRA,COLUMN_COMMENT FROM information_schema.COLUMNS " + "WHERE table_schema = '%s' AND table_name = '%s'", DBNAME, TABLE_NAME)


    private fun fm(sql: String, vararg params: String): String {
        return String.format(sql, *params)
    }

}

package com.gelonggld.db2bkg.utils.db.sql

/**
 * Created by gelon on 2017/10/28.
 */
object Oracle {


    val TABLE_NAME = "{tableName}"
    val COLUMN_CONTENT = " {ColumnContent}"
    val NAME = "{name}"
    val TYPE = "{type}"
    val TYPE_LENGTH = "{typeLength}"
    val IF_NULL = "{ifNull}"
    val COMMENT = "{comment}"
    val DEFAULT = "{default}"
    val DBNAME = "{dbName}"
    val SPIT = ","


    val NULL = "NULL"
    val NOT_NULL = "NOT NULL"
    val DEFAULT_STR = fm("DEFAULT %s", DEFAULT)
    val LEFT_BRACKET = "("
    val RIGHT_BRACKET = ")"
    val DATE_STR = "DATE"
    val SHOW_DATABASE = "SELECT DATABASE()"
    val SHOW_TABLES = "SHOW TABLES"

    val USER_INFORMATION = "USE information_schema"


    val USE_DB = fm("USE %s", DBNAME)
    val DROP_FIELD = "ALTER TABLE \"%s\" DROP COLUMN \"%s\""
    val ADD_FIELD = "ALTER TABLE \"%s\" ADD ( \"%s\"  %s%s DEFAULT %s %s)"
    val ADD_COMMENT = "comment on column \"%s\".\"%s\" is '%s'"
    val CREATE_TABLE = fm("CREATE TABLE \"%s\" ( %s)", TABLE_NAME, COLUMN_CONTENT)
    val CREATE_TABLE_COLUMN = fm("\"%s\"  %s%s %s %s", NAME, TYPE, TYPE_LENGTH, DEFAULT, IF_NULL)
    val CREATE_TABLE_PRI = fm("PRIMARY KEY (\"%s\")", NAME)
    val SELECT_TABLE_INFO = fm("select TABLE_COLUMN.COLUMN_NAME,TABLE_COLUMN.DATA_TYPE,TABLE_COLUMN.DATA_LENGTH,TABLE_COLUMN.NULLABLE,TABLE_COLUMN.DATA_DEFAULT,TABLE_COMMENT.COMMENTS " +
            "from user_tab_columns TABLE_COLUMN,user_col_comments TABLE_COMMENT " +
            "where TABLE_COLUMN.Table_Name='%s' AND  TABLE_COMMENT.Table_Name = TABLE_COLUMN.Table_Name AND TABLE_COLUMN.COLUMN_NAME = TABLE_COMMENT.COLUMN_NAME", TABLE_NAME)

    fun fm(sql: String, vararg params: String): String {
        return String.format(sql, *params)
    }


}

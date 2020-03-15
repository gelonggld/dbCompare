package com.gelonggld.db2bkg.utils.db.bean


import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.db.SqlBean
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.db.sql.Oracle
import com.gelonggld.db2bkg.utils.db.typemapping.OracleTypeMappingJa
import com.gelonggld.db2bkg.model.TableCreate
import com.gelonggld.db2bkg.model.TableData
import com.gelonggld.db2bkg.utils.db.typemapping.OracleTypeMappingKt
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Savepoint
import java.util.ArrayList


/**
 * Created by gelon on 2017/10/27.
 */
class OracleSqlBean : SqlBean {


    override fun selectDBName(connection: Connection): String? {
        var resultSet: ResultSet? = null
        try {
            resultSet = connection.prepareStatement("select SYS_CONTEXT('USERENV','INSTANCE_NAME') from dual").executeQuery()
            if (resultSet!!.next()) {
                return resultSet.getString(1)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            SqlUtil.recyclerResultSet(resultSet)
        }
        return null
    }


    @Throws(SQLException::class)
    override fun allDBTable(connection: Connection): ArrayList<String> {
        val tableSet: ResultSet = connection.prepareStatement("select TABLE_NAME from user_tables").executeQuery()
        val dbTableNames = ArrayList<String>()
        while (tableSet.next()) {
            dbTableNames.add(tableSet.getString(1))
        }
        SqlUtil.recyclerResultSet(tableSet)
        return dbTableNames
    }


    override fun getDBFields(tableName: String, connection: Connection, dbName: String): ArrayList<DBField>? {
        var resultSet: ResultSet? = null
        val dbFields = ArrayList<DBField>()
        try {
            resultSet = connection.prepareStatement(Oracle.SELECT_TABLE_INFO.replace(Oracle.TABLE_NAME, tableName)).executeQuery()
            if (resultSet != null) {
                while (resultSet.next()) {
                    val dbField = DBField()
                    dbField.name = resultSet.getString(1)
                    dbField.type = resultSet.getString(2)
                    dbField.rang = resultSet.getInt(3)
                    processNullable(resultSet.getString(4), dbField)
                    dbField.defaultValue = resultSet.getString(5)
                    dbField.comment = resultSet.getString(6)
                    dbFields.add(dbField)
                }
            }
            return dbFields
        } catch (e: SQLException) {
            e.printStackTrace()
            SqlUtil.recyclerResultSet(resultSet)
            return null
        } finally {
            SqlUtil.recyclerResultSet(resultSet)
        }
    }


    override fun createTable(tableCreates: List<TableCreate>, priTableCreate: TableCreate, tableName: String,conn: Connection) {
        val sb = StringBuffer()
        for (tableCreate in tableCreates) {
            appendColumnSql(sb, tableCreate, tableCreate.isPri())
        }
        sb.append(Oracle.SPIT).append(Oracle.CREATE_TABLE_PRI.replace(Oracle.NAME, priTableCreate.getDbFieldName()!!))
        val createSql = Oracle.CREATE_TABLE.replace(Oracle.TABLE_NAME, tableName).replace(Oracle.COLUMN_CONTENT, sb.toString())
        var sp: Savepoint? = null
        try {
            conn.autoCommit = false
            sp = conn.setSavepoint("a")
            conn.prepareStatement(createSql).execute()
            for (tableCreate in tableCreates) {
                comment(tableCreate.getDbFieldName()!!, tableCreate.getFieldComment()!!, conn, tableName)
            }
            conn.commit()
        } catch (e: SQLException) {
            if (sp != null) {
                try {
                    conn.rollback(sp)
                } catch (e1: SQLException) {
                    e1.printStackTrace()
                }

            }
            e.printStackTrace()
        }

    }

    @Throws(SQLException::class)
    private fun comment(dbFileName: String, comment: String, conn: Connection, tableName: String) {
        val sql = String.format(Oracle.ADD_COMMENT, tableName, dbFileName, comment)
        conn.prepareStatement(sql).execute()
    }


    private fun appendColumnSql(sb: StringBuffer, tableCreate: TableCreate, pri: Boolean) {
        val ifNull = if (tableCreate.getCanNull()!!.isSelected) Oracle.NULL else Oracle.NOT_NULL

        var defaultStr = Oracle.DEFAULT_STR.replace(Oracle.DEFAULT, tableCreate.getDefaultValue()!!.text)
        if (pri) {
            defaultStr = ""
        }
        var typeLength = Oracle.LEFT_BRACKET + tableCreate.getFieldRang()!!.text + Oracle.RIGHT_BRACKET
        if (tableCreate.getDbFieldType()!! == Oracle.DATE_STR) {
            typeLength = ""
        }
        val sqlColumn = Oracle.CREATE_TABLE_COLUMN
                .replace(Oracle.NAME, tableCreate.getDbFieldName()!!)
                .replace(Oracle.TYPE, tableCreate.getDbFieldType()!!)
                .replace(Oracle.TYPE_LENGTH, typeLength)
                .replace(Oracle.IF_NULL, ifNull)
                .replace(Oracle.DEFAULT, defaultStr)
        if (sb.length != 0) {
            sb.append(",").append(sqlColumn)
        } else {
            sb.append(sqlColumn)
        }
    }


    fun processNullable(info: String, dbField: DBField) {
        dbField.notNull = info == "N"
    }


    override fun motifyDBTable(addToDBs: List<TableData>, deleteFromDBs: List<TableData>, addCommitToDBs: List<TableData>, dbTable: String,kt : Boolean, connection: Connection) {
        var savepoint: Savepoint? = null
        try {
            connection.autoCommit = false
            savepoint = connection.setSavepoint("b")
            for (tableData in deleteFromDBs) {
                connection.prepareStatement(String.format(Oracle.DROP_FIELD, dbTable, tableData.getdName())).execute()
            }
            for (tableData in addToDBs) {
                val ifNull = if (tableData.getCanNone()!!.isSelected) Oracle.NULL else Oracle.NOT_NULL
                var typeLength = Oracle.LEFT_BRACKET + tableData.getFieldRang()!!.text + Oracle.RIGHT_BRACKET
                if (tableData.getmType() == "java.util.Date") {
                    typeLength = ""
                }
                val default = if(tableData.getCanNone()!!.isSelected) "DEFAULT ${tableData.getDefaultValue()!!.text}" else ""
                val sql = String.format(Oracle.ADD_FIELD, dbTable, DBConvertUtil.beanField2DB(tableData.getmName()!!),
                        DBConvertUtil.getBean2DBMapType(tableData.getmType(),kt),
                        typeLength, default, ifNull)
                connection.prepareStatement(sql).execute()

                comment(DBConvertUtil.beanField2DB(tableData.getmName()!!), DBConvertUtil.converEmptyComment(tableData.getmNote()), connection, dbTable)

            }
            for (tableData in addCommitToDBs) {
                val sql = String.format(Oracle.ADD_COMMENT, dbTable, tableData.getdName(), tableData.getdType(),
                        DBConvertUtil.converEmptyComment(tableData.getmNote()))
                connection.prepareStatement(sql).execute()
            }
            connection.commit()
        } catch (e: SQLException) {
            if (savepoint != null) {
                try {
                    connection.rollback(savepoint)
                } catch (e1: SQLException) {
                    e1.printStackTrace()
                }

            }
            e.printStackTrace()
        }

    }


    override fun getDb2BeanMapGet(dbType: String,kt:Boolean): String {
        if(kt){
            return OracleTypeMappingKt.getDb2BeanMapGet(dbType)
        }else{
            return OracleTypeMappingJa.getDb2BeanMapGet(dbType)
        }
    }

    override fun getBean2DBMapGet(beanType: String,kt:Boolean): String {
        if(kt){
            return OracleTypeMappingKt.getBean2DBMapGet(beanType)
        }else {
            return OracleTypeMappingJa.getBean2DBMapGet(beanType)
        }
    }


}

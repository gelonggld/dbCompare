package com.gelonggld.db2bkg.utils.db.bean


import com.gelonggld.db2bkg.exceptions.StringProcessException
import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.utils.DBConvertUtil
import com.gelonggld.db2bkg.utils.db.SqlBean
import com.gelonggld.db2bkg.utils.db.SqlUtil
import com.gelonggld.db2bkg.utils.db.sql.Mysql
import com.gelonggld.db2bkg.utils.db.typemapping.MysqlTypeMappingJa
import com.gelonggld.db2bkg.model.TableCreate
import com.gelonggld.db2bkg.model.TableData
import com.gelonggld.db2bkg.utils.db.typemapping.MysqlTypeMappingKt
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Savepoint
import java.util.ArrayList


/**
 * Created by gelon on 2017/10/27.
 */
class MysqlBean : SqlBean {


    override fun selectDBName(connection: Connection): String? {
        var resultSet: ResultSet? = null
        try {
            resultSet = connection.prepareStatement(Mysql.SHOW_DATABASE).executeQuery()
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
        val tableSet: ResultSet = connection.prepareStatement(Mysql.SHOW_TABLES).executeQuery()
        val dbTableNames = ArrayList<String>()
        while (tableSet.next()) {
            dbTableNames.add(tableSet.getString(1))
        }
        SqlUtil.recyclerResultSet(tableSet)
        return dbTableNames
    }


    override fun getDBFields(tableName: String, connection: Connection, dbName: String): ArrayList<DBField>? {
        try {
            connection.prepareStatement(Mysql.USER_INFORMATION).execute()
        } catch (e: SQLException) {
            e.printStackTrace()
            //            error("执行>>>USE information_schema   出错");
            return null
        }

        var resultSet: ResultSet? = null
        val dbFields = ArrayList<DBField>()
        try {

            resultSet = connection.prepareStatement(Mysql.SELECT_TABLE_INFO.replace(Mysql.DBNAME, dbName).replace(Mysql.TABLE_NAME, tableName)).executeQuery()
            if (resultSet != null) {
                while (resultSet.next()) {
                    val dbField = DBField()
                    dbField.name = resultSet.getString(1)
                    processType(resultSet.getString(2), dbField)
                    processRang(resultSet.getString(2), dbField)
                    processTypeExt(resultSet.getString(2), dbField)
                    processNullable(resultSet.getString(3), dbField)
                    dbField.isPri = resultSet.getString(4) == "PRI"
                    dbField.defaultValue = resultSet.getString(5)
                    dbField.ext = resultSet.getString(6)
                    dbField.comment = resultSet.getString(7)
                    dbFields.add(dbField)
                }
            }
            connection.prepareStatement(Mysql.USE_DB.replace(Mysql.DBNAME, dbName)).execute()
            return dbFields
        } catch (e: SQLException) {
            e.printStackTrace()
            SqlUtil.recyclerResultSet(resultSet)
            return null
        } finally {
            SqlUtil.recyclerResultSet(resultSet)
        }
    }


    override fun createTable(tableCreates: List<TableCreate>, priTableCreate: TableCreate, tableName: String, conn: Connection) {
        val sb = StringBuffer()
        for (tableCreate in tableCreates) {
            appendColumnSql(sb, tableCreate, tableCreate.isPri.value)
        }
        sb.append(Mysql.SPIT).append(Mysql.CREATE_TABLE_PRI.replace(Mysql.NAME, priTableCreate.dbFieldName.value))
        val createSql = Mysql.CREATE_TABLE.replace(Mysql.TABLE_NAME, tableName).replace(Mysql.COLUMN_CONTENT, sb.toString())
        var sp: Savepoint? = null
        try {
            conn.autoCommit = false
            sp = conn.setSavepoint()
            conn.prepareStatement(createSql).execute()
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


    private fun appendColumnSql(sb: StringBuffer, tableCreate: TableCreate, pri: Boolean) {
        val ifNull = if (tableCreate.canNull.value) Mysql.NULL else Mysql.NOT_NULL
        var auto = ""
        if (tableCreate.autoIncrement.value) {
            auto = Mysql.AUTO_INCREMENT_STR
        }
        var defaultStr = Mysql.DEFAULT_STR.replace(Mysql.DEFAULT, tableCreate.defaultValue.value)
        if (pri || !tableCreate.canNull.value ) {
            defaultStr = ""
        }
        var typeLength = Mysql.LEFT_BRACKET + tableCreate.fieldRang.value + Mysql.RIGHT_BRACKET
        if (tableCreate.dbFieldType.value == Mysql.DATE_STR) {
            typeLength = ""
        }
        val sqlColumn = Mysql.CREATE_TABLE_COLUMN
                .replace(Mysql.NAME, tableCreate.dbFieldName.value)
                .replace(Mysql.TYPE, tableCreate.dbFieldType.value)
                .replace(Mysql.TYPE_LENGTH, typeLength)
                .replace(Mysql.IF_NULL, ifNull)
                .replace(Mysql.AUTO_INCREMENT, auto)
                .replace(Mysql.DEFAULT, defaultStr)
                .replace(Mysql.COMMENT, tableCreate.fieldComment.value!!)
        if (sb.isNotEmpty()) {
            sb.append(",").append(sqlColumn)
        } else {
            sb.append(sqlColumn)
        }
    }


    override fun motifyDBTable(addToDBs: List<TableData>, deleteFromDBs: List<TableData>, addCommitToDBs: List<TableData>, dbTable: String,kt:Boolean, connection: Connection) {
        var savepoint: Savepoint? = null
        try {
            connection.autoCommit = false
            savepoint = connection.setSavepoint()
            for (tableData in deleteFromDBs) {
                connection.prepareStatement(String.format(Mysql.DROP_FIELD, dbTable, tableData.dName.value)).execute()
            }
            for (tableData in addToDBs) {
                val ifNull = if (tableData.canNone.value) Mysql.NULL else Mysql.NOT_NULL
                var typeLength = Mysql.LEFT_BRACKET + tableData.fieldRang.value + Mysql.RIGHT_BRACKET
                if (tableData.mType.value == "java.util.Date") {
                    typeLength = ""
                }
                val default = if(tableData.canNone.value) "DEFAULT ${tableData.defaultValue.value}" else ""
                val sql = String.format(Mysql.ADD_FIELD, dbTable, DBConvertUtil.beanField2DB(tableData.mType.value!!),
                        DBConvertUtil.getBean2DBMapType(tableData.mType.value,kt),
                        typeLength, ifNull, default, DBConvertUtil.converEmptyComment(tableData.mNote.value))
                connection.prepareStatement(sql).execute()
            }
            for (tableData in addCommitToDBs) {
                val sql = String.format(Mysql.ADD_COMMENT, dbTable, tableData.dName.value, tableData.dType.value, tableData.dataNum,
                        DBConvertUtil.converEmptyComment(tableData.mNote.value))
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
            return MysqlTypeMappingKt.getDb2BeanMapGet(dbType)
        }else{
            return MysqlTypeMappingJa.getDb2BeanMapGet(dbType)
        }

    }

    override fun getBean2DBMapGet(beanType: String,kt:Boolean): String {
        if(kt){
            return MysqlTypeMappingKt.getBean2DBMapGet(beanType)
        }else {
            return MysqlTypeMappingJa.getBean2DBMapGet(beanType)
        }
    }


    fun processType(info: String, dbField: DBField) {
        if (info.contains("(")) {
            dbField.type = info.substring(0, info.indexOf("("))
        } else if (info.contains(" ")) {
            dbField.type = info.substring(0, info.indexOf(" "))
        } else {
            dbField.type = info
        }
    }

    @Throws(StringProcessException::class)
    fun processRang(info: String, dbField: DBField) {
        if (info.contains("(")) {
            try {
                val numStr: String
                if (info.contains(",")) {
                    numStr = info.substring(info.indexOf("(") + 1, info.indexOf(","))
                } else {
                    numStr = info.substring(info.indexOf("(") + 1, info.indexOf(")"))
                }
                dbField.rang = Integer.parseInt(numStr)
            } catch (e: Exception) {
                throw StringProcessException("在截取  $info   边界的时候发生错误")
            }

        }
    }

    fun processTypeExt(info: String, dbField: DBField) {
        if (info.contains(" ")) {
            dbField.typeExt = info.substring(info.indexOf(" ") + 1, info.length)
        }
    }


    private fun processNullable(info: String, dbField: DBField) {
        dbField.notNull = info == "NO"
    }


}

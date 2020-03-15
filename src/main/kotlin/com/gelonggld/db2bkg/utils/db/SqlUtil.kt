package com.gelonggld.db2bkg.utils.db


import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.model.TableCreate
import com.gelonggld.db2bkg.model.TableData
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList


/**
 * Created by gelon on 2017/10/25.
 */
object SqlUtil {


    lateinit var sqlBean: SqlBean

    @Suppress("unused")
    fun recyDBConn(connection: Connection, tableSet: ResultSet) {
        recyclerResultSet(tableSet)
        recycleConn(connection)
    }

    fun recyclerResultSet(tableSet: ResultSet?) {
        if (tableSet != null) {
            try {
                tableSet.close()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }

        }
    }


    fun recycleConn(connection: Connection?) {
        if (connection != null) {
            try {
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }

        }
    }


    fun selectDBName(connection: Connection): String? {
        return sqlBean.selectDBName(connection)
    }

    @Throws(SQLException::class)
    fun allDBTable(connection: Connection): ArrayList<String> {
        return sqlBean.allDBTable(connection)
    }

    fun getDBFields(tableName: String, connection: Connection, dbName: String): ArrayList<DBField>? {
        return sqlBean.getDBFields(tableName, connection, dbName)
    }

    fun createTable(tableCreates: List<TableCreate>, priTableCreate: TableCreate, tableName: String, conn: Connection) {
        sqlBean.createTable(tableCreates, priTableCreate, tableName, conn)
    }

    fun motifyDBTable(addToDBs: List<TableData>, deleteFromDBs: List<TableData>, addCommitToDBs: List<TableData>, dbTable: String,kt:Boolean, connection: Connection) {
        sqlBean.motifyDBTable(addToDBs, deleteFromDBs, addCommitToDBs, dbTable,kt, connection)
    }


    fun getDb2BeanMapGet(dbType: String,kt :Boolean): String {
        return sqlBean.getDb2BeanMapGet(dbType,kt)
    }

    fun getBean2DBMapGet(beanType: String,kt :Boolean): String {
        return sqlBean.getBean2DBMapGet(beanType,kt)
    }


}

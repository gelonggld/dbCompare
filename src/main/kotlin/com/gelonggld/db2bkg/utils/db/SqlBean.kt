package com.gelonggld.db2bkg.utils.db


import com.gelonggld.db2bkg.model.DBField
import com.gelonggld.db2bkg.model.TableCreate
import com.gelonggld.db2bkg.model.TableData
import java.sql.Connection
import java.sql.SQLException
import java.util.ArrayList

/**
 * Created by gelon on 2017/10/27.
 */
interface SqlBean {


    fun selectDBName(connection: Connection): String?

    @Throws(SQLException::class)
    fun allDBTable(connection: Connection): ArrayList<String>

    fun getDBFields(tableName: String, connection: Connection, dbName: String): ArrayList<DBField>?

    fun createTable(tableCreates: List<TableCreate>, priTableCreate: TableCreate, tableName: String, conn: Connection)

    fun motifyDBTable(addToDBs: List<TableData>, deleteFromDBs: List<TableData>, addCommitToDBs: List<TableData>, dbTable: String,kt:Boolean, connection: Connection)

    fun getDb2BeanMapGet(dbType: String,kt :Boolean): String

    fun getBean2DBMapGet(beanType: String,kt :Boolean): String
}

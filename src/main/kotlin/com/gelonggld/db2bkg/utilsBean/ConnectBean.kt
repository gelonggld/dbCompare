package com.gelonggld.db2bkg.utilsBean

/**
 * Created by gelon on 2017/6/7.
 */
class ConnectBean(val url: String, val drive: String, val username: String, val password: String) {
    lateinit var databaseName: String
}

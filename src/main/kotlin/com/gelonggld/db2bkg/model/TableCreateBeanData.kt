package com.gelonggld.db2bkg.model

import androidx.compose.runtime.mutableStateOf

/**
 * Created by gelon on 2017/10/24.
 */
class TableCreateBeanData(var tableName: String) {
    var mapper = mutableStateOf(false)
    var service= mutableStateOf(false)
    var generator= mutableStateOf(false)
}

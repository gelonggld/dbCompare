package com.gelonggld.db2bkg.utils

/**
 * Created by gelon on 2017/12/11.
 */
object LogUtil {

    fun log(text: String?) {
        text?.let {
            println(text)
        }
    }

}

package com.gelonggld.db2bkg.utils

import javax.swing.*
import java.awt.*

/**
 * Created by gelon on 2017/9/29.
 */
object DialogUtil {

    fun centSelf(dialog: JDialog, dialogWidth: Int, dialogHeight: Int) {
        val kit = Toolkit.getDefaultToolkit() // 定义工具包
        val screenSize = kit.screenSize // 获取屏幕的尺寸
        val screenWidth = screenSize.width // 获取屏幕的宽
        val screenHeight = screenSize.height // 获取屏幕的高
        dialog.setLocation(screenWidth / 2 - dialogWidth / 2, screenHeight / 2 - dialogHeight / 2)
    }
}

package com.gelonggld.db2bkg

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposePanel
import com.gelonggld.db2bkg.Application.Companion.CHECK
import com.gelonggld.db2bkg.dialogs.CheckDialog
import com.gelonggld.db2bkg.utils.DialogUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.WindowConstants

val show = mutableStateOf(CHECK)
lateinit var project: Project
lateinit var rootFile: VirtualFile

class Application : JDialog() {


    init {
        isModal = true
        DialogUtil.centSelf(this, 900, 600)
        contentPane.isFocusable = true
        contentPane.requestFocus()
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        val composePanel = ComposePanel()
        contentPane.add(composePanel)
        composePanel.setContent { content() }
    }

    override fun getPreferredSize() = Dimension(900,600)


    companion object {

        const val CHECK = 1
        const val CREATE_BEAN = 2
        const val CREATE_TABLE = 3
        const val DB_COMPARE = 4
        const val NONE = 0

    }



    @Composable
    fun content() = when (show.value) {
        CHECK -> { CheckDialog().content() }
        else -> { CheckDialog().content() }
    }

}
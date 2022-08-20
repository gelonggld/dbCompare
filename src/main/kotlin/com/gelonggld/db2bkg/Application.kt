package com.gelonggld.db2bkg

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.gelonggld.db2bkg.Application.CHECK
import com.gelonggld.db2bkg.dialogs.CheckDialog
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

val show = mutableStateOf(CHECK)
lateinit var project : Project
lateinit var rootFile: VirtualFile

object Application {

    const val CHECK = 1
    const val CREATE_BEAN = 2
    const val CREATE_TABLE = 3
    const val DB_COMPARE = 4
    const val NONE = 0

    fun show() = application {

        when(show.value) {
            CHECK -> page { CheckDialog().content() }
        }

    }

    @Composable
    fun ApplicationScope.page(content: @Composable FrameWindowScope.() -> Unit) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "db compare"
        ) {
            MaterialTheme { content.invoke(this) }
        }
    }


}
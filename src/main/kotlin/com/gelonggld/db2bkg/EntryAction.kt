package com.gelonggld.db2bkg

import com.gelonggld.db2bkg.dialogs.CheckDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project

lateinit var project : Project
class EntryAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        project = e.getData(PlatformDataKeys.PROJECT) ?: return
        val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        val checkDialog = CheckDialog(project,virtualFile)
        checkDialog.pack()
        checkDialog.isVisible = true
    }

}

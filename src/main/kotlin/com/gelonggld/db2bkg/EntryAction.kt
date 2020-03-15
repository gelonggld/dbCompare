package com.gelonggld.db2bkg

import com.gelonggld.db2bkg.dialogs.CheckDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys

class EntryAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return
        val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        val checkDialog = CheckDialog(project,virtualFile)
        checkDialog.pack()
        checkDialog.isVisible = true
    }
}

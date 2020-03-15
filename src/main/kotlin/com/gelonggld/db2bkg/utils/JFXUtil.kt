package com.gelonggld.db2bkg.utils

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.TableView
import javafx.scene.paint.Color

object JFXUtil {

    fun addTable( jfxPanel: JFXPanel,tableView: TableView<*>) {
        Platform.setImplicitExit(false)
        Platform.runLater {
            val root = Group()
            val scene = Scene(root, Color.ALICEBLUE)
            val tableView = tableView
            root.children.add(tableView)
            jfxPanel.scene = scene
        }
    }


}
package com.gelonggld.db2bkg.model

import javafx.geometry.Pos
import javafx.scene.control.RadioButton
import javafx.scene.control.TableCell
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox

import java.util.EnumSet

/**
 * Created by gelon on 2017/10/10.
 */
class RadioButtonCell<S, T : Enum<T>>(private val enumeration: EnumSet<T>) : TableCell<S, T>() {

    override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        if (!empty) {
            // gui setup
            val hb = HBox(7.0)
            hb.alignment = Pos.CENTER
            val group = ToggleGroup()

            // create a radio button for each 'element' of the enumeration
            for (enumElement in enumeration) {
                val radioButton = RadioButton(enumElement.toString())
                radioButton.userData = enumElement
                radioButton.toggleGroup = group
                hb.children.add(radioButton)
                if (enumElement == item) {
                    radioButton.isSelected = true
                }
            }

            // issue events on change of the selected radio button
            group.selectedToggleProperty().addListener { observable, oldValue, newValue ->
                tableView.edit(index, tableColumn)
                this@RadioButtonCell.commitEdit(newValue.userData as T)
            }
            graphic = hb
        }
    }
}

package com.gelonggld.db2bkg.model

import javafx.scene.control.CheckBox

/**
 * Created by gelon on 2017/10/24.
 */
class TableList(private var tableName: String) {
    private var mapper: CheckBox
    private var service: CheckBox
    private var generator: CheckBox


    init {
        mapper = CheckBox("生成mapper")
        service = CheckBox("生成service")
        generator = CheckBox("生成")

        service.selectedProperty().addListener { _, _, newValue ->
            if (newValue!!) {
                mapper.isSelected = true
                generator.isSelected = true
            }
        }

        mapper.selectedProperty().addListener { _, _, newValue ->
            if (newValue!!) {
                generator.isSelected = true
            }
        }
    }


    fun getTableName(): String {
        return tableName
    }

    fun setTableName(tableName: String): TableList {
        this.tableName = tableName
        return this
    }

    fun getMapper(): CheckBox {
        return mapper
    }

    fun setMapper(mapper: CheckBox): TableList {
        this.mapper = mapper
        return this
    }

    fun getService(): CheckBox {
        return service
    }

    fun setService(service: CheckBox): TableList {
        this.service = service
        return this
    }

    fun getGenerator(): CheckBox {
        return generator
    }

    fun setGenerator(generator: CheckBox): TableList {
        this.generator = generator
        return this
    }



    companion object {


        fun Build(tableName: String): TableList {
            return TableList(tableName)
        }
    }
}

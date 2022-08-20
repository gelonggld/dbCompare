package com.gelonggld.db2bkg.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gelonggld.db2bkg.Application.NONE
import com.gelonggld.db2bkg.show

object ViewComponent {

    @Composable
    fun topBar(title: String) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = { show.value = NONE }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null
                    )
                }
            }
        )
    }


}
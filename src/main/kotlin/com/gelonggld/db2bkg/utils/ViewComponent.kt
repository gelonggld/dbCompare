package com.gelonggld.db2bkg.utils

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import com.gelonggld.db2bkg.Application.Companion.NONE
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
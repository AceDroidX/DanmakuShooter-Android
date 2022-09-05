package io.github.acedroidx.danmaku.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel

object SettingsCompose {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsView(vm: SettingsViewModel = hiltViewModel()) {
        val cookie by vm.biliCookie.observeAsState()
        Column() {
            Text(text = "Cookie设置")
            cookie?.let { fromVM -> TextField(value = fromVM, onValueChange = { vm.saveBiliCookie(it)}) }
//            Button(onClick = { cookie?.let { vm.saveBiliCookie(it) } }) {
//                Text(text = "保存")
//            }
        }
    }
}
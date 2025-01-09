package io.github.acedroidx.danmaku.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.model.DanmakuMode
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.ui.theme.AppTheme

object EditDanmakuProfile {
    @Composable
    fun Profile(
        profile: DanmakuConfig,
        emoticonVM: EmoticonViewModel = hiltViewModel(),
        onChange: ((DanmakuConfig) -> Unit)
    ) {
        AppTheme {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(label = { Text(text = "房间号") },
                        value = profile.roomid.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { it1 -> profile.copy(roomid = it1) }
                                ?.let { it2 -> onChange(it2) }
                        })
                }
                OutlinedTextField(label = { Text(text = "弹幕内容") },
                    value = profile.msg,
                    onValueChange = { onChange(profile.copy(msg = it)) })
                if (profile.msgMode == DanmakuMode.EMOTION) {
                    EmotionSelector.LaunchButton(emoticonVM, profile, onChange)
                }
                OutlinedTextField(label = { Text(text = "发送间隔") },
                    value = profile.interval.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { it1 -> profile.copy(interval = it1) }
                            ?.let { it2 -> onChange(it2) }
                    })
                MsgModeComposable(profile, onChange)
                ShootModeComposable(profile, onChange)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MsgModeComposable(profile: DanmakuConfig, onChange: ((DanmakuConfig) -> Unit)) {
        var expanded by remember { mutableStateOf(false) }
        val msgModes = DanmakuMode.entries
        // We want to react on tap/press on TextField to show menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = profile.msgMode.desc,
                onValueChange = {},
                label = { Text("弹幕模式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                // colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                msgModes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.desc) },
                        onClick = {
                            onChange(profile.copy(msgMode = selectionOption))
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShootModeComposable(profile: DanmakuConfig, onChange: ((DanmakuConfig) -> Unit)) {
        var expanded by remember { mutableStateOf(false) }
        val shootModes = DanmakuShootMode.entries
        // We want to react on tap/press on TextField to show menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = profile.shootMode.desc,
                onValueChange = {},
                label = { Text("发送模式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                // colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                shootModes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.desc) },
                        onClick = {
                            onChange(profile.copy(shootMode = selectionOption))
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

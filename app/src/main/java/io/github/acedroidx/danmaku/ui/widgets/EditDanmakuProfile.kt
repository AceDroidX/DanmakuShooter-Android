package io.github.acedroidx.danmaku.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.ui.theme.AppTheme

object EditDanmakuProfile {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Profile(profile: DanmakuConfig, onChange: ((DanmakuConfig) -> Unit)) {
        var expanded by remember { mutableStateOf(false) }
        AppTheme {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        label = { Text(text = "房间号") },
                        value = profile.roomid.toString(),
                        onValueChange = { onChange(profile.copy(roomid = it.toInt())) }
                    )
                }
                OutlinedTextField(
                    label = { Text(text = "弹幕内容") },
                    value = profile.msg,
                    onValueChange = { onChange(profile.copy(msg = it)) })
                OutlinedTextField(
                    label = { Text(text = "发送间隔") },
                    value = profile.interval.toString(),
                    onValueChange = { onChange(profile.copy(interval = it.toInt())) })
                val shootModes = DanmakuShootMode.values()
                // We want to react on tap/press on TextField to show menu
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
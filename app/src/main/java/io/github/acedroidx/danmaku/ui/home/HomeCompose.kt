package io.github.acedroidx.danmaku.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import io.github.acedroidx.danmaku.MainViewModel
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.ui.theme.AppTheme
import io.github.acedroidx.danmaku.ui.widgets.EditDanmakuProfile
import kotlinx.coroutines.launch

object HomeCompose {
    @Composable
    fun MyComposable(mainVM: MainViewModel, homeVM: HomeViewModel = hiltViewModel()) {
        val text by mainVM.text.observeAsState()
        val logText by mainVM.logText.observeAsState()
        val isForeground by mainVM.isForeground.observeAsState()
        val isRunning by mainVM.isRunning.observeAsState()
        val profile by homeVM.danmakuConfig.observeAsState()
        homeVM.getMainProfile()
        Column {
            text?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            profile?.let {
                EditDanmakuProfile.Profile(it) { p ->
                    homeVM.danmakuConfig.value = p
                    mainVM.viewModelScope.launch {
                        homeVM.saveDanmakuConfig(p)
                        mainVM.updateDanmakuData(p)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("启动后台服务", color = MaterialTheme.colorScheme.onBackground)
                isForeground?.let {
                    Switch(
                        checked = it,
                        onCheckedChange = { mainVM.isForeground.value = it })
                }
                Text("发送弹幕", color = MaterialTheme.colorScheme.onBackground)
                isRunning?.let {
                    Switch(
                        checked = it,
                        onCheckedChange = {
                            mainVM.viewModelScope.launch {
                                homeVM.danmakuConfig.value?.let { data ->
                                    mainVM.updateDanmakuData(data)
                                }
                                mainVM.isRunning.value = it
                            }
                        })
                }
            }
            Row {
                Button(onClick = { mainVM.clearLog() }) {
                    Text("清除日志")
                }
                Button(onClick = { homeVM.isAddProfile.value = true }) {
                    Text("添加配置")
                }
            }
            Text("输出日志", color = MaterialTheme.colorScheme.onBackground)
            logText?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        val openDialog = homeVM.isAddProfile.observeAsState()
        if (openDialog.value == true) {
            homeVM.danmakuConfig.value?.let { MyAlertDialog(it, homeVM) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyAlertDialog(profile: DanmakuConfig, homeVM: HomeViewModel) {
        AppTheme {
            var name by remember { mutableStateOf("主页弹幕配置") }
            AlertDialog(
                title = {
                    Text(text = "配置名称")
                },
                text = {
                    TextField(value = name, onValueChange = { name = it })
                },
                confirmButton = {
                    Button(
                        onClick = {
                            homeVM.addProfile(name, profile)
                        }) {
                        Text("添加")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            homeVM.isAddProfile.value = false
                        }) {
                        Text("取消")
                    }
                },
                onDismissRequest = {
                    homeVM.isAddProfile.value = false
                }
            )
        }
    }
}
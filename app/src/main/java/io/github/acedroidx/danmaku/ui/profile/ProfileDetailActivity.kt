package io.github.acedroidx.danmaku.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.R
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.ui.theme.AppTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileDetailActivity : ComponentActivity() {
    private val viewModel: ProfileDetailViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.findProfileById(intent.getIntExtra("id", 0))
        setContent {
            val profile by viewModel.profile.observeAsState()
            AppTheme {
                Scaffold(topBar = {
                    SmallTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { closeActivity() }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close_24dp),
                                    contentDescription = "关闭"
                                )
                            }
                        },
                        title = { Text(text = "配置") },
                        actions = {
                            IconButton(onClick = { profile?.let { saveAndClose(it) } }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_24dp),
                                    contentDescription = "保存"
                                )
                            }
                        })
                }) { contentPadding ->
                    Box(modifier = Modifier.padding(contentPadding)) {
                        Column {
                            Text("id:${profile?.id}")
                            profile?.let { Profile(profile = it) }
                        }
                    }
                }
            }
        }
    }

    private fun saveAndClose(profile: DanmakuConfig) {
        lifecycleScope.launch {
            viewModel.saveProfile(profile)
            finish()
        }
    }

    private fun closeActivity() {
        finish()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    val example = DanmakuConfig(
        1,
        "主页配置文件",
        "test",
        DanmakuShootMode.NORMAL,
        8000,
        9920249,
        21452505
    )
    Profile(example)
    Profile(profile = example)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(profile: DanmakuConfig, viewModel: ProfileDetailViewModel = hiltViewModel()) {
    var expanded by remember { mutableStateOf(false) }
    AppTheme {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    label = { Text(text = "房间号") },
                    value = profile.roomid.toString(),
                    onValueChange = {
                        viewModel.profile.value = viewModel.profile.value?.copy(roomid = it.toInt())
                    })
            }
            OutlinedTextField(
                label = { Text(text = "弹幕内容") },
                value = profile.msg,
                onValueChange = {
                    viewModel.profile.value = viewModel.profile.value?.copy(msg = it)
                })
            OutlinedTextField(
                label = { Text(text = "发送间隔") },
                value = profile.interval.toString(),
                onValueChange = {
                    viewModel.profile.value = viewModel.profile.value?.copy(interval = it.toInt())
                })
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
                                viewModel.profile.value =
                                    viewModel.profile.value?.copy(shootMode = selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
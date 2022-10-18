package io.github.acedroidx.danmaku.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import io.github.acedroidx.danmaku.ui.widgets.EditDanmakuProfile
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
                    TopAppBar(
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
                            IconButton(onClick = { profile?.let { deleteAndClose(it) } }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_baseline_delete_24),
                                    contentDescription = "删除"
                                )
                            }
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

    private fun deleteAndClose(profile: DanmakuConfig) {
        lifecycleScope.launch {
            viewModel.delProfile(profile)
            finish()
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
    AppTheme {
        Column {
            OutlinedTextField(
                label = { Text(text = "配置名称") },
                value = profile.name,
                onValueChange = {
                    viewModel.profile.value = viewModel.profile.value?.copy(name = it)
                })
            EditDanmakuProfile.Profile(profile = profile) { viewModel.profile.value = it }
        }
    }
}
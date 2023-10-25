package io.github.acedroidx.danmaku.ui.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewModelScope
import io.github.acedroidx.danmaku.MainViewModel
import io.github.acedroidx.danmaku.R
import kotlinx.coroutines.launch

object LogCompose {
    @Composable
    fun LogView(vm: MainViewModel) {
        val log by vm.serviceRepository.logText.collectAsState()
        Column() {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.serviceRepository.clearLogText() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = "清除日志"
                    )
                }
            }
            Text(log, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
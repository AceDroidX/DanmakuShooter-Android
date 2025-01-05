package io.github.acedroidx.danmaku.ui.log

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import io.github.acedroidx.danmaku.MainViewModel
import io.github.acedroidx.danmaku.R
import kotlinx.coroutines.launch

object LogCompose {
    @Composable
    fun LogView(vm: MainViewModel) {
        val log by vm.serviceRepository.logText.collectAsState()
        val scrollState = rememberScrollState()
        var isScrolledToBottom by remember { mutableStateOf(true) }

        // Check if the user has scrolled to the bottom
        LaunchedEffect(scrollState.maxValue, scrollState.value) {
            isScrolledToBottom = scrollState.value == scrollState.maxValue
        }

        // Scroll to the bottom only if the user was already at the bottom when a new log entry arrives
        LaunchedEffect(log) {
            if (isScrolledToBottom) {
                scrollState.scrollTo(scrollState.maxValue)
            }
        }

        Column() {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.serviceRepository.clearLogText() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = "清除日志"
                    )
                }
            }
            Box(
                modifier = Modifier
                    .height(600.dp)
                    .verticalScroll(scrollState)

            ) {
                Text(log, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
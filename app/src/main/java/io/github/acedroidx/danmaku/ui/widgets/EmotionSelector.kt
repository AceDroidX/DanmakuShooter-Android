package io.github.acedroidx.danmaku.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.model.Emoticon
import io.github.acedroidx.danmaku.model.EmoticonGetStatus
import io.github.acedroidx.danmaku.model.EmoticonGroup

object EmotionSelector {
    @Composable
    private fun EmoticonPicker(
        profile: DanmakuConfig,
        emoticonGroups: List<EmoticonGroup>,
        onChange: ((DanmakuConfig) -> Unit)
    ) {
        var selectedEmoticonGroup by remember { mutableStateOf<EmoticonGroup?>(emoticonGroups.first()) }

        @Composable
        fun EmoticonGroupButton(emoticonGroup: EmoticonGroup) {
            Button(onClick = {
                selectedEmoticonGroup = emoticonGroup
            })
            {
                EmoticonImage(emoticonGroup.current_cover)
                val text = if (emoticonGroup.name != null) {
                    emoticonGroup.name!!
                } else {
                    emoticonGroup.pkg_name
                }
                Text(text = text)
            }
        }

        @Composable
        fun EmoticonGroupButtons(emoticonGroups: List<EmoticonGroup>) {
            LazyRow(modifier = Modifier.padding(bottom = 16.dp)) {
                items(emoticonGroups) {
                    EmoticonGroupButton(it)
                }
            }
        }

        Column {
            EmoticonGroupButtons(emoticonGroups)
            selectedEmoticonGroup?.let {
                EmoticonSelector(it, profile, onChange)
            }
        }
    }

    @Composable
    private fun EmoticonImage(url: String) {
        AsyncImage(
            model = url.replace("http://", "https://"),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun EmoticonSelector(
        emoticonGroup: EmoticonGroup,
        profile: DanmakuConfig,
        onChange: (DanmakuConfig) -> Unit
    ) {
        Box(
            modifier = Modifier
                .height(300.dp)
        ) {
            FlowRow {
                emoticonGroup.emoticons
                    .filterNot { it.perm == 0 }
                    .forEach {
                        EmoticonButton(profile, it, onChange)
                    }
            }
        }
    }

    @Composable
    private fun EmoticonButton(
        profile: DanmakuConfig,
        emoticon: Emoticon,
        onChange: (DanmakuConfig) -> Unit,
    ) {
        Button(onClick = {
            onChange(
                profile.copy(
                    msg = addEmoticonUniqueToMsg(profile, emoticon)
                )
            )
        }) {
            EmoticonImage(emoticon.url)
            Text(text = emoticon.emoji)
        }
    }

    private fun addEmoticonUniqueToMsg(
        profile: DanmakuConfig,
        emoticon: Emoticon
    ) = if (profile.msg.isEmpty()) {
        emoticon.emoticon_unique
    } else {
        profile.msg + "\n${emoticon.emoticon_unique}"
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LaunchButton(
        emoticonVM: EmoticonViewModel,
        profile: DanmakuConfig,
        onChange: ((DanmakuConfig) -> Unit)
    ) {
        val emoticonGroups by emoticonVM.emoticonRepository.emoticonGroups.collectAsState()
        val status by emoticonVM.emoticonRepository.status.collectAsState()
        val message by emoticonVM.emoticonRepository.message.collectAsState()
        var showBottomSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )
        Button(onClick = {
            emoticonVM.getEmoticonGroups(profile.roomid)
            showBottomSheet = true
        }) {
            Text(text = "表情")
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    emoticonVM.emoticonRepository.setStatus(EmoticonGetStatus.None)
                },
                sheetState = sheetState
            ) {
                val textModifier = Modifier.padding(bottom = 30.dp)
                when (status) {
                    EmoticonGetStatus.GetSuccess -> {
                        emoticonGroups?.let { EmoticonPicker(profile, it, onChange) }
                    }

                    EmoticonGetStatus.GetFailed -> {
                        Text(modifier = textModifier, text = "获取表情失败,$message")
                    }

                    EmoticonGetStatus.Loading -> {
                        Text(modifier = textModifier, text = "获取表情中...")
                    }

                    else -> {
                        Text(modifier = textModifier, text = "未知错误")
                    }
                }
            }
        }
    }
}
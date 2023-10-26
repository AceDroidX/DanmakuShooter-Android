package io.github.acedroidx.danmaku.ui.profile

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import io.github.acedroidx.danmaku.MainViewModel
import io.github.acedroidx.danmaku.R
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import kotlinx.coroutines.launch

object ProfilesCompose {
    @Composable
    fun MyComposable(mainVM: MainViewModel, profilesVM: ProfilesViewModel = hiltViewModel()) {
        val context = LocalContext.current
        val profiles by profilesVM.profiles.observeAsState()
        val isRunning by mainVM.serviceRepository.isRunning.collectAsState()
//            Text("Hello Compose!", color = MaterialTheme.colorScheme.onBackground)
        Column() {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("启动")
                Switch(checked = isRunning, onCheckedChange = {
                    mainVM.viewModelScope.launch {
                        profiles?.find { it.id == profilesVM.choseProfileId.value }?.let { found ->
                            mainVM.startDanmakuService(context, it, found)
                        }
                    }
                })
            }
            profiles?.let { ProfileList(mainVM, it) }
        }
    }

    @Composable
    fun ProfileList(
        mainVM: MainViewModel,
        profiles: List<DanmakuConfig>,
        profilesVM: ProfilesViewModel = hiltViewModel()
    ) {
        val choseProfileId = profilesVM.choseProfileId.observeAsState()
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(profiles) { profile ->
                Profile(mainVM, profile, choseProfileId.value == profile.id)
            }
        }
    }

    @Composable
    fun Profile(
        mainVM: MainViewModel,
        profile: DanmakuConfig,
        isSelected: Boolean = false,
        profilesVM: ProfilesViewModel = hiltViewModel(),
    ) {
        Card(colors = if (isSelected) CardDefaults.cardColors() else CardDefaults.elevatedCardColors(),
            elevation = if (isSelected) CardDefaults.cardElevation() else CardDefaults.elevatedCardElevation(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    profilesVM.onClickCard(profile)
                }) {
            ProfileRaw(profile)
        }
    }

    @Composable
    fun ProfileRaw(profile: DanmakuConfig, profilesVM: ProfilesViewModel = hiltViewModel()) {
        val context = LocalContext.current
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(profile.name, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    context.startActivity(
                        Intent(
                            context, ProfileDetailActivity::class.java
                        ).putExtra("id", profile.id)
                    )
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pencil_24dp),
                        contentDescription = "编辑"
                    )
                }
                IconButton(
                    onClick = { profilesVM.delProfile(profile) },
                    enabled = profile.id != 1,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = "删除"
                    )
                }
            }
            Row(Modifier.height(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ID:" + profile.id.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(0.1f))
                Text(
                    profile.roomid.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(0.1f))
                Text(
                    profile.shootMode.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(0.1f))
                Text(
                    profile.interval.toString() + " ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
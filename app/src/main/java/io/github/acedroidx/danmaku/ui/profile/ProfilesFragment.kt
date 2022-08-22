package io.github.acedroidx.danmaku.ui.profile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.DanmakuService
import io.github.acedroidx.danmaku.R
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.ui.theme.AppTheme

@AndroidEntryPoint
class ProfilesFragment : Fragment() {
    private var mService: DanmakuService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("ProfilesFragment", "onServiceConnected")
            val binder = service as DanmakuService.LocalBinder
            mService = binder.getService()

            mService!!.isRunning.observe(viewLifecycleOwner) {
                Log.d("ProfilesFragment", "isRunning: $it")
                if (profilesViewModel.isRunning.value != it) {
                    profilesViewModel.isRunning.value = it
                }
                switchBtn?.isChecked = it
            }
            profilesViewModel.isRunning.observe(viewLifecycleOwner) {
                Log.d("ProfilesFragment", "profilesViewModel.isRunning.observe:$it")
                if (mService!!.isRunning.value != it) {
                    mService!!.danmakuData.value = profilesViewModel.danmakuData.value
                    mService!!.isRunning.value = it
                    mService!!.isForeground.value = it
                    if (it) DanmakuService.startDanmakuService(context!!)
                }
                switchBtn?.isChecked = it
            }
            profilesViewModel.isRunning.value = mService!!.isRunning.value
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("ProfilesFragment", "onServiceDisconnected")
        }
    }

    private val profilesViewModel: ProfilesViewModel by viewModels()

    private var switchBtn: SwitchMaterial? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("ProfilesFragment", "onCreateView")
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.profiles_action_menu, menu)
                // https://stackoverflow.com/questions/22918812/add-switch-widget-to-actionbar-and-respond-to-change-event
                switchBtn =
                    menu.findItem(R.id.app_bar_switch).actionView?.findViewById(R.id.action_switch)
                switchBtn!!.setOnCheckedChangeListener { button, value ->
                    profilesViewModel.isRunning.value = value
                }
                Log.d("ProfilesFragment", "switchBtn")
                mService?.let { profilesViewModel.isRunning.value = it.isRunning.value }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                Log.w("ProfilesFragment", menuItem.itemId.toString())
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        Intent(context, DanmakuService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MyComposable()
            }
        }
    }

    @Preview(name = "Light Mode")
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark Mode"
    )
    @Composable
    fun PreviewCompose() {
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
    }

    @Composable
    fun MyComposable(viewModel: ProfilesViewModel = hiltViewModel()) {
        val profiles = viewModel.profiles.observeAsState()
        AppTheme {
            Column {
                Text("Hello Compose!", color = MaterialTheme.colorScheme.onBackground)
                profiles.value?.let { ProfileList(it) }
            }
        }
    }

    @Composable
    fun ProfileList(profiles: List<DanmakuConfig>, viewModel: ProfilesViewModel = hiltViewModel()) {
        val choseProfileId = viewModel.choseProfileId.observeAsState()
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(profiles) { profile ->
                Profile(profile, choseProfileId.value == profile.id)
            }
        }
    }

    @Composable
    fun Profile(
        profile: DanmakuConfig,
        isSelected: Boolean = false,
        viewModel: ProfilesViewModel = hiltViewModel()
    ) {
        AppTheme {
            if (isSelected) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onClickCard(profile) }) {
                    ProfileRaw(profile)
                }
            } else {
                ElevatedCard(
                    Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onClickCard(profile) }) {
                    ProfileRaw(profile)
                }
            }
        }
    }

    @Composable
    fun ProfileRaw(profile: DanmakuConfig, viewModel: ProfilesViewModel = hiltViewModel()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(profile.name, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.weight(1f))
                Text(
                    "ID:" + profile.id.toString(),
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = { viewModel.delProfile(profile) },
                    enabled = profile.id != 1
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = "删除"
                    )
                }
            }
            Row(Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    profile.roomid.toString(),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unbindService(connection)
        mService = null
        switchBtn = null
    }
}
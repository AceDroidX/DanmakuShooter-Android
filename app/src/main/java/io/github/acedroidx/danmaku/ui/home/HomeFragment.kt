package io.github.acedroidx.danmaku.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.DanmakuService
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.databinding.FragmentHomeBinding
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.ui.theme.AppTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var mService: DanmakuService
    private var mBound: Boolean = false
    private val homeViewModel: HomeViewModel by viewModels()

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.d("HomeFragment", "onServiceConnected")
            val binder = service as DanmakuService.LocalBinder
            mService = binder.getService()
            mBound = true

            mService.isRunning.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "isRunning: $it")
                if (homeViewModel.isRunning.value != it) {
                    homeViewModel.isRunning.value = it
                }
            }
            mService.isForeground.observe(viewLifecycleOwner) {
                if (homeViewModel.isForeground.value != it) {
                    homeViewModel.isForeground.value = it
                }
            }
            mService.logText.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "mService.logText.observe")
                if (homeViewModel.logText.value != it) {
                    homeViewModel.logText.value = it
                }
            }
            homeViewModel.isRunning.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "homeViewModel.isRunning.observe:$it")
                viewLifecycleOwner.lifecycleScope.launch {
                    homeViewModel.updateDanmakuConfig()
                    mService.danmakuData.value = homeViewModel.serviceDanmakuData.value
                    if (mService.isRunning.value != it) {
                        mService.isRunning.value = it
                    }
                }
            }
            homeViewModel.isForeground.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "isForeground:$it")
                if (mService.isForeground.value != it) {
                    mService.isForeground.value = it
                    if (it) DanmakuService.startDanmakuService(context!!)
                }
            }
            homeViewModel.logText.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "homeViewModel.logText.observe")
                if (mService.logText.value != it) {
                    mService.logText.value = it
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("HomeFragment", "onServiceDisconnected")
            mBound = false
        }
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HomeFragment", "onCreateView")
        Intent(context, DanmakuService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.viewModel = homeViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.composeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // In Compose world
                MyComposable()
            }
        }
        // 初始化数据
        homeViewModel.danmakuConfig.observe(viewLifecycleOwner) {
            Log.d("HomeFragment", "danmakuConfig.observe:$it")
            homeViewModel.loadDanmakuConfig()
            homeViewModel.danmakuConfig.removeObservers(viewLifecycleOwner)
            // 添加空的监听器以保证数据自动更新
            homeViewModel.danmakuConfig.observe(viewLifecycleOwner) {}
            binding.editTextRoomid.setOnFocusChangeListener { view, b -> if (!b) homeViewModel.onFocusChange() }
            binding.editTextDanmaku.setOnFocusChangeListener { view, b -> if (!b) homeViewModel.onFocusChange() }
            binding.editTextInterval.setOnFocusChangeListener { view, b -> if (!b) homeViewModel.onFocusChange() }
            homeViewModel.danmakuMultiMode.observe(viewLifecycleOwner) { homeViewModel.onFocusChange() }
        }
        return root
    }

    @Composable
    fun MyComposable(viewModel: HomeViewModel = hiltViewModel()) {
        val openDialog = viewModel.isAddProfile.observeAsState()
        if (openDialog.value == true) {
            MyAlertDialog()
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
        MyAlertDialog()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyAlertDialog(viewModel: HomeViewModel = hiltViewModel()) {
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
                            viewModel.addProfile(name)
                        }) {
                        Text("添加")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            viewModel.isAddProfile.value = false
                        }) {
                        Text("取消")
                    }
                },
                onDismissRequest = {
                    viewModel.isAddProfile.value = false
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "onDestroyView")
        _binding = null
        activity?.unbindService(connection)
        mBound = false
    }
}
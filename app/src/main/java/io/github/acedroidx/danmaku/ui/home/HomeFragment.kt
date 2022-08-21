package io.github.acedroidx.danmaku.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.DanmakuService
import io.github.acedroidx.danmaku.databinding.FragmentHomeBinding
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
        homeViewModel.danmakuConfig.observe(viewLifecycleOwner) {}
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "onDestroyView")
        _binding = null
        activity?.unbindService(connection)
        mBound = false
    }
}
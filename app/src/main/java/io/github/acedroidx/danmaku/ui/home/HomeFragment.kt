package io.github.acedroidx.danmaku.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.DanmakuService
import io.github.acedroidx.danmaku.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var mService: DanmakuService
    private var mBound: Boolean = false
    private val homeViewModel: HomeViewModel by viewModels()

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as DanmakuService.LocalBinder
            mService = binder.getService()
            mBound = true

            mService.logText.observe(viewLifecycleOwner) {
                homeViewModel.logText.value = it
            }
            homeViewModel.serviceDanmakuConfig.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "homeViewModel.serviceDanmakuConfig.observe:$it")
                mService.danmakuConfig.value = it
            }
            homeViewModel.isRunning.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "homeViewModel.isRunning.observe:$it")
                mService.isRunning.value = it
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startDanmakuService()
        Intent(context, DanmakuService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDetach() {
        super.onDetach()
        activity?.unbindService(connection)
        mBound = false
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.viewModel = homeViewModel
        binding.lifecycleOwner = this

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun startDanmakuService() {
        Intent(context, DanmakuService::class.java).also { intent ->
            context?.startService(intent)
        }
    }
}
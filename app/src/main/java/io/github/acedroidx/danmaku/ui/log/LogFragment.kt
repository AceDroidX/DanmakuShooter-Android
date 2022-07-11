package io.github.acedroidx.danmaku.ui.log

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.DanmakuService
import io.github.acedroidx.danmaku.R
import io.github.acedroidx.danmaku.databinding.FragmentLogBinding

@AndroidEntryPoint
class LogFragment : Fragment() {
    private lateinit var mService: DanmakuService
    private var mBound: Boolean = false
    private val logViewModel: LogViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("LogFragment", "onServiceConnected")
            val binder = service as DanmakuService.LocalBinder
            mService = binder.getService()
            mBound = true

            mService.logText.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "mService.logText.observe")
                if (logViewModel.text.value != it) {
                    logViewModel.text.value = it
                }
            }
            logViewModel.text.observe(viewLifecycleOwner) {
                Log.d("HomeFragment", "homeViewModel.logText.observe")
                if (mService.logText.value != it) {
                    mService.logText.value = it
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("LogFragment", "onServiceDisconnected")
            mBound = false
        }
    }


    private var _binding: FragmentLogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Intent(context, DanmakuService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.viewModel = logViewModel
        binding.lifecycleOwner = this
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.log_action_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                logViewModel.text.value = ""
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        activity?.unbindService(connection)
        mBound = false
    }
}
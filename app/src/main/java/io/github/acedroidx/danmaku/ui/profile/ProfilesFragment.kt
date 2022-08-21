package io.github.acedroidx.danmaku.ui.profile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.switchmaterial.SwitchMaterial
import io.github.acedroidx.danmaku.DanmakuService
import io.github.acedroidx.danmaku.R
import io.github.acedroidx.danmaku.databinding.FragmentProfilesBinding


class ProfilesFragment : Fragment() {
    private var mService: DanmakuService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("ProfilesFragment", "onServiceConnected")
            val binder = service as DanmakuService.LocalBinder
            mService = binder.getService()

            mService!!.isRunning.observe(viewLifecycleOwner) {
                Log.d("ProfilesFragment", "isRunning: $it")
                if (viewModel.isRunning.value != it) {
                    viewModel.isRunning.value = it
                }
                switchBtn?.isChecked = it
            }
            viewModel.isRunning.observe(viewLifecycleOwner) {
                Log.d("ProfilesFragment", "viewModel.isRunning.observe:$it")
                if (mService!!.isRunning.value != it) {
                    mService!!.isRunning.value = it
                    mService!!.isForeground.value = it
                    if (it) DanmakuService.startDanmakuService(context!!)
                }
                switchBtn?.isChecked = it
            }
            viewModel.isRunning.value = mService!!.isRunning.value
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("ProfilesFragment", "onServiceDisconnected")
        }
    }

    private val viewModel: ProfilesViewModel by viewModels()
    private lateinit var binding: FragmentProfilesBinding

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
                    menu.findItem(R.id.app_bar_switch).actionView.findViewById(R.id.action_switch)
                switchBtn!!.setOnCheckedChangeListener { button, value ->
                    viewModel.isRunning.value = value
                }
                Log.d("ProfilesFragment", "switchBtn")
                mService?.let { viewModel.isRunning.value = it.isRunning.value }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                Log.w("ProfilesFragment", menuItem.itemId.toString())
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        Intent(context, DanmakuService::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        binding = FragmentProfilesBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unbindService(connection)
        mService = null
        switchBtn = null
    }
}
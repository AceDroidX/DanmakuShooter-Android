package io.github.acedroidx.danmaku.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.acedroidx.danmaku.databinding.FragmentSettingsBinding

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel: SettingsViewModel by viewModels()

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.viewModel = settingsViewModel
        binding.lifecycleOwner = this

//        val textView: TextView = binding.textNotifications
//        settingsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        val biliCookieEditText: EditText = binding.editTextBiliCookie
//        settingsViewModel.biliCookie.observe(viewLifecycleOwner) {
//            biliCookieEditText.setText(it)
//        }
        val updateSettingsBinding: View = binding.buttonUpdateSettings
        updateSettingsBinding.setOnClickListener {
            settingsViewModel.saveBiliCookie(
                biliCookieEditText.text.toString()
            )
        }

        settingsViewModel.fetchBiliCookie()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
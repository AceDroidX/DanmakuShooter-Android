package io.github.acedroidx.danmaku.ui.profile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.github.acedroidx.danmaku.R
import io.github.acedroidx.danmaku.databinding.FragmentLogBinding
import io.github.acedroidx.danmaku.databinding.FragmentProfilesBinding

class ProfilesFragment : Fragment() {

    private val viewModel: ProfilesViewModel by viewModels()
    private lateinit var binding: FragmentProfilesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfilesBinding.inflate(inflater, container, false)
//        binding.viewModel =
//        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

}
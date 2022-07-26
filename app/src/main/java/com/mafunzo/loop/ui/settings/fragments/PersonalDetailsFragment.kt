package com.mafunzo.loop.ui.settings.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.mafunzo.loop.databinding.FragmentPersonalDetailsBinding
import com.mafunzo.loop.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as SettingsActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as SettingsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as SettingsActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as SettingsActivity).supportActionBar?.title = "Personal Details"
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as SettingsActivity).finish()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
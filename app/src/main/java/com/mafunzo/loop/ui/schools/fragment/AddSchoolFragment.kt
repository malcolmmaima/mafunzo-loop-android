package com.mafunzo.loop.ui.schools.fragment

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentSchoolsBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.announcements.adapters.AnnouncementAdapter
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.schools.adapter.SchoolAdapter
import com.mafunzo.loop.ui.schools.viewmodel.SchoolsViewModel
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddSchoolFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentSchoolsBinding
    private val schoolsViewModel: SchoolsViewModel by viewModels()
    private lateinit var schoolAdapter: SchoolAdapter
    private val searchResultLimit = 20L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSchoolsBinding.inflate(inflater, container, false)

        binding.swipeContainer.setOnRefreshListener(this)
        binding.swipeContainer.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        binding.swipeContainer.post {
            binding.swipeContainer.isRefreshing = true
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        initializeUI()
        initObservers()
        setupSchoolsAdapter()
    }

    private fun initializeUI() {
        schoolsViewModel.searchSchools(binding.etSearch.text.toString().trim(), searchResultLimit)

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                schoolsViewModel.searchSchools(s.toString(), searchResultLimit)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                schoolsViewModel.foundSchools.observe(viewLifecycleOwner) {
                    binding.swipeContainer.isRefreshing = false
                    if(it.isNotEmpty()) {
                        binding.rvSchools.visible()
                        binding.tvNoSchools.gone()
                        schoolAdapter.saveData(it)
                        binding.rvSchools.scrollToPosition(0)
                    } else {
                        binding.rvSchools.gone()
                        binding.tvNoSchools.visible()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                schoolsViewModel.schoolAdded.observe(viewLifecycleOwner) {
                    if(it) {
                        Toast.makeText(context, "School added", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        binding.root.snackbar("Error adding school")
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                schoolsViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                    binding.swipeContainer.isRefreshing = false
                    if(error != null) {
                        binding.root.snackbar(error)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                schoolsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                    binding.swipeContainer.isRefreshing = isLoading
                }
            }
        }
    }


    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Schools"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    private fun setupSchoolsAdapter() {
        schoolAdapter = SchoolAdapter()
        schoolAdapter.onItemClick { schoolResponse ->
            //material dialog to confirm add school
            MaterialAlertDialogBuilder(binding.root.context)
                .setTitle("Add school")
                .setMessage("Are you sure you want to add ${schoolResponse.schoolName}?")
                .setPositiveButton("Yes") { _, _ ->
                    schoolsViewModel.updateUserDetailsSchools(schoolResponse)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.rvSchools.apply {
            adapter = schoolAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

    }

    override fun onRefresh() {
        schoolsViewModel.searchSchools(binding.etSearch.text.toString().trim(), searchResultLimit)
    }
}
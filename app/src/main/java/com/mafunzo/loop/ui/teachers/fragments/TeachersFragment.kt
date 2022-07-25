package com.mafunzo.loop.ui.teachers.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentTeachersBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.teachers.adapter.TeacherAdapter
import com.mafunzo.loop.ui.teachers.viewmodel.TeachersViewModel
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeachersFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: FragmentTeachersBinding
    private val teachersViewModel: TeachersViewModel by viewModels()
    private lateinit var teacherAdapter: TeacherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeachersBinding.inflate(inflater, container, false)

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
        fetchTeachers()
        initObservers()
        setupTeachersAdapter()
    }

    private fun setupTeachersAdapter() {
        teacherAdapter = TeacherAdapter()
        teacherAdapter.onItemClick { teacherResponse ->
            findNavController().navigate(R.id.action_teachersFragment_to_viewTeacherFragment, Bundle().apply {
                putParcelable(Constants.TEACHER_STRING_KEY, teacherResponse)
            })
        }

        binding.rvTeachers.apply {
            adapter = teacherAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun initObservers() {
        teachersViewModel.teachers.observe(viewLifecycleOwner) { teachers ->
            binding.swipeContainer.isRefreshing = false
            if (teachers.isNotEmpty()) {
                binding.rvTeachers.visible()
                binding.tvNoTeachers.gone()
                teacherAdapter.saveData(teachers)
                binding.rvTeachers.scrollToPosition(0)
            } else {
                binding.rvTeachers.gone()
                binding.tvNoTeachers.visible()
            }
        }

        teachersViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.swipeContainer.isRefreshing = false
            if(error != null) {
                binding.root.snackbar(error)
            }
        }

        teachersViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if(isLoading) {
                binding.swipeContainer.isRefreshing = true
                binding.rvTeachers.gone()
                binding.tvNoTeachers.gone()
            } else {
                binding.swipeContainer.isRefreshing = false
                binding.rvTeachers.visible()
            }
        }
    }

    private fun fetchTeachers() {
        teachersViewModel.fetchTeachers()
    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Teachers"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onRefresh() {
        fetchTeachers()
    }

}
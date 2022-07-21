package com.mafunzo.loop.ui.announcements.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentAnnouncementsBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.announcements.adapters.AnnouncementAdapter
import com.mafunzo.loop.ui.announcements.viewmodel.AnnouncementsViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnnouncementsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentAnnouncementsBinding
    private val announcementsViewModel: AnnouncementsViewModel by viewModels()
    private lateinit var announcementAdapter: AnnouncementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)

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
        fetchAnnouncements()
        initObservers()
        setupAnnouncementAdapter()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                announcementsViewModel.announcements.observe(viewLifecycleOwner) { announcements ->
                    binding.swipeContainer.isRefreshing = false
                    if(announcements.isNotEmpty()) {
                        binding.rvAnnouncements.visible()
                        binding.tvNoAnnouncements.gone()
                        announcementAdapter.saveData(announcements)
                        binding.rvAnnouncements.scrollToPosition(0)
                    } else {
                        binding.rvAnnouncements.gone()
                        binding.tvNoAnnouncements.visible()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                announcementsViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                    binding.swipeContainer.isRefreshing = false
                    if(error != null) {
                        binding.root.snackbar(error)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                announcementsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                    if(isLoading) {
                        binding.swipeContainer.isRefreshing = true
                        binding.rvAnnouncements.gone()
                        binding.tvNoAnnouncements.gone()
                    } else {
                        binding.swipeContainer.isRefreshing = false
                        binding.rvAnnouncements.visible()
                    }
                }
            }
        }
    }

    private fun fetchAnnouncements() {
        announcementsViewModel.getAnnouncements()
    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Announcements"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    private fun setupAnnouncementAdapter() {
        announcementAdapter = AnnouncementAdapter()
        announcementAdapter.onItemClick { announcementResponse ->
            findNavController().navigate(R.id.action_announcementsFragment_to_viewAnnouncementFragment, Bundle().apply {
                putParcelable(Constants.ANNOUNCEMENT_STRING_KEY, announcementResponse)
            })
        }

        binding.rvAnnouncements.apply {
            adapter = announcementAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

    }

    override fun onRefresh() {
        fetchAnnouncements()
    }
}
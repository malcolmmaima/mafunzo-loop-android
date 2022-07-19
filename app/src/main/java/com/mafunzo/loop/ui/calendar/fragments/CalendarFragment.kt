package com.mafunzo.loop.ui.calendar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentCalendarBinding
import com.mafunzo.loop.ui.calendar.adapters.CalendarEventAdapter
import com.mafunzo.loop.ui.calendar.viewmodel.CalendarViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CalendarFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener  {
    private lateinit var binding: FragmentCalendarBinding
    private val calendarViewModel: CalendarViewModel by viewModels()
    private lateinit var calendarEventAdapter: CalendarEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

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
        initializeCalendar()
        initializeCalendarObservers()
        setupCalendarAdapter()
    }

    private fun initializeCalendarObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                calendarViewModel.calendarEvents.observe(viewLifecycleOwner) { events ->
                    binding.swipeContainer.isRefreshing = false
                    if(events.isNotEmpty()) {
                        binding.rvEvents.visible()
                        binding.tvNoEvents.gone()
                        calendarEventAdapter.saveData(events)
                        binding.rvEvents.scrollToPosition(0)
                    } else {
                        binding.rvEvents.gone()
                        binding.tvNoEvents.visible()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                calendarViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                    binding.swipeContainer.isRefreshing = false
                    if(error != null) {
                        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                calendarViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                    if(isLoading) {
                        binding.swipeContainer.isRefreshing = true
                        binding.rvEvents.gone()
                        binding.tvNoEvents.gone()
                    } else {
                        binding.swipeContainer.isRefreshing = false
                        binding.rvEvents.visible()
                    }
                }
            }
        }
    }

    private fun setupCalendarAdapter() {
        calendarEventAdapter = CalendarEventAdapter()
        calendarEventAdapter.onItemClick { calendarEventResponse ->
            //TODO: navigate to event details
        }

        binding.rvEvents.apply {
            adapter = calendarEventAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun initializeCalendar() {
        calendarViewModel.fetchCalendar()

        binding.calendarView.setOnDateChangeListener {
                _, calYear, calMonth, calDay ->
            val year = calYear.toString()
            val month = (calMonth+1).toString()
            val day = calDay.toString()
            val date = "$year-$month-$day"

            Toast.makeText(context, date, Toast.LENGTH_LONG).show()
        }

    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Calendar"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onRefresh() {
        calendarViewModel.fetchCalendar()
    }
}
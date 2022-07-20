package com.mafunzo.loop.ui.calendar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.calendar.adapters.CalendarEventAdapter
import com.mafunzo.loop.ui.calendar.viewmodel.CalendarViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.convertDateToTimeInMillis
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class CalendarFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener  {
    private lateinit var binding: FragmentCalendarBinding
    private val calendarViewModel: CalendarViewModel by viewModels()
    private lateinit var calendarEventAdapter: CalendarEventAdapter
    private var selectedDate: Long = 0L

    private var c = Calendar.getInstance()
    private var year = c.get(Calendar.YEAR).toString()
    private var month = (c.get(Calendar.MONTH) + 1).toString()
    private var day = c.get(Calendar.DAY_OF_MONTH).toString()

    private var calendarIsVisible = true

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
            findNavController().navigate(R.id.action_calendarFragment_to_viewEventFragment, Bundle().apply {
                putParcelable(Constants.EVENT_STRING_KEY, calendarEventResponse)
            })
        }

        binding.rvEvents.apply {
            adapter = calendarEventAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun initializeCalendar() {
        //initialize calendar for initial fetch
        var date = "$year-$month-$day"

        selectedDate = date.convertDateToTimeInMillis()

        calendarViewModel.fetchCalendar(selectedDate) //fetch today's events

        binding.calendarView.setOnDateChangeListener {
                _, calYear, calMonth, calDay ->
             year = calYear.toString()
             month = (calMonth+1).toString()
             day = calDay.toString()
             date = "$year-$month-$day"

            //convert selected date to milliseconds
            selectedDate = date.convertDateToTimeInMillis()

            calendarViewModel.fetchCalendar(selectedDate)
        }

        binding.tvViewAll.setOnClickListener {

            year = c.get(Calendar.YEAR).toString()
            month = (c.get(Calendar.MONTH) + 1).toString()
            day = c.get(Calendar.DAY_OF_MONTH).toString()
            var today = "$year-$month-$day"

            if(binding.calendarView.isVisible){
                binding.calendarView.gone()
                calendarIsVisible = false
                binding.tvViewAll.text = getString(R.string.show_calendar)
                calendarViewModel.fetchAllUpcoming(today.convertDateToTimeInMillis())
            } else {
                binding.calendarView.visible()
                calendarIsVisible = true
                binding.tvViewAll.text = getString(R.string.view_all_upcoming)
                calendarViewModel.fetchCalendar(selectedDate)
            }
        }
    }

    private fun refreshEvents() {
        if(binding.calendarView.isVisible){
            calendarViewModel.fetchCalendar(selectedDate)
        } else {
            year = c.get(Calendar.YEAR).toString()
            month = (c.get(Calendar.MONTH) + 1).toString()
            day = c.get(Calendar.DAY_OF_MONTH).toString()
            var today = "$year-$month-$day"
            calendarViewModel.fetchAllUpcoming(today.convertDateToTimeInMillis())
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
        refreshEvents()
    }

    override fun onResume() {
        super.onResume()

        if(calendarIsVisible) {
            binding.calendarView.visible()
        } else {
            binding.calendarView.gone()
            binding.tvViewAll.text = getString(R.string.show_calendar)
        }
        refreshEvents()
    }
}
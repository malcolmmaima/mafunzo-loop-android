package com.mafunzo.loop.ui.calendar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView.OnDateChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mafunzo.loop.databinding.FragmentCalendarBinding
import com.mafunzo.loop.ui.calendar.viewmodel.CalendarViewModel
import com.mafunzo.loop.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CalendarFragment : Fragment() {
    private lateinit var binding: FragmentCalendarBinding
    private val calendarViewModel: CalendarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        initializeCalendar()
    }

    private fun initializeCalendar() {

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
}
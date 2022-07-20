package com.mafunzo.loop.ui.calendar.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.CalendarEventResponse
import com.mafunzo.loop.databinding.FragmentViewEventBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.formatMonthDay
import com.mafunzo.loop.utils.formatStartEndTime
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewEventFragment : Fragment() {

    private lateinit var binding: FragmentViewEventBinding
    private val calendarEvent: CalendarEventResponse? by lazy {
        arguments?.getParcelable(Constants.EVENT_STRING_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(calendarEvent == null) {
            Snackbar.make(binding.root, getString(R.string.no_event_found), Snackbar.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpView()
    }

    private fun setUpView() {
        binding.cardEventTitle.text = calendarEvent?.title
        binding.eventTimeTV.text = "${calendarEvent?.let { formatMonthDay(it.start) }} from ${calendarEvent?.let { event ->
            formatStartEndTime(
                event.start,
                event.end
            )
        }}"
        binding.eventBodyTV.text = calendarEvent?.description

    }

    private fun setUpToolbar() {
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Event"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }
}
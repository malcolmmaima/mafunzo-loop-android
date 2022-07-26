package com.mafunzo.loop.ui.timetable.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentTimeTableBinding
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.timetable.adapter.SubjectAdapter
import com.mafunzo.loop.ui.timetable.viewmodel.TimeTableViewModel
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TimeTableFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: FragmentTimeTableBinding
    private val timeTableViewModel: TimeTableViewModel by viewModels()
    private var selectedGrade = "grade_1" //default
    private var selectedDay = 0
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeTableBinding.inflate(inflater, container, false)

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
        initListeners()
        initObservers()
        setupSubjectssAdapter()
    }

    private fun setupSubjectssAdapter() {
        subjectAdapter = SubjectAdapter()
        subjectAdapter.onItemClick {
            // do something
        }

        binding.rvTimeTable.apply {
            adapter = subjectAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    private fun initObservers() {
        timeTableViewModel.subjects.observe(viewLifecycleOwner) { teachers ->
            binding.swipeContainer.isRefreshing = false
            if (teachers.isNotEmpty()) {
                binding.rvTimeTable.visible()
                binding.tvNoSubjects.gone()
                subjectAdapter.saveData(teachers)
                binding.rvTimeTable.scrollToPosition(0)
            } else {
                binding.rvTimeTable.gone()
                binding.tvNoSubjects.visible()
            }
        }

        timeTableViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.swipeContainer.isRefreshing = false
            if(error != null) {
                binding.root.snackbar(error)
            }
        }

        timeTableViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if(isLoading) {
                binding.swipeContainer.isRefreshing = true
                binding.rvTimeTable.gone()
                binding.tvNoSubjects.gone()
            } else {
                binding.swipeContainer.isRefreshing = false
                binding.rvTimeTable.visible()
            }
        }
    }


    private fun initListeners() {
        timeTableViewModel.getTimeTable(selectedGrade, selectedDay) //initialize

        binding.mondayButton.setOnClickListener {
            selectedDay = 0
            setButtonBackground(binding.mondayButton)
            timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
        }

        binding.tuesdayButton.setOnClickListener {
            selectedDay = 1
            setButtonBackground(binding.tuesdayButton)
            timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
        }

        binding.wednesdayButton.setOnClickListener {
            selectedDay = 2
            setButtonBackground(binding.wednesdayButton)
            timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
        }

        binding.thursdayButton.setOnClickListener {
            selectedDay = 3
            setButtonBackground(binding.thursdayButton)
            timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
        }

        binding.fridayButton.setOnClickListener {
            selectedDay = 4
            setButtonBackground(binding.fridayButton)
            timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
        }

        binding.saturdayButton.setOnClickListener {
            selectedDay = 5
            setButtonBackground(binding.saturdayButton)
            timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
        }

        val gradesList = arrayListOf<String>()
        for(i in 1..12) {
            gradesList.add("Grade $i")
        }

        binding.selectGradeSpinner.apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, gradesList)
            setSelection(0)
        }

        binding.selectGradeSpinner.onItemSelectedListener = object : OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGrade = gradesList[position].replace(" ", "_")
                timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //do nothing
            }

            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //do nothing
            }
        }
    }

    private fun setButtonBackground(button: AppCompatButton) {
        binding.mondayButton.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.mondayButton.setTextColor(resources.getColor(R.color.black, null))

        binding.tuesdayButton.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.tuesdayButton.setTextColor(resources.getColor(R.color.black, null))

        binding.wednesdayButton.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.wednesdayButton.setTextColor(resources.getColor(R.color.black, null))

        binding.thursdayButton.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.thursdayButton.setTextColor(resources.getColor(R.color.black, null))

        binding.fridayButton.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.fridayButton.setTextColor(resources.getColor(R.color.black, null))

        binding.saturdayButton.setBackgroundColor(resources.getColor(R.color.white, null))
        binding.saturdayButton.setTextColor(resources.getColor(R.color.black, null))

        button.setBackgroundResource(R.drawable.round_shape_active_btn)
        button.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Timetable"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onRefresh() {
        timeTableViewModel.getTimeTable(selectedGrade, selectedDay)
    }

}
package com.mafunzo.loop.ui.teachers.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide.with
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.TeachersResponse
import com.mafunzo.loop.databinding.FragmentViewTeacherBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewTeacherFragment : Fragment() {

    private lateinit var binding: FragmentViewTeacherBinding
    private val teacher: TeachersResponse? by lazy {
        arguments?.getParcelable(Constants.TEACHER_STRING_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(teacher == null) {
            binding.root.snackbar(getString(R.string.no_teacher))
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewTeacherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpView()
    }

    private fun setUpView() {
        binding.cardTeacherTitle.text = "${teacher?.firstName} ${teacher?.lastName}"
        binding.subjectsTV.text = teacher?.subjects?.joinToString(", ") ?: "No subjects"
        binding.gradeTV.text = "Grades: ${teacher?.grades?.joinToString(", ") ?: "No grades"}"
        binding.phoneNumberTV.text = "Phone: ${teacher?.phoneNumber}"
        binding.emailAddressTV.text = "Email: ${teacher?.emailAddress}"
        binding.teacherStatusTV.text = "Status: ${teacher?.status}"

        when(teacher?.status) {
            "ACTIVE" -> {
                binding.teacherStatusTV.setTextColor(resources.getColor(R.color.green, null))
            }
            "INACTIVE" -> {
                binding.teacherStatusTV.setTextColor(resources.getColor(R.color.red, null))
            }
             else -> {
                 binding.teacherStatusTV.setTextColor(resources.getColor(R.color.grey, null))
             }
        }

        teacher?.profilePic.let {
            Log.d("ViewTeacherFragment", "profilePic: $it")
            with(this)
                .load(it).placeholder(R.drawable.ic_teachers)
                .error(android.R.drawable.stat_notify_error)
                .into(binding.profilePicIV)
        }

        binding.phoneNumberTV.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${teacher?.phoneNumber}")
            startActivity(intent)
        }

        binding.emailAddressTV.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:${teacher?.emailAddress}")
            startActivity(intent)
        }
    }

    private fun setUpToolbar() {
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Teacher"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }
}
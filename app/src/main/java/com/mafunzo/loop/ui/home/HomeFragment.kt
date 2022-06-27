package com.mafunzo.loop.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mafunzo.loop.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeHomeWidgets()
    }

    private fun initializeHomeWidgets() {
        binding.cvAnnouncements.setOnClickListener {
//            activity?.supportFragmentManager?.beginTransaction()
//                ?.replace(R.id.fragment_container, AnnouncementsFragment())
//                ?.addToBackStack(null)
//                ?.commit()
            Toast.makeText(context, "Announcements", Toast.LENGTH_SHORT).show()
        }

        binding.cvCalendar.setOnClickListener {
            Toast.makeText(context, "Calendar", Toast.LENGTH_SHORT).show()
        }

        binding.cvRequests.setOnClickListener {
            Toast.makeText(context, "Requests", Toast.LENGTH_SHORT).show()
        }

        binding.cvTeachers.setOnClickListener {
            Toast.makeText(context, "Teachers", Toast.LENGTH_SHORT).show()
        }

        binding.cvSchoolBus.setOnClickListener {
            Toast.makeText(context, "School Bus", Toast.LENGTH_SHORT).show()
        }

        binding.cvContact.setOnClickListener {
            Toast.makeText(context, "Contact", Toast.LENGTH_SHORT).show()
        }
    }
}
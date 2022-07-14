package com.mafunzo.loop.ui.announcements.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.AnnouncementResponse
import com.mafunzo.loop.databinding.FragmentViewAnnouncementBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.formatDateTime
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewAnnouncementFragment : Fragment() {

    private lateinit var binding: FragmentViewAnnouncementBinding
    private val announcement: AnnouncementResponse? by lazy {
        arguments?.getParcelable(Constants.ANNOUNCEMENT_STRING_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(announcement == null) {
            Snackbar.make(binding.root, getString(R.string.no_announcement_found), Snackbar.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAnnouncementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpView()
    }

    private fun setUpView() {
        binding.cardAnnouncementTitle.text = announcement?.announcementTitle
        binding.announcementTimeTV.text = announcement?.announcementTime?.formatDateTime()
        binding.announcementBodyTV.text = announcement?.announcementBody

        if(announcement?.announcementImage?.isEmpty() == false) {
            binding.announcementImage.visible()

            Glide.with(this)
                .load(announcement?.announcementImage)
                .into(binding.announcementImage)
        }
    }

    private fun setUpToolbar() {
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Announcement"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }
}
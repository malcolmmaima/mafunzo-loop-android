package com.mafunzo.loop.ui.announcements.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mafunzo.loop.data.models.responses.AnnouncementResponse
import com.mafunzo.loop.databinding.AnnouncementItemBinding
import com.mafunzo.loop.utils.formatDateTime


class AnnouncementAdapter : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {
    private var viewAnnouncementCallback: ((AnnouncementResponse) -> Unit)? = null


    fun onItemClick(onItemClick: (AnnouncementResponse) -> Unit) {
        this.viewAnnouncementCallback = onItemClick
    }

    class AnnouncementViewHolder(val binding: AnnouncementItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val binding =
            AnnouncementItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementViewHolder(binding)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<AnnouncementResponse>() {
        override fun areItemsTheSame(
            oldItem: AnnouncementResponse,
            newItem: AnnouncementResponse
        ): Boolean {
            return oldItem.announcementTime == newItem.announcementTime
        }

        override fun areContentsTheSame(
            oldItem: AnnouncementResponse,
            newItem: AnnouncementResponse
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun saveData(announcementResponse: List<AnnouncementResponse>) {
        asyncListDiffer.submitList(announcementResponse)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcementData = asyncListDiffer.currentList[position]
        val context = holder.binding.root.context
        holder.binding.apply {

            cardAnnouncementTitle.text = announcementData.announcementTitle
            announcementTimeTV.text = announcementData.announcementTime?.formatDateTime()
            announcementBodyTV.text = announcementData.announcementBody
        }

        holder.binding.announcementCard.setOnClickListener {
            viewAnnouncementCallback?.invoke(announcementData)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}
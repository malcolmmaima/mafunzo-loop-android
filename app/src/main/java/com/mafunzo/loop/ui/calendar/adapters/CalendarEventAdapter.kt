package com.mafunzo.loop.ui.calendar.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mafunzo.loop.data.models.responses.CalendarEventResponse
import com.mafunzo.loop.databinding.CalendarItemBinding
import com.mafunzo.loop.utils.formatStartEndTime

class CalendarEventAdapter : RecyclerView.Adapter<CalendarEventAdapter.CalendarViewHolder>() {
    private var viewEventCallback: ((CalendarEventResponse) -> Unit)? = null

    fun onItemClick(onItemClick: (CalendarEventResponse) -> Unit) {
        this.viewEventCallback = onItemClick
    }

    class CalendarViewHolder(val binding: CalendarItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding =
            CalendarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarViewHolder(binding)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<CalendarEventResponse>() {
        override fun areItemsTheSame(
            oldItem: CalendarEventResponse,
            newItem: CalendarEventResponse
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: CalendarEventResponse,
            newItem: CalendarEventResponse
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun saveData(calendarEventResponse: List<CalendarEventResponse>) {
        asyncListDiffer.submitList(calendarEventResponse)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val calendarData = asyncListDiffer.currentList[position]
        holder.binding.apply {

            cardEventTitle.text = calendarData.title
            eventTimeTV.text = formatStartEndTime(calendarData.start, calendarData.end)
        }

        holder.binding.eventCard.setOnClickListener {
            viewEventCallback?.invoke(calendarData)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}
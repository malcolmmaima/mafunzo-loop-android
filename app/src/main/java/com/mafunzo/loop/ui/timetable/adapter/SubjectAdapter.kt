package com.mafunzo.loop.ui.timetable.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.SubjectRespone
import com.mafunzo.loop.databinding.TimetableItemBinding
import com.mafunzo.loop.utils.formatStartEndTime

class SubjectAdapter : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {
    private var viewSubjectCallback: ((SubjectRespone) -> Unit)? = null


    fun onItemClick(onItemClick: (SubjectRespone) -> Unit) {
        this.viewSubjectCallback = onItemClick
    }

    class SubjectViewHolder(val binding: TimetableItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding =
            TimetableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<SubjectRespone>() {
        override fun areItemsTheSame(
            oldItem: SubjectRespone,
            newItem: SubjectRespone
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SubjectRespone,
            newItem: SubjectRespone
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun saveData(subjectRespone: List<SubjectRespone>) {
        asyncListDiffer.submitList(subjectRespone)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subjectData = asyncListDiffer.currentList[position]
        holder.binding.apply {

            subjectTitleTV.text = subjectData.subjectName
            subjectTimeTV.text = subjectData.startTime?.let { subjectData.endTime?.let { it1 ->
                formatStartEndTime(it,
                    it1
                )
            } }
            subjectTeacherTV.text = subjectData.assignedTeacher

        }

        holder.binding.subjectCard.setOnClickListener {
            viewSubjectCallback?.invoke(subjectData)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}
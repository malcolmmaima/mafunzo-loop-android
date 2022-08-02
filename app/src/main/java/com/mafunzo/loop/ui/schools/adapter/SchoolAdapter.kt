package com.mafunzo.loop.ui.schools.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mafunzo.loop.data.models.responses.SchoolResponse
import com.mafunzo.loop.databinding.SchoolItemBinding


class SchoolAdapter : RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {
    private var viewSchoolCallback: ((SchoolResponse) -> Unit)? = null


    fun onItemClick(onItemClick: (SchoolResponse) -> Unit) {
        this.viewSchoolCallback = onItemClick
    }

    class SchoolViewHolder(val binding: SchoolItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val binding =
            SchoolItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SchoolViewHolder(binding)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<SchoolResponse>() {
        override fun areItemsTheSame(
            oldItem: SchoolResponse,
            newItem: SchoolResponse
        ): Boolean {
            return oldItem.schoolName == newItem.schoolName
        }

        override fun areContentsTheSame(
            oldItem: SchoolResponse,
            newItem: SchoolResponse
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun saveData(schoolResponse: List<SchoolResponse>) {
        asyncListDiffer.submitList(schoolResponse)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val schoolData = asyncListDiffer.currentList[position]
        holder.binding.apply {

            schoolNameTV.text = schoolData.schoolName
            schoolLocation.text = schoolData.schoolLocation
        }

        holder.binding.schoolcard.setOnClickListener {
            viewSchoolCallback?.invoke(schoolData)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}
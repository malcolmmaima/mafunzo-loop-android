package com.mafunzo.loop.ui.teachers.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.with
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.TeachersResponse
import com.mafunzo.loop.databinding.TeacherItemBinding


class TeacherAdapter : RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder>() {
    private var viewTeacherCallback: ((TeachersResponse) -> Unit)? = null


    fun onItemClick(onItemClick: (TeachersResponse) -> Unit) {
        this.viewTeacherCallback = onItemClick
    }

    class TeacherViewHolder(val binding: TeacherItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val binding =
            TeacherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeacherViewHolder(binding)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<TeachersResponse>() {
        override fun areItemsTheSame(
            oldItem: TeachersResponse,
            newItem: TeachersResponse
        ): Boolean {
            return oldItem.firstName == newItem.firstName
        }

        override fun areContentsTheSame(
            oldItem: TeachersResponse,
            newItem: TeachersResponse
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun saveData(teachersResponse: List<TeachersResponse>) {
        asyncListDiffer.submitList(teachersResponse)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        val teacherData = asyncListDiffer.currentList[position]
        holder.binding.apply {

            cardTeacherTitle.text = "${teacherData.firstName} ${teacherData.lastName}"
            subjectsTV.text = teacherData.subjects?.joinToString(", ") ?: "No subjects"
            gradeTV.text = "Grades: ${teacherData.grades?.joinToString(", ") ?: "No grades"}"
            teacherBioTV.text = teacherData.bio ?: "..."

            teacherData.profilePic.let {
                with(root)
                    .load(it).placeholder(R.drawable.ic_teachers)
                    .error(android.R.drawable.stat_notify_error)
                    .into(profilePicIV)

            }
        }

        holder.binding.teacherCard.setOnClickListener {
            viewTeacherCallback?.invoke(teacherData)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}
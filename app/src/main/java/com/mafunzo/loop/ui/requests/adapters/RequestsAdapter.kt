package com.mafunzo.loop.ui.requests.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mafunzo.loop.data.models.responses.UserRequestResponse
import com.mafunzo.loop.databinding.RequestItemBinding
import com.mafunzo.loop.utils.formatDateTime


class RequestsAdapter : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {
    private var viewRequestCallback: ((UserRequestResponse) -> Unit)? = null


    fun onItemClick(onItemClick: (UserRequestResponse) -> Unit) {
        this.viewRequestCallback = onItemClick
    }

    class RequestViewHolder(val binding: RequestItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding =
            RequestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<UserRequestResponse>() {
        override fun areItemsTheSame(
            oldItem: UserRequestResponse,
            newItem: UserRequestResponse
        ): Boolean {
            return oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(
            oldItem: UserRequestResponse,
            newItem: UserRequestResponse
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun saveData(userRequestResponse: List<UserRequestResponse>) {
        asyncListDiffer.submitList(userRequestResponse)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val requestData = asyncListDiffer.currentList[position]
        holder.binding.apply {

            requestSubjectTv.text = requestData.subject
            requestTimeTV.text = requestData.createdAt?.formatDateTime()
        }

        holder.binding.requestCard.setOnClickListener {
            viewRequestCallback?.invoke(requestData)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}
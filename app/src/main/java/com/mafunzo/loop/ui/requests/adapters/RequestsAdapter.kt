package com.mafunzo.loop.ui.requests.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.UserRequestResponse
import com.mafunzo.loop.databinding.RequestItemBinding
import com.mafunzo.loop.di.Constants
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
        val context = holder.binding.root.context
        holder.binding.apply {

            requestSubjectTv.text = requestData.subject
            requestTimeTV.text = requestData.createdAt.formatDateTime()

            //if status is pending color is grey, if status is processing then set color to blue
            //if status is approved the color is green and if status is cancelled then color is red
            when (requestData.status) {
                Constants.REQUEST_STATUS_PENDING -> {
                    statusIV.setBackgroundColor(context.getColor(R.color.grey))
                }
                Constants.REQUEST_STATUS_PROCESSING -> {
                    statusIV.setBackgroundColor(context.getColor(R.color.colorSecondary))
                }
                Constants.REQUEST_STATUS_APPROVED -> {
                    statusIV.setBackgroundColor(context.getColor(R.color.green))
                }
                Constants.REQUEST_STATUS_CANCELLED -> {
                    statusIV.setBackgroundColor(context.getColor(R.color.red))
                }
                Constants.REQUEST_STATUS_REJECTED -> {
                    statusIV.setBackgroundColor(context.getColor(R.color.red))
                }
                else -> {
                    statusIV.setBackgroundColor(context.getColor(R.color.grey))
                }
            }

        }

        holder.binding.requestCard.setOnClickListener {
            viewRequestCallback?.invoke(requestData)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }
}
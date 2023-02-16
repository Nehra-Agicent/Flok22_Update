package com.flok22.android.agicent.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.NotificationItemsBinding
import com.flok22.android.agicent.model.pendingRequest.Data
import com.flok22.android.agicent.utils.aws.S3Utils
import com.flok22.android.agicent.utils.getTiming

class NotificationAdapter(val onItemClick: (pos: Int, userChat: Data, src: String) -> Unit) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val notificationViewHolder = NotificationItemsBinding.inflate(inflater, parent, false)
        return NotificationViewHolder(notificationViewHolder)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val differCallback = object : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.request_by == newItem.request_by
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class NotificationViewHolder(private val binding: NotificationItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: Data) {
            binding.userTextNotification.text =
                itemView.context?.getString(
                    R.string.at_same_place, history.full_name, history.place_name
                )
            binding.timeOfNotification.text = getTiming(history.created_datetime)
            Glide.with(itemView.context)
                .load(S3Utils.generateS3ShareUrl(itemView.context, history.profile_pic))
                .centerCrop().into(binding.userIconNotification)

            binding.userTextNotification.setOnClickListener {
                onItemClick(absoluteAdapterPosition, history, "root")
            }
        }
    }
}
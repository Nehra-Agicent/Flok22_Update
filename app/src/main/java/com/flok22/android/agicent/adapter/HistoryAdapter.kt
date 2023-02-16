package com.flok22.android.agicent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.HistoryItemsBinding
import com.flok22.android.agicent.model.historicalData.Data
import com.flok22.android.agicent.utils.aws.S3Utils
import com.flok22.android.agicent.utils.getTiming

class HistoryAdapter(
    private val historyList: ArrayList<Data>?,
    val context: Context?,
    val onClick: (position: Int, history: Data) -> Unit
) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val historyViewHolder = HistoryItemsBinding.inflate(inflater, parent, false)
        return HistoryViewHolder(historyViewHolder)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        historyList?.get(position)?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int = historyList?.size!!

    inner class HistoryViewHolder(private val binding: HistoryItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: Data) {
            binding.userNameHistory.text =
                context?.getString(R.string.at_same_place, history.full_name, history.place_name)
            Glide.with(binding.userIconHistory.context)
                .load(S3Utils.generateS3ShareUrl(context, history.profile_pic)).centerCrop()
                .into(binding.userIconHistory)
            binding.timeOfNotificationHistory.text = getTiming(history.checked_in_datetime)
            itemView.setOnClickListener {
                onClick(absoluteAdapterPosition, history)
            }
        }
    }
}
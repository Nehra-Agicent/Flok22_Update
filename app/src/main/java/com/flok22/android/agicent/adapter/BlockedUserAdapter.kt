package com.flok22.android.agicent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flok22.android.agicent.databinding.BlockedUserItemBinding
import com.flok22.android.agicent.model.blockedUser.Data
import com.flok22.android.agicent.utils.aws.S3Utils

class BlockedUserAdapter(
    private val context: Context,
    private val blockedUserData: ArrayList<Data>,
    val onItemClick: (pos: Int, userId: Int) -> Unit
) :
    RecyclerView.Adapter<BlockedUserAdapter.BlockedUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val blockedUserViewHolder = BlockedUserItemBinding.inflate(inflater, parent, false)
        return BlockedUserViewHolder(blockedUserViewHolder)
    }

    override fun onBindViewHolder(holder: BlockedUserViewHolder, position: Int) =
        holder.bind(blockedUserData[position])

    override fun getItemCount(): Int {
        return blockedUserData.size
    }

    inner class BlockedUserViewHolder(private val binding: BlockedUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Data) {
            Glide.with(binding.icon.context)
                .load(S3Utils.generateS3ShareUrl(context, data.profile_pic)).centerCrop()
                .into(binding.icon)
            binding.name.text = data.full_name
            binding.unblockButton.setOnClickListener {
                onItemClick(absoluteAdapterPosition, data.user_id)
            }
        }
    }
}
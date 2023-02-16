package com.flok22.android.agicent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flok22.android.agicent.databinding.UserChatsBinding
import com.flok22.android.agicent.model.chat.Data
import com.flok22.android.agicent.utils.aws.S3Utils
import com.flok22.android.agicent.utils.getTiming

class ChatAdapter(
    private val chatList: List<Data>,
    val context: Context?,
    val onItemClick: (pos: Int, userChat: Data, src: String) -> Unit
) :
    RecyclerView.Adapter<ChatAdapter.UserProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val userProfileViewHolder = UserChatsBinding.inflate(inflater, parent, false)
        return UserProfileViewHolder(userProfileViewHolder)
    }

    override fun onBindViewHolder(holder: UserProfileViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class UserProfileViewHolder(private val binding: UserChatsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userChat: Data) {
            binding.userNameChat.text = userChat.user_name
            binding.timeOfMessage.text = getTiming(userChat.created_datetime)
//            binding.messageContent.text = userChat.msg

            if (context != null) {
                Glide.with(binding.userIconChat.context)
                    .load(S3Utils.generateS3ShareUrl(context, userChat.profile_pic)).centerCrop()
                    .into(binding.userIconChat)
            }
//            binding.userNameChat.setOnClickListener {}
            itemView.setOnClickListener {
                onItemClick(absoluteAdapterPosition, userChat, "root")
            }
        }
    }
}
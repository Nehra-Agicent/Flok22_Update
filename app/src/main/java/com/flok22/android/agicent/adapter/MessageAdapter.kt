package com.flok22.android.agicent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flok22.android.agicent.databinding.ItemContainerReceivedMessageBinding
import com.flok22.android.agicent.databinding.ItemContainerSentMessageBinding
import com.flok22.android.agicent.model.allChatMsg.GetChatMessage
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.convertTo12Hours
import com.flok22.android.agicent.utils.getLocalDateTime
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MessageAdapter(
    val context: Context,
    private var messageList: ArrayList<GetChatMessage>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MESSAGE_SENT = 0
        private const val TYPE_MESSAGE_RECEIVED = 1
    }

    inner class SentMessageViewHolder(var binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sentMsg: GetChatMessage) {
            binding.sentMessage.text = sentMsg.message

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val output = SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault())
            val data = sdf.parse(getLocalDateTime(sentMsg.created_datetime))
            val formattedDate = output.format(data)

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatted = current.format(formatter)

            val militaryTime = convertTo12Hours(formattedDate.substring(11, 19))
            if (formatted != sentMsg.created_datetime.substring(0, 10)) {
                binding.timeOfMessage.text =
                    formattedDate.substring(0, 11).replace("-", "/") + militaryTime
            } else {
                binding.timeOfMessage.text = militaryTime
            }
        }

    }

    inner class ReceiveMessageViewHolder(val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(receiveMsg: GetChatMessage) {
            binding.receiveMessage.text = receiveMsg.message

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val output = SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault())
            val data = sdf.parse(getLocalDateTime(receiveMsg.created_datetime))
            val formattedDate = output.format(data)

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatted = current.format(formatter)

            val militaryTime = convertTo12Hours(formattedDate.substring(11, 19))
            if (formatted != receiveMsg.created_datetime.substring(0, 10)) {
                binding.timeOfReceivedMessage.text =
                    formattedDate.substring(0, 11).replace("-", "/") + militaryTime
            } else {
                binding.timeOfReceivedMessage.text = militaryTime
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].sender_id == SharedPreferenceManager(context).userId.toInt()) {
            TYPE_MESSAGE_SENT
        } else {
            TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_MESSAGE_RECEIVED -> {
                val binding =
                    ItemContainerReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                return ReceiveMessageViewHolder(binding)
            }
            else -> {
                val binding =
                    ItemContainerSentMessageBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                return SentMessageViewHolder(binding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TYPE_MESSAGE_SENT -> {
                (holder as SentMessageViewHolder).bind(messageList[position])
            }

            TYPE_MESSAGE_RECEIVED -> {
                (holder as ReceiveMessageViewHolder).bind(messageList[position])
            }
        }
    }

    override fun getItemCount(): Int = messageList.size
}

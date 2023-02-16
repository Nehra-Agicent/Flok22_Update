package com.flok22.android.agicent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flok22.android.agicent.databinding.PlaceCheckedInItemBinding
import com.flok22.android.agicent.model.checkIn.CheckInUser
import com.flok22.android.agicent.utils.aws.S3Utils
import com.flok22.android.agicent.utils.hide
import com.flok22.android.agicent.utils.show
import com.flok22.android.agicent.utils.showToast

class CheckedInUserAdapter(
    private val context: Context,
    private val userData: ArrayList<CheckInUser>,
    val onItemClick: (pos: Int, userData: CheckInUser) -> Unit
) :
    RecyclerView.Adapter<CheckedInUserAdapter.CheckedInUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckedInUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val checkedInUserViewHolder = PlaceCheckedInItemBinding.inflate(inflater, parent, false)
        return CheckedInUserViewHolder(checkedInUserViewHolder)
    }

    override fun onBindViewHolder(holder: CheckedInUserViewHolder, position: Int) =
        userData[position].let { holder.bind(it) }

    override fun getItemCount(): Int {
        return userData.size
    }

    inner class CheckedInUserViewHolder(private val binding: PlaceCheckedInItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userData: CheckInUser) {
            binding.userName.text = userData.full_name
            val imageUrl = S3Utils.generateS3ShareUrl(context, userData.profile_pic)
            Glide.with(binding.profileIcon.context).load(imageUrl).centerCrop()
                .into(binding.profileIcon)

            val isUserBusy: Boolean
            if (userData.is_busy == 1) {
                isUserBusy = true
                binding.blackTransparency.show()
            } else {
                isUserBusy = false
                binding.blackTransparency.hide()
            }
            itemView.setOnClickListener {
                if (isUserBusy && userData.is_busy_with_me != 1) {
                    context.showToast("User engaged with other")
                } else {
                    onItemClick(absoluteAdapterPosition, userData)
                }
            }
        }
    }
}
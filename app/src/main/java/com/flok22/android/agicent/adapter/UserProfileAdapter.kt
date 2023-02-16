package com.flok22.android.agicent.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flok22.android.agicent.databinding.UserProfileListItemsBinding
import com.flok22.android.agicent.model.checkIn.CheckInUser
import com.flok22.android.agicent.model.checkIn.Data
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.aws.S3Utils
import com.flok22.android.agicent.utils.hide
import com.flok22.android.agicent.utils.show
import com.flok22.android.agicent.utils.showToast

class UserProfileAdapter(
    private val userData: Data,
    val context: Context?,
    val onItemClick: (userProfile: CheckInUser) -> Unit
) :
    RecyclerView.Adapter<UserProfileAdapter.UserProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val userProfileViewHolder = UserProfileListItemsBinding.inflate(inflater, parent, false)
        return UserProfileViewHolder(userProfileViewHolder)
    }

    override fun onBindViewHolder(holder: UserProfileViewHolder, position: Int) {
        holder.bind(userData.check_in_users[position])
    }

    override fun getItemCount(): Int {
        return userData.check_in_users.size
    }

    inner class UserProfileViewHolder(private val binding: UserProfileListItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userProfile: CheckInUser) {
            binding.checkedInUser.text = userProfile.full_name
            if (context != null) {
                Glide.with(context)
                    .load(S3Utils.generateS3ShareUrl(context, userProfile.profile_pic)).centerCrop()
                    .into(binding.userIcon)
            }

            val isUserBusy: Boolean
            if (userProfile.is_busy == 1) {
                isUserBusy = true
                binding.blackTransparency.show()
            } else {
                isUserBusy = false
                binding.blackTransparency.hide()
            }

            binding.userIcon.setOnClickListener {
                if (SharedPreferenceManager(context!!).placeId == userData.place_info.place_id) {
                    if (isUserBusy && userProfile.is_busy_with_me != 1) {
                        context.showToast("User engaged with other")
                    } else {
                        onItemClick(userProfile)
                    }
                }
            }
        }
    }
}
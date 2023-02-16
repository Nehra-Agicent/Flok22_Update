package com.flok22.android.agicent.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flok22.android.agicent.databinding.PlaceListItemsBinding
import com.flok22.android.agicent.model.PlaceData
import com.flok22.android.agicent.utils.getDistanceInMetre
import com.flok22.android.agicent.utils.hide
import com.flok22.android.agicent.utils.invisible
import com.flok22.android.agicent.utils.show

class NearbyPlaceAdapter(
    private val placeList: ArrayList<PlaceData>?,
    /*private val latitude: Double,
    private val longitude: Double,*/
    val onItemClick: (placeId: Int) -> Unit
) : RecyclerView.Adapter<NearbyPlaceAdapter.NearByPlaceViewHolder>() {

    inner class NearByPlaceViewHolder(private val binding: PlaceListItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(placeData: PlaceData) {

            //handle visibility
            binding.placeLocked.hide()
            binding.actionCheckIn.show()

            //set views
            binding.countOfPeople.text = placeData.checked_in_count.toString()
            /*val distance = getDistanceMeters(
                latitude, longitude, placeData.place_latitude, placeData.place_longtitude
            )*/
            val distance = getDistanceInMetre(placeData.distance)

            val distanceWithMetre = "$distance m"

            if (distance >= 500) {
                binding.actionCheckIn.invisible()
                binding.textContainer.invisible()

                binding.showPlaceData.show()
                binding.placeLocked.show()

                binding.placeName.text = placeData.place_name
                binding.placeDistance.text = distanceWithMetre
            } else {
                binding.actionCheckIn.show()
                binding.textContainer.show()

                binding.showPlaceData.invisible()
                binding.placeLocked.invisible()

                binding.nameOfPlace.text = placeData.place_name
                binding.distanceOfPlace.text = distanceWithMetre
            }

            //click place name or icon to show place detail

            binding.textContainer.setOnClickListener {
                onItemClick(placeData.place_id)
            }
            binding.actionCheckIn.setOnClickListener {
                onItemClick(placeData.place_id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearByPlaceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val nearByPlaceViewHolder = PlaceListItemsBinding.inflate(inflater, parent, false)
        return NearByPlaceViewHolder(nearByPlaceViewHolder)
    }

    override fun onBindViewHolder(holder: NearByPlaceViewHolder, position: Int) {
        placeList?.get(position)?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return placeList?.size ?: 0
    }
}
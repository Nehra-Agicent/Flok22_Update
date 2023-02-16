package com.flok22.android.agicent.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.flok22.android.agicent.R
import com.flok22.android.agicent.adapter.NearbyPlaceAdapter
import com.flok22.android.agicent.bottomsheet.CheckInUserTopFragment
import com.flok22.android.agicent.databinding.LayoutPlaceListBinding
import com.flok22.android.agicent.model.PlaceData
import com.flok22.android.agicent.utils.MyGestureDetector
import com.flok22.android.agicent.utils.TapListener
import com.flok22.android.agicent.utils.getDistanceInMetre

class NearMeDialog(
    private val _placesList: ArrayList<PlaceData>,
    /*_latitude: Double,
    _longitude: Double,*/
    private val tapListener: TapListener
) : DialogFragment() {

    private lateinit var binding: LayoutPlaceListBinding
    private var placesList: ArrayList<PlaceData>? = null
    /*private val latitude = _latitude
    private val longitude = _longitude*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = context?.let { Dialog(it, R.style.Theme_Dialog) }
        val view = View.inflate(context, R.layout.layout_place_list, null)
        binding = LayoutPlaceListBinding.bind(view)
        dialog!!.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setGravity(Gravity.TOP)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        view.setOnTouchListener(object : MyGestureDetector(requireContext()) {
            override fun onSwipeUp() {
                super.onSwipeUp()
                dismiss()
            }
        })

        placesList = ArrayList()
        var distance: Int?
        for (i in 0 until _placesList.size) {
            /*val listLat = _placesList[i].place_latitude
            val listLang = _placesList[i].place_longtitude
            distance = getDistanceMeters(latitude, longitude, listLat, listLang)*/
            distance = getDistanceInMetre(_placesList[i].distance)
            if (distance <= 1000) {
                placesList?.add(_placesList[i])
            }
        }

        val adapter = NearbyPlaceAdapter(placesList/*, latitude, longitude*/) {
            getPlaceDetail(it)
        }

        binding.placeDetailRv.layoutManager = LinearLayoutManager(requireContext())
        binding.placeDetailRv.adapter = adapter
        return dialog
    }

    private fun getPlaceDetail(placeId: Int) {
        val bundle = Bundle()
        bundle.putInt("placeId", placeId)
        val dialog = CheckInUserTopFragment(requireContext(), tapListener)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "CheckInUserTopFragment")
    }
}
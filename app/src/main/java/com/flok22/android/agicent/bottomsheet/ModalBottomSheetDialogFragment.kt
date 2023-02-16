package com.flok22.android.agicent.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.ModalBottomSheetBinding
import com.flok22.android.agicent.utils.Constants.PLACE_IMAGE
import com.flok22.android.agicent.utils.Constants.TAG
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = ModalBottomSheetDialogFragment()
    }

    private val className = ModalBottomSheetDialogFragment::class.java.simpleName
    private lateinit var binding: ModalBottomSheetBinding
    private var name: String? = null
    private var address: String = ""
    private var distance: Long = 0
    private var distanceInKm: Double = 0.0
    private var peopleCount = 0
    private var photoKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        val bundle = this.arguments
        if (bundle != null) {
            photoKey = bundle.getString("photoKey", "")
            name = bundle.getString("name", "hello")
            address = bundle.getString("address", "hello")
            distance = bundle.getLong("distance")
            distanceInKm = distance * 0.001
            peopleCount = bundle.getInt("peopleCount")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDialog()
        Log.i(TAG, "onViewCreated: $photoKey")
        val placeUrl = PLACE_IMAGE.replace("<place_key>", photoKey)
        Log.d(TAG, "$className onViewCreated: $placeUrl")
        Glide.with(requireContext()).load(placeUrl).centerCrop().into(binding.placeImage)
        binding.placeDetail.text = getString(R.string.place_name, name)
        binding.address.text = address
        binding.distanceFromUser.text =
            getString(R.string.distance_in_between, distanceInKm.toString())
        binding.peopleCount.text = getString(R.string.people_count, peopleCount.toString())
        binding.checkIn.setOnClickListener {
            /*val dialog = CheckInUserTopFragment.newInstance()
            val b = Bundle()
            b.putString("name", name)
            dialog.arguments = b
            dialog.show(childFragmentManager, "Check-in Bottom Sheet")*/
        }
    }

    private fun initDialog() {
        requireDialog().window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        requireDialog().window?.statusBarColor =
            requireContext().getColor(android.R.color.transparent)
    }
}
package com.flok22.android.agicent.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flok22.android.agicent.R
import com.flok22.android.agicent.bottomsheet.CheckInUserTopFragment
import com.flok22.android.agicent.databinding.FragmentMapBinding
import com.flok22.android.agicent.model.LatLngModel
import com.flok22.android.agicent.model.NearByPlaceResponse
import com.flok22.android.agicent.model.PlaceData
import com.flok22.android.agicent.model.checkIn.PlaceIdModel
import com.flok22.android.agicent.model.userCheckedInStatus.UserCheckedInResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.ui.CheckedInUserStatusActivity
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.*
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapFragment : Fragment(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks,
    TapListener {

    private val className = MapFragment::class.java.simpleName

    private lateinit var binding: FragmentMapBinding
    private lateinit var locationRequest: LocationRequest
    private var currentLocationMarker: Marker? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var mGoogleMap: GoogleMap
    private var latitude = 0.0
    private var longitude = 0.0
    private val updateInterval = (5 * 1000).toLong() // 5 seconds
    private val fastInterval = (2 * 1000).toLong() // 1 seconds
    private lateinit var locationProvider: FusedLocationProviderClient
    private var placesList = ArrayList<PlaceData>()
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var prefHelper: SharedPreferenceManager
    private var shouldShowDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$className onCreate")
        prefHelper = SharedPreferenceManager(requireContext())
        locationProvider =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 50F
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d(TAG, "$className request locationUpdate")
                    updateMapUi(location.latitude, location.longitude)
                    val lat = prefHelper.placeLat.toDoubleOrNull()
                    val lang = prefHelper.placeLng.toDoubleOrNull()
                    if (lat != null && lang != null) {
                        val distance = getDistanceMeters(latitude, longitude, lat, lang)
                        Log.d(TAG, "$className request locationUpdate distance: $distance")
                        if (distance > 200 && latitude >= 0.0) {
                            Log.d(TAG, "$className onLocationResult: d > 200")
                            if (shouldShowDialog) {
                                val dialog = LocationCheckOutDialog.newInstance(this@MapFragment)
                                if (!dialog.isDialogVisible)
                                    dialog.show(childFragmentManager, LocationCheckOutDialog.TAG)
                            }
                            sendLocationCheckOutBroadCast()
                        }
                    }
//                    stopLocationUpdates()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, b: Bundle?): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userStatusFab.hide()

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
//        binding.spinKitView.hide()
        Log.d(TAG, "$className onMapReady")
        mGoogleMap = map
        if (!hasLocationPermission())
            requestLocationPermission()
        else
            enableLocation()
        mGoogleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_dark_theme)
        )
        binding.showPlaceList.setOnClickListener {
            if (placesList.isNotEmpty()) {
                val dialog =
                    NearMeDialog(placesList, /*latitude, longitude,*/ this@MapFragment)
                dialog.show(childFragmentManager, "")
            } else {
                requireContext().showToast("No nearby place to check in")
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "$className onResume")
        shouldShowDialog = true
        locationProvider.requestLocationUpdates(locationRequest, locationCallback, null)
        checkUserCheckedInStatus()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val task = locationProvider.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                updateMapUi(location.latitude, location.longitude)
            } else {
                locationProvider.requestLocationUpdates(
                    locationRequest, locationCallback, null
                )
            }
        }
    }

    private fun updateMapUi(latitude: Double, longitude: Double) {
        val distance = getDistanceMeters(this.latitude, this.longitude, latitude, longitude)

        Log.d(
            TAG,
            "$className updateMapUi:this.latlng ${this.latitude},${this.longitude} latLng: $latitude,$longitude"
        )
        if (distance > 50) {
            this.latitude = latitude
            this.longitude = longitude
            val latLng = LatLng(latitude, longitude)
            if (currentLocationMarker != null) {
                currentLocationMarker!!.remove()
            }
            Log.d(TAG, "$className inside if: latLng: $latLng")
            mGoogleMap.uiSettings.isScrollGesturesEnabled = true
            currentLocationMarker = mGoogleMap.addMarker(
                MarkerOptions().position(latLng).title("current location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location))
            )
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
            if (context?.isNetworkAvailable() == true) {
                nearByPlaceApiCall(latitude, longitude)
            } else {
                Log.d(TAG, "updateMapUi: inside map frag show network toast")
                context?.showNetworkToast()
            }
        } else {
            Log.d(TAG, "updateMapUi: distance is $distance")
        }
    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without location permission",
            Constants.LOCATION_PERMISSION_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionDenied(this, perms.first())) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) = enableLocation()

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /*private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }*/

    private fun enableLocation() {
        Log.d(TAG, "enableLocation: ")
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result = LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                //when GPS is on
                Log.i(TAG, "$className checkGps: inside try")
                val response = task.getResult(ApiException::class.java)
                /**
                 * Not needed cause here only called when location is disabled
                 */
                if (response.locationSettingsStates?.isLocationPresent == true) {
                    getCurrentLocation()
                }
            } catch (ex: ApiException) {
                ex.printStackTrace()
                when (ex.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        //here we send request to enable GPS
                        Log.i(TAG, "$className checkGps:inside when show location dialog ")
                        /*startIntentSenderForResult(
                            ex.status.resolution?.intentSender,
                            MainActivity.LOCATION_SETTING_REQUEST,
                            null, 0, 0, 0, null
                        )*/
                        val intentSender = (ex as ResolvableApiException).resolution.intentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        enableLocationRequest.launch(intentSenderRequest)
                    } catch (sendIntentEx: IntentSender.SendIntentException) {
                    }
                    //when setting is unavailable
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    private val enableLocationRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    getCurrentLocation()
                }
                RESULT_CANCELED -> {
                    enableLocation()
                }
            }
        }

    private fun nearByPlaceApiCall(latitude: Double, longitude: Double) {
//        binding.spinKitView.visibility = View.VISIBLE

        val call = RetrofitBuilder.apiService.getNearByPlaces(
            prefHelper.authKey, LatLngModel(latitude.toString(), longitude.toString())
        )
        call?.enqueue(object : Callback<NearByPlaceResponse?> {
            override fun onResponse(
                call: Call<NearByPlaceResponse?>,
                response: Response<NearByPlaceResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        //binding.spinKitView.visibility = View.GONE

                        placesList.clear()

                        binding.showPlaceList.show()
                        val nearByPlace = response.body()
                        for (i in 0 until nearByPlace!!.data.size) {
                            val markerOptions = MarkerOptions()
                            val places = nearByPlace.data[i]
                            val lat = places.place_latitude
                            val lng = places.place_longtitude
                            val latLng = LatLng(lat, lng)
                            markerOptions.position(latLng)
                            placesList.addAll(listOf(response.body()!!.data[i]))

                            val markerLayout =
                                activity?.layoutInflater?.inflate(
                                    R.layout.map_num_marker_layout, null
                                )!!
                            val markerText = markerLayout.findViewById<TextView>(R.id.people_count)
                            markerText?.text = places.checked_in_count.toString()
                            markerLayout.measure(
                                View.MeasureSpec.makeMeasureSpec(
                                    0, View.MeasureSpec.UNSPECIFIED
                                ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                            )
                            markerLayout.layout(
                                0, 0, markerLayout.measuredWidth, markerLayout.measuredHeight
                            )
                            val bitmap = markerLayout.let {
                                Bitmap.createBitmap(
                                    it.measuredWidth, markerLayout.measuredHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                            }
                            val canvas = bitmap?.let { Canvas(it) }
                            markerLayout.draw(canvas)
                            markerOptions.icon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                            this@MapFragment.mGoogleMap.addMarker(markerOptions)
                        }
                        this@MapFragment.mGoogleMap.setOnMarkerClickListener { marker ->
                            for (i in 0 until placesList.size) {
                                val listLat = placesList[i].place_latitude
                                val listLang = placesList[i].place_longtitude
                                val listLatLang = LatLng(listLat, listLang)
                                if (marker.position == listLatLang) {
                                    val distance =
                                        getDistanceMeters(latitude, longitude, listLat, listLang)
                                    if (distance <= 500) {
                                        val id = placesList[i].place_id
                                        Log.d(TAG, "onResponse: placeId:$id")
                                        getPlaceDetail(PlaceIdModel(id))
                                    } else {
                                        requireContext().showToast("Too far to checkIn")
                                    }
                                }
                            }
                            true
                        }
//                        checkUserCheckedInStatus()
                    }
                    500 -> {
                        binding.showPlaceList.invisible()
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<NearByPlaceResponse?>, t: Throwable) {
//                binding.spinKitView.visibility = View.GONE
                binding.showPlaceList.invisible()
                Log.d(TAG, "MapsFragment nearByPlaceApiCall onFailure: $t")
                context?.showToast("${t.message}")
            }
        })
    }

    private fun checkUserCheckedInStatus() {
        val call = RetrofitBuilder.apiService.getUserCheckedInStatus(prefHelper.authKey)
        call?.enqueue(object : Callback<UserCheckedInResponse?> {
            override fun onResponse(
                call: Call<UserCheckedInResponse?>,
                response: Response<UserCheckedInResponse?>
            ) {
                Log.d(TAG, "$className checkUserCheckedInStatus onResponse: ${response.code()}")
                when (response.code()) {
                    200 -> {
                        prefHelper.placeLat = response.body()?.data?.place_latitude.toString()
                        prefHelper.placeLng = response.body()?.data?.place_longtitude.toString()
                        prefHelper.placeId = response.body()?.data!!.place_id
                        binding.userStatusFab.apply {
                            show()
                            setOnClickListener {
                                val intent =
                                    Intent(
                                        requireActivity(), CheckedInUserStatusActivity::class.java
                                    )
                                response.body()?.data?.let { it1 ->
                                    intent.putExtra("placeId", it1.place_id)
                                }
                                startActivity(intent)
                            }
                        }
                    }
                    401 -> {
                        binding.userStatusFab.invisible()
                    }
                    500 -> {
                        binding.userStatusFab.invisible()
                        context?.showToast("Database error")
                    }
                }
            }

            override fun onFailure(call: Call<UserCheckedInResponse?>, t: Throwable) {
                binding.userStatusFab.invisible()
                Log.d(TAG, "$className checkUserCheckedInStatus onFailure: $t")
            }
        })
    }

    private fun getPlaceDetail(placeIdModel: PlaceIdModel) {
        val bundle = Bundle()
        bundle.putInt("placeId", placeIdModel.place_id)
        val dialog = CheckInUserTopFragment(requireContext(), this@MapFragment)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "CheckInUserTopFragment")
    }

//    private fun stopLocationUpdates() = locationProvider.removeLocationUpdates(locationCallback)

    override fun onTapped() = checkUserCheckedInStatus()

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "$className onPause")
        shouldShowDialog = false
//        stopLocationUpdates()
    }

    private fun sendLocationCheckOutBroadCast() {
        val requestIntent = Intent("outOfPlaceRadius")
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(requestIntent)
    }

    override fun onStop() {
        super.onStop()
        shouldShowDialog = false
    }

}

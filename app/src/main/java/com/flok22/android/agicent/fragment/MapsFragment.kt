package com.flok22.android.agicent.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.FragmentMapsBinding
import com.flok22.android.agicent.model.*
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsBinding
    lateinit var mLocationRequest: LocationRequest
    private lateinit var mLastLocation: Location
    lateinit var markerOptions: MarkerOptions
    lateinit var locationCallback: LocationCallback
    lateinit var currentPlaces: GooglePlacesResponse
    private lateinit var mGoogleMap: GoogleMap
    private var latitude = 0.0
    private var longitude = 0.0
    private val updateInterval = (15 * 1000).toLong() // 15 seconds
    private val fastInterval = (5 * 1000).toLong() // 5 seconds
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isGranted = false
    val list = ArrayList<Result>()//create Arraylist type of model
    private var placesList = ArrayList<PlaceData>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
/*
    override fun onMapReady(map: GoogleMap) {
        mGoogleMap = map
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (!checkSinglePermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) &&
            !checkSinglePermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) &&
            !checkSinglePermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            requestPermission()
            return
        }
        val locationRequest = LocationRequest.create()
        locationRequest.interval = updateInterval
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval = fastInterval
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.equals(null)) {
                    makeToast(requireContext(), "location is null", Toast.LENGTH_LONG)
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        context?.let {
                            makeToast(
                                it, "location is ${location.longitude}", Toast.LENGTH_LONG
                            )
                        }
                    }
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()!!
        )
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                val latLng = LatLng(latitude, longitude)

                mGoogleMap.addMarker(
                    MarkerOptions().position(latLng).title("current location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location))
                )
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
            }
        }
    }

    private fun requestPermission() {
        fetchDataFromSecondActivity.launch(null)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (isGranted) {
            getCurrentLocation()
        } else {
            makeToast(
                requireContext(), "Location required: Please enable from settings",
                Toast.LENGTH_LONG
            )
        }
    }*/

    override fun onStart() {
        super.onStart()
        //startLocationUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        mGoogleMap = map
        getLocation()
        mGoogleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_dark_theme
            )
        )

        //val style = MapStyleOptions.loadRawResourceStyle(requireContext(), R.string.style_json)
        //mGoogleMap.setMapStyle(style)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    protected fun startLocationUpdates() {
        // initialize location request object
        mLocationRequest = LocationRequest.create()
        mLocationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            setInterval(updateInterval)
        }

        // initialize location setting request builder object
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        // initialize location service object
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        settingsClient!!.checkLocationSettings(locationSettingsRequest)

        // call register location listener
//        registerLocationListener()
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {
        // initialize location callback object
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { onLocationChanged(it) }
            }
        }
        /*val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                onLocationChanged(p0.lastLocation)
            }
        }*/
        // 4. add permission if android version is greater then 23
        if (Build.VERSION.SDK_INT >= 23 && checkPermission()) {
            LocationServices.getFusedLocationProviderClient(requireContext())
                .requestLocationUpdates(
                    mLocationRequest!!, locationCallback,
                    Looper.myLooper()!!
                )
        }
    }

    private fun onLocationChanged(location: Location) {
        // create message for toast with updated latitude and longitudefa
        var msg = "Updated Location: " + location.latitude + " , " + location.longitude

        // show toast message with updated location
        //  Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
        val latLng = LatLng(location.latitude, location.longitude)

        Log.d(ContentValues.TAG, "onLocationChanged: $latLng")
        mGoogleMap!!.clear()
        mGoogleMap!!.addMarker(
            MarkerOptions().position(latLng).title("Current Location" + latLng).icon(
                BitmapDescriptorFactory.fromResource(
                    R.drawable.user_location
                )
            )
        )
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        // nearByPlaceApiCall()

    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            requestPermissions()
            return false
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1
        )
    }

    fun drawMarker(latLng: LatLng) {
        markerOptions = MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bell_fill))
        //    .draggable(true)
//            .snippet(getAddress(latLng.latitude,latLng.longitude))
        //  binding.tvLatLng.text = "${latLng.latitude},${latLng.longitude}"
        try {
            // binding.tvSetAddress.text = "${getAddress(latLng.latitude, latLng.longitude)}"
        } catch (e: Exception) {
            Log.d("ErrorMessage", "drawMarker: ${e.message}")
        }

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
//       var currentMarker = mGoogleMap.addMarker(markerOptions)
//        currentMarker?.showInfoWindow()

        val circle: Circle = mGoogleMap.addCircle(
            CircleOptions().center(
                LatLng(latitude.toDouble(), longitude.toDouble())
            )
        )
        circle.isVisible = true
        //   getZoomLevel(circle)

        latitude = latLng.latitude
        longitude = latLng.longitude
        Log.d("LATLANGS", "drawMarker: $latitude $longitude")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
/*            if (permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION) {
               registerLocationListner()
*/
        }
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            getLocation()
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                Log.d(TAG, "getLocation: ${checkPermission()} & ${isLocationEnabled()}")
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(Activity()) { task ->
                    val location: Location? = task.result
                    Log.d(TAG, "getLocation: isLocation null: ${location == null}")
                    /*if (location != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        *//*if (mGoogleMap != null) {
                            mGoogleMap!!.addMarker(
                                MarkerOptions().position(LatLng(list[0].latitude, list[0].longitude))
                                    .title("Current Location"+list[0].getAddressLine(0))
                            )
                        }*//*
                        mGoogleMap.uiSettings.setScrollGesturesEnabled(false)
                        latitude = list[0].latitude
                        longitude = list[0].longitude
                        val latLng = LatLng(latitude, longitude)
                        mGoogleMap.addMarker(
                            MarkerOptions().position(latLng).title("Current Location")
                                .icon(
                                    BitmapDescriptorFactory.fromResource(R.drawable.user_location)
                                )
                        )
                        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                        val circle: Circle = mGoogleMap.addCircle(
                            CircleOptions().center(
                                LatLng(latitude, longitude)
                            )
                        )
                        circle.isVisible = true
                        nearByPlaceApiCall(latitude, longitude)
*//*
                           drawMarker(latLng)
                        addDataTofirebase(latitude,longitude)
                        Toast.makeText(requireContext(), "location:${latitude }${longitude}", Toast.LENGTH_LONG).show()

                            mainBinding.apply {
                                tvLatitude.text = "Latitude\n${list[0].latitude}"
                                tvLongitude.text = "Longitude\n${list[0].longitude}"
                                tvCountryName.text = "Country Name\n${list[0].countryName}"
                                tvLocality.text = "Locality\n${list[0].locality}"
                                tvAddress.text = "Address\n${list[0].getAddressLine(0)}"
                            }
                        *//*
                    }*/
                }
            } else {
                Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }


    private fun nearByPlaceApiCall(latitude: Double, longitude: Double) {
        Log.d(TAG, "nearByPlaceApiCall:")

        /*mGoogleMap.clear()
        val url = getUrl(latitude, longitude)*/
        val call = RetrofitBuilder.apiService.getNearByPlaces(
            SharedPreferenceManager(requireContext()).authKey,
            LatLngModel(
                latitude.toString(), longitude.toString()
            )
        )
        call?.enqueue(object : Callback<NearByPlaceResponse?> {
            override fun onResponse(
                call: Call<NearByPlaceResponse?>,
                response: Response<NearByPlaceResponse?>
            ) {
                Log.d(TAG, "nearByPlaceApiCall: inside onResponse(): ${response.isSuccessful}")
                if (response.isSuccessful) {
                    binding.showPlaceList.visibility = View.VISIBLE
                    val nearByPlace = response.body()
//                    val placeList = nearByPlace!!.data
                    for (i in 0 until nearByPlace!!.data.size) {
                        val markerOptions = MarkerOptions()
                        val places = nearByPlace.data[i]
                        val lat = places.place_latitude
                        val lng = places.place_longtitude
                        val latLng = LatLng(lat, lng)
                        markerOptions.position(latLng)
                        placesList.addAll(listOf(response.body()!!.data[i]))

                        val markerLayout =
                            activity?.layoutInflater?.inflate(R.layout.map_num_marker_layout, null)
                        val markerText = markerLayout?.findViewById<TextView>(R.id.people_count)
                        markerText?.text = i.toString()
                        markerLayout?.measure(
                            View.MeasureSpec.makeMeasureSpec(
                                0, View.MeasureSpec.UNSPECIFIED
                            ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )
                        markerLayout?.layout(
                            0, 0, markerLayout.measuredWidth, markerLayout.measuredHeight
                        )
                        val bitmap = markerLayout?.let {
                            Bitmap.createBitmap(
                                it.measuredWidth, markerLayout.measuredHeight,
                                Bitmap.Config.ARGB_8888
                            )
                        }
                        val canvas = bitmap?.let { Canvas(it) }
                        markerLayout?.draw(canvas)
                        markerOptions.icon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })
                        this@MapsFragment.mGoogleMap.addMarker(markerOptions)
                        this@MapsFragment.mGoogleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                    binding.showPlaceList.setOnClickListener {
                        if (placesList.isNotEmpty()) {
                            /*val dialog = NearMeDialog(placesList, latitude, longitude)
                            dialog.show(childFragmentManager, "")*/
                        }
                    }
                    /*this@MapsFragment.mGoogleMap.setOnMarkerClickListener { marker ->
                        for (i in 0 until placesList.size) {
                            val listLat = placesList[i].place_latitude
                            val listLang = placesList[i].place_longtitude
                            val listLatLang = LatLng(listLat, listLang)
                            if (marker.position == listLatLang) {
                                val distance =
                                    getDistanceMeters(latitude, longitude, listLat, listLang)
                                val dialog = ModalBottomSheetDialogFragment.newInstance()
                                val b = Bundle()
                                b.putString("name", placesList[i].place_name)
                                b.putString("address", placesList[i].place_address)
                                b.putLong("distance", distance)
                                b.putInt("peopleCount", placesList[i].checked_in_count)
                                Log.d(
                                    TAG,
                                    "MapsFragment() nearby onResponse: key ->${placesList[i].place_photo_key}"
                                )
                                b.putString("photoKey", placesList[i].place_photo_key)
                                dialog.arguments = b
                                dialog.show(childFragmentManager, "")
                            }
                        }
                        true
                    }*/
                }
            }

            override fun onFailure(call: Call<NearByPlaceResponse?>, t: Throwable) {
                Log.d(TAG, "MapsFragment nearByPlaceApiCall onFailure: $t")
                requireContext().showToast("${t.message}")
            }
        })

        /*val call1 = RetrofitGoogleClient.googleApiService.getNearByPlaces(url)
        call1.enqueue(object : Callback<GooglePlacesResponse> {
            override fun onResponse(
                call: Call<GooglePlacesResponse>,
                response: Response<GooglePlacesResponse>
            ) {
                currentPlaces = response.body()!!
                if (response.isSuccessful) {
                    for (i in 0 until response.body()!!.results.size) {
                        val markerOptions = MarkerOptions()
                        val googlePlaces = response.body()!!.results[i]
                        val lat = googlePlaces.geometry.location.lat
                        val lng = googlePlaces.geometry.location.lng
                        val placesName = googlePlaces.name
                        val latLng = LatLng(lat, lng)
                        markerOptions.position(latLng)
                        // Here added response in list added by me

                        list.addAll(listOf(response.body()!!.results[i]))
                        //markerOptions.title(placesName)

                        // set custom marker here
                        val markerLayout =
                            activity?.layoutInflater?.inflate(R.layout.map_num_marker_layout, null)
                        val markerText = markerLayout?.findViewById<TextView>(R.id.people_count)
                        markerText?.text = i.toString()
                        markerLayout?.measure(
                            View.MeasureSpec.makeMeasureSpec(
                                0, View.MeasureSpec.UNSPECIFIED
                            ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )
                        markerLayout?.layout(
                            0, 0, markerLayout.measuredWidth, markerLayout.measuredHeight
                        )
                        val bitmap = markerLayout?.let {
                            Bitmap.createBitmap(
                                it.measuredWidth, markerLayout.measuredHeight,
                                Bitmap.Config.ARGB_8888
                            )
                        }
                        val canvas = bitmap?.let { Canvas(it) }
                        markerLayout?.draw(canvas)
                        markerOptions.icon(bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) })


                        //markerOptions.snippet(i.toString())//Assign index for Marker

                        this@MapsFragment.mGoogleMap.addMarker(markerOptions)
                        this@MapsFragment.mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                        this@MapsFragment.mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
                    }
                    mGoogleMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
                        override fun onMarkerClick(marker: Marker): Boolean {
                            //  Log.d("Him", "onMarkerClick: ${marker.position}")
                            for (i in 0 until list.size) {
                                val listLat = list[i].geometry.location.lat
                                val listLang = list[i].geometry.location.lng
                                val listLatLang = LatLng(listLat, listLang)

                                if (marker.position.equals(listLatLang)) {
                                    val distance =
                                        getDistanceMeters(latitude, longitude, listLat, listLang)
                                    Log.d("Him", "onMarkerClick: ${list[i].name}")
                                    val dialog = ModalBottomSheetDialogFragment.newInstance()
                                    val b = Bundle()
                                    b.putString("name", list[i].name)
                                    b.putString("address", list[i].vicinity)
                                    b.putLong("distance", distance)
                                    dialog.arguments = b
                                    dialog.show(childFragmentManager, "")

                                }
                            }

                            return true
                        }

                    })
                }
            }

            override fun onFailure(call: Call<GooglePlacesResponse>, t: Throwable) {
                requireContext().showToast("${t.message}")
            }
        })*/
    }

/*private fun getUrl(latitude: Double, longitude: Double): String {
    val googlePlaceUrl =
        StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
    googlePlaceUrl.append("?location=$latitude,$longitude")
    googlePlaceUrl.append("&radius=10000")//10 Km
    googlePlaceUrl.append("&type=restaurant")
    googlePlaceUrl.append("&key=AIzaSyBpix5fIryUOOxewaG6lmnyseg5r4ZUvuo")
    Log.d("URL_DEBUG", "getUrl:${googlePlaceUrl} ")
    return googlePlaceUrl.toString()
}*/
}
package com.bangkit.fixmyrideapp.view.maps

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.bangkit.fixmyrideapp.R
import com.bangkit.fixmyrideapp.data.response.NearbyItem
import com.bangkit.fixmyrideapp.data.utils.Result
import com.bangkit.fixmyrideapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val mapsViewModel: MapsViewModel by viewModels {
        MapsViewModel.SearchFoodRecipeFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getLocationNearby()

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true

    }


    private fun getLocationNearby() {
        binding.btnYourLocation.setOnClickListener {
            //check location permission
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100
                )
                return@setOnClickListener
            }
            val location = fusedLocationClient.lastLocation
            location.addOnSuccessListener {
                if (it != null){
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val radius = "1000"
                    val count = "20"
                    mapsViewModel.getNearbyLocation(latitude.toString(), longitude.toString(), radius, count).observe(this) {
                        when (it) {
                            is Result.Loading -> {}
                            is Result.Error -> {
                                Log.e("Error", it.error.toString())
                            }

                            is Result.Success -> {
                                addManyMarkerLocation(mMap, it.data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addManyMarkerLocation(googleMap: GoogleMap, data: List<NearbyItem>) {
        data.forEach { dataLoc ->
            val markerMap = MarkerOptions()
            val latLong = LatLng(dataLoc.latitude, dataLoc.longitude)
            markerMap.position(latLong)
                .title(dataLoc.name)
                .snippet(dataLoc.formattedAddress)


            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            val marker = googleMap.addMarker(markerMap)
            marker?.tag = dataLoc
        }
    }

//    private val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                getMyLocation()
//            }
//        }
//
//    private fun getMyLocation() {
//        if (ContextCompat.checkSelfPermission(
//                this.applicationContext,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            mMap.isMyLocationEnabled = true
//
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//    }

}
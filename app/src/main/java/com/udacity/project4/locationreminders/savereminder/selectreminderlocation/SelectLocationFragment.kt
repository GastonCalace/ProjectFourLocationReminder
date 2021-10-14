package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest.*
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private val TAG = SelectLocationFragment::class.java.simpleName
        private val REQUEST_LOCATION_PERMISSION = 1
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            _viewModel.selectedPOI.value = null
            _viewModel.longitude.value = null
            _viewModel.latitude.value = null
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (isPermissionGranted()){ enableMyLocation() }
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)

    }

    private fun onLocationSelected() {
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
        }
        catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can'' find style. Error: ", e)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions().position(poi.latLng).title(poi.name)
            )
            poiMarker.showInfoWindow()
            locationSelected(poi)
        }
    }

    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)

            )
            val location = String.format(Locale.getDefault(),
                "%s (%s)",
                R.string.dropped_pin,
                snippet)
            locationSelected(PointOfInterest(latLng, location, location))
        }
    }

    private fun locationSelected(poi: PointOfInterest) {
        _viewModel.selectedPOI.value = poi
        _viewModel.latitude.value = poi.latLng.latitude
        _viewModel.longitude.value = poi.latLng.longitude
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted())
            zoomInMyPosition()
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(ACCESS_FINE_LOCATION) ,
                REQUEST_LOCATION_PERMISSION
            )
        }

    }

    @SuppressLint("MissingPermission")
    private fun zoomInMyPosition() {
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val homeLatLng = LatLng(it.latitude, it.longitude)
                    val zoomLevel = 18f
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        if (grantResults.isEmpty() ||
                grantResults[0] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                createSnackBar()
        } else {
            enableMyLocation()
        }
    }


    private fun createSnackBar() {
        Snackbar.make(
                binding.mapConstraint,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_LONG
        )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
    }

}

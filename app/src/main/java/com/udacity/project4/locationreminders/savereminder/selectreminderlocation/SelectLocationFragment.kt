package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest.*
import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
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


//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected

//        TODO: call this function after the user confirms on the selected location

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        enableMyLocation()
        setMapStyle(map)
        setPoiClick(map)
        setMapLongClick(map)

    }

    private fun onLocationSelected() {
        _viewModel.navigationCommand.value = NavigationCommand.Back
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

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
        _viewModel.reminderSelectedLocationStr.value = poi.name
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

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
                    ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //Moves camera to current location
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
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(ACCESS_FINE_LOCATION) ,
                REQUEST_LOCATION_PERMISSION
            )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                enableMyLocation()
        }
    }

}

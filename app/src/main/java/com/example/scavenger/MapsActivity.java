package com.example.scavenger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private  LocationHelper locationHelper = new LocationHelper();
    private GoogleMap mMap;
    private Location mLocation;
    private String TAG = "MapActivity";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = false;
    private Marker mLocationMarker;
    private LatLng mLatLng;
    private final SnackbarHelper snackbarHelper = new SnackbarHelper();

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private Place mPLace;
    private PlacesClient placesClient;
    private String [] mStoreNames = {"Walmart Supercenter","Walmart","Pay Less Super Market","Target","Kroger","Meijer","Albertsons","Market","Food"};
   // private HashMap<Integer,String> results = new HashMap<>();
    private Integer key =0;
    private String s;
    public String cityscore;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Double lat;
    private Double lng;
    private boolean alreadyHaveLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

         database = FirebaseDatabase.getInstance();
         myRef = database.getReference("Location");

        getLocationPermission();
        getLocation();
        Places.initialize(this, "AIzaSyCUNENQ8f5kPUVh-xWUkRtx3yuiMDeqTAM");
        placesClient = Places.createClient(this);
    }


    private void getLocation(){

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                lat = dataSnapshot.child("Lat").getValue(Double.class);
                lng = dataSnapshot.child("Long").getValue(Double.class);
                mLatLng = new LatLng(lat,lng);
                getDeviceLocation(mLatLng);
                alreadyHaveLocation = true;
                setupMap(mLatLng, DEFAULT_ZOOM);
                snackbarHelper.showMessage(MapsActivity.this,mLatLng.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    private void addMarkers(String  placeId, int i){

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG);

// Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
            mLocationMarker = mMap.addMarker(new MarkerOptions()
                     .position(place.getLatLng()).title(place.getName()));

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });


    }



    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                            COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        if (alreadyHaveLocation = false) {
            locationHelper.getDeviceLocation(MapsActivity.this, mLocationPermissionsGranted,
                    latLng -> {
                        setupMap(latLng, DEFAULT_ZOOM);
                        if (mLocationPermissionsGranted) {
                            getDeviceLocation(latLng);

                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }


                        }
                    });
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    public void getDeviceLocation(LatLng latLng){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        double distance = 104.52;
        setupMap(latLng,DEFAULT_ZOOM);
        HashMap<Integer,String> results = new HashMap<>();
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        LatLng NE = new LatLng(latLng.latitude+.1,latLng.longitude+.2);
        LatLng SW = new LatLng(latLng.latitude,latLng.longitude - .1);

        LatLng targetNorteast = SphericalUtil.computeOffset(NE, distance * Math.sqrt(2), 45);
        LatLng targetSouthwest = SphericalUtil.computeOffset(SW, distance * Math.sqrt(2), 225);
        RectangularBounds bounds = RectangularBounds.newInstance(targetSouthwest,targetNorteast);


                    for (String s : mStoreNames) {


                        // Use the builder to create a FindAutocompletePredictionsRequest.
                        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                                // Call either setLocationBias() OR setLocationRestriction().
                                .setLocationRestriction(bounds)
                                .setOrigin(latLng)
                                .setCountries("US")
                                .setSessionToken(token)
                                .setQuery(s)
                                .build();

                        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                            int i =0 ;
                            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                                key+=1;
                                Log.i(TAG, prediction.getPlaceId());
                                Log.i(TAG, prediction.getPrimaryText(null).toString());
                                // mLocationMarker = mMap.addMarker(new MarkerOptions().position(prediction.getPlaceId())


                                if(prediction.getPlaceTypes().contains(Place.Type.GROCERY_OR_SUPERMARKET)) {
                                    results.put(key,prediction.getPrimaryText(null).toString());
                                    addMarkers(prediction.getPlaceId(), i);
                                }

                                if (s.equals(mStoreNames[mStoreNames.length-1])){
                                    Log.d("Test Filter", results.toString());
                                   Compute compute = new Compute();
                                   compute.getScore(results);
                                    break;
                                }

                            }
                        }
                        ).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                                snackbarHelper.showMessage(this, "Error");
                            }
                        });

                    }
    }

    private void setupMap(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void drawBounds(RectangularBounds bounds){

        PolygonOptions polygonOptions =  new PolygonOptions()
                .add(new LatLng(bounds.getNortheast().latitude, bounds.getNortheast().longitude))
                .add(new LatLng(bounds.getSouthwest().latitude, bounds.getNortheast().longitude))
                .add(new LatLng(bounds.getSouthwest().latitude, bounds.getSouthwest().longitude))
                .add(new LatLng(bounds.getNortheast().latitude, bounds.getSouthwest().longitude))
                .strokeColor(this.getColor(R.color.quantum_black_100));
        mMap.addPolygon(polygonOptions);

    }

}
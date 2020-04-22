package com.example.scavenger;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Results extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final String TAG = "Results: ";
    private boolean mPermission;
    private static final float DEFAULT_ZOOM = 10f;
    private String cityName;
    private ImageView cityPhoto;
    private LatLng mLatLng;
    private PlacesClient placesClient;
    SnackbarHelper snackbarHelper = new SnackbarHelper();
    public interface LocationListener{
        void OnLocation(LatLng latLng) throws IOException;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initMap();
        Places.initialize(this, "AIzaSyCUNENQ8f5kPUVh-xWUkRtx3yuiMDeqTAM");
        placesClient = Places.createClient(this);

        TextView city = findViewById(R.id.city);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LocationHelper mLocationHelper = new LocationHelper();
        mLocationHelper.getLocationPermission(this,Results.this, permission->{
            if(permission){
                initMap();
                mPermission = permission;
                mLocationHelper.getDeviceLocation(this,permission,location->{
                    mLatLng = location;
                setupMap(location,DEFAULT_ZOOM);
                    List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                    cityName = addresses.get(0).getLocality();
                    city.setText(cityName);
                    getCityID();
                });
                }
        });


        RatingBar myRatingBar = findViewById(R.id.city_rating);
        float current = myRatingBar.getRating();

        ObjectAnimator anim = ObjectAnimator.ofFloat(myRatingBar, "rating", current, 5f);
        anim.setDuration(7000);
        anim.start();



        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.getView().setBackgroundColor(getResources().getColor(R.color.transparent_white));
        EditText etPlace = (EditText)autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlace.setHint("Search for a city");
        etPlace.setHintTextColor(getResources().getColor(R.color.quantum_black_100));


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.RATING, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

                if (place.getRating() != null) {
                    snackbarHelper.showMessage(Results.this, "Location Entered Does Not Seem To Be A City");
                } else {
                    setupMap(place.getLatLng(), DEFAULT_ZOOM);
                    cityName = place.getName();
                    city.setText(cityName);
                    setCityPhoto(place.getId());
                    mLatLng = place.getLatLng();
                    snackbarHelper.showMessage(Results.this, mLatLng.toString());
                }
            }

            @Override
            public void onError(@NonNull Status status) {

            }

        });

    }


    public void getDeviceLocation (Context context, Boolean mLocationPermissionsGranted, LocationHelper.LocationListener listener){

        try {
            if (mLocationPermissionsGranted) {
                listener.OnLocation(mLatLng);
            }
        }
        catch (SecurityException | IOException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void getCityID() {
        final String[] placeId = new String[1];


        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.PHOTO_METADATAS);

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        RectangularBounds bounds = RectangularBounds.newInstance(
                mLatLng,
                new LatLng(mLatLng.latitude + .15, mLatLng.longitude + .1));

        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationRestriction(bounds)
                .setOrigin(mLatLng)
                .setCountries("US")
                .setSessionToken(token)
                .setQuery(cityName)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.i(TAG, prediction.getPlaceId());
                Log.i(TAG, prediction.getPrimaryText(null).toString());
                // mLocationMarker = mMap.addMarker(new MarkerOptions().position(prediction.getPlaceId())
                placeId[0] = prediction.getPlaceId();

            }
            setCityPhoto(placeId[0]);


        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());

            }
        });

    }

    private void setCityPhoto(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();



            Log.i(TAG, "Place found: " + place.getName());
            Toast.makeText(this, place.getId(),Toast.LENGTH_LONG);
            // Get the photo metadata.
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the attribution text.
            String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                cityPhoto = findViewById(R.id.city_photo);
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                cityPhoto.setBackground(d);


            }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
         });
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
       // Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mPermission) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Intent fullMap = new Intent(Results.this, MapsActivity.class);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setOnMapClickListener((LatLng v)-> startActivity(fullMap));
        }
    }

    private void setupMap(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(Results.this);
    }
}

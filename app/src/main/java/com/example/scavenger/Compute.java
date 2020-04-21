package com.example.scavenger;

import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Compute {

    private PlacesClient placesClient;

    private String TAG = "MapActivity";

    public void compute(LatLng mLatLng){

    String [] mStoreNames = {"Walmart","Pay Less","Target","Kroger","Meijer","Albertsons",};
    for (String s : mStoreNames) {

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
            .setQuery(s)
            .build();

    placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
            Log.i(TAG, prediction.getPlaceId());
            Log.i(TAG, prediction.getPrimaryText(null).toString());
            // mLocationMarker = mMap.addMarker(new MarkerOptions().position(prediction.getPlaceId())
            //  addMarkers(prediction.getPlaceId());
        }
    }).addOnFailureListener((exception) -> {
        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            //    snackbarHelper.showMessage(this,"Error");
        }
    });

        }
    }
}

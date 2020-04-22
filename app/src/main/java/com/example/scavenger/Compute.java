package com.example.scavenger;

import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Compute {

    private String [] mStoreNames = {"Walmart Supercenter","Walmart","Pay Less Super Market","Target","Kroger","Meijer","Albertsons","Market","Food"};
    // private HashMap<Integer,String> results = new HashMap<>();
    private Integer key =0;
    private PlacesClient placesClient;

    public HashMap<Integer,String> computeStores(LatLng latLng){

        double distance = 104.52;
        //setupMap(latLng,DEFAULT_ZOOM);
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
                               // addMarkers(prediction.getPlaceId(), i);
                            }

                            if (s.equals(mStoreNames[mStoreNames.length-1])){
                                Log.d("Test Filter", results.toString());
                                break;
                            }

                        }
                    }
            ).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    //snackbarHelper.showMessage(this, "Error");
                }
            });

        }

        return results;
    }

    public float getScore(HashMap<Integer,String> filteredResults){

        final String TAG= "Compute";
        int total = filteredResults.size();
        TextView cityscore;

        String [] mStoreNames = {"Walmart Supercenter","Walmart","Pay Less Super Market","Target","Kroger","Meijer","Albertsons","Market"};
        HashMap<String, Integer>  matches = new HashMap<>();
        Collection<String> places = filteredResults.values();
        Set<String> dupeSet = new HashSet<>();
       float score =3;

        for (String t : places) {
            if (Collections.frequency(places, t) >= 0) {

                matches.put(t,Collections.frequency(places, t));
                score = 5;
            }
        }

        Log.d(TAG, filteredResults.values().toString());

        int i =0;

        for (String s: mStoreNames){}
        i+=1;

        if(filteredResults.containsValue(mStoreNames[i]) ){

           // Log.d(TAG,String.valueOf(matches.toString()) );
        }
       // Log.d(TAG, String.valueOf(score));
    //    Log.d(TAG, filteredResults.toString());
        return score;
    }

}

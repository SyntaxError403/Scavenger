package com.example.scavenger;

import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Compute {




    public void getScore(HashMap<Integer,String> filteredResults){

        final String TAG= "Compute";
        int total = filteredResults.size();
        int score =0 ;
        String [] mStoreNames = {"Walmart Supercenter","Walmart","Pay Less Super Market","Target","Kroger","Meijer","Albertsons","Market"};
        HashMap<String, Integer>  matches = new HashMap<>();
        Collection<String> places = filteredResults.values();
        Set<String> dupeSet = new HashSet<>();

        for (String t : places) {
            if (Collections.frequency(places, t) >= 2) {

                matches.put(t,Collections.frequency(places, t));
            }
        }

        Log.d(TAG, matches.toString());

        int i =0;

        for (String s: mStoreNames){}
        i+=1;

        if(filteredResults.containsValue(mStoreNames[i]) ){
            score +=1;
           // Log.d(TAG,String.valueOf(matches.toString()) );
        }
       // Log.d(TAG, String.valueOf(score));
    //    Log.d(TAG, filteredResults.toString());
    }

}

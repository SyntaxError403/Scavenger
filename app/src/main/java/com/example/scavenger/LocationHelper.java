package com.example.scavenger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

public class LocationHelper {


        private static final String TAG ="LocationHelper Services";
        private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
        private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

        public interface PermissionListener {
            void OnPermission (Boolean mLocationPermissionsGranted);
        }

        private Boolean mLocationPermissionsGranted = false;
        private android.location.Location mLocation;
        private LatLng mLatLng;
        private FusedLocationProviderClient mFusedLocationProviderClient;

        public interface LocationListener{
            void OnLocation(LatLng latLng) throws IOException;
        }


        public void getLocationPermission(Context context, Activity activity, PermissionListener permissionListener){
            Log.d(TAG, "getLocationPermission: getting location permissions");

            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};

            if(ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(context.getApplicationContext(),
                        COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    mLocationPermissionsGranted = true;
                    permissionListener.OnPermission(mLocationPermissionsGranted);
                    // initMap();
                }else{
                    ActivityCompat.requestPermissions(activity,
                            permissions,
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            }else{
                ActivityCompat.requestPermissions(activity,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }

        public void getDeviceLocation(Context context, Boolean mLocationPermissionsGranted, LocationListener listener){
            Log.d(TAG, "getDeviceLocation: getting the devices current location");

            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

            try{
                if(mLocationPermissionsGranted){

                    final Task location = mFusedLocationProviderClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: found location!");
                                mLocation = (android.location.Location) task.getResult();

                                //TODO: APP CRASHES ON NULL LOCATION
                                //TODO: GET LAST KNOWN LOCATION
                                //TODO: WIFI LOCATION

                                mLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                                try {
                                    listener.OnLocation(mLatLng);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                Log.d(TAG, "onComplete: current location is null");
                                Toast.makeText(context, "unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }catch (SecurityException e){
                Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
            }
        }


    }


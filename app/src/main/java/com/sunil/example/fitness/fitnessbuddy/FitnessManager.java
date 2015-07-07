package com.sunil.example.fitness.fitnessbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Sunil on 7/6/2015.
 */
public class FitnessManager implements LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    Location mLocation;

    Activity mContext;
    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    public static final String TAG = "FitnessBuddy:";
    private static final String SUBTAG = "FitnessManager:";

    public FitnessManager(Activity context){
        mContext = context;
        mGoogleApiClient = buildGoogleApiClient();
        mGoogleApiClient.connect();
    }
    protected synchronized GoogleApiClient buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        return mGoogleApiClient;
    }

    public Location getCurrentLocation(){
        Location mLastLocation = mLocation;
        Log.i(TAG, SUBTAG + "getCurrentLocation():" + mLastLocation);
        return mLastLocation;

    }

    public void stopLocationUpdates() {
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
        Log.d(TAG, SUBTAG + "Location update stopped...");
    }

    public void resetGoogleApiClient(){
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
            mGoogleApiClient=null;
    }
    public void startLocationUpdates() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(mGoogleApiClient.isConnected()) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.d(TAG, SUBTAG + "Location update started : ");
        }
    }

    public boolean isGpsOn(){
        final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsOn = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        Log.d(TAG, SUBTAG + "isGpsOn():"+isGpsOn);
        return isGpsOn;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG,SUBTAG+"onLocationChanged():"+location);
        mLocation = location;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.i(TAG,SUBTAG+"onConnected():"+mLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), mContext, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                        }
                    }).show();
            return;
        }
    }
}

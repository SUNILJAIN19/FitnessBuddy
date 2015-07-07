package com.sunil.example.fitness.fitnessbuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sunil.example.fitness.fitnessbuddy.FitnessManager;
import com.sunil.example.fitness.fitnessbuddy.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A placeholder fragment containing a simple view.
 */
public class FitnessHomeActivityFragment extends Fragment implements View.OnClickListener{
    private Button mStartButton, mStopButton;
    private TextView mStartLatitudeTextView, mStartLongitudeTextView, mStartTimeTextView;
    private TextView mStopLatitudeTextView, mStopLongitudeTextView, mStopTimeTextView;
    private TextView mSpeedTextView, mDurationTextView,mDistanceTextView;
    Context mContext;
    FitnessManager mFitnessManager;
    Location mStartLocation,mStopLocation;
    long startTime,stopTime,elapsedTime;
    private float mDistance,mSpeed;
    boolean mGpsActivityStarted;
    boolean runStarted;
    private static final String TAG = FitnessManager.TAG;
    private static final String SUBTAG = "FitnessHomeActivityFragment:";

    public FitnessHomeActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (!isGooglePlayServicesAvailable()) {
            getActivity().finish();
        }
        mFitnessManager = new FitnessManager(getActivity());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fitness_home, container, false);
        mStartButton = (Button)view.findViewById(R.id.start_button);
        mStopButton = (Button)view.findViewById(R.id.stop_button);

        mStartLatitudeTextView = (TextView)view.findViewById(R.id.start_latitude_textView);
        mStartLongitudeTextView = (TextView)view.findViewById(R.id.start_longitude_textView);
        mStartTimeTextView = (TextView)view.findViewById(R.id.start_time_textView);

        mStopLatitudeTextView = (TextView)view.findViewById(R.id.stop_latitude_textView);
        mStopLongitudeTextView = (TextView)view.findViewById(R.id.stop_longitude_textView);
        mStopTimeTextView = (TextView)view.findViewById(R.id.stop_time_textView);

        mDurationTextView = (TextView)view.findViewById(R.id.duration_textView);
        mSpeedTextView = (TextView) view.findViewById(R.id.speed_textView);
        mDistanceTextView = (TextView) view.findViewById(R.id.distance_textView);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mGpsActivityStarted && !runStarted){
            mGpsActivityStarted = false;
            onStartRun();
        } else{
            updateButtons();
        }


    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFitnessManager.stopLocationUpdates();
        mFitnessManager.resetGoogleApiClient();
        mStartButton.setOnClickListener(null);
        mStopButton.setOnClickListener(null);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.start_button:
                if(mFitnessManager.isGpsOn()){
                    onStartRun();
                } else {
                    showSettingsAlert();
                }
                break;
            case R.id.stop_button:
                onStopRun();
                break;
        }
    }

    public boolean isRunStarted() {
        return runStarted;
    }

    private void onStartRun(){
        runStarted = true;
        mFitnessManager.startLocationUpdates();
        setStartLocation();
        startTime = new Date().getTime();
        updateButtons();
        updateStartLocationUI();
        resetStopLocation();
        Log.i(TAG, SUBTAG + "startTime:" + startTime);
    }

    private void onStopRun(){
        runStarted = false;
        mFitnessManager.stopLocationUpdates();
        setStopLocation();
        stopTime = new Date().getTime();
        setResults();
        updateButtons();
        updateStopLocationUI();
        Log.i(TAG, SUBTAG + "stopTime:" + stopTime);
    }
    private void resetStopLocation(){
        mStopLocation = null;
        updateStopLocationUI();
        stopTime = 0;
    }

    public void updateButtons(){
        mStartButton.setEnabled(!runStarted);
        mStopButton.setEnabled(runStarted);
    }

    public void updateStartLocationUI(){
        Log.i(TAG, SUBTAG + "updateStartLocationUI()..." + mStartLocation);
        if(mStartLocation!=null) {
            mStartLatitudeTextView.setText(String.valueOf(mStartLocation.getLatitude()));
            mStartLongitudeTextView.setText(String.valueOf(mStartLocation.getLongitude()));
            mStartTimeTextView.setText(formatTime(startTime));
        }
    }

    public void updateStopLocationUI(){
        if(mStopLocation != null) {
            mStopLatitudeTextView.setText(String.valueOf(mStopLocation.getLatitude()));
            mStopLongitudeTextView.setText(String.valueOf(mStopLocation.getLongitude()));
            mStopTimeTextView.setText(formatTime(stopTime));
            mDurationTextView.setText(formatDuration(elapsedTime));
            mDistanceTextView.setText(String.valueOf(mDistance)+" Ms");
            mSpeedTextView.setText(String.valueOf(mSpeed)+" M/S");
        } else{
            mStopLatitudeTextView.setText("");
            mStopLongitudeTextView.setText("");
            mStopTimeTextView.setText("");
            mDistanceTextView.setText("");
            mDurationTextView.setText("");
            mSpeedTextView.setText("");

        }
    }

    private String formatTime(long millis){
       String formattedTime= DateFormat.getTimeInstance().format(new Date(millis));
        return formattedTime.toString();
    }

    private String formatDuration(Long milliSeconds){
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    public void setStartLocation(){
        mStartLocation = mFitnessManager.getCurrentLocation();
    }

    private void setStopLocation(){
        mStopLocation = mFitnessManager.getCurrentLocation();
    }

    private void setResults(){
        if(mStartLocation != null && mStopLocation!=null){
            mDistance = mStartLocation.distanceTo(mStopLocation);
            elapsedTime = stopTime - startTime;
            mSpeed = mDistance /((elapsedTime)/1000);
        }
    }

    public void showSettingsAlert() {
        Log.i(TAG,SUBTAG+"showSettingsAlert()...");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS settings");
        alertDialog
                .setMessage("GPS is not enabled.Please Enable it for better accuracy.");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mGpsActivityStarted = true;
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        onStartRun();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 0).show();
            return false;
        }
    }


}

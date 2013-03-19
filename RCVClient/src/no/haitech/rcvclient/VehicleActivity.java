/*
 * Copyright (C) 2013 Thomas Le
 * 
 * This file is part of RCVClient.
 *
 * RCVClient is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RCVClient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public license
 * along with RCVClient. If not, see <http://www.gnu.org/licenses/>.
 */
package no.haitech.rcvclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

/**
 * VehicleActivity class.
 * Handling the Vehicle views.
 * 
 * @author Thomas Le
 * @see CameraView
 * @see MjpegInputStream
 */
public class VehicleActivity extends Activity {
    private final String TAG = "VehicleView";
    private CameraView cameraView;
    private StreamCam streamCam;
    private ToggleButton tbCamera;
    
    /*
     * Hardcoded demo URL
     */
    private final String URL = 
            "http://trackfield.webcam.oregonstate.edu/axis-cgi/mjpg/"
            + "video.cgi?resolution=800x600&amp%3bdummy=1333689998337";
//    String URL = "http://85.199.39.242/cgi-bin/video640x480.mjpg";
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets View
        setContentView(R.layout.activity_vehicle);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        

        // Camera toggle button
        tbCamera = (ToggleButton) findViewById(R.id.tbCamera);
        tbCamera.setOnClickListener(tbCameraListener());
        tbCamera.setChecked(cameraView.getIsStreaming());
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_vehicle, menu);
        return true;
    }
    
    
    
    @Override
    protected void onResume() {
        super.onResume();
        
        /*
         * Checks if the toggle camera button is true,
         * Starts the stream if true.
         */
        if(tbCamera.isChecked()) {
            streamCam = new StreamCam();
            streamCam.execute(URL);
            cameraView.startStream();
        }
    }
    
    
    
    @Override
    protected void onPause() {
        super.onPause();
        
        /*
         * Checks if the toggle camera button is true.
         * Stops the stream if true. 
         */
        if(tbCamera.isChecked()) {
            cameraView.setSource(null);
            cameraView.stopStream();
            if(streamCam != null) {
                streamCam.cancel(true);
            }
        }
    }
    
    
    
    /**
     * onClickListener for camera toggle button.
     * if toggle button is true, starts the camera.
     * if toggle button is false, stops the camera and clears the display.
     * @return onClickListener
     */
    private OnClickListener tbCameraListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tbCamera.isChecked()) {
                    streamCam = new StreamCam();
                    streamCam.execute(URL);
                    cameraView.startStream();
                } else {
                    cameraView.setSource(null);
                    cameraView.stopStream();
                    if(streamCam != null) {
                        streamCam.cancel(true);
                    }
                    cameraView.clearDisplay();
                }
            }
        };
    }
    
    
    
    /*
     * streamCam AsyncTask class for connecting to camera.
     */
    private class StreamCam extends AsyncTask<String, Void, MjpegInputStream> {
        private URL streamURL;
        private HttpURLConnection urlConnection ;
        
        
        
        @Override
        protected MjpegInputStream doInBackground(String... sURL) {
            if(isCancelled()) return null;
                try {
                    streamURL = new URL(sURL[0]);
                    urlConnection = (HttpURLConnection) streamURL.openConnection();
                    return new MjpegInputStream(urlConnection.getInputStream());

                } catch (MalformedURLException e) {
                    Log.d(TAG, "doInBackground(): ", e);
                } catch (IOException e) {
                    Log.d(TAG, "doInBackground(): ", e);
                }
            return null;
        }
        
        
        
        @Override
        protected void onPostExecute(MjpegInputStream r) {
            super.onPostExecute(r);
            cameraView.setSource(r);
        }
    }
}

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * CameraView that extends SurfaceView (@see {@link SurfaceView}).
 * Make view screen for camera.
 * 
 * @author Thomas Le
 * @see VehicleActivity
 * @see SurfaceView
 * @see SurfaceHolder.Callback
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "CameraView";    
    private MjpegThread mThread;
    private MjpegInputStream misSource;
    private SurfaceHolder surfaceHolder;
    private Bitmap bitmap;
    private Paint paint;
    private Rect screenSize;
    
    
    
    /**
     * Constructor 
     * Calling method init()
     * 
     * @param c 
     *        Context
     */
    public CameraView(Context c) {
        super(c);
        init(c);
    }
    
    
    
    /**
     * Constructor
     * Calling method init()
     * 
     * @param c 
     *        Context
     * @param a 
     *        Attribute
     */
    public CameraView(Context c, AttributeSet a) {
        super(c, a);
        init(c);
    }
    
    
    
    /*
     * Initializes objects
     */
    private void init(Context c) {
        /*
         * Access to the underlying surface
         * Allows you to control the surface size and format, etc.
         */
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        
        paint = new Paint();
    }
    
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if(canvas != null) {
                // Sets background color;
                canvas.drawColor(Color.RED); 
                /*
                 * Check if the misSource still exist, then draw JPEG images.
                 */
                if(misSource != null) {
                    bitmap = misSource.readMjpegFrame();
                    canvas.drawBitmap(bitmap, null, screenSize, paint);
                }
            }


        } catch (IOException e) {
            Log.d(TAG, "onDraw(): ", e);
        }
    }
    
    
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }
    
    
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Sets the screenSize / Rectangle size of stream.
        // When change screenSize, remember change MjpegInputStream JPEG size.
        screenSize = new Rect(0, 0, getWidth(), getHeight());
    }
    
    
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Stops the Stream.
        stopStream();
    }
    
    
    
    /**
     * Clears the display. Setting canvas black.
     */
    public void clearDisplay() {
        Canvas canvas = surfaceHolder.lockCanvas();
        synchronized (surfaceHolder) {
            try {
                canvas.drawColor(Color.BLACK);
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
    
    
    
    /**
     * Sets the source of the stream, and sets the hasSource to true.
     * @param s 
     *        MjpegInputStream ({@link MjpegInputStream} camera source
     */
    public void setSource(MjpegInputStream s) {
        misSource = s;
        mThread.setHasSource(true);
    }

    
    
    /**
     * Method for starting the camera stream. 
     */
    public void startStream() {
        mThread = new MjpegThread(surfaceHolder, this);
        mThread.setIsStreaming(true);
        mThread.start();
    }

    

    /**
     * Method for stopping the camera stream.
     */
    public void stopStream() {
        if(mThread != null) {
            mThread.setIsStreaming(false);
            boolean retry = true;
            while(retry) {
                try {
                    mThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    Log.d(TAG, "stopStream(): ", e);
                }
            }
        }
    }
    
    
    
    /**
     * Mutator method
     * Check if the thread is running.
     * @return boolean, if true the thread is running, if not false.
     */
    public boolean getIsStreaming() {
        if(mThread == null) return false;
        return mThread.getIsStreaming();
    }
    
    
    
    /*
     * Thread for drawing on the SurfaceView/Display.
     */
    private class MjpegThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private CameraView cameraView;
        private boolean isStreaming;
        private boolean hasSource;
        
        
        
        /**
         * Constructor
         * 
         * @param sh
         *        SurfaceHolder ({@link SurfaceHolder})
         * @param cv CameraView
         *        CameraView (@link {@link CameraView})
         */
        public MjpegThread(SurfaceHolder sh, CameraView cv) {
            surfaceHolder = sh;
            cameraView = cv;
            isStreaming = false;
            hasSource = false;
        }
        
        
        
        @Override
        public void run() {
            Canvas canvas = null;

            /*
             * Loops the run() if streaming is true. Calling onDraw().
             * Sleeps 500ms to use less CPU.
             */
            while(isStreaming) {
                if(hasSource) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas();
                        synchronized (surfaceHolder) {
                            cameraView.onDraw(canvas);
                        }
                        sleep(500);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "run(): ", e);
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }
        }
        
        
        
        /**
         * Mutator method
         * Sets the the thread to run loop.
         * @param b
         *        true sets to currently streaming, false is not streaming.
         */
        public void setIsStreaming(Boolean b) { isStreaming = b; }
        
        
        
        /**
         * Mutator method
         * Check if the thread is still running-loop / streaming.
         * @return boolean, true if the thread is running, false not running.
         */
        public boolean getIsStreaming() { return isStreaming; }
        
        
        
        /**
         * Mutator method
         * Sets the stream has source.
         * @param b
         *        true if the stream has source, false stream has not.
         */
        public void setHasSource(Boolean b) { hasSource = b; }
    }
}

package com.littlebandit.piborgrc.remotecontrol;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.littlebandit.piborgrc.R;
import com.littlebandit.piborgrc.customviews.RCButton;

import org.freedesktop.gstreamer.GStreamer;

/**
 * @author J414NP3R
 * @version 1.0
 */

public class PiCameraStream extends Activity implements SurfaceHolder.Callback
{
    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

    private SurfaceView mSurfaceView;

    private TcpClient mTcpClient;

    private final static String TAG = "PiCameraStream";

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize GStreamer and warn if it fails
        try
        {
            GStreamer.init(this);
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.picam_stream);
        setButtons();

        mSurfaceView = this.findViewById(R.id.surface_video);
        mSurfaceView.getHolder().addCallback(this);

        is_playing_desired = true;

        nativeInit();

        // Start TCP Client
        new ConnectTask().execute("");
    }

    private void setButtons()
    {
        RCButton btnForward = findViewById(R.id.btnForward);
        RCButton btnReverse = findViewById(R.id.btnReverse);
        RCButton btnLeft = findViewById(R.id.btnLeft);
        RCButton btnRight = findViewById(R.id.btnRight);

        btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                int action = motionEvent.getActionMasked();

                if(action == MotionEvent.ACTION_DOWN) driveMetalBorg(1, 1);
                else if(action == MotionEvent.ACTION_UP) stopMetalBorg();
                else return false;

                return true;
            }
        });

        btnReverse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                int action = motionEvent.getActionMasked();

                if(action == MotionEvent.ACTION_DOWN) driveMetalBorg(-1, -1);
                else if(action == MotionEvent.ACTION_UP) stopMetalBorg();
                else return false;

                return true;
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                int action = motionEvent.getActionMasked();

                if(action == MotionEvent.ACTION_DOWN) driveMetalBorg(-1, 1);
                else if(action == MotionEvent.ACTION_UP) stopMetalBorg();
                else return false;

                return true;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                int action = motionEvent.getActionMasked();

                if(action == MotionEvent.ACTION_DOWN) driveMetalBorg(1, -1);
                else if(action == MotionEvent.ACTION_UP) stopMetalBorg();
                else return false;

                return true;
            }
        });
    }

    private void driveMetalBorg(int left, int right)
    {
        if (mTcpClient != null)
        {
            mTcpClient.sendMessage("/set/" + left*-1 + "/" + right*-1);
            // mTcpClient.sendMessage("/set/" + left + "/" + right);
        }
    }

    private void stopMetalBorg()
    {
        if(mTcpClient != null)
        {
            mTcpClient.sendMessage("/off");
        }
    }

    protected void onDestroy()
    {
        nativeFinalize();
        super.onDestroy();
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message)
    {
        final TextView tv = this.findViewById(R.id.textview_message);
        runOnUiThread (new Runnable() {
            public void run()
            {
                tv.setText(message);
            }
        });
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized ()
    {
        Log.i ("GStreamer", "Gst initialized. Restoring state, to stream.");
        // Restore previous playing state
        if (is_playing_desired)
        {
            nativePlay();
        }
        else
        {
            nativePause();
        }
    }

    static
    {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("cam_stream");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
        //is_playing_desired = true;
        //nativePlay();
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient>
    {
        @Override
        protected TcpClient doInBackground(String... message)
        {
            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message)
                {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....
        }
    }

    @Override
    public void onBackPressed()
    {
        if (mTcpClient != null) mTcpClient.stopClient();
        super.onBackPressed();
    }
}

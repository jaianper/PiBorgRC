package com.littlebandit.piborgrc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.littlebandit.piborgrc.remotecontrol.PiCameraStream;
//import com.xd.racast.networkservice.WiFiDirectManager;

public class RCDiddyBorgActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //WiFiDirectManager.newInstance(this).initializeService();

        Button btnCameraStream = findViewById(R.id.btnPICameraStream);
        btnCameraStream.setOnClickListener(this);

        Button btnRemoteControl = findViewById(R.id.btnRemoteControl);
        btnRemoteControl.setOnClickListener(this);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        //WiFiDirectManager.getInstance().registerReceiver();
        //WiFiDirectManager.getInstance().createGroup();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        //WiFiDirectManager.getInstance().unregisterReceiver();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPICameraStream:
            {
                Intent intent = new Intent(RCDiddyBorgActivity.this, PiCameraStream.class);
                startActivity(intent);
            }break;
            case R.id.btnRemoteControl:
            {
                //Intent intent = new Intent(RCDiddyBorgActivity.this, Other.class);
                //startActivity(intent);
            }break;
        }
    }
}

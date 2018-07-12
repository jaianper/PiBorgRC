package com.littlebandit.piborgrc.wifip2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * @author J414NP3R
 * @version 1.0
 */

public class WiFiDirectManager implements ConnectionInfoListener, GroupInfoListener
{
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private static WiFiDirectManager ourInstance;
    private Activity mActivity;

    private final static String TAG = "WiFiDirectManager";

    private final static int SERVER_PORT = 9001;
    private static final String SERVICE_INSTANCE = "_wifip2pdiddyborg";
    private static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static WiFiDirectManager newInstance(Activity activity)
    {
        ourInstance = new WiFiDirectManager(activity);
        return ourInstance;
    }

    public static WiFiDirectManager getInstance()
    {
        return ourInstance;
    }

    public WiFiDirectManager(Activity activity)
    {
        mActivity = activity;
    }

    public void initializeService()
    {
        mManager = (WifiP2pManager) mActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mActivity, mActivity.getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, mActivity);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void startLocalService() {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                Log.d(TAG, "Command successful!");
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.e(TAG, "Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY");
            }
        });
    }

    public void createGroup()
    {
        //final GroupInfoListener giListener = this;
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Device is ready to accept incoming connections from peers.
                Log.d(TAG, "Device is ready to accept incoming connections from peers.");

                //mManager.requestGroupInfo(mChannel, giListener);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(mActivity, "P2P group creation failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Intent registerReceiver()
    {
        return mActivity.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unregisterReceiver()
    {
        mActivity.unregisterReceiver(mReceiver);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info)
    {
        Log.d(TAG, "****** Connection Info ******");
        Log.d(TAG, info.toString());

        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */
        if (info.isGroupOwner)
        {
            mManager.requestGroupInfo(mChannel, this);
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group)
    {
        Log.d(TAG, "****** Group Info ******");
        Log.d(TAG, group.toString());
        Log.d(TAG, "Interface: " + group.getInterface());
        Log.d(TAG, "NetworkName: " + group.getNetworkName());
        Log.d(TAG, "Passphrase: " + group.getPassphrase());

        startLocalService();
    }
}

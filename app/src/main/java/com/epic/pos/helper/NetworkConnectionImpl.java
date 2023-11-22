package com.epic.pos.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkConnectionImpl implements NetworkConnection{
    private Context context;

    public NetworkConnectionImpl(Context context) {
        this.context = context;
    }

    /**
     * Check the network connection of the device.
     *
     * @param context current context value
     * @return true if network connection available false otherwise
     */
    @Override
    public boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}

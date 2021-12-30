package com.purnendu.yourtask;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    NetworkChangeCallBack changeCallBack;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!(NetworkUtility.getConnectivityStatus(context)))
            changeCallBack.whenNotConnected();

        if (NetworkUtility.getConnectivityStatus(context))
            changeCallBack.whenConnected();

    }

    public void setCallBack(NetworkChangeCallBack changeCallBack) {
        this.changeCallBack = changeCallBack;
    }
}
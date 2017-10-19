package com.plsr.sunil.customnotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by sunil on 8/19/17.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    static MainActivity m = new MainActivity();

    @Override
    public void onReceive(Context context, Intent intent) {

//        Intent service = new Intent(context, MsgPushService.class);
//        context.startService(service);
        Log.d("CustomNotify", "Boot Completion Recieved");



        LocalBroadcastManager.getInstance(context).registerReceiver(m.onNotice, new IntentFilter("Msg"));
//        registerFlag = true;
    }

}
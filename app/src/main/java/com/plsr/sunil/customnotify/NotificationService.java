package com.plsr.sunil.customnotify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;

import com.plsr.sunil.customnotify.Model.AppDetails;
import com.plsr.sunil.customnotify.Model.CONST;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;

/**
 * Created by sunil on 6/23/17.
 */

public class NotificationService  extends NotificationListenerService {

    Context context;
    TextToSpeech jarvis;
    Boolean sendBroadcast = true;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        sendBroadcast = true;


        //check if the notification is ongi=oing notification
        if (!sbn.isOngoing() && sbn.isClearable()){
            //if it is not an ongoing notification and it is clearable then only doi the code

            String pack = sbn.getPackageName();

/*
        String ticker = sbn.getNotification().tickerText.toString();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();

            //Retriving Application Deatils object from the package name
            AppDetails app = getAppDetailsFromPackage(pack);

            //speaking only if we get appName from the realm db
            customNotifyApp(app);
*/

            if (sendBroadcast)
                sendBroadcast = checkWiFi();
            if (sendBroadcast)
                sendBroadcast = checkTime();


            if (sendBroadcast) {
                Intent msgrcv = new Intent("Msg");
                msgrcv.putExtra("package", pack);
                LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
            }

        }




    }


    private Boolean checkWiFi() {


        //we should send boolean value which will tell to send the broadcast or not
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean switchWifi = mPrefs.getBoolean(CONST.WiFiSwitch, false);
        //if the switch is not true then the notification should work irrespective of current wifi
        if (switchWifi == false)
            return true;




        Gson gson = new Gson();
        String selectedWifiNetworks = mPrefs.getString(CONST.WiFiPrefKey, "");

        //if there is no wifi preference, then CustomNotify should speak for every notification irrespective of wifi network
        if (selectedWifiNetworks == "")
            return true;

        ArrayList<String> checkedWiFiSSIDs = gson.fromJson(selectedWifiNetworks, ArrayList.class);
        if (checkedWiFiSSIDs.size() == 0)
            return true;
        else{
            WifiManager wifiManager;
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo currentlyConnectedTo = wifiManager.getConnectionInfo();


            if (checkedWiFiSSIDs.contains(currentlyConnectedTo.getSSID()))
                return true;
            else
                return false;
        }
    }

    private Boolean checkTime() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean switchTime = mPrefs.getBoolean(CONST.timeSwitch, false);
        //if the switch is not true then the notification should work irrespective of current time
        if (switchTime == false)
            return true;


        int startHour, startMinute, endHour, endMinute;
        Calendar rightNow = Calendar.getInstance();
        int currentHour, currentMinute;
        currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
        currentMinute = rightNow.get(Calendar.MINUTE);

        startHour = mPrefs.getInt(CONST.timeStartHourPK, 99);
        startMinute = mPrefs.getInt(CONST.timeStartMinutePK, 99);
        endHour = mPrefs.getInt(CONST.timeEndHourPK, 99);
        endMinute = mPrefs.getInt(CONST.timeEndMinutePK, 99);

        if (startHour == 99 || startMinute == 99 || endHour == 99 || endMinute == 99) {
            //if the time is not set, then it should work always
            return true;
        } else if ((startHour < currentHour) && (currentHour < endHour)) {
            return true;
        } else if (startHour == currentHour) {
            if (currentMinute >= startMinute)
                return true;
            else
                return false;
        } else if (endHour == currentHour) {
            if (currentMinute <= endMinute)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }


    private AppDetails getAppDetailsFromPackage(String pack) {
        Realm realm;
        realm = Realm.getDefaultInstance();
        AppDetails realmResult = realm.where(AppDetails.class).equalTo("packageName",pack).equalTo("doAct",true).findFirst();
        return realmResult;
    }

    private void customNotifyApp(AppDetails app){

        if (app!=null){
            //Setting up TTS
            jarvis = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        jarvis.setLanguage(Locale.US);
                    }
                }
            });

            if (app.getDoName())
                jarvis.speak(app.getAppName(), TextToSpeech.QUEUE_FLUSH, null, "jarvIsSpeaKING");

            if (app.getDoVibrate()){
                Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vibrator.vibrate(500);
            }

        }
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
}


package com.plsr.sunil.customnotify;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.plsr.sunil.customnotify.Model.CONST;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppSettings extends AppCompatActivity {

    CardView wifiCardView, timeCardView;
    TextView wifiNetworks, timetext;
    Switch wifiSwitch, timeSwitch;
    ArrayList<String> checkedWiFiSSIDs;
    List<CharSequence> ssidList;
    List<WifiConfiguration> wifiConfigurationList;
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        wifiCardView = (CardView) findViewById(R.id.wifiCardView);
        wifiNetworks = (TextView) findViewById(R.id.wifiFooter);
        wifiSwitch = (Switch) findViewById(R.id.wifiSwitch);
        timeCardView = (CardView) findViewById(R.id.timeCardView);
        timetext = (TextView) findViewById(R.id.timeFooter);
        timeSwitch = (Switch) findViewById(R.id.timeSwitch);

        wifiConfigurationList = getAvailableWifiConfigurations();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //setting WiFi Footer
        String checkedWiFiNetworks = retriveWiFiFromPreference();
        if (checkedWiFiNetworks == null) {
            wifiNetworks.setText("Currently no WiFi network is selected so, Custom Notify will always work.");
        } else {
            wifiNetworks.setText("Networks: "+checkedWiFiNetworks);
        }

        //setting time footer
        Time time = retriveTimeFromPreference();
        if (time == null) {
            timetext.setText("Time: Not Set");
        } else {
            displayTime(time);
        }


        //setting wifi and time switchs
        Boolean wiFiChecked = retriveBoolFromPreference(CONST.WiFiSwitch);
        wifiSwitch.setChecked(wiFiChecked);
        if (wiFiChecked == false) {
            wifiNetworks.setEnabled(false);
        } else {
            wifiNetworks.setEnabled(true);
        }

        Boolean timeChecked = retriveBoolFromPreference(CONST.timeSwitch);
        timeSwitch.setChecked(timeChecked);



        wifiCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled() == false) {
                    handleWiFiTurnedOff();
                    wifiConfigurationList = getAvailableWifiConfigurations();
                }
                else {
                    wifiConfigurationList = getAvailableWifiConfigurations();
                    displayAlertDialog(wifiConfigurationList);
                }
            }
        });

        wifiSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean state = wifiSwitch.isChecked();

                wifiNetworks.setEnabled(state);

                saveToPreference(CONST.WiFiSwitch, state);

                if (state == true){
                    if (wifiManager.isWifiEnabled() == false) {
                        handleWiFiTurnedOff();
                        wifiConfigurationList = getAvailableWifiConfigurations();
                    }
                    else {
                        wifiConfigurationList = getAvailableWifiConfigurations();
                        displayAlertDialog(wifiConfigurationList);
                    }
                } else {
                    //if wifi switch is turned off
                }


            }
        });


        timeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTime();
            }
        });

        timeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean state = timeSwitch.isChecked();

                if(state == true) {
                    Time t = retriveTimeFromPreference();
                    if (t == null) {
                        //time is not set, first time should be set
                        selectTime();
                    }
                }

                saveToPreference(CONST.timeSwitch, state);
            }
        });


        //timeCardView.setVisibility(View.INVISIBLE);
    }




    private void handleWiFiTurnedOff() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        builder.setTitle("Switch On WiFi")
                .setMessage("To use this feature WiFi should be On, do you want to turn on WiFi?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // should turn on WiFi
                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifiManager.setWifiEnabled(true);


                        //waiting for 3 seconds to turn on wifi: displaying progress dialog for 3 seconds
                        final ProgressDialog progressDialog = ProgressDialog.show(AppSettings.this, "", "Turning On WiFi...",
                                true);
                        progressDialog.show();


                        new CountDownTimer(3000, 1000) {

                            public void onTick(long millisUntilFinished) {

                            }

                            public void onFinish() {
                                progressDialog.dismiss();
                            }
                        }.start();




                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }


    public List<WifiConfiguration> getAvailableWifiConfigurations() {
        WifiManager wifiManager;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();

//        Log.d("WiFi", wifiConfigurationList.toString());
        if (wifiConfigurationList == null){
            //if the WiFi is turned off
            return null;
        }

        //To sort known networks
        Collections.sort(wifiConfigurationList, new WifiComparator());
        //to get the currently connected network at the top
        WifiInfo currentlyConnectedTo = wifiManager.getConnectionInfo();
        int connectedIndex = 999;
        for (WifiConfiguration wifiConfiguration: wifiConfigurationList){
            if (wifiConfiguration.SSID.equals(currentlyConnectedTo.getSSID())){
                connectedIndex = wifiConfigurationList.indexOf(wifiConfiguration);
            }
        }
        if (connectedIndex != 999){
            //if there is any wifi currently connected to, then I am moving it to the top of the list
            WifiConfiguration currentlyConnectedWifi = wifiConfigurationList.get(connectedIndex);
            wifiConfigurationList.remove(currentlyConnectedWifi);
            wifiConfigurationList.add(0, currentlyConnectedWifi);
        }

//        printitng all wifi ssid's
//        for (WifiConfiguration wifiConfiguration: wifiConfigurationList){
//            Log.d("WiFi", wifiConfiguration.SSID);
//        }


        return wifiConfigurationList;
    }

    public static class WifiComparator implements Comparator<WifiConfiguration> {

        @Override
        public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
            return lhs.SSID.compareToIgnoreCase(rhs.SSID);
        }
    }


    public void displayAlertDialog(final List<WifiConfiguration> wifiConfigurationList) {

        checkedWiFiSSIDs = new ArrayList<String>();


        ssidList = new ArrayList<CharSequence>();

        for (WifiConfiguration wifiConfiguration: wifiConfigurationList){
            ssidList.add(wifiConfiguration.SSID);
        }

        final CharSequence[] dialogList=  ssidList.toArray(new CharSequence[ssidList.size()]);
        final AlertDialog.Builder builderDialog = new AlertDialog.Builder(AppSettings.this);
        builderDialog.setTitle("Select Item");
        int count = dialogList.length;
        boolean[] is_checked = populateCheckedWiFis(count); // set is_checked boolean false;

        //modifying is_checked so that if there is already any wifi in sharedPref which is check, it will show as check in alert dialog box also




        // Creating multiple selection by using setMutliChoiceItem method
        builderDialog.setMultiChoiceItems(dialogList, is_checked,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton, boolean isChecked) {
                    }
                });
        builderDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView list = ((AlertDialog) dialog).getListView();
                        // make selected item in the comma seprated string
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < list.getCount(); i++) {
                            boolean checked = list.isItemChecked(i);
                            if (checked) {

                                //checked wifi will come here
                                checkedWiFiSSIDs.add((String) ssidList.get(i));

                            }
                        }
                        /*Check string builder is empty or not. If string builder is not empty.<br />
                          It will display on the screen.
                         */
                        //setting display
                        if (checkedWiFiSSIDs.size() == 0){
                            //if there are no checked wifi ssid's
                            wifiNetworks.setText("");
                            wifiNetworks.setVisibility(View.GONE);

                            saveToPreference("WiFi", checkedWiFiSSIDs);
                            wifiNetworks.setText("Currently no WiFi network is selected so, Custom Notify will always work.");

                        } else {
                            //if there is atleast 1 checked wifi ssid
                            wifiNetworks.setVisibility(View.VISIBLE);
                            StringBuilder sb = new StringBuilder();
                            for (String s: checkedWiFiSSIDs) {
                                sb.append(s);
                                if (checkedWiFiSSIDs.get(checkedWiFiSSIDs.size() - 1) != s)
                                    sb.append(", ");
                            }

                            wifiNetworks.setText("Networks: " + sb.toString());
                            Log.d("CustomNotify", String.valueOf(checkedWiFiSSIDs));

                            //saving wifi ssid's to shared preference
                            saveToPreference(CONST.WiFiPrefKey, checkedWiFiSSIDs);

                        }

                        //saving ssdi to shared preference
//                        SharedPreferences.Editor editor = getSharedPreferences("sharedAppPref", MODE_PRIVATE).edit();
//                        editor.putString("WiFiSSIDs", String.valueOf(checkedWiFiSSIDs));
//                        editor.apply();

                    }
                });
        builderDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do stuff if cancle is pressed
                    }
                });
        AlertDialog alert = builderDialog.create();
        alert.show();
    }

    private boolean[] populateCheckedWiFis(int count) {
        boolean[] is_checked = new boolean[count];

        //retriving from shared preference
        SharedPreferences mPrefs = this.getSharedPreferences(CONST.prefName, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(CONST.WiFiPrefKey, "");

        if (json == ""){
            return is_checked;
        } else {
            //if there is any already checked wifi networks
            ArrayList<String> checkedWiFiSSIDs = gson.fromJson(json, ArrayList.class);
            if (checkedWiFiSSIDs.size() == 0)
                return is_checked;
            else{
                for (int i = 0; i < checkedWiFiSSIDs.size(); i++) {

                    if (ssidList.contains(checkedWiFiSSIDs.get(i))) {
                         is_checked[ssidList.indexOf(checkedWiFiSSIDs.get(i))] = true;
                    }
                }

                return is_checked;
            }
        }

    }



    private String retriveWiFiFromPreference() {
        //we should send boolean value which will tell to send the broadcast or not
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppSettings.this);
        Gson gson = new Gson();
        String json = mPrefs.getString(CONST.WiFiPrefKey, "");

        //if there is no wifi preference, then CustomNotify should speak for every notification irrespective of wifi network
        if (json == "")
            return null;

        ArrayList<String> checkedWiFiSSIDss = gson.fromJson(json, ArrayList.class);

        if (checkedWiFiSSIDss.size() == 0)
            return null;
        else{
            StringBuilder sb = new StringBuilder();
            for (String SSID: checkedWiFiSSIDss) {
                sb.append(SSID);
            }
            return sb.toString();
        }

    }











    //TIME CODE

    private Time retriveTimeFromPreference() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppSettings.this);

        int startHour, startMinute, endHour, endMinute;

        startHour = mPrefs.getInt(CONST.timeStartHourPK, 99);
        startMinute = mPrefs.getInt(CONST.timeStartMinutePK, 99);
        endHour = mPrefs.getInt(CONST.timeEndHourPK, 99);
        endMinute = mPrefs.getInt(CONST.timeEndMinutePK, 99);

        if (startHour == 99 || startMinute == 99 || endHour == 99 || endMinute == 99) {
            return null;
        } else {
            Time time = new Time(startHour, endHour, startMinute, endMinute);
            return time;
        }
    }

    private Boolean retriveBoolFromPreference(String key) {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppSettings.this);
        Boolean ans = mPrefs.getBoolean(key, false);
        Log.d(CONST.commonTag, "Key: " + key + "  Value: " + ans);
        return ans;
    }

    private void selectTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        final int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(AppSettings.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(final TimePicker timePicker, int selectedHour, int selectedMinute) {




                final int startHour, startMinute;
                startHour = selectedHour;
                startMinute = selectedMinute;

                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(AppSettings.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Toast.makeText(getBaseContext(),startHour + " : " +startMinute + " To "+hourOfDay + " : " +minute, Toast.LENGTH_SHORT).show();
                        //setTimeSchedule(startHour,startMinute, hourOfDay, minute);
                        Time t = new Time(startHour, hourOfDay, startMinute, minute);
                        saveTimeToPreference(t);
                        displayTime(t);


                    }
                }, hour, minute, false);
                timePickerDialog.setTitle("Select End Time");
                timePickerDialog.setCancelable(false);
                timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Time t = retriveTimeFromPreference();
                        if (t == null) {
                            timeSwitch.setChecked(false);
                            saveToPreference(CONST.timeSwitch, false);
                        }
                    }
                });
                timePickerDialog.show();




            }






        }, hour, minute, false);
        mTimePicker.setTitle("Select Start Time");
        mTimePicker.setCancelable(false);
        mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Time t = retriveTimeFromPreference();
                if (t == null) {
                    timeSwitch.setChecked(false);
                    saveToPreference(CONST.timeSwitch, false);
                }

                Log.d(CONST.commonTag, "TimePicker Cancelled.");
            }
        });
        mTimePicker.show();
    }

    private void saveTimeToPreference(Time t) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppSettings.this);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putInt(CONST.timeStartHourPK, t.startHour);
        prefsEditor.putInt(CONST.timeStartMinutePK, t.startMinute);
        prefsEditor.putInt(CONST.timeEndHourPK, t.endHour);
        prefsEditor.putInt(CONST.timeEndMinutePK, t.endMinute);
        prefsEditor.commit();

    }

    private void saveToPreference(String key, Boolean value) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppSettings.this);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putBoolean(key, value);
        Log.d(CONST.commonTag, "Saving to preference: Key: " + key + "  Value: "+ value);
        prefsEditor.commit();
    }

    private void saveToPreference(String key, ArrayList<String> checkedWiFiSSIDs) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppSettings.this);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(checkedWiFiSSIDs); // myObject - instance of MyObject
        prefsEditor.putString(key, json);
        prefsEditor.commit();
    }

    private void displayTime(Time time) {
        //converting 24hr format to 12 hour format
        String startMeridian, endMeredian;
        if (time.startHour >= 12) {
            startMeridian = "PM";
            if (time.startHour > 12)
                time.startHour = time.startHour - 12;
        }
        else {
            startMeridian = "AM";
            if (time.startHour == 0)
                time.startHour = 12;
        }

        if (time.endHour >= 12) {
            endMeredian = "PM";
            if (time.endHour > 12)
                time.endHour = time.endHour - 12;
        }
        else {
            endMeredian = "AM";
            if (time.endHour == 0)
                time.endHour = 12;
        }

        timetext.setText("Time: From " + time.startHour + ":" + time.startMinute + startMeridian +" to " + time.endHour + ":" + time.endMinute + endMeredian);

    }




}

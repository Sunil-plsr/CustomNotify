package com.plsr.sunil.customnotify;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.plsr.sunil.customnotify.Model.AppDetails;
import com.plsr.sunil.customnotify.Model.CONST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    ArrayList<AppDetails> apps;
    public static ArrayList<ApplicationInfo> appsInfo = new ArrayList<>();
    ListView listView;
    private Realm realm;
    ProgressDialog pd;
    AlertDialog.Builder builder;
    Boolean isAlertDialogVisible = false;
    static TextToSpeech jarvis;
    Integer changedAppIndex;
    AppsAdapter adapter;
    static Context previousContext = null;
    private static final int REQUEST_CODE_INTRO = 555;
    Boolean firstUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("CustomNotify", "onCreate called");

        //if this is the first time app has launched
        firstUse = retriveBoolFromPreference(CONST.firstUseKey);
        if (firstUse == true) {
            Intent i = new Intent(MainActivity.this, MainIntroActivity.class);

            startActivityForResult(i, REQUEST_CODE_INTRO);
        }


        apps = new ArrayList<>();


        setProgressDialog();
        generateListView();
        removeProgressDialog();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, AppOptions.class);
                i.putExtra("app", apps.get(position).getPackageName());
                changedAppIndex = position;

                View appIconView = findViewById(R.id.appIcon);


                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(MainActivity.this, ((ViewGroup)view).getChildAt(0), "profile");
                startActivity(i, options.toBundle());
            }
        });


        //code for alert dialog
        builder = new AlertDialog.Builder(MainActivity.this);
        //check if the app has notification ascess
        //If first use is true then the user will be directed to NotificationListner page, no need to display dialog
        if (firstUse == false)
            checkNotificationAccess();


        //Notification Code
        setupNotificationCode();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                // Finished the intro
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                //if the user has seen the tutorial then it should not be displayed again so saving to preference
                saveToPreference(CONST.firstUseKey, false);
            } else {
                // Cancelled the intro. You can then e.g. finish this activity too.
                finish();
            }
        }
    }


    //Notification related broadcast reciever which will be called, when a new notification arrives
    public static BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");


            Log.d("CustomNotify", "Broadcast Recieved.");

            AppDetails app = getAppDetailsFromPackage(pack);

            if (app!=null){


                if (jarvis == null){
                    //Setting up TTS
                    jarvis=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                jarvis.setLanguage(Locale.US);
                            }
                        }
                    });

                }


                if (app.getDoName())

                    jarvis.speak(app.getAppName(), TextToSpeech.QUEUE_FLUSH, null, "jarvIsSpeaKING");

                if (app.getDoVibrate()){
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    int vibrateType = app.getVibrateType();
                    if (vibrateType == 1)
                        vibrator.vibrate(500);
                    else if (vibrateType == 2){
                        long pattern[] = {0, 500, 500, 2000};
                        vibrator.vibrate(pattern,-1);
                    } else {
                        long pattern[] = {0, 1000, 500, 1000};
                        vibrator.vibrate(pattern,-1);
                    }
                }

            }

            //because jarvis is running contiously in backgroung and consuming resources.
            //jarvis.shutdown();



        }
    };




    private void setupNotificationCode() {

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));


        jarvis=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    jarvis.setLanguage(Locale.US);
                }
            }
        });
    }


    private static AppDetails getAppDetailsFromPackage(String pack) {
        Realm realm;
        realm = Realm.getDefaultInstance();
        AppDetails realmResult = realm.where(AppDetails.class).equalTo("packageName",pack).equalTo("doAct",true).findFirst();
        return realmResult;
    }


    //Method that will check if a particular app has a launch intent and return only those apps
    private ArrayList<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (null != this.getPackageManager().getLaunchIntentForPackage(info.packageName)) {
                    apps.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return apps;
    }

    private void setProgressDialog(){
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Please wait...");
        //pd.setTitle("Loading:");
        //pd.setCancelable(false);
        pd.show();
    }


    private void removeProgressDialog(){
        if (pd != null)
            if (pd.isShowing())
                pd.dismiss();
    }

    private void saveToRealmDB(final AppDetails a){
/*

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                AppDetails newApplication = bgRealm.createObject(AppDetails.class);
                newApplication.setPackageName(a.getPackageName());
                newApplication.setAppName(a.getAppName());
                newApplication.setDoAct(false);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // Transaction was a success.
                Log.d("Realm", "Created App "+a.getAppName()+" in realm");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Transaction failed and was automatically canceled.
                Log.d("Realm", "Failed Creating app due to error:\n"+error.getMessage());
            }
        });
*/


        realm.beginTransaction();
/*
        AppDetails app = realm.createObject(AppDetails.class);
        app.setPackageName(a.getPackageName());
        app.setAppName(a.getAppName());
        app.setDoAct(a.getDoAct());
        app.setDoName(a.getDoName());
        app.setDoVibrate(a.getDoVibrate());
        app.setVibrateType(a.getVibrateType());
*/
        realm.copyToRealm(a);
        realm.commitTransaction();

    }


    private void checkNotificationAccess(){
        String notificationListenerString = Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners");
        //Check notifications access permission
        if (notificationListenerString == null || !notificationListenerString.contains(getPackageName()))
        {
            //The notification access has not acquired yet!
            //code for alert dialog to ask for permission




            builder.setTitle("Activate Custom Notify")
                    .setMessage("Please grant Notification Access to Custom Notify")
                    .setPositiveButton("Grant Access", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Launching settings page where user can give notification access
                            isAlertDialogVisible = false;
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            isAlertDialogVisible = true;
        }else{
            //Your application has access to the notifications
        }
    }



    private void generateListView(){
        //Retriving apps already in realmDB
        realm = Realm.getDefaultInstance();
        RealmResults<AppDetails> realmResults = realm.where(AppDetails.class).findAll();

        //If database have already a list of apps
        if (/*realmResults != null && realmResults.size()>0*/ true){

            //storing package names of the realm apps
            ArrayList<String> packageArray = new ArrayList<>();
            for (AppDetails realmResult : realmResults)
                packageArray.add(realmResult.getPackageName());

            //Collecting data about the currently installed apps
            appsInfo = checkForLaunchIntent(this.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA));
            //copying from appsInfo to AppDetails
            for (ApplicationInfo packageInfo : appsInfo) {

                AppDetails appDetails = new AppDetails();

                if (packageArray.contains(packageInfo.packageName)){
                    //if the app is already in the realm databse
                    apps.add(realm.where(AppDetails.class).contains("packageName", packageInfo.packageName).findFirst());
                    Log.d("realm",realm.where(AppDetails.class).contains("packageName", packageInfo.packageName).findFirst().getAppName());

                } else {
                    //if it is a newly installed app which is not in the database
                    appDetails.setPackageName(packageInfo.packageName);
                    appDetails.setAppName(packageInfo.loadLabel(getPackageManager()).toString());
                    appDetails.setDoAct(false);
                    saveToRealmDB(appDetails);
                    apps.add(appDetails);
                }
            }
        } else {

            //Collecting data about the installed apps
            appsInfo = checkForLaunchIntent(this.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA));
            //copying from appsInfo to AppDetails
            for (ApplicationInfo packageInfo : appsInfo) {
                AppDetails appDetails = new AppDetails();
                appDetails.setPackageName(packageInfo.packageName);
                appDetails.setAppName(packageInfo.loadLabel(getPackageManager()).toString());
                appDetails.setDoAct(false);
                apps.add(appDetails);
            }

        }

        //setting up listview
        listView = (ListView) findViewById(R.id.lvApps);
        Collections.sort(apps, new Comparator<AppDetails>() {
            @Override
            public int compare(AppDetails o1, AppDetails o2) {

                return o1.getAppName().compareTo(o2.getAppName());
            }
        });
//        apps.sort(new Comparator<AppDetails>() {
//            @Override
//            public int compare(AppDetails o1, AppDetails o2) {
//
//                return o1.getAppName().compareTo(o2.getAppName());
//            }
//        });
        adapter= new AppsAdapter(this, R.layout.app_list_row,apps);
        listView.setAdapter(adapter);
    }


    private void updateAppsData(Integer changedAppIndex, AppDetails newValue){
        realm.beginTransaction();
        apps.get(changedAppIndex).setVibrateType(newValue.getVibrateType());
        apps.get(changedAppIndex).setDoAct(newValue.getDoAct());
        apps.get(changedAppIndex).setDoVibrate(newValue.getDoAct());
        apps.get(changedAppIndex).setDoName(newValue.getDoName());
        realm.commitTransaction();

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        Log.d("CustomNotify", "dialog created by onres");
        if (isAlertDialogVisible == false && firstUse == false)
            checkNotificationAccess();
        Log.d("CustomNotify", "onResume called");


        if (changedAppIndex != null){
            //code to update ListView
            Log.d("CustomNotify", "changed index");
            //notWorking
//            listView.setItemChecked(changedAppIndex, true/*apps.get(changedAppIndex).getDoAct()*/);

            AppDetails changedApp = apps.get(changedAppIndex);
            AppDetails newValue = realm.where(AppDetails.class).equalTo("packageName",changedApp.getPackageName()).findFirst();
            updateAppsData(changedAppIndex, newValue);


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("CustomNotify", "onPause called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed())
            realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater= getMenuInflater();
        menuInflater.inflate(R.menu.men,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.appSettings){
            Intent i = new Intent(MainActivity.this, AppSettings.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }



    private Boolean retriveBoolFromPreference(String key) {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Boolean ans = mPrefs.getBoolean(key, true);
        Log.d(CONST.commonTag, "Key: " + key + "  Value: " + ans);
        return ans;
    }

    private void saveToPreference(String key, Boolean value) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putBoolean(key, value);
        Log.d(CONST.commonTag, "Saving to preference: Key: " + key + "  Value: "+ value);
        prefsEditor.commit();
    }

}

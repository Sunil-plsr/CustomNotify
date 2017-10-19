package com.plsr.sunil.customnotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.plsr.sunil.customnotify.Model.AppDetails;
import com.plsr.sunil.customnotify.Model.CONST;

import io.realm.Realm;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class AppOptions extends AppCompatActivity {

    ImageView appIcon;
    TextView appTVName, appTVSpeak, appTVVibrate;
    CheckBox appDoAct, appDoName, appDoVibrate;
    AppDetails application;
    Realm realm;
    RadioGroup vibrationRadioGroup;
    RadioButton vibration1, vibration2, vibration3;

    public final String SHOWCASE_ID = "600";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_options);

        realm = Realm.getDefaultInstance();

        application = getAppDetailsFromPackage((String) getIntent().getSerializableExtra("app"));
        appIcon = (ImageView) findViewById(R.id.aoIcon);
        appTVName = (TextView) findViewById(R.id.aoName);
        appTVSpeak = (TextView) findViewById(R.id.aoTVSpeakName);
        appTVVibrate = (TextView) findViewById(R.id.aoTVVibrate);
        appDoAct = (CheckBox) findViewById(R.id.aoDoAct);
        appDoName = (CheckBox) findViewById(R.id.aoCBName);
        appDoVibrate = (CheckBox) findViewById(R.id.aoCBVibrate);
        vibrationRadioGroup = (RadioGroup) findViewById(R.id.aoRGvibrate);
        vibration1 = (RadioButton) findViewById(R.id.aoRBV1);
        vibration2 = (RadioButton) findViewById(R.id.aoRBV2);
        vibration3 = (RadioButton) findViewById(R.id.aoRBv3);



        setupUI();


        checkForFirstLaunch();

        setOnClickListners();
    }

    private void checkForFirstLaunch() {
        Boolean firstUse = retriveBoolFromPreference(CONST.firstUseAppOptions);

        if (firstUse == true) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(250);


            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

            sequence.setConfig(config);

            sequence.addSequenceItem(findViewById(R.id.aoCBName),
                    "Use this CheckBox to toggle Speaking Name of the App when a notification arrives", "Got it!");

            sequence.addSequenceItem(findViewById(R.id.aoCBVibrate),
                    "Use this CheckBox to toggle vibrating with different pattern when a notification arrives", "GOT IT");


            sequence.start();

            saveToPreference(CONST.firstUseAppOptions, false);
        }

    }

    private void setupUI() {
        try{
            appIcon.setImageDrawable(AppOptions.this.getPackageManager().getApplicationIcon(application.getPackageName()));
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        appTVName.setText(application.getAppName());
        appDoAct.setChecked(application.getDoAct());

        if (application.getDoAct()){
            //if the application is set to do act, then setting UI according to the application data
            appDoName.setChecked(application.getDoName());
            appDoVibrate.setChecked(application.getDoVibrate());
            if (application.getDoVibrate()){
                switch (application.getVibrateType()){
                    case 1:
                        vibration1.setChecked(true);
                        break;
                    case 2:
                        vibration2.setChecked(true);
                        break;
                    case 3:
                        vibration3.setChecked(true);
                        break;
                }
            } else {
                //DoVibrate is not checked case
//                appTVVibrate.setEnabled(false);
//                vibration1.setEnabled(false);
//                vibration2.setEnabled(false);
//                vibration3.setEnabled(false);

            }
        } else {
            //disabling app options if DoAct is not set
//            appDoName.setEnabled(false);
//            appDoVibrate.setEnabled(false);
//            appTVSpeak.setEnabled(false);
//            appTVVibrate.setEnabled(false);
//            vibration1.setEnabled(false);
//            vibration2.setEnabled(false);
//            vibration3.setEnabled(false);

        }
    }


    private void setOnClickListners(){

        appDoAct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (appDoAct.isChecked() == false) {
                    appDoName.setChecked(false);
                    appDoVibrate.setChecked(false);
                }
//                //appDoVibrate.setEnabled(isChecked);
//                //appDoName.setEnabled(isChecked);
//                vibrationRadioGroup.setEnabled(isChecked);
//                vibration1.setEnabled(isChecked);
//                vibration2.setEnabled(isChecked);
//                vibration3.setEnabled(isChecked);

                realm.beginTransaction();
                application.setDoAct(appDoAct.isChecked());
                realm.commitTransaction();
            }
        });

        appDoVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (appDoVibrate.isChecked())
                    appDoAct.setChecked(true);




                int vibrateType = getVibrateType();

                realm.beginTransaction();
                application.setVibrateType(vibrateType);
                application.setDoVibrate(appDoVibrate.isChecked());
                realm.commitTransaction();
            }
        });

        vibrationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                int vibrateType = getVibrateType();
                Vibrator vibrator = (Vibrator) AppOptions.this.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrateType == 1)
                    vibrator.vibrate(500);
                else if (vibrateType == 2){
                    long pattern[] = {0, 500, 500, 2000};
                    vibrator.vibrate(pattern,-1);
                } else {
                    long pattern[] = {0, 1000, 500, 1000};
                    vibrator.vibrate(pattern,-1);
                }

                realm.beginTransaction();
                application.setVibrateType(vibrateType);
                realm.commitTransaction();
            }
        });

        appDoName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (appDoName.isChecked())
                    appDoAct.setChecked(true);


                realm.beginTransaction();
                application.setDoName(appDoName.isChecked());
                realm.commitTransaction();
            }
        });

        findViewById(R.id.aoBSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saving to realm




//                realm.executeTransaction(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        application.setDoName(appDoName.isChecked());
//                        application.setDoVibrate(appDoVibrate.isChecked());
//                        application.setDoAct(appDoAct.isChecked());
//                        application.setVibrateType(getVibrateType());
//                        Log.d(CONST.commonTag, "Saving to Realm "+application.toString());
//                        realm.copyToRealmOrUpdate(application);
//                    }
//                });



                realm.beginTransaction();
                application.setDoName(appDoName.isChecked());
                application.setDoVibrate(appDoVibrate.isChecked());
                application.setDoAct(appDoAct.isChecked());
                application.setVibrateType(getVibrateType());
                Log.d(CONST.commonTag, "Saving to Realm "+application.toString());
                realm.commitTransaction();

                realm.close();

                supportFinishAfterTransition();

            }
        });




    }

    private int getVibrateType(){

        switch (vibrationRadioGroup.getCheckedRadioButtonId()){
            case R.id.aoRBV1:
                return 1;
            case R.id.aoRBV2:
                return 2;
            case  R.id.aoRBv3:
                return 3;
        }

        return 1;
    }

    private AppDetails getAppDetailsFromPackage(String pack){

        AppDetails realmResult = realm.where(AppDetails.class).equalTo("packageName",pack).findFirst();
        Log.d(CONST.commonTag, "Retrived from Realm:"+realmResult.toString());
        return realmResult;
    }

    private void saveToPreference(String key, Boolean value) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppOptions.this);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putBoolean(key, value);
        Log.d(CONST.commonTag, "Saving to preference: Key: " + key + "  Value: "+ value);
        prefsEditor.commit();
    }

    private Boolean retriveBoolFromPreference(String key) {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(AppOptions.this);
        Boolean ans = mPrefs.getBoolean(key, true);
        Log.d(CONST.commonTag, "Key: " + key + "  Value: " + ans);
        return ans;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
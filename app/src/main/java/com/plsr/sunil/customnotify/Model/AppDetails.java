package com.plsr.sunil.customnotify.Model;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by sunil on 6/21/17.
 */

public class AppDetails extends RealmObject implements Serializable {
    @PrimaryKey
    @Required
    String packageName;

    String appName;
    //    String appIcon;
//    String beforeName, afterName
    Boolean doName, doVibrate, doAct;
    int vibrateType = 1;


    public AppDetails(String packageName, String appName, Boolean doName, Boolean doVibrate,Boolean doAct, int vibrateType) {
        this.packageName = packageName;
        this.appName = appName;
        this.doName = doName;
        this.doVibrate = doVibrate;
        this.doAct = doAct;
        this.vibrateType = vibrateType;
    }

    public AppDetails() {
        this.packageName = "default";
        this.appName = "defaultApp";
        this.doName = false;
        this.doVibrate = false;
        this.doAct = false;
        this.vibrateType = -1;
    }

    public AppDetails(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Boolean getDoName() {
        return doName;
    }

    public void setDoName(Boolean doName) {
        this.doName = doName;
    }

    public Boolean getDoVibrate() {
        return doVibrate;
    }

    public void setDoVibrate(Boolean doVibrate) {
        this.doVibrate = doVibrate;
    }

    public Boolean getDoAct() {
        return doAct;
    }

    public void setDoAct(Boolean doAct) {
        this.doAct = doAct;
    }

    public int getVibrateType() {
        return vibrateType;
    }

    public void setVibrateType(int vibrateType) {
        this.vibrateType = vibrateType;
    }
}
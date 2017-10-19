package com.plsr.sunil.customnotify;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.plsr.sunil.customnotify.Model.AppDetails;

import java.util.List;

import io.realm.Realm;

/**
 * Created by sunil on 6/21/17.
 */

public class AppsAdapter extends ArrayAdapter<AppDetails> {
    List<AppDetails> myData;
    Context myContext;
    int myResource;
    private Realm realm;

    public AppsAdapter(Context context, int resource, List<AppDetails> objects) {
        super(context, resource, objects);
        this.myContext=context;
        this.myData=objects;
        this.myResource=resource;
        realm = Realm.getDefaultInstance();
        // MainActivity activity= myContext;
    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView==null){
            LayoutInflater inflater= (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView= inflater.inflate(myResource,parent,false);
        }

        TextView appName;
        ImageView appIcon;
        CheckBox appChecked;

        appName = (TextView) convertView.findViewById(R.id.appName);
        appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
        appChecked = (CheckBox) convertView.findViewById(R.id.appChecked);


        appName.setText(myData.get(position).getAppName());

         //loading apps icon


        appIcon.setTag(myData.get(position).getPackageName());
        appIcon.setImageResource(R.mipmap.ic_launcher);
        new LoadIcon(appIcon).execute();

/*
        Drawable icon = null;
        try {
            appIcon.setTag(myData.get(position).getPackageName());
            new LoadIcon(appIcon).execute();
            icon = myContext.getPackageManager().getApplicationIcon(myData.get(position).getPackageName() );
            appIcon.setImageDrawable(icon);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
*/
        if (myData.get(position).getDoAct())
            appChecked.setChecked(true);
        else
            appChecked.setChecked(false);


        appChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        myData.get(position).setDoAct(!myData.get(position).getDoAct());
                        myData.get(position).setDoName(myData.get(position).getDoAct());
                        realm.copyToRealmOrUpdate(myData.get(position));
                    }
                });
            }
        });


        return convertView;
    }




    class LoadIcon extends AsyncTask<Object, Void, Drawable> {

        private ImageView imv;
        private String pack;

        public LoadIcon(ImageView imv) {
            this.imv = imv;
            this.pack = imv.getTag().toString();
        }

        @Override
        protected Drawable doInBackground(Object... params) {
            Drawable icon = null;
            try {
                icon = myContext.getPackageManager().getApplicationIcon(pack);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.d("CustomNotify", "Package not found");
            }
            return icon;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (!imv.getTag().toString().equals(pack)) {
               /* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                return;
            }

            if(result != null && imv != null){
                imv.setVisibility(View.VISIBLE);
                imv.setImageDrawable(result);
            }else{
                imv.setVisibility(View.GONE);
            }
        }

    }
}


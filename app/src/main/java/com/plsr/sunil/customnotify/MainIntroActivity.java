package com.plsr.sunil.customnotify;

/**
 * Created by sunil on 9/14/17.
 */

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

/**
 * Created by sunil on 9/10/17.
 */

public class MainIntroActivity extends IntroActivity {
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add slides, edit configuration...

        addSlide(new SimpleSlide.Builder()
                .title("Bored of same notification sound from every app?")
                .description("Use Custom Notify to create unique notification tones for every Application.")
                .image(R.drawable.intropic1)
                .background(R.color.colorIntro1)
                .backgroundDark(R.color.colorIntroDark1)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Choose which apps to enable")
                .description("Click on the CheckBox to handle notifications from that particular app")
                .image(R.drawable.app_intro_1)
                .background(R.color.colorIntro2)
                .backgroundDark(R.color.colorIntroDark2)
                .scrollable(false)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Enable Custom Notify")
                .description("Please give Notification access to custom notify to detect notifications")
                .image(R.drawable.notification)
                .background(R.color.colorIntro3)
                .backgroundDark(R.color.colorIntroDark3)
                .scrollable(false)
                .build());



    }
}
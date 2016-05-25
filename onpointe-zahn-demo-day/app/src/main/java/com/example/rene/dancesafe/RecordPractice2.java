package com.example.rene.dancesafe;

/*
    This class displays the main
    two options the user have:
    - Record New Moves
    - Practice Existing Moves
*/

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class RecordPractice2 extends ActionBarActivity {

    LinearLayout record;       // Used to animate "Record New Moves" button
    LinearLayout practice;     // Used to animate "Practice Existing Moves" buttin
    LinearLayout options_tab;  // Used to animate options tab

    // Animate all LinearLayouts onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_practice2);

        record = (LinearLayout)findViewById(R.id.record_new_moves);
        practice = (LinearLayout)findViewById(R.id.practice_existing_moves);
        options_tab = (LinearLayout)findViewById(R.id.options_tab);

        Animation anim_record = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_top);
        Animation anim_practice = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);
        Animation anim_options = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        record.setAnimation(anim_record);
        practice.setAnimation(anim_practice);
        options_tab.setAnimation(anim_options);

        anim_record.start();
        anim_practice.start();
        anim_options.start();
    }


    // Record new moves
    public void recordMoves(View view) {
        Intent intent = new Intent(this, record.class);
        startActivity(intent);
    }


    // Practice existing moves
    public void practiceMoves(View view) {
        Intent intent = new Intent(this, practice_existing.class);
        startActivity(intent);
    }


    // Navigation buttons
    public void home(View view) {
        Intent intent = new Intent(this, RecordPractice2.class);
        startActivity(intent);
    }

    public void bluetooth(View view) {
        Intent intent = new Intent(this, ListBluetoothDevices.class);
        startActivity(intent);
    }

    public void video_recorded(View view) {
        Intent intent = new Intent(this, column_play_back.class);
        startActivity(intent);
    }

    public void video_practiced(View view) {
        Intent intent = new Intent(this, column_practiced.class);
        startActivity(intent);
    }

//    public void user_account(View view) {
//        Intent intent = new Intent(this, user_account.class);
//        startActivity(intent);
//    }

    public void user_account(View view) {
        Intent intent = new Intent(this, user_account.class);
        startActivity(intent);
    }

}
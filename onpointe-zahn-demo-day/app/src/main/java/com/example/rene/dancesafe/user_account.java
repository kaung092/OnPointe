package com.example.rene.dancesafe;

/*
    This class displays
    the user's account info
    ***TO BE IMPLEMENTED***
*/

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class user_account extends AppCompatActivity {

    LinearLayout options_tab;           // Used to animate options tab

    //TextView mItem = (TextView) findViewById(R.id.item_detail);

    // Fade in the options bar onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        options_tab = (LinearLayout)findViewById(R.id.options_tab);
        Animation anim_options = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        options_tab.setAnimation(anim_options);
        anim_options.start();
    }

/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_user_account, container, false);

        //Your button goes here
        Button btnStart = (Button) rootView.findViewById(R.id.btnTimer);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CountDownTimer(30000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        Toast.makeText(user_account.this, "seconds remaining: " + millisUntilFinished / 1000, Toast.LENGTH_LONG).show();
                    }

                    public void onFinish() {
                        Toast.makeText(user_account.this, "Finished!", Toast.LENGTH_LONG).show();
                    }
                }.start();
            }
        });



        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText("Great");
        }



        return rootView;
    }

    */

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

    public void user_account(View view) {
        Intent intent = new Intent(this, user_account.class);
        startActivity(intent);
    }

}
package com.example.rene.dancesafe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    // Load login page
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final ImageView iv_logo = (ImageView) findViewById(R.id.imageView);
        //final Animation anim_logo = AnimationUtils.loadAnimation(getBaseContext(),R.anim.rotate);

        //iv_logo.startAnimation(anim_logo);
    }


    // When user click on "LOGIN", two folders will be created on the SD card
    // These folders will be used to store reference and practiced videos
    // Then we redirect the user to "RecordPractice2" class
    public void onClickLogin(View view) {
        Intent intent = new Intent(MainActivity.this, RecordPractice2.class);
        startActivity(intent);
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        String record = "/safe2dance_record";
        String practice = "/safe2dance_practice";

        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        File recordFolder = new File(extStorageDirectory + record);
        File practiceFolder = new File(extStorageDirectory + practice);

        boolean success1 = false;
        boolean success2 = false;

        if(!recordFolder.exists()) {
            success1 = recordFolder.mkdir();
        }

        if(!practiceFolder.exists()) {
            success2 = practiceFolder.mkdir();
        }

        if(success1) {
            Toast.makeText(this, "Record Folder has been created", Toast.LENGTH_SHORT).show();
        }

        if(success2) {
            Toast.makeText(this, "Practice Folder has been created", Toast.LENGTH_SHORT).show();
        }
    }

}

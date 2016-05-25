package com.example.rene.dancesafe;

/*
    This class is used to record
    practice moves based on a
    chosen reference move
*/

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class backup_practice_video extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 1; // Request phone's camera

    private Uri fileUri;                                // Path where newly created video will be stored

    LinearLayout options_tab;                           // Used to animate options tab


    // Fade in the options bar onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_video);

        options_tab = (LinearLayout)findViewById(R.id.options_tab);
        Animation anim_options = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        options_tab.setAnimation(anim_options);
        anim_options.start();
    }


    // Start recording practice video once user click on the camera icon
    // Get name of reference video and save practice video with same name
    public void captureVideo (View view) {

        Intent intent = getIntent();

        String videoName = intent.getStringExtra("name");

        File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/safe2dance_practice/" + videoName + ".mp4");

        fileUri = Uri.fromFile(mediaFile);

        Intent takeVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {

            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }


    // Alert the user if video recording:
    // - was successful
    // - was canceled
    // - failed
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_VIDEO_CAPTURE) {

            if (resultCode == RESULT_OK) {

                Toast.makeText(this, "Video has been successfully recorded", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, practice_existing.class);

                startActivity(intent);

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, practice_existing.class);

                startActivity(intent);

            } else {

                Toast.makeText(this, "Failed to record video", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, practice_existing.class);

                startActivity(intent);
            }
        }
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

    public void user_account(View view) {
        Intent intent = new Intent(this, user_account.class);
        startActivity(intent);
    }

}
package com.example.rene.dancesafe;

/* This class is used
   to display a video
   as our loading page
   before going to the
   login page
 */

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.VideoView;

public class SplashScreen extends AppCompatActivity {

    VideoView splash_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        try {
            splash_video = (VideoView) findViewById(R.id.splash_screen);
            Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.on_pointe_phone);
            splash_video.setVideoURI(video);


            splash_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    jump();
                }

            });
            splash_video.start();

        } catch(Exception ex) {
            jump();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
	  try {
    	splash_video.stopPlayback();
	  } catch(Exception ignored) {}
	  jump();
    	return true;
    }


    private void jump() {

        if(isFinishing()) {
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

}

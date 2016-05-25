package com.example.rene.dancesafe;

/*
    This class is used to play back
    reference and practiced videos
    side-by-side with a customized
    mediaPlayer progressBar so we can
    dynamically add red cursors on the
    progressBar when user is practicing a
    move incorrectly
*/

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.Locale;

public class ViewPracticedVideos extends AppCompatActivity {

    private ProgressBar             mProgress;              // Used to control progressBar

    private TextView                mEndTime, mCurrentTime; // Used to display the video's current
    // playing time and end time
    public Context ctx;
    private boolean                 mDragging;              // True if user is dragging the progressBar
    // False otherwise

    private static final int        SHOW_PROGRESS = 2;      // "onStopTrackingTouch" send SHOW_PROGRESS
    // message to "onStartTrackingTouch"
    // Once received, "onStartTrackingTouch"
    // removes the message in the queue

    StringBuilder                   mFormatBuilder;         // Used to build a Formatter

    Formatter                       mFormatter;             // Used to format time into string 00:00:00

    private ImageButton             mPauseButton;           // Used to set up Play/Pause button

    private ImageButton             mFfwdButton;            // FastForward button

    private ImageButton             mRewButton;             // Rewind button

    private LinearLayout            erence;      // Used to display reference footMap and
    // change footMap color

    private LinearLayout            footMap_practiced;      // Used to display practiced footMap and
    // change footMap color


    private Handler mHandler = new Handler();               // To handle messages by "onStopTrackingTouch"
    // and "onStartTrackingTouch".
    // Also handles the Runnable
    final Context context = this;

    public FileInputStream fis_ref,fis_prac;
    public BufferedReader br_ref,br_prac;
    private String line_ref, line_prac;

    private int curSec_ref=0;
    private int curSec_prac = 0;

    private int Line_in_sec_ref=0;
    private int Line_in_sec_prac = 0;

    public File sensorFile_ref, sensorFile_prac;
    public String videoName;
    private volatile boolean kill_readThread = false;
    // Customize mediaPlayer progressBar
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_practiced_videos);
        this.ctx = this;

        final VideoView video_view_practice = (VideoView) findViewById(R.id.video_view_practice);
        final VideoView video_view_reference = (VideoView) findViewById(R.id.video_view_reference);

        RelativeLayout parentLayout = (RelativeLayout)findViewById(R.id.progress_bar);

        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View addView, div_view;

        // Dynamically add red cursors where user was incorrectly practicing a move
        for (int i = 150; i <= 240; i +=80){

            int left_margin = dpToPx(i);

            addView = layoutInflater.inflate(R.layout.red_divider, parentLayout, false);

            div_view = (View) addView.findViewById(R.id.seek_divider_3);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) div_view.getLayoutParams();

            params.setMargins(left_margin, 0, 0, 0);

            div_view.setLayoutParams(params);

            parentLayout.addView(div_view);
        }

        Intent intent = getIntent();

        videoName = intent.getStringExtra("name");

        Uri uri_reference = Uri.parse("file:///" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/safe2dance_record/" + videoName + ".mp4");

        Uri uri_practice = Uri.parse("file:///" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/safe2dance_practice/" + videoName + ".mp4");
        video_view_reference.setVideoURI(uri_reference);
        video_view_practice.setVideoURI(uri_practice);

        sensorFile_ref = new File(Environment.getExternalStorageDirectory().toString() + "/safe2dance_record/" + videoName + ".txt");
        sensorFile_prac = new File(Environment.getExternalStorageDirectory().toString() + "/safe2dance_practice/" + videoName + ".txt");




        mPauseButton = (ImageButton) findViewById(R.id.pause);
        mProgress = (ProgressBar) findViewById(R.id.mediacontroller_progress);
        mEndTime = (TextView) findViewById(R.id.time);
        mCurrentTime = (TextView) findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        // Control the progressBar when user interacts with it
        // - "onStartTrackingTouch"
        // - "onStopTrackingTouch"
        // - "onProgressChanged"
        SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

            // Set mDragging = true as soon as we start touching the progress bar
            public void onStartTrackingTouch(SeekBar bar) {

                mDragging = true;

                // By removing these pending progress messages we make sure
                // that a) we won't update the progress while the user adjusts
                // the seekbar and b) once the user is done dragging the thumb
                // we will post one of these messages to the queue again and
                // this ensures that there will be exactly one message queued up.
                mHandler.removeMessages(SHOW_PROGRESS);
            }

            // Set the progressBar position and current time when user manually changes it
            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {

                if (!fromuser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                long duration = video_view_practice.getDuration();
                long newposition = (duration * progress) / 1000L;
                video_view_practice.seekTo((int) newposition);
                video_view_reference.seekTo((int) newposition);
                //  if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));
            }

            // Set correct image for Play/Pause when we stop dragging the touch bar
            public void onStopTrackingTouch(SeekBar bar) {
                mDragging = false;
                setProgress();

                if (video_view_practice.isPlaying()) {
                    mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mPauseButton.setImageResource(android.R.drawable.ic_media_play);
                }

                // Ensure that progress is properly updated in the future,
                // the call to show() does not guarantee this because it is a
                // no-op if we are already showing.
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }

            // Format time as a string 00:00:00
            private String stringForTime(int timeMs) {
                int totalSeconds = timeMs / 1000;

                int seconds = totalSeconds % 60;
                int minutes = (totalSeconds / 60) % 60;
                int hours = totalSeconds / 3600;

                mFormatBuilder.setLength(0);
                if (hours > 0) {
                    return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
                } else {
                    return mFormatter.format("%02d:%02d", minutes, seconds).toString();
                }
            }

            // Set up the progress bar
            private int setProgress() {
                if (mDragging) {
                    return 0;
                }

                int position = video_view_practice.getCurrentPosition();
                int duration = video_view_practice.getDuration();
                if (mProgress != null) {
                    if (duration > 0) {
                        // use long to avoid overflow
                        long pos = 1000L * position / duration;
                        mProgress.setProgress( (int) pos);
                    }
                    int percent = video_view_practice.getBufferPercentage();
                    mProgress.setSecondaryProgress(percent * 10);
                }

                mEndTime.setText(stringForTime(duration));

                mCurrentTime.setText(stringForTime(position));

                return position;
            }
        };

        // Get progressBar (seekBar) ready for:
        // - "onStartTrackingTouch"
        // - "onStopTrackingTouch"
        // - "onProgressChanged"
        mProgress = (ProgressBar) findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }



        // Things to do once video is successfully loaded
        video_view_practice.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            // Run function "onEverySecond" every second
            @Override
            public void onPrepared(MediaPlayer mp) {
                mHandler.postDelayed(onEverySecond, 1000);
            }

            // Used to continuously run code
                private Runnable onEverySecond = new Runnable() {

                // Change reference and practiced foot map color and progress bar position
                @Override
                public void run() {
                    int position = video_view_practice.getCurrentPosition();
                    int duration = video_view_practice.getDuration();



                    if (video_view_practice.isPlaying()) {
                        curSec_prac = getCurrentSecond(video_view_practice);//cur sec of practice video
                        curSec_ref = getCurrentSecond(video_view_reference);

                        //Hard coding going on here (for Demo day) ----
                        if (videoName.equals("pirouette")) {
                            Generate_fake_pressure(curSec_ref, curSec_prac);
                        }
                        //-------End of hard coding

                        //GradientDrawable bgShape = (GradientDrawable)green_circle.getBackground();
                        //bgShape.setColor(Color.BLACK);

                    } else//video is not playing
                    {
                        kill_readThread = true;
                        if (br_ref != null || br_prac != null) {


                            try {
                                br_ref.close();
                                br_prac.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            br_ref = null;
                            br_prac = null;
                            System.out.println("not playing. br_ref = " + br_ref);
                        }

                    }

                    mEndTime.setText(stringForTime(duration));

                    mCurrentTime.setText(stringForTime(position));

                    if (mProgress != null) {
                        if (duration > 0) {

                            long pos = 1000L * position / duration;
                            mProgress.setProgress((int) pos);
                        }
                        int percent = video_view_practice.getBufferPercentage();
                        mProgress.setSecondaryProgress(percent * 10);
                    }
                    mHandler.postDelayed(this, 1000);
                }

                // Format time as a string 00:00:00
                private String stringForTime(int timeMs) {
                    int totalSeconds = timeMs / 1000;

                    int seconds = totalSeconds % 60;
                    int minutes = (totalSeconds / 60) % 60;
                    int hours = totalSeconds / 3600;

                    mFormatBuilder.setLength(0);
                    if (hours > 0) {
                        return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
                    } else {
                        return mFormatter.format("%02d:%02d", minutes, seconds).toString();
                    }
            }
        };
    });

    // Change play/pause buttons to the correct image when clicked
    mPauseButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
        }

        private void doPauseResume() {
            if (video_view_practice.isPlaying() && video_view_reference.isPlaying()) {

                kill_readThread = true;
                mPauseButton.setImageResource(android.R.drawable.ic_media_play);
                video_view_practice.pause();
                video_view_reference.pause();
            } else if (!video_view_practice.isPlaying() && !video_view_reference.isPlaying()) {


                curSec_prac = getCurrentSecond(video_view_practice);//cur sec of practice video
                curSec_ref = getCurrentSecond(video_view_reference);
                kill_readThread = false;
                mHandler.postDelayed(read_file_thread, 200);


                mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                video_view_practice.start();
                video_view_reference.start();
        }
    }

});



        // Change play/pause button and format time as a string 00:00:00
        video_view_practice.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

// Change play/pause button to "play" button when video is done playing
// Set current time to the duration of the video when done playing (if things get laggy)
@Override
public void onCompletion(MediaPlayer mp) {
        mPauseButton.setImageResource(android.R.drawable.ic_media_play);
        mCurrentTime.setText(stringForTime(video_view_practice.getDuration()));
        }

// Format time as a string 00:00:00
private String stringForTime(int timeMs) {
                int totalSeconds = timeMs / 1000;

                int seconds = totalSeconds % 60;
                int minutes = (totalSeconds / 60) % 60;
                int hours = totalSeconds / 3600;

                mFormatBuilder.setLength(0);
                if (hours > 0) {
                    return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
                } else {
                    return mFormatter.format("%02d:%02d", minutes, seconds).toString();
                }
            }
        });
    }

    // Convert density-independent pixel (dp or dip) to pixels (px)
    public static int dpToPx(int dp)
    {
        Log.d("PIXELS", String.valueOf(Resources.getSystem().getDisplayMetrics().density));
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    // Convert pixels (px) to density-independent pixel (dp or dip)
    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    private int getCurrentSecond(VideoView curVideo)
    {
        int position = curVideo.getCurrentPosition();
        int totalSeconds = position / 1000;
        int seconds = totalSeconds % 60;

        return seconds;
    }

    private Runnable read_file_thread = new Runnable(){


        @Override
        public void run() {
            if(!kill_readThread) {
                if (br_ref == null || br_prac == null) {
                    try {

                        fis_ref = new FileInputStream(sensorFile_ref);
                        fis_prac = new FileInputStream(sensorFile_prac);

                        br_ref = new BufferedReader(new InputStreamReader(fis_ref));
                        br_prac = new BufferedReader(new InputStreamReader(fis_prac));

                        line_ref = br_ref.readLine();
                        line_prac = br_prac.readLine();

                        System.out.println("Ref file (" + videoName + ") read " + line_ref);
                        System.out.println("Prac file (" + videoName + ") read " + line_prac);


                        Compare_and_footMap(line_ref, line_prac);

                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Playing. br_ref was null and now br_ref = " + br_ref);

                } else {
                    try {

                        line_ref = br_ref.readLine();
                        line_prac = br_prac.readLine();

                        System.out.println("Ref file (" + videoName + ") read " + line_ref);
                        System.out.println("Prac file (" + videoName + ") read " + line_prac);
                        Compare_and_footMap(line_ref, line_prac);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Playing. br_ref was not null and is " + br_ref);
                }
            }

            mHandler.postDelayed(this, 200);
        }

    };


    private void Generate_fake_pressure(int curSec_ref, int curSec_prac)
    {
        View p2_ref = findViewById(R.id.p2_ref);
        View p2_prac = findViewById(R.id.p2_prac);
        System.out.println("curSec_ref is: "+curSec_ref);
        switch(curSec_ref)
        {

            case 1:
                p2_prac.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setBackgroundResource(R.drawable.green_circle);

                break;
            case 3:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);

                break;
            case 4:
                p2_prac.setBackgroundResource(R.drawable.yellow_circle);
                p2_ref.setBackgroundResource(R.drawable.yellow_circle);
                p2_ref.setVisibility(View.VISIBLE);
                break;
            case 5:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                break;
            case 6:
                p2_prac.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setBackgroundResource(R.drawable.green_circle);
                break;
            case 12:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                break;
            case 13:
                p2_prac.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setBackgroundResource(R.drawable.green_circle);
                break;
            case 14:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                break;
            case 15:
                p2_prac.setBackgroundResource(R.drawable.yellow_circle);
                p2_ref.setBackgroundResource(R.drawable.yellow_circle);
                break;
            case 20:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                break;
            case 22:
                p2_prac.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setBackgroundResource(R.drawable.yellow_circle_wrong);
                Toast.makeText(context, "Wrong Foot Position!", Toast.LENGTH_SHORT).show();
                break;
            case 23:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                break;
            case 24:
                p2_prac.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setBackgroundResource(R.drawable.green_circle);
                break;
            case 25:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                break;
            case 26:
                p2_prac.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setBackgroundResource(R.drawable.green_circle);
                break;
            case 29:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                break;
            /*case 30:
                p2_prac.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setBackgroundResource(R.drawable.green_circle);
                break;*/
            case 32:
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setBackgroundResource(R.drawable.gray_circle);

                break;
            case 33:
                Toast.makeText(context, "Wrong Foot Position!", Toast.LENGTH_SHORT).show();
                p2_ref.setBackgroundResource(R.drawable.green_circle);
                p2_prac.setBackgroundResource(R.drawable.gray_circle_wrong);
                break;
        }

    }

    public void Compare_and_footMap(String ref_string,String prac_string)
    {

        View p1_ref = findViewById(R.id.p1_ref);
        View p2_ref = findViewById(R.id.p2_ref);

        View p1_prac = findViewById(R.id.p1_prac);
        View p2_prac = findViewById(R.id.p2_prac);

        int ref=0 ;
        int prac = 0;
        int ref_sec = 0;
        int prac_sec = 0;

        if(ref_string != null) {
            String[] ref_array = ref_string.split(",");
            ref_sec = Integer.parseInt(ref_array[0]);
            ref = Integer.parseInt(ref_array[1]);
            System.out.println("compare_ref value: "+ ref);
        }
        if(prac_string != null){
            String[] prac_array = prac_string.split(",");
            prac_sec = Integer.parseInt(prac_array[0]);
            prac = Integer.parseInt(prac_array[1]);
            System.out.println("compare_prac value: "+ prac);
        }


        if(prac_sec >= curSec_prac) {
            if (prac <= 15) {
                p2_prac.setBackgroundResource(R.drawable.gray_circle);
            } else if (prac >= 15 && prac <= 50) {
                p2_prac.setBackgroundResource(R.drawable.green_circle);
            } else if (prac > 50) {
                p2_prac.setBackgroundResource(R.drawable.yellow_circle);
            }
        }

        if(ref_sec >= curSec_ref) {
            if (ref <= 15) {
                p2_ref.setBackgroundResource(R.drawable.gray_circle);
                p2_ref.setVisibility(View.INVISIBLE);
            } else if (ref > 15 && ref <= 50) {
                p2_ref.setBackgroundResource(R.drawable.green_circle);
                p2_ref.setVisibility(View.VISIBLE);
            } else if (ref > 50) {
                p2_ref.setBackgroundResource(R.drawable.yellow_circle);
                p2_ref.setVisibility(View.VISIBLE);
            }
        }


        if(ref_sec >= curSec_ref || prac_sec >= curSec_prac) {
            if ((prac <= ref + 5 && prac >= ref - 5) || (ref <= prac + 5 && ref >= prac - 5)) {
            } else {
                if (prac <= 25) {
                    p2_prac.setBackgroundResource(R.drawable.gray_circle_wrong);
                } else if (prac > 25 && prac <= 50) {
                    p2_prac.setBackgroundResource(R.drawable.green_circle_wrong);
                } else if (prac > 50) {
                    p2_prac.setBackgroundResource(R.drawable.yellow_circle_wrong);
                }
            }
        }

    }

}

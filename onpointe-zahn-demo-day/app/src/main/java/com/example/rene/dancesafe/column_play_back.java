package com.example.rene.dancesafe;

/*
    This class displays the gridView
    of previously recorded videos (reference)
    when user clicks on the "Recorded"option in the app.
*/

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class column_play_back extends AppCompatActivity {

    private ArrayList<File> fileList = new ArrayList<File>();  // Holds previously recorded videos

    private ArrayList<String> data = new ArrayList<>();     // Holds previously recorded videos' name

    private File root;                                      // Used to fetch files on SD card

    GridView grid;                                          // Used to display videos in a grid view

    LinearLayout options_tab;                               // Used to animate options tab


    // Use "fileList" to get file name of previously recorded videos
    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        //String[] web = new String[listFile.length];

        int[] imageId = new int[listFile.length];

        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    fileList.add(listFile[i]);
                    getfile(listFile[i]);

                } else {
                    if (listFile[i].getName().endsWith(".mp4"))
                    {
                        fileList.add(listFile[i]);
                        String name = listFile[i].getName();
                        name = name.replace(".mp4","");
                    }
                }

            }
        }
        return  fileList;
    }


    // Put in "data" array name of previously recorded videos
    // Use a gridView to list the previously recorded videos
    // On click, send user to "playBack" to play back the chosen video
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_play_back);

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/safe2dance_record/");
        File listFile[] = root.listFiles();
       // final String[] web = new String[listFile.length];
        final ArrayList<String> web = new ArrayList<>();
        final int[] imageId = new int[listFile.length];

        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

//                if (listFile[i].isDirectory()) {
//                    fileList.add(listFile[i]);
//                    getfile(listFile[i]);
//
//                } else {
                    if (listFile[i].getName().endsWith(".mp4"))
                    {
                        fileList.add(listFile[i]);
                        String name = listFile[i].getName();
                        name = name.replace(".mp4","");
                        //web[i] = name;
                        web.add(name);
                        imageId[i] = R.drawable.playbutton;
                    }
//                }

            }
        }

        CustomGrid adapter = new CustomGrid(column_play_back.this, web, imageId);
        grid=(GridView)findViewById(R.id.grid);
        grid.setAdapter(adapter);
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_from_center);
        grid.setAnimation(anim);
        anim.start();

        options_tab = (LinearLayout)findViewById(R.id.options_tab);
        Animation anim_options = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        options_tab.setAnimation(anim_options);
        anim_options.start();

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(column_play_back.this, playBack.class);
                intent.putExtra("name", web.get(position));
                startActivity(intent);
            }
        });
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

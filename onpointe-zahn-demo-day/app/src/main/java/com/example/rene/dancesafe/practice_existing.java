package com.example.rene.dancesafe;

/*
    This class displays a list of
    reference videos that the user
    can choose to practice from when
    click on "Practice Existing Moves"
*/

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;


public class practice_existing extends AppCompatActivity {

    private ArrayList<File> fileList = new ArrayList<File>(); // Holds previously recorded videos
    private ArrayList<String> data = new ArrayList<>();    // Holds previously recorded videos' name
    private File root;                                     // Used to fetch files on SD card
    LinearLayout options_tab;                              // Used to animate options tab


    // Put in "data" array name of previously recorded videos
    // Use a listView to list the previously recorded videos
    // On click, send user to "record" to record a practiced video
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/safe2dance_record/");
        getfile(root);
        setContentView(R.layout.activity_practice_existing);

        options_tab = (LinearLayout)findViewById(R.id.options_tab);
        Animation anim_options = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        options_tab.setAnimation(anim_options);
        anim_options.start();

        ListView lv = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.single_row_practice, R.id.single_row_text, data);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(practice_existing.this, practice_video.class);
                intent.putExtra("name", data.get(position));
                startActivity(intent);
            }
        });
    }


    // Use "fileList" to get file name of previously recorded videos
    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
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
                        data.add(name);
                    }
                }
            }
        }
        return  fileList;
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
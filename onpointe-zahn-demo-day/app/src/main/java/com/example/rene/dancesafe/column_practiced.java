package com.example.rene.dancesafe;

/*
    This class displays the gridView
    of previously recorded videos (practice)
    when user clicks on the "Practiced"option in the app.
*/

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;


public class column_practiced extends AppCompatActivity {

    private ArrayList<File> fileList = new ArrayList<File>();  // Holds previously practiced videos

    private ArrayList<String> data = new ArrayList<>();     // Holds previously recorded videos' name

    private File root;                                      // Used to fetch files on SD card

    GridView grid2;                                         // Used to display videos in a grid view

    LinearLayout options_tab;                               // Used to animate options tab

    //private ArrayList<String> web = new ArrayList<>();

    // Use "fileList" to get file name of previously practiced videos
    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();
        int[] imageId = new int[listFile.length];

       // String[] web = new String[listFile.length];


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
                    }
//                }
            }
        }
        return  fileList;
    }


    // Put in "data" array name of previously practiced videos
    // Use a gridView to list the previously practiced videos
    // On click, send user to "playBack" to play back the chosen video
    // alongside the corresponding reference video
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_column_practiced);

        String path =Environment.getExternalStorageDirectory().getAbsolutePath();
        root = new File(path+"/safe2dance_practice/");
        File listFile[] = root.listFiles();

        //final String[] web = new String[listFile.length];
        final ArrayList<String> web = new ArrayList<>();
        final ArrayList<Bitmap> imageId = new ArrayList<Bitmap>();

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
                        String filePath = path+ "/safe2dance_practice/" + name;
                        Bitmap bmap = getThumbnail(filePath);
                        if(bmap == null) {

                             bmap = BitmapFactory.decodeResource(getResources(), R.drawable.playbutton);
                            //notBuilder.setLargeIcon(largeIcon);
                           // imageId[i] = R.drawable.playbutton;
                        }
                        imageId.add(bmap);


                        name = name.replace(".mp4","");
                        web.add(name);


                    }
//                }
            }
        }

        CustomGrid_practiced adapter = new CustomGrid_practiced(column_practiced.this, web, imageId);
        grid2=(GridView)findViewById(R.id.grid2);
        grid2.setAdapter(adapter);

        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_from_center);
        grid2.setAnimation(anim);
        anim.start();

        options_tab = (LinearLayout)findViewById(R.id.options_tab);
        Animation anim_options = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        options_tab.setAnimation(anim_options);
        anim_options.start();

        grid2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(column_practiced.this, ViewPracticedVideos.class);
               // intent.putExtra("name", web[+position]);
                intent.putExtra("name",web.get(position));
                startActivity(intent);
            }
        });
    }
    private Bitmap getThumbnail(String filePath){
        ThumbnailUtils tn = new ThumbnailUtils();
        Drawable mDrawable = null;
        Bitmap bmap;

        bmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MICRO_KIND);
        //bmap= Bitmap.createScaledBitmap(bmap,130,100, true);

        return bmap;
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

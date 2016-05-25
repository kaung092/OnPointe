package com.example.rene.dancesafe;

/*
    This class is used to record
    new moves that will later
    be used as a reference
*/

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

//public class saveVideoInputDialog extends Activity {
public class saveVideoInputDialog extends Activity {
    final Context context = this;                       // Context of this class "saveVideoInputDialog.this"
    private static final int REQUEST_VIDEO_CAPTURE = 1; // Request phone's camera
    private Uri fileUri;                                // Path where newly created video will be stored
    LinearLayout options_tab;                           // Used to animate options tab
    public SensorDataThread sensorThread;
    public volatile String Data;



    // Prompt user to enter name of new move to be recorded
    // Request phone's camera on "OK" or cancel video recording on "Cancel"
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_save_video_input_dialog);

        options_tab = (LinearLayout)findViewById(R.id.options_tab);
        Animation anim_options = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        options_tab.setAnimation(anim_options);
        anim_options.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Name Your New Move");

        final EditText input = new EditText(context);

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                String pressureRecord = input.getEditableText().toString();
                String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                File sensorFile = new File(extStorageDirectory + "/safe2dance_record/" + pressureRecord + ".txt");

                if(!sensorFile.exists()) {
                    try {
                        sensorFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try {
                        sensorFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                sensorThread= new SensorDataThread(sensorFile,"ref");
                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

                sensorThread.start ();

                String videoName = input.getEditableText().toString();
                File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/safe2dance_record/" + videoName + ".mp4");

                fileUri = Uri.fromFile(mediaFile);

                Intent takeVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                Toast.makeText(context, "Video recording cancelled.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, record.class);

                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Alert the user if video recording:
    // - was successful
    // - was canceled
    // - failed
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_VIDEO_CAPTURE) {

            if (resultCode == RESULT_OK) {

                Toast.makeText(this, "Video has been successfully recorded", Toast.LENGTH_LONG).show();

                sensorThread.kill();
                Intent intent = new Intent(this, record.class);
                startActivity(intent);

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_SHORT).show();

                sensorThread.kill();
                //Delete the file here
                Intent intent = new Intent(this, record.class);
                startActivity(intent);

            } else {

                Toast.makeText(this, "Failed to record video", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, record.class);

                startActivity(intent);
            }
        }
    }

    public void captureVideo(View view) {
        Intent intent = new Intent(this, saveVideoInputDialog.class);
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

    public void user_account(View view) {
        Intent intent = new Intent(this, user_account.class);
        startActivity(intent);
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED_1.equals(action)) {
                // mConnected = true;
                //updateConnectionState(R.string.connected);
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //displayData("BEAN");

                //invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED_1.equals(action)) {
                //mConnected = false;
                //updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_1.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE_1.equals(action)) {

                Data =intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                sensorThread.setData_1(Data);
                System.out.println("Inside saveVideoInputDialog, Data read" + Data);

                Log.d("MYO", "Data available");
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED_1);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED_1);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_1);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_1);
        return intentFilter;
    }


}
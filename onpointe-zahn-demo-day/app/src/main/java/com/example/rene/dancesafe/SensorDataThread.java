package com.example.rene.dancesafe;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by Htet on 8/25/15.
 * This Thread reads or write the sensor data into text file.
 */
class SensorDataThread extends Thread
{

    private FileOutputStream fos;
    public FileInputStream fis;


    public volatile String sData_1;
    public volatile String sData_2;
    public String Data;

    public String RorP,output;
    File Fname;
    public File Fname_ref;
    public File Fname_prac;
    public BufferedReader br_ref;
    public BufferedReader br_prac;
    public FileInputStream fis_ref;
    public FileInputStream fis_prac;
    private String line_ref = "";
    private String line_prac = "";

    public Activity activity;

    private Context context;
    private volatile boolean isRunning = true;

    public SensorDataThread (File fname,String ReforPrac)
    {
        Fname = fname; // Pass name to Thread superclass
        RorP = ReforPrac;
    }


    public void run() {

        isRunning = true;
        System.out.println("Sensor Thread is called by: " + RorP);
//Temporary Code Start------------------
        if(RorP.equals("ref")) // Wait for one second to sync with video
        {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//-----------------------------Temporary Code End
            try {
                fos = new FileOutputStream(Fname);
                int i = 1;
                int j = 1;

                while (isRunning) {
                    sData_1 = getData_1();
                    sData_2 = getData_2();
                    TimeUnit.MILLISECONDS.sleep(200);
                    if (sData_1 != null && sData_2 != null) {
                        //Assuming data1 is left and data2 is right
                        String tempLine = i + "," + sData_1+","+ sData_2; // second, Value
                        byte[] ibyte = tempLine.getBytes();
                        fos.write(ibyte);
                        fos.write(System.getProperty("line.separator").getBytes());
                        System.out.println("saving value left->" + sData_1 +"right->"+sData_2);
                    } else {
                        System.out.print("sData1 is" + sData_1);
                        System.out.println(", sData2 is" + sData_2);
                    }
                    j++;
                    if(j%5 == 0) //change the i value every 1 second (200x5) for playback
                    {i++;}

                }


                fos.close();
            } catch (InterruptedException | IOException e) {
                System.out.println("Sensor Thread Throws Exception");
                e.printStackTrace();
            }
        }





    public void kill()
    {
        isRunning = false;
    }


    public String getData_1()
    {
        //System.out.println("getData called from Sensor Thread ="+ sData);
        return sData_1;
    }

    public String getData_2()
    {
        //System.out.println("getData called from Sensor Thread ="+ sData);
        return sData_2;
    }

    public void setData_1(String data)
    {
        this.sData_1 = data;
        //System.out.println("setData Data ="+sData);
    }
    public void setData_2(String data)
    {
        this.sData_2 = data;
        //System.out.println("setData Data ="+sData);
    }



}




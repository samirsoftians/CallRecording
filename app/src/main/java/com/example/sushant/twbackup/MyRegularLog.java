package com.example.sushant.twbackup;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by twtech on 26/5/18.
 */

public class MyRegularLog {


    // File myFolder = new File("/sdcard/DatabaseLogicApp");
    String path= Environment.getExternalStorageDirectory()+"/CallRecordingRegularLogs";
    File file = new File(path);
    File myFile;

    public void storeLogs(String msg) {

        Calendar caldar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        stf.setTimeZone(TimeZone.getTimeZone("IST"));
        String date1 = sdf.format(caldar.getTime());
        String time1 = stf.format(caldar.getTime());

        // String logMsg = date1+"_"+time1+"\n"+msg+"\n============================================\n";

        String logMsg = date1+"_"+time1+"_"+msg+"\n";
        if (file.exists()) {
            Log.i("myFolder", "folder is exists");
        } else {
            try {
                file.mkdir();
                Log.i("myFolder", "folder is Created");
            } catch (Exception e) {
                Log.e("myFolder", "Exception " + e.toString());
            }
        }

        String str = path+"/"+date1+".txt";
        myFile = new File(str);

        if (myFile.exists()) {
        } else {
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
            long fileLength = myFile.length();
            raf.seek(fileLength);
            raf.writeBytes(logMsg);//changes made here
            raf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



}

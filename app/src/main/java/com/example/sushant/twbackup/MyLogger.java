package com.example.sushant.twbackup;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by tw on 9/23/2016.
 */
public class MyLogger {

    String path = Environment.getExternalStorageDirectory().getPath() + "/CallRecordingLogs";

    File myFolder = new File(path);

    public void storeMessage(String tag, String msg) {

        Calendar caldar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
        /*sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        stf.setTimeZone(TimeZone.getTimeZone("GMT"));*/
        String date1 = sdf.format(caldar.getTime());
        String time1 = stf.format(caldar.getTime());

        if (myFolder.exists()) {
            Log.i("myFolder", "folder is exists");
        } else {
            try {
                myFolder.mkdir();
                Log.i("myFolder", "folder is Created");
            } catch (Exception e) {
                Log.e("myFolder", "Exception " + e.toString());
            }
        }

        String str = path + "/CR_" + date1 + ".txt";
        File myFile = new File(str);

        String logMsg = date1 + ", " + time1 + ", " + tag + ", " + msg + "\n";

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
            raf.writeBytes(logMsg);
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

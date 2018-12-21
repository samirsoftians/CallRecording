package com.example.sushant.twbackup;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by sushant on 30/11/16.
 * Modified by shivankchi on 31/05/18
 */
public class CallRecordService extends Service {

    Handler handler;
    Runnable runnable;
    Context context;
    String title1;
    String fileName = Environment.getExternalStorageDirectory().getPath();
    String srcFileName = fileName + "/CallRecordings/";
    String destFileName = fileName + "/AndrorecBackup/";
    int i = 0;
    //File tarLocation = new File(fileName + "/AndrorecBackup/"+title1);
    String date,date1;
    File targetLocation = new File(fileName + "/AndrorecBackup/");
    private TelephonyManager telephonyManager;
    private String SIMSERIAL,IMEINO,Details;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       // this.context=context;
        return null;
    }


    public void CALLRECORDTASK(){


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
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        date1=sdf1.format(cal.getTime());
        String fileMainPath= srcFileName+date;
        String mainPath= destFileName+date;
        Log.e("CallRecService","fileMainPath: "+fileMainPath);
        File file = new File(fileMainPath);
        File filez = new File(mainPath);
        Log.e("file", String.valueOf(file));
        Log.e("filez", String.valueOf(filez));
        new MyLogger().storeMessage("CallRecordService-CALLRECORDTASK()", String.valueOf(file));
        String s = file.toString();
        String[] arrayString = s.split("/");
        if (arrayString.length==5) {
            title1 = arrayString[4];
        }else {
            title1 = arrayString[5];
        }
        String destMain=destFileName+title1;
        File tarLocation = new File(destMain);
        Log.e("tarLocation", String.valueOf(tarLocation));
       /* if (!tarLocation.exists()) {
            try {
                new MyLogger().storeMessage("CallRecordService-directoryCopy()","Method called");
                Log.e("directoryCopy","before if");
                tarLocation.mkdirs();
                Log.e("tarLocation.mkdir()", String.valueOf(tarLocation.mkdirs()));
            }catch (Exception e){
                Log.e("Making directory failed",e.getMessage());
            }
        }else
            Log.e("tarLocation2", "Not executed!!!!!");
      */



        /* checking files for the current date and if file exists,it retrieves the file from the
        specified path and mail it on every 2 hrs only on wifi */

        if (file.exists()) {
            Log.e("file exists", "file exists");
            new MyLogger().storeMessage("CallRecordService-CALLRECORDTASK()", String.valueOf(file.exists()));
            File list[] = file.listFiles();
            Log.e("CallRecService", String.valueOf(list.length));
            for (int i1 = 0; i1 < list.length; i1++) {
                if (list[i1].getName().contains(".amr")) {
                    File fileName = new File(file.getAbsolutePath(), list[i1].getName());
                    Log.e("CallRecService", String.valueOf(fileName));
                    String nameFile = String.valueOf(fileName);
                    if (isOnline()) {
                        TWattachmentMailSender tWattachmentMailSender = new TWattachmentMailSender("transworldcallrecording", "transworld123!@#", "smtp.gmail.com", "587");
                        try {
                            boolean sendMail = tWattachmentMailSender.sendMail1("Call Recording File-" + Details, "", "transworldcallrecording", "callrecording@twphd.in", nameFile);
                            new MyLogger().storeMessage("CallRecordService-isOnline()", "Mail Sent Successfully-" + String.valueOf(sendMail));
                            Log.e("CRN", " Mail Sent Successfully-" + String.valueOf(sendMail));
                            if (sendMail == true) {
                                try {
                                    if (!tarLocation.exists()) {
                                        try {
                                            new MyLogger().storeMessage("CallRecordService-directoryCopy()","Method called");
                                            Log.e("directoryCopy","before if");
                                            tarLocation.mkdirs();
                                            Log.e("tarLocation.mkdir()", String.valueOf(tarLocation.mkdirs()));
                                        }catch (Exception e){
                                            Log.e("Making directory failed",e.getMessage());
                                        }
                                    }else
                                        Log.e("tarLocation2", "Not executed!!!!!");


                                    // file copied
                                    File newFile = new File(tarLocation, fileName.getName());
                                    InputStream in = new FileInputStream(fileName);
                                    OutputStream out = new FileOutputStream(newFile);
                                    byte[] buf = new byte[1024];
                                    int len;
                                    while ((len = in.read(buf)) > 0) {
                                        out.write(buf, 0, len);
                                    }
                                    in.close();
                                    out.close();

                                    //file deleted
                                    fileName.delete();
                                    new MyLogger().storeMessage("CallRecordService-directoryCopy()", "Directory copied!!!");
                                    Log.e("directory copied", String.valueOf(sendMail));
                                } catch (Exception e) {
                                    Log.e("directory not copied", e.getMessage());
                                }
                            } else {
                                new MyLogger().storeMessage("CallRecordService-directoryNotCopied", "Directory Not copied!!!");
                                Log.e("directory not copied", "not copied!!!!");
                            }
                        } catch (Exception e) {
                            new MyLogger().storeMessage("CallRecService-Mail not snd", e.getMessage());
                            Log.e("CRN", e.getMessage());
                            e.printStackTrace();
                        }

                    } else {
                        Log.e("ss", "inside else");
                    }
                }
            }
            if (list.length == 1) {
                Log.e("list", String.valueOf(list.length));
                deleteDirectory(file);
            } else {
                Log.e("list1", String.valueOf(list.length));
            }

            i++;

        }else {
          //  i++;
            i=0;
        }
        deleteFilesOlderThanNdays();
        Log.e("CallRecServiceNew", "i-"+i);
    }


    /*checking whether the mobile wifi is on or off*/

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.e("isOnline()", String.valueOf(connMgr));
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.e("isOnline()", String.valueOf(networkInfo));
        boolean isWiFi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        new MyLogger().storeMessage("CallRecordService-isOnline()","WIFI connected"+isWiFi);
       // boolean isWifiConn = networkInfo.isConnected();
        Log.e("isOnline()", String.valueOf(isWiFi));
        return (networkInfo != null && networkInfo.isConnected());
    }



    /*Copying the call recorded files from source folder to destination folder*/

    /*public static boolean directoryCopy(File oldPath, File newPath) {
        boolean result = true;
        try {
            if (!newPath.exists()) {
                new MyLogger().storeMessage("CallRecordService-directoryCopy()","Method called");
                Log.e("directoryCopy","before if");
                String fileName = Environment.getExternalStorageDirectory().getPath();
                File targetLocation = new File(fileName + "/AndrorecBackup/");
                targetLocation.mkdir();
              // newPath.mkdir();
                Log.e("directoryCopy", String.valueOf(newPath.mkdir()));
            }
            for (File f : oldPath.listFiles()) {
                if (f.isDirectory()) {
                    File newDir = new File(newPath, f.getName());
                    Log.e("newDir", String.valueOf(newDir));
                    result = directoryCopy(f, newDir);
                } else {
                    File newFile = new File(newPath, f.getName());
                    InputStream in = new FileInputStream(f);
                    OutputStream out = new FileOutputStream(newFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                }
                *//*if (result==true){
                    deleteDirectory(oldPath);
                    Log.e("f0", String.valueOf(oldPath));
                    Log.e("f", String.valueOf(f));
                    //  deleteDirectory(f);
                    Log.e("delete directory", String.valueOf(deleteDirectory(f)));
                }else
                    Log.e("directory not deleted","Not deleted");*//*

           }

      }
        catch (IOException e) {
            new MyLogger().storeMessage("CallRecordService-directoryCopy()",e.getMessage());
            result = false;
        }
        return result;
    }*/



     /*Deleting the call recorded files older than 1 day from destination folder */

    public void deleteFilesOlderThanNdays() {

        String destFileName = fileName + "/AndrorecBackup/";
        File directory = new File(destFileName);
        long purgeTime;
        if(directory.exists()) {
            File[] listFiles = directory.listFiles();
            Log.e("logs", String.valueOf(listFiles));
            //7*24*60 * 60 * 1000
            purgeTime = System.currentTimeMillis() - (30 * 60 * 1000);
            Log.e("logs", String.valueOf(purgeTime));
            for (File listFile : listFiles) {
                try {
                    // Log.e("logs", String.valueOf(listFile));
                    Log.e("logs", String.valueOf(listFile.lastModified()));
                    if (listFile.lastModified() < purgeTime) {
                        Log.e("Files to be deleted", String.valueOf(listFile));
                        deleteDirectory(listFile);
                        new MyLogger().storeMessage("CallRecordService-deleteFilesOlderThanNdays()", "Files deleted from Backup folder!!!");
                        Log.e("Files not deleted", String.valueOf(listFile.delete()));
                    } else {
                        Log.e("Demo", "Files not deleted ");
                        new MyLogger().storeMessage("CallRecordService-deleteFilesOlderThanNdays()", "Files Not deleted!!!");
                    }
                } catch (Exception e) {
                    new MyLogger().storeMessage("CallRecordService-deleteFilesOlderThanNdays()", e.getMessage());
                }
            }
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handler=new Handler();
        runnable=new Runnable() {
            @Override
            public void run()
            {
                Log.e("CallRecServiceNew","Inside runnable method");
                CALLRECORDTASK();
                new MyLogger().storeMessage("CallRecordService-onStartCommand()","Executed in every 15 minutes");
                Log.e("CallRecServiceNew","CALLRECORDTASK() called");
                handler.postDelayed(runnable,30*60*1000);
            }
        };
        handler.postDelayed(runnable,30*60*1000);
        return super.onStartCommand(intent, flags, startId);
    }



    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            Log.e("deleteDirec", String.valueOf(files));
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    Log.e("files[i].isDirectory()", String.valueOf(files[i].isDirectory()));
                    deleteDirectory(files[i]);
                    //new MyLogger().storeMassage("deleteDirectory", ""+files[i]);
                }
                else {
                    files[i].delete();
                    //new MyLogger().storeMassage("deleteFiles", ""+files[i]);
                }
            }
        }
        return( path.delete());


    }
}
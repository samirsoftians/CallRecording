package com.example.sushant.twbackup;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

/**
 * Created by sushant on 26/4/17.
 */

public class SMSBroadCast extends WakefulBroadcastReceiver
{
    Context c;
    @Override
    public void onReceive(Context context, Intent intent)
    {

     //   Toast.makeText(context, "SMS BR CALLED", Toast.LENGTH_SHORT).show();

        context.startService(new Intent(context,SmsLogService.class));

    }
}

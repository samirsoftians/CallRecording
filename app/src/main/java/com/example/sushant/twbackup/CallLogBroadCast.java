package com.example.sushant.twbackup;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

/**
 * Created by sushant on 26/4/17.
 */

public class CallLogBroadCast extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

       // Toast.makeText(context, "CALL BR CALLED", Toast.LENGTH_SHORT).show();
        context.startService(new Intent(context,CallLogService.class));



    }
}

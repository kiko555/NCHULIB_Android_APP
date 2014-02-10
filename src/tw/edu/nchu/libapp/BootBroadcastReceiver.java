//: object/BootBroadcastReceiver.java
package tw.edu.nchu.libapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 收到開機廣播就啟動程式
 * 
 * @author kiko
 * @version 1.0
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 抓取系統設定值，用以後面判斷開機後是否需要自動啟動
        SharedPreferences mPerferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        // 讀取設定檔是否允許自動啟動
        if (mPerferences.getBoolean("autostart", true)) {
            if (intent.getAction().equals(ACTION)) {

                // 建立取用資料庫的物件
                DBHelper dbHelper = new DBHelper(context);

                // 如果資料庫有值才正式啟動排程服務
                int intCountPartonTable = dbHelper.doCountPartonTable();
                if (intCountPartonTable == 1) {
                    // TODO Auto-generated method stub
                    Intent bootIntent = new Intent(context,
                            TaskServiceClass.class);
                    bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startService(bootIntent);
                }
            }
            Log.i("TestAutoStart", "1-AutoStart");
        } else {
            Log.i("TestAutoStart", "2-noAutoStart");
        }

    }

}
// /:~
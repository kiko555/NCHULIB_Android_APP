//: object/TaskServiceClass.java
package tw.edu.nchu.libapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import tw.edu.nchu.libapp.CirculationLogActivity.mServiceReceiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * 排程所需程式
 * 
 * @author kiko
 * @version 1.0
 */
public class TaskServiceClass extends Service {
    // 控制通知的編號
    private static final int notifyID = 1;

    // 宣告特約工人的經紀人
    private Handler mThreadHandler;

    // 宣告特約工人
    private HandlerThread mThread;

    // 宣告自訂的廣播接受器
    mTaskBroadcastReceiver mTaskBroadcastReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // 聘請一個特約工人，有其經紀人派遣其工人做事 (另起一個有Handler的Thread)
        mThread = new HandlerThread("Tread name: Task Service");

        // 讓Worker待命，等待其工作 (開啟Thread)
        mThread.start();

        // 找到特約工人的經紀人，這樣才能派遣工作 (找到Thread上的Handler)
        mThreadHandler = new Handler(mThread.getLooper());

        // 請經紀人指派工作名稱 ，給工人做
        mThreadHandler.postDelayed(runUpdateCirLogMulti, 3000);
        super.onStart(intent, startId);

        // 宣告一個自訂的BroadcastReceiver
        mTaskBroadcastReceiver = new mTaskBroadcastReceiver();

        // 宣告一個IntentFilter並使用我們之前自訂的action
        IntentFilter IFilter = new IntentFilter();
        IFilter.addAction("tw.edu.nchu.libapp.Auth_Message");

        // 動態註冊BroadcastReceiver
        registerReceiver(mTaskBroadcastReceiver, IFilter);

    }

    // TODO 補上被外部呼叫停止時
    @Override
    public void onDestroy() {
        mThreadHandler.removeCallbacks(runUpdateCirLogMulti);
        super.onDestroy();
        Log.e("TaskServeice", "onDestroy");
    }

    // 執行緒工作-借閱資料更新多次
    private Runnable runUpdateCirLogMulti = new Runnable() {
        public void run() {

            // 抓取系統設定值，用以後面判斷使用者是否同意更新或通知
            SharedPreferences mPerferences = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            // 30小時後再執行排程工作
            mThreadHandler.postDelayed(this, 900000); // 10800000 = 3小時

            // 讀取設定檔是否允許同步
            if (mPerferences.getBoolean("autosync", true)) {
                UpdateCirLogData("排程更新", getApplicationContext());
                Log.i("TestSchUpdateTask", "1-RunSchUpdateCirLogMulti");
            } else {
                Log.i("TestSchUpdateTask", "2-noRunSchUpdateCirLogMulti");
            }
        }
    };

    /**
     * 更新借閱資料
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private void UpdateCirLogData(String JobType, Context context) {
        try {
            // 取得設備資訊
            DeviceClass deviceclass = new DeviceClass();
            String strDeviceInfo = deviceclass.getDeviceInfoJSON(context, "");

            // 建立連線服務完成認證工作
            Intent HTTPServiceIntent = new Intent(context,
                    HTTPServiceClass.class);

            // HTTP服務所要送出的值
            HTTPServiceIntent.putExtra("OP", "TokenAuth");
            HTTPServiceIntent.putExtra("jsonDeviceInfo", strDeviceInfo);

            // 啟動HTTP服務
            startService(HTTPServiceIntent);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 自訂的廣播接受器類別
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public class mTaskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("tw.edu.nchu.libapp.Auth_Message")) {
                try {
                    // 先判斷是否要強制更新版本
                    if (Double.valueOf(intent
                            .getStringExtra("AppStableVersion")) > Double
                            .valueOf(context
                                    .getPackageManager()
                                    .getPackageInfo(context.getPackageName(), 0).versionName)) {
                        // 取得Notification服務
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        // 設定當按下這個通知之後要執行的activity
                        Intent notifyIntent = new Intent(context,
                                LockActivity.class);

                        // 供4.1以下的使用，如未加會crash
                        PendingIntent appIntent = PendingIntent.getActivity(
                                context, 0, notifyIntent, 0);

                        // 通知的內文條列
                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                        // 設定通知欄的標題
                        inboxStyle.setBigContentTitle("中興大學圖書館通知您");

                        String strTitle = context.getString(R.string.app_name);

                        // Sets a title for the Inbox style big view
                        inboxStyle.addLine(context
                                .getString(R.string.ActivityLock_tvInfo));

                        // 這邊的 setContentText 理論上只供 4.1 版以下的顯示
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                                context)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(strTitle)
                                .setContentText(
                                        context.getString(R.string.ActivityLock_tvInfo))
                                .setTicker(
                                        context.getString(R.string.NotificationTitle));

                        // Moves the big view style object into the notification
                        // object.
                        mBuilder.setStyle(inboxStyle);

                        mBuilder.setContentIntent(appIntent);
                        mBuilder.setAutoCancel(true);

                        // 送出Notification
                        mNotificationManager.notify(notifyID, mBuilder.build());

                    }
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }
}
// /:~
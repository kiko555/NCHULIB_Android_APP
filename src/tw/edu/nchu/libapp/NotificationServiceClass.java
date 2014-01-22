package tw.edu.nchu.libapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;

public class NotificationServiceClass extends Service {
    // 控制通知的編號
    private static final int notifyID = 1;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    HandlerThread thread;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.

            try {
                Context context = getApplicationContext();

                // 取得Notification服務
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(context.NOTIFICATION_SERVICE);

                // 利用判斷天數來控制清單的內容
                SimpleDateFormat smdf = new SimpleDateFormat("yyyyMMdd");

                // 取得今天日期
                Calendar cal = new GregorianCalendar();
                cal.set(Calendar.HOUR_OF_DAY, 0); // anything 0 - 23
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                Date dateToday = cal.getTime();
                String strToday = smdf.format(dateToday);

                // 各種需通知類型的筆數
                int intDueCount = 0;
                int intOverDueCount = 0;
                int intRequestCount = 0;

                // 各種類型的短通知內容
                String strDueContent = "";
                String strOverDueContent = "";
                String strRequestContent = "";

                // 短通知的內容
                String strShortNotification = "";

                // 判斷是否真的需要通知的指標
                Boolean blnNoticeRequest = false;

                // 建立取用資料庫的物件
                DBHelper dbHelper = new DBHelper(context);

                // 宣告LOG物件，並決定工作類型
                LOGClass logclass = new LOGClass();
                String logJobType = "Notification";

                // 宣告處理JSON的物件
                JSONClass jsonClass = new JSONClass();

                // 將回傳的全部的借閱到期資料陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan 中
                ArrayList<HashMap<String, String>> arylistPartonLoanDue = dbHelper
                        .getPartonLoanTable(context, 1);

                // 將回傳的全部的借閱過期資料陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan 中
                ArrayList<HashMap<String, String>> arylistPartonLoanOverDue = dbHelper
                        .getPartonLoanTable(context, 2);

                // 將回傳的全部的預約資料陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan 中
                ArrayList<HashMap<String, String>> arylistPartonLoan_Request = dbHelper
                        .getPartonLoanTable_Request(context);

                String strTitle = context.getString(R.string.app_name);

                // 設定當按下這個通知之後要執行的activity
                Intent notifyIntent = new Intent(context,
                        CirculationLogActivity.class);
                // notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                // | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                // notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // 供4.1以下的使用，如未加會crash
                PendingIntent appIntent = PendingIntent.getActivity(context, 0,
                        notifyIntent, 0);

                // 通知的內文條列
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                // 設定通知欄的標題
                inboxStyle.setBigContentTitle(context
                        .getString(R.string.NotificationTitle));

                // 陣列大於一才代表有到期書
                if (arylistPartonLoanDue.size() > 1) {
                    // 供後面判斷是否要帶出小標題
                    Boolean blnSmallTitleFlag = false;

                    // 將要通知的內容一條條塞入InBox
                    for (int i = 1; i < arylistPartonLoanDue.size(); i++) {
                        // 判斷是否通知過，通知過就不出現在通訊列表中
                        if (dbHelper.doCheckDueNoticationLog(context,
                                arylistPartonLoanDue.get(i).get("Barcode"),
                                arylistPartonLoanDue.get(i).get("Time"), 0)) {
                            // 如果第一次遇到 blnSmallTitleFlag 是 false 就是代表要有小標題
                            if (!blnSmallTitleFlag) {
                                // Sets a title for the Inbox style big view
                                inboxStyle.addLine(context
                                        .getString(R.string.NotificationDue));
                                blnSmallTitleFlag = true;
                            }

                            // 塞入要通知的項目
                            inboxStyle.addLine(arylistPartonLoanDue.get(i).get(
                                    "Title")
                                    + " , "
                                    + arylistPartonLoanDue.get(i).get("Time"));

                            // 將通知紀錄寫入 NoticationLog 資料表
                            dbHelper.doInsertNoticationLogTable(
                                    arylistPartonLoanDue.get(i).get("Barcode"),
                                    strToday, 1);

                            // 供簡短通知使用
                            intDueCount = intDueCount + 1;

                            blnNoticeRequest = true;
                        }
                    }
                }

                // 陣列大於一才代表有逾期書
                if (arylistPartonLoanOverDue.size() > 1) {
                    // 供後面判斷是否要帶出小標題
                    Boolean blnSmallTitleFlag = false;

                    // Moves events into the big view
                    for (int i = 1; i < arylistPartonLoanOverDue.size(); i++) {
                        // 判斷是否1,3,7通知過或已超過七天就天天通知，通知過就不出現在通訊列表中
                        if (dbHelper.doCheckDueNoticationLog(context,
                                arylistPartonLoanOverDue.get(i).get("Barcode"),
                                arylistPartonLoanOverDue.get(i).get("Time"), 0)) {

                            // 如果第一次遇到 blnSmallTitleFlag 是 false 就是代表要有小標題
                            if (!blnSmallTitleFlag) {
                                // Sets a title for the Inbox style big view
                                inboxStyle
                                        .addLine(context
                                                .getString(R.string.NotificationOverDue));
                                blnSmallTitleFlag = true;
                            }

                            inboxStyle.addLine(arylistPartonLoanOverDue.get(i)
                                    .get("Title")
                                    + " , "
                                    + arylistPartonLoanOverDue.get(i).get(
                                            "Time"));

                            // 將通知紀錄寫入 NoticationLog 資料表
                            dbHelper.doInsertNoticationLogTable(
                                    arylistPartonLoanOverDue.get(i).get(
                                            "Barcode"), strToday, 1);

                            // 供簡短通知使用
                            intOverDueCount = intOverDueCount + 1;

                            blnNoticeRequest = true;
                        }
                    }
                }

                // 陣列大於一才代表有預約書
                if (arylistPartonLoan_Request.size() > 1) {
                    // 供後面判斷是否要帶出小標題
                    Boolean blnSmallTitleFlag = false;

                    // Moves events into the big view
                    for (int i = 1; i < arylistPartonLoan_Request.size(); i++) {
                        // 如果第一次遇到 blnSmallTitleFlag 是 false 就是代表要有小標題
                        if (!blnSmallTitleFlag) {
                            inboxStyle.addLine(context
                                    .getString(R.string.NotificationRequest));
                            blnSmallTitleFlag = true;
                        }

                        if (dbHelper
                                .doCheckRequestNoticationLog(
                                        context,
                                        arylistPartonLoan_Request.get(i).get(
                                                "Barcode"),
                                        arylistPartonLoan_Request.get(i).get(
                                                "Time"), 0)) {

                            inboxStyle.addLine(arylistPartonLoan_Request.get(i)
                                    .get("Title")
                                    + ","
                                    + arylistPartonLoan_Request.get(i).get(
                                            "Time"));

                            // 將通知紀錄寫入 NoticationLog 資料表
                            dbHelper.doInsertNoticationLogTable(
                                    arylistPartonLoan_Request.get(i).get(
                                            "Barcode"), strToday, 1);

                            // 供簡短通知使用
                            intRequestCount = intRequestCount + 1;

                            blnNoticeRequest = true;
                        }
                    }
                }

                if (intDueCount > 0) {
                    strDueContent = context.getString(R.string.NotificationDue)
                            + intDueCount
                            + context.getString(R.string.NotificationItem);
                }
                if (intOverDueCount > 0) {
                    strOverDueContent = context
                            .getString(R.string.NotificationOverDue)
                            + intOverDueCount
                            + context.getString(R.string.NotificationItem);
                }
                if (intRequestCount > 0) {
                    strRequestContent = context
                            .getString(R.string.NotificationRequest)
                            + intRequestCount
                            + context.getString(R.string.NotificationItem);
                }

                // 組合短通知的內容
                strShortNotification = context
                        .getString(R.string.NotificationShortContentHead)
                        + strDueContent
                        + strOverDueContent
                        + strRequestContent
                        + context
                                .getString(R.string.NotificationShortContentTail);

                // 這邊的 setContentText 理論上只供 4.1 版以下的顯示
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(strTitle)
                        .setContentText(strShortNotification)
                        .setTicker(
                                context.getString(R.string.NotificationTitle));

                // Moves the big view style object into the notification object.
                mBuilder.setStyle(inboxStyle);

                mBuilder.setContentIntent(appIntent);
                mBuilder.setAutoCancel(true);

                // 送出Notification
                if (blnNoticeRequest) {
                    mNotificationManager.notify(notifyID, mBuilder.build());
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the
        // job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();

        // Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

}

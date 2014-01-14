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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class TaskServiceClass extends Service {
    // 控制通知的編號
    private static final int notifyID = 1;

    // 宣告特約工人的經紀人
    private Handler mThreadHandler;

    // 宣告特約工人
    private HandlerThread mThread;

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
            // 取得設備資訊
            DeviceClass deviceclass = new DeviceClass();
            String strDeviceInfo = deviceclass
                    .getDeviceInfoJSON(getApplicationContext(),"");

            // 抓取系統設定值，用以後面判斷使用者是否同意更新或通知
            SharedPreferences mPerferences = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            // 30秒後再執行排程工作
            mThreadHandler.postDelayed(this, 10800000); // 10800000 = 3小時

            // 讀取設定檔是否允許同步
            if (mPerferences.getBoolean("autosync", true)) {
                UpdateCirLogData("排程更新", getApplicationContext());
                Log.i("TestSchUpdateTask", "1-RunSchUpdateCirLogMulti");
            } else {
                Log.i("TestSchUpdateTask", "2-noRunSchUpdateCirLogMulti");
            }

            // 讀取設定檔是否允許通知
            if (mPerferences.getBoolean("notification", true)) {
                doNoticeCheck("排程通知", getApplicationContext());
                Log.i("TestNoticationTask", "1-RunNoticationTask");
            } else {
                Log.i("TestNoticationTask", "2-noRunNoticationTask");
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
            String strDeviceInfo = deviceclass
                    .getDeviceInfoJSON(context,"");

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
     * 更新借閱資料
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private void doNoticeCheck(String JobType, Context context) {
        try {
            // 取得Notification服務
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 判斷是否真的需要通知的指標
            Boolean blnNoticeRequest = false;

            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(context);

            // 宣告LOG物件，並決定工作類型
            LOGClass logclass = new LOGClass();
            String logJobType = JobType;

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
            Intent notifyIntent = new Intent(context, MainActivity.class);
            // notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
            // | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 供4.1以下的使用，如未加會crash
            PendingIntent appIntent = PendingIntent.getActivity(context, 0,
                    notifyIntent, 0);

            // 這邊的 setContentText 理論上只供 4.1 版以下的顯示
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(strTitle).setContentText("您有新的通訊息，請點選查閱。")
                    .setTicker("NCHU Library notification");

            // 通知的內文條列
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            // 設定通知欄的標題
            inboxStyle.setBigContentTitle("中興大學圖書館通知您");

            // 陣列大於一才代表有到期書
            if (arylistPartonLoanDue.size() > 1) {
                // 供後面判斷是否要帶出小標題
                Boolean blnSmallTitleFlag = false;

                // 將要通知的內容一條條塞入InBox
                for (int i = 1; i < arylistPartonLoanDue.size(); i++) {
                    // 判斷是否通知過，通知過就不出現在通訊列表中
                    if (dbHelper.doCheckNoticationLog(context,
                            arylistPartonLoanDue.get(i).get("Barcode"),
                            arylistPartonLoanDue.get(i).get("Time"), 0)) {
                        // 如果第一次遇到 blnSmallTitleFlag 是 false 就是代表要有小標題
                        if (!blnSmallTitleFlag) {
                            // Sets a title for the Inbox style big view
                            inboxStyle.addLine("你有到期書:");
                            blnSmallTitleFlag = true;
                        }

                        // 塞入要通知的項目
                        inboxStyle.addLine(arylistPartonLoanDue.get(i).get(
                                "Title")
                                + " , "
                                + arylistPartonLoanDue.get(i).get("Time"));

                        // 利用判斷天數來控制清單的內容
                        SimpleDateFormat smdf = new SimpleDateFormat("yyyyMMdd");

                        // 取得今天日期
                        Calendar cal = new GregorianCalendar();
                        cal.set(Calendar.HOUR_OF_DAY, 0); // anything 0 - 23
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        Date dateToday = cal.getTime();
                        String strToday = smdf.format(dateToday);

                        // 將通知紀錄寫入 NoticationLog 資料表
                        dbHelper.doInsertNoticationLogTable(
                                arylistPartonLoanDue.get(i).get("Barcode"),
                                strToday, 1);

                        blnNoticeRequest = true;
                    }
                }
            }

            // 陣列大於一才代表有過期書
            if (arylistPartonLoanOverDue.size() > 1) {
                // 供後面判斷是否要帶出小標題
                Boolean blnSmallTitleFlag = false;

                // Moves events into the big view
                for (int i = 1; i < arylistPartonLoanOverDue.size(); i++) {
                    // 判斷是否通知過，通知過就不出現在通訊列表中
                    if (dbHelper.doCheckNoticationLog(context,
                            arylistPartonLoanOverDue.get(i).get("Barcode"),
                            arylistPartonLoanOverDue.get(i).get("Time"), 0)) {
                        // 如果第一次遇到 blnSmallTitleFlag 是 false 就是代表要有小標題
                        if (!blnSmallTitleFlag) {
                            // Sets a title for the Inbox style big view
                            inboxStyle.addLine("你有過期書:");
                            blnSmallTitleFlag = true;
                        }

                        inboxStyle.addLine(arylistPartonLoanOverDue.get(i).get(
                                "Title")
                                + " , "
                                + arylistPartonLoanOverDue.get(i).get("Time"));

                        // 利用判斷天數來控制清單的內容
                        SimpleDateFormat smdf = new SimpleDateFormat("yyyyMMdd");

                        // 取得今天日期
                        Calendar cal = new GregorianCalendar();
                        cal.set(Calendar.HOUR_OF_DAY, 0); // anything 0 - 23
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        Date dateToday = cal.getTime();
                        String strToday = smdf.format(dateToday);

                        // 將通知紀錄寫入 NoticationLog 資料表
                        dbHelper.doInsertNoticationLogTable(
                                arylistPartonLoanOverDue.get(i).get("Barcode"),
                                strToday, 1);

                        blnNoticeRequest = true;
                    }

                }
            }

            // 陣列大於一才代表有預約書
            if (arylistPartonLoan_Request.size() > 1) {
                // Sets a title for the Inbox style big view
                inboxStyle.addLine("你有預約書:");

                // Moves events into the big view
                for (int i = 1; i < arylistPartonLoan_Request.size(); i++) {
                    inboxStyle.addLine(arylistPartonLoan_Request.get(i).get(
                            "Title")
                            + ","
                            + arylistPartonLoan_Request.get(i).get("Time"));
                }

                blnNoticeRequest = true;
            }

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
    }

    /**
     * 更新借閱資料
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private void doCheckIfNotice(Context context, String BarCode, int CheckType) {
        try {
            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(context);

            // 宣告處理JSON的物件
            JSONClass jsonClass = new JSONClass();

            // 將回傳的全部的借閱到期資料陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan 中
            ArrayList<HashMap<String, String>> arylistPartonLoanDue = dbHelper
                    .getPartonLoanTable(context, 1);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

package tw.edu.nchu.libapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class TaskServiceClass extends Service {

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
        mThread = new HandlerThread("name");

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
            // TODO 補上抓系統設定的排程更新參數
            SharedPreferences mPerferences = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            // 30秒後再執行排程工作
            mThreadHandler.postDelayed(this, 10800000);

            if (mPerferences.getBoolean("autosync", true)) {
                UpdateCirLogData("排程更新", getBaseContext());
                Log.i("TestSchUpdateTask", "1-RunSchUpdateCirLogMulti");
            } else {
                Log.i("TestSchUpdateTask", "2-noRunSchUpdateCirLogMulti");
            }

            if (mPerferences.getBoolean("notification", true)) {
                doNoticeCheck("排程通知", getBaseContext());
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
            // 宣告LOG物件，並決定工作類型
            LOGClass logclass = new LOGClass();
            String logJobType = JobType;

            // 宣告處理JSON的物件
            JSONClass jsonClass = new JSONClass();

            // 呼叫Token認證程序
            AuthClass authclass = new AuthClass();
            String strReturnContent = authclass
                    .doTokenAuth(context, logJobType);

            if (strReturnContent != null) {
                // 呼叫jsonClass處理JSON並寫入資料庫，會回傳交易狀態的各項值
                HashMap<String, String> hmOpResult = jsonClass
                        .setTokenResultJSONtoDB(strReturnContent, context);

                try {
                    // 如果系統運作正常才繼續下去
                    if (hmOpResult.get("OpResult").equals("Success")) {
                        // 如果認證成功才執行
                        if (hmOpResult.get("AuthResult").equals("Success")) {
                            // 寫log
                            logclass.setLOGtoDB(context, logJobType,
                                    new java.text.SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss")
                                            .format(new java.util.Date()),
                                    "5.認證成功");
                        } else {
                            // 寫log
                            logclass.setLOGtoDB(context, logJobType,
                                    new java.text.SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss")
                                            .format(new java.util.Date()),
                                    "5.認證失敗-Patron或Device Token錯誤");
                            // 認證失敗就丟個警告
                            Toast.makeText(
                                    context,
                                    R.string.ActivityCirculationLog_toastTokenFail,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // TODO: 增加系統狀態的判斷
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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
    private void doNoticeCheck(String JobType, Context context) {
        try {
            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(context);

            // 宣告LOG物件，並決定工作類型
            LOGClass logclass = new LOGClass();
            String logJobType = JobType;

            // 宣告處理JSON的物件
            JSONClass jsonClass = new JSONClass();

            // 將回傳的全部的借閱資料陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan 中
            ArrayList<HashMap<String, String>> arylistPartonLoan = dbHelper
                    .getPartonLoanTable(context, 1);

            String strContent = "";
            // 匯整要通知的內容
            for (int i = 1; i < arylistPartonLoan.size(); i++) {
                strContent = strContent + arylistPartonLoan.get(i).get("Title")
                        + "-" + arylistPartonLoan.get(i).get("Time") + "/r/n";
            }
            strContent = strContent + "即將到期";

            String strTitle = context.getString(R.string.app_name);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(strTitle).setContentText("你有到期書。")
                    .setTicker("NCHU Library notification");

            // 通知的內文條列
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            // 設定當按下這個通知之後要執行的activity
            Intent notifyIntent = new Intent(context, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle("你有到期書:");

            // Moves events into the big view
            for (int i = 1; i < arylistPartonLoan.size(); i++) {
                inboxStyle.addLine(arylistPartonLoan.get(i).get("Title") + ","
                        + arylistPartonLoan.get(i).get("Time"));
            }
            // Moves the big view style object into the notification object.
            mBuilder.setStyle(inboxStyle);

            // 供4.1以下的使用，如未加會crash
            PendingIntent appIntent = PendingIntent.getActivity(context, 0,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 顯示在狀態列的文字
            // mBuilder = "NCHU Library notification.";

            // 取得Notification服務
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mBuilder.setContentIntent(appIntent);
            mBuilder.setAutoCancel(true);

            // 送出Notification
            mNotificationManager.notify(0, mBuilder.build());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

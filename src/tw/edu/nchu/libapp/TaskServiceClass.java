package tw.edu.nchu.libapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
            // TODO 補上抓系統設定的排程更新參數
            SharedPreferences mPerferences = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            // 30秒後再執行排程工作
            mThreadHandler.postDelayed(this, 5000); // 10800000 = 3小時

            // 讀取設定檔是否允許同步
            if (mPerferences.getBoolean("autosync", true)) {
                UpdateCirLogData("排程更新", getBaseContext());
                Log.i("TestSchUpdateTask", "1-RunSchUpdateCirLogMulti");
            } else {
                Log.i("TestSchUpdateTask", "2-noRunSchUpdateCirLogMulti");
            }

            // 讀取設定檔是否允許通知
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
        // 宣告LOG物件，並決定工作類型
        LOGClass logclass = new LOGClass();
        String logJobType = JobType;

        // 宣告處理JSON的物件
        JSONClass jsonClass = new JSONClass();

        // 呼叫Token認證程序
        AuthClass authclass = new AuthClass();
        String strReturnContent = authclass.doTokenAuth(context, logJobType);

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
                                        .format(new java.util.Date()), "5.認證成功");
                    } else {
                        // 寫log
                        logclass.setLOGtoDB(context, logJobType,
                                new java.text.SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss")
                                        .format(new java.util.Date()),
                                "5.認證失敗-Patron或Device Token錯誤");
                        // 認證失敗就丟個警告
                        Toast.makeText(context,
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

            // 這邊的 setContentText 理論上只供 4.1 版以下的顯示
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(strTitle).setContentText("您有新的通訊息，請點選查閱。")
                    .setTicker("NCHU Library notification");

            // 通知的內文條列
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            // 設定當按下這個通知之後要執行的activity
            Intent notifyIntent = new Intent(context, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 設定通知欄的標題
            inboxStyle.setBigContentTitle("中興大學圖書館通知您");
            
            // 陣列大於一才代表有到期書
            if (arylistPartonLoanDue.size() > 1) {
                // Sets a title for the Inbox style big view
                inboxStyle.addLine("你有到期書:");

                // Moves events into the big view
                for (int i = 1; i < arylistPartonLoanDue.size(); i++) {
                    inboxStyle.addLine(arylistPartonLoanDue.get(i).get("Title")
                            + "," + arylistPartonLoanDue.get(i).get("Time"));
                }
            }

            // 陣列大於一才代表有過期書
            if (arylistPartonLoanOverDue.size() > 1) {
                // Sets a title for the Inbox style big view
                inboxStyle.addLine("你有過期書:");

                // Moves events into the big view
                for (int i = 1; i < arylistPartonLoanOverDue.size(); i++) {
                    inboxStyle
                            .addLine(arylistPartonLoanOverDue.get(i).get(
                                    "Title")
                                    + ","
                                    + arylistPartonLoanOverDue.get(i).get(
                                            "Time"));
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
            }

            // Moves the big view style object into the notification object.
            mBuilder.setStyle(inboxStyle);

            // 供4.1以下的使用，如未加會crash
            PendingIntent appIntent = PendingIntent.getActivity(context, 0,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 取得Notification服務
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 控制通知的編號
            int notifyID = 1;

            mBuilder.setContentIntent(appIntent);
            mBuilder.setAutoCancel(true);

            // 送出Notification
            mNotificationManager.notify(notifyID, mBuilder.build());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

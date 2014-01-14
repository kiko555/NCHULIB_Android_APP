package tw.edu.nchu.libapp;

import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

public class HTTPServiceClass extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    HandlerThread thread;

    // 宣告LOG物件，並決定工作類型
    LOGClass logclass = new LOGClass();
    String logJobType = "";

    // 宣告處理JSON的物件
    JSONClass jsonClass = new JSONClass();

    // 宣告認網路連線所需的各項變數
    String strOP = "";
    String strID = "";
    String strPassword = "";
    String strJsonDeviceInfo = "";
    String strReturnContent = "";

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // 宣告認證狀態所要用的hashmap
        HashMap<String, String> hmOpResult;

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.

            if (strOP.equals("AccAuth")) {
                logJobType = "帳密登入";

                // 呼叫帳密認證程序
                AuthClass authclass = new AuthClass();
                strReturnContent = authclass.doPasswordAuth(
                        getApplicationContext(), strID, strPassword,
                        strJsonDeviceInfo);

                // 如果真的有回傳值
                if (strReturnContent != null) {
                    // 呼叫jsonClass處理JSON並寫入資料庫，會回傳交易狀態的各項值
                    hmOpResult = jsonClass.setLoginJSONtoDB(strReturnContent,
                            getApplicationContext());
                }
            } else if (strOP.equals("TokenAuth")) {
                logJobType = "Token登入";

                // 呼叫Token認證程序
                AuthClass authclass = new AuthClass();
                strReturnContent = authclass.doTokenAuth(
                        getApplicationContext(), logJobType, strJsonDeviceInfo);

                // 如果真的有回傳值
                if (strReturnContent != null) {
                    // 呼叫jsonClass處理JSON並寫入資料庫，會回傳交易狀態的各項值
                    hmOpResult = jsonClass.setTokenResultJSONtoDB(
                            strReturnContent, getApplicationContext());
                }

            }

            // 建立廣播所需辨識碼
            Intent intent = new Intent("tw.edu.nchu.libapp.Auth_Message");

            // 判斷是否為空，如果是空的代表連線有問題，根本沒收到值，反之則要去判斷值的內容
            if (strReturnContent != null) {
                try {
                    // 如果系統運作正常才繼續下去
                    if (hmOpResult.get("OpResult").equals("Success")) {
                        // 如果認證成功才執行
                        if (hmOpResult.get("AuthResult").equals("Success")) {
                            // 寫log
                            logclass.setLOGtoDB(getApplicationContext(),
                                    logJobType, new java.text.SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss")
                                            .format(new java.util.Date()),
                                    "5.認證成功");

                            // 登入成功，廣播登入成功
                            intent.putExtra("OP", "AccAuth");
                            intent.putExtra("OpResult", "Success");
                            intent.putExtra("AuthResult", "Success");
                            sendBroadcast(intent);

                        } else {
                            // 寫log
                            logclass.setLOGtoDB(getApplicationContext(),
                                    logJobType, new java.text.SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss")
                                            .format(new java.util.Date()),
                                    "5.認證失敗-帳密或Token錯誤");

                            // 登入失敗，廣播登入失敗
                            intent.putExtra("OP", "AccAuth");
                            intent.putExtra("OpResult", "Success");
                            intent.putExtra("AuthResult", "Fail");
                            sendBroadcast(intent);
                        }
                    } else {
                        // TODO: 增加系統狀態的判斷
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                // 寫log
                logclass.setLOGtoDB(getApplicationContext(), logJobType,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new java.util.Date()),
                        "5.認證失敗-連線異常");

                // 登入失敗，廣播登入失敗
                intent.putExtra("OP", "AccAuth");
                intent.putExtra("OpResult", "Fail");
                intent.putExtra("AuthResult", "Fail");
                sendBroadcast(intent);
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

        // 帶入呼叫所傳的參數
        strOP = intent.getStringExtra("OP");
        strJsonDeviceInfo = intent.getStringExtra("jsonDeviceInfo");
        if (strOP.equals("AccAuth")) {
            strID = intent.getStringExtra("txID");
            strPassword = intent.getStringExtra("txPassword");
        } else if (strOP.equals("TokenAuth")) {
            // 目前不需先做額外處理
        }

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

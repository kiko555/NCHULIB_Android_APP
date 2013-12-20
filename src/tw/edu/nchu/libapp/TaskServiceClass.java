package tw.edu.nchu.libapp;

import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
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
        mThreadHandler.postDelayed(runUpdateCirLogMulti, 30000);
        super.onStart(intent, startId);
    }

    // TODO 補上被外部呼叫停止時
    @Override
    public void onDestroy() {
        mThreadHandler.removeCallbacks(runUpdateCirLogMulti);
        super.onDestroy();
        Log.e("TaskServeice","onDestroy");
    }

    // 執行緒工作-借閱資料更新多次
    private Runnable runUpdateCirLogMulti = new Runnable() {
        public void run() {
            // TODO 補上抓系統設定的排程更新參數
            // if (run) {

            mThreadHandler.postDelayed(this, 30000);
            UpdateCirLogData("排程更新", getBaseContext());

            // }
            Log.i("TestAsyncTask", "1-runUpdateCirLogMulti");
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
}

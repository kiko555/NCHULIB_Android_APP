//: object/MainActivity.java
package tw.edu.nchu.libapp;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * 前導程式，用以判斷應該轉到何畫面
 * 
 * @author kiko
 * @version 1.0
 */
public class MainActivity extends ActionBarActivity {

    // 建立取用資料庫的物件
    DBHelper dbHelper = new DBHelper(MainActivity.this);

    // 宣告特約工人的經紀人
    private Handler mThreadHandler;

    // 宣告特約工人
    private HandlerThread mThread;

    // 執行緒工作-借閱資料更新多次
    private Runnable runUpdateCirLogMulti = new Runnable() {
        public void run() {
            // TODO 補上抓系統設定的排程更新參數
            // if (run) {

            mThreadHandler.postDelayed(this, 30000);
            UpdateCirLogData("排程更新");

            // }
            Log.i("TestAsyncTask", "1-runUpdateCirLogMulti");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        int intCountPartonTable = dbHelper.doCountPartonTable();
        if (intCountPartonTable == 1) {
            // 在資料庫中有登入紀錄，立刻跳轉到借閱紀錄畫面
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, CirculationLogActivity.class);

            // 聘請一個特約工人，有其經紀人派遣其工人做事 (另起一個有Handler的Thread)
            mThread = new HandlerThread("name");

            // 讓Worker待命，等待其工作 (開啟Thread)
            mThread.start();

            // 找到特約工人的經紀人，這樣才能派遣工作 (找到Thread上的Handler)
            mThreadHandler = new Handler(mThread.getLooper());

            // 請經紀人指派工作名稱 ，給工人做
            mThreadHandler.postDelayed(runUpdateCirLogMulti, 30000);

            startActivity(intent);
            //finish();
        } else {
            // 在資料庫中無登入紀錄，立刻跳轉到登入畫面
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            //finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // 選單鈕被按住後會出現在動作
    @Override
    public boolean onOptionsItemSelected(MenuItem Item) {
        switch (Item.getItemId()) {
        case R.id.action_login:
            try {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        case R.id.action_cirlog:
            try {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CirculationLogActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        case R.id.action_settings:
            Toast.makeText(MainActivity.this, Item.getTitle(),
                    Toast.LENGTH_LONG).show();
            return true;
        case R.id.action_exit:
            finish();
            return true;
        default:
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    /**
     * 更新借閱資料
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private void UpdateCirLogData(String JobType) {
        try {
            // 宣告LOG物件，並決定工作類型
            LOGClass logclass = new LOGClass();
            String logJobType = JobType;

            // 宣告處理JSON的物件
            JSONClass jsonClass = new JSONClass();

            // 呼叫Token認證程序
            AuthClass authclass = new AuthClass();
            String strReturnContent = authclass.doTokenAuth(
                    MainActivity.this, logJobType);

            if (strReturnContent != null) {
                // 呼叫jsonClass處理JSON並寫入資料庫，會回傳交易狀態的各項值
                HashMap<String, String> hmOpResult = jsonClass
                        .setTokenResultJSONtoDB(strReturnContent,
                                MainActivity.this);

                try {
                    // 如果系統運作正常才繼續下去
                    if (hmOpResult.get("OpResult").equals("Success")) {
                        // 如果認證成功才執行
                        if (hmOpResult.get("AuthResult").equals("Success")) {
                            // 寫log
                            logclass.setLOGtoDB(MainActivity.this,
                                    logJobType, new java.text.SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss")
                                            .format(new java.util.Date()),
                                    "5.認證成功");
                        } else {
                            // 寫log
                            logclass.setLOGtoDB(MainActivity.this,
                                    logJobType, new java.text.SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss")
                                            .format(new java.util.Date()),
                                    "5.認證失敗-Patron或Device Token錯誤");
                            // 認證失敗就丟個警告
                            Toast.makeText(
                                    MainActivity.this,
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

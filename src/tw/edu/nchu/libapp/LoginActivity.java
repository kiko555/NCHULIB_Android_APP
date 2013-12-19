//: object/LoginActivity.java
package tw.edu.nchu.libapp;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 登入畫面所需程式
 * 
 * @author kiko
 * @version 1.0
 */
public class LoginActivity extends ActionBarActivity {
    /**
     * btLogin 登入按鈕 txID 讀者證號輸入欄 txPassword 密碼輸入欄
     */
    private Button btLogin;
    private EditText txID;
    private EditText txPassword;
    private CheckBox cbNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_login);

        // 隱藏讀取鈕
        setSupportProgressBarIndeterminateVisibility(false);

        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();

        // 同意條款的確認欄
        cbNotice = (CheckBox) findViewById(R.id.checkBox_LoginNotice);
        cbNotice.setOnCheckedChangeListener(cbListener);

        // 登入鈕
        btLogin = (Button) findViewById(R.id.button1);

        // 監聽ok鈕的動作
        btLogin.setOnClickListener(btListener);

        // 帶入填寫的欄位
        txID = (EditText) findViewById(R.id.editText1);
        txPassword = (EditText) findViewById(R.id.editText2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_activity, menu);
        return true;
    }

    // 選單鈕被按住後會出現在動作
    @Override
    public boolean onOptionsItemSelected(MenuItem Item) {
        switch (Item.getItemId()) {
        case R.id.action_systemlog:
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, SystemLogActivity.class);
            startActivity(intent);
            return true;
        default:
            return false;
        }
    }

    /**
     * 登入按鈕的動作，點選後開始執行HTTP連線工作
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private OnClickListener btListener = new OnClickListener() {
        public void onClick(View v) {
            /**
             * logJobType 工作類型
             * 
             * logStartTime 工作執行開始時間
             * 
             * logEndTime 工作結束時間
             * 
             * logExecuteStatus 工作執行狀態
             */

            if (cbNotice.isChecked()) {
                // 宣告LOG物件，並決定工作類型
                LOGClass logclass = new LOGClass();
                String logJobType = "帳密登入";

                // 宣告處理JSON的物件
                JSONClass jsonClass = new JSONClass();

                // Toast.makeText(MainActivity.this,
                // R.string.JSON_DataLoading,Toast.LENGTH_SHORT).show();

                // 資料開始抓取讀取鈕可見
                setSupportProgressBarIndeterminateVisibility(true);

                // 設定 HTTP Post 設備資訊參數
                JSONObject jsonDeviceInfo = null;
                try {
                    // 產生DeviceToken
                    DeviceClass deviceclass = new DeviceClass();
                    String strDeviceToken = deviceclass.doMakeDeviceToken(txID
                            .getEditableText().toString());

                    // 取得設備解析度
                    String strDisplayMetrics;
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    strDisplayMetrics = dm.heightPixels + " * "
                            + dm.widthPixels;

                    // 取得設備 SDK 版本
                    String strSdkVersion = Build.VERSION.SDK;

                    // 將前三項用JSON的格式串在一起
                    jsonDeviceInfo = new JSONObject();
                    jsonDeviceInfo.put("DeviceToken", strDeviceToken);
                    jsonDeviceInfo.put("AndroidVersion", strSdkVersion);
                    jsonDeviceInfo.put("Resolution", strDisplayMetrics);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // 呼叫帳密認證程序
                AuthClass authclass = new AuthClass();
                String strReturnContent = authclass.doPasswordAuth(
                        LoginActivity.this, txID.getEditableText().toString(),
                        txPassword.getEditableText().toString(),
                        jsonDeviceInfo.toString());

                if (strReturnContent != null) {
                    // 呼叫jsonClass處理JSON並寫入資料庫，會回傳交易狀態的各項值
                    HashMap<String, String> hmOpResult = jsonClass
                            .setLoginJSONtoDB(strReturnContent,
                                    LoginActivity.this);

                    try {
                        // 如果系統運作正常才繼續下去
                        if (hmOpResult.get("OpResult").equals("Success")) {
                            // 如果認證成功才執行
                            if (hmOpResult.get("AuthResult").equals("Success")) {
                                // 寫log
                                logclass.setLOGtoDB(LoginActivity.this,
                                        logJobType,
                                        new java.text.SimpleDateFormat(
                                                "yyyy-MM-dd HH:mm:ss")
                                                .format(new java.util.Date()),
                                        "5.認證成功");

                                // 登入成功，立刻Main來帶出排程功能
                                Intent intent = new Intent();
                                intent.setClass(LoginActivity.this,
                                        MainActivity.class);
                                startActivity(intent);
                            } else {
                                // 寫log
                                logclass.setLOGtoDB(LoginActivity.this,
                                        logJobType,
                                        new java.text.SimpleDateFormat(
                                                "yyyy-MM-dd HH:mm:ss")
                                                .format(new java.util.Date()),
                                        "5.認證失敗-帳密錯誤");
                                // 認證失敗就丟個警告
                                Toast.makeText(LoginActivity.this,
                                        R.string.ActivityLogin_toastLoginFail,
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

                // 資料抓取完畢將讀取鈕移除
                setSupportProgressBarIndeterminateVisibility(false);
            } else {
                // 警告要同意條款
                Toast.makeText(LoginActivity.this, R.string.action_LoginNotice,
                        Toast.LENGTH_SHORT).show();
            }

        }
    };

    /**
     * 監聽條款確認鈕是否有打勾
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private CheckBox.OnCheckedChangeListener cbListener = new CheckBox.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(
                        LoginActivity.this);

                dialog.setTitle(R.string.ActivityLogin_adNotice_Title); // 設定dialog
                                                                        // 的title顯示內容
                dialog.setMessage(R.string.ActivityLogin_adNotice_Message);
                dialog.setIcon(android.R.drawable.ic_menu_info_details);// 設定dialog
                // 的ICON
                dialog.setCancelable(false); // 關閉 Android
                                             // 系統的主要功能鍵(menu,home等...)

                dialog.setPositiveButton(
                        R.string.ActivityLogin_adNotice_btAgree,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // 按下"同意"以後要做的事情
                            }
                        });

                dialog.setNegativeButton(
                        R.string.ActivityLogin_adNotice_btNoAgree,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                cbNotice.setChecked(false);
                            }
                        });

                dialog.show();
            }
        }
    };

    /**
     * 確認是否資料庫是空的，如果有資料就跳轉到流通紀錄畫面
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private void CheckIfDBEmpty() {
        /**
         * dbHelper 資料庫的物件
         * 
         * intCountPartonTable 讀者資料表筆數
         */
        // 建立取用資料庫的物件
        DBHelper dbHelper = new DBHelper(LoginActivity.this);

        // 取得讀者資料表筆數
        int intCountPartonTable = dbHelper.doCountPartonTable();

        // 在資料庫中有登入紀錄，立刻跳轉到流通紀錄畫面
        if (intCountPartonTable == 1) {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, CirculationLogActivity.class);
            startActivity(intent);
            finish();
        }

        // 資料庫關閉
        dbHelper.close();

    }

}
// /:~
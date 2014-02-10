//: object/LoginActivity.java
package tw.edu.nchu.libapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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

    // 宣告自訂的廣播接受器
    mServiceReceiver mServiceReceiver;

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

        // 宣告一個自訂的BroadcastReceiver , 稍後我們會在onResume() 動態註冊
        mServiceReceiver = new mServiceReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();

        // 宣告一個IntentFilter並使用我們之前自訂的action
        IntentFilter IFilter = new IntentFilter();
        IFilter.addAction("tw.edu.nchu.libapp.Auth_Message");

        // 動態註冊BroadcastReceiver
        registerReceiver(mServiceReceiver, IFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();

        // 動態註銷BroadcastReceiver
        unregisterReceiver(mServiceReceiver);
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

            if (!txID.getEditableText().toString().matches("")
                    && !txPassword.getEditableText().toString().matches("")) {
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

                    // 取得設備資訊
                    DeviceClass deviceclass = new DeviceClass();
                    String strDeviceInfo = deviceclass.getDeviceInfoJSON(
                            getApplicationContext(), txID.getEditableText()
                                    .toString());

                    // 建立連線服務完成認證工作
                    Intent HTTPServiceIntent = new Intent(LoginActivity.this,
                            HTTPServiceClass.class);

                    // HTTP服務所要送出的值
                    HTTPServiceIntent.putExtra("OP", "AccAuth");
                    HTTPServiceIntent.putExtra("txID", txID.getEditableText()
                            .toString());
                    HTTPServiceIntent.putExtra("txPassword", txPassword
                            .getEditableText().toString());
                    HTTPServiceIntent.putExtra("jsonDeviceInfo", strDeviceInfo);

                    // 啟動HTTP服務
                    startService(HTTPServiceIntent);

                } else {
                    // 警告要同意條款
                    Toast.makeText(LoginActivity.this,
                            R.string.action_LoginNotice, Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                // 警告帳號跟密碼不能留空
                Toast.makeText(LoginActivity.this,
                        R.string.ActivityLogin_toastFieldEmpty,
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

    /**
     * 自訂的廣播接受器類別
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public class mServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            Log.d("Test", "Data:" + intent.getStringExtra("Data"));
            // 顯示之前擺入sentIntent的附加資訊

            if (intent.getAction().equals("tw.edu.nchu.libapp.Auth_Message")) {

                // 資料抓取完畢將讀取鈕移除
                setSupportProgressBarIndeterminateVisibility(false);

                try {
                    // 先判斷是否要強制更新版本
                    if (Double.valueOf(intent
                            .getStringExtra("AppStableVersion")) > Double
                            .valueOf(context
                                    .getPackageManager()
                                    .getPackageInfo(context.getPackageName(), 0).versionName)) {
                        // 如果版本過舊就強迫跳鎖定畫面
                        Intent intent1 = new Intent();
                        intent1.setClass(LoginActivity.this, LockActivity.class);
                        startActivity(intent1);
                        finish();
                    } else {

                        // 操作成功與否的判斷，如果成功之後才判斷認證結果
                        if (intent.getStringExtra("OpResult").equals("Success")) {
                            if (intent.getStringExtra("AuthResult").equals(
                                    "Success")) {
                                // 登入成功，立刻Main來帶出排程功能
                                Intent intent1 = new Intent();
                                intent1.setClass(LoginActivity.this,
                                        MainActivity.class);
                                startActivity(intent1);
                            } else {
                                // 認證失敗就丟個警告
                                Toast.makeText(LoginActivity.this,
                                        R.string.ActivityLogin_toastLoginFail,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (intent.getStringExtra("OpInfo").equals("")) {
                                // 特別針對失敗中有丟OpInfo的警告
                                Toast.makeText(LoginActivity.this,
                                        R.string.Check_Network,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // 操作失敗就丟個連線警告
                                Toast.makeText(LoginActivity.this,
                                        intent.getStringExtra("OpInfo"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
// /:~
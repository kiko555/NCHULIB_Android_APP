//: object/SettingsActivity.java
package tw.edu.nchu.libapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 設定畫面所需程式
 * 
 * @author kiko
 * @version 1.0
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener, OnPreferenceClickListener {
    /**
     * btLogout 登出按鈕
     * 
     * mPreferences 設定的選單
     * 
     * 
     */
    private Button btLogout;
    SharedPreferences mPreferences;
    Boolean blnLogoutClicked;

    // 宣告LOG物件，並決定工作類型
    LOGClass logclass = new LOGClass();
    String logJobType = "設定操作";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 不同版本的android應用不同的畫面結構，因為Honeycomb前，畫面多了一個回前頁的按鈕，
        // 為何要這樣做是因為ActionBar沒有實作PreferenceActivity，為了怕使用者手機沒有回上一頁的按鈕，所以先實作一個
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // 此外為了有ActionBar的頁面，再多補上一個快速回到首頁的功能鈕 for >= 4.1 (API level 16)
            getActionBar().setDisplayHomeAsUpEnabled(true);
            setContentView(R.layout.activity_settings_api16up);
        } else {
            setContentView(R.layout.activity_settings);
        }
        addPreferencesFromResource(R.xml.mypreferences);

        // 實作設定的登出欄監聽功能
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Preference mPreferences = (Preference) findPreference("logoutKey");
        mPreferences.setOnPreferenceClickListener(this);

    }

    // 註冊設定選單變動實的監聽器
    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    // 反註冊設定選單變動實的監聽器
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // 當設定選單有所變動時，針對不同欄有不同的動作
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {

        // 實作選單物件，供後續取用
        SharedPreferences mPerferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        // Let's do something a preference value changes
        if (key.equals("autosync")) {
            // 判斷同步設定產生對應內容
            if (mPerferences.getBoolean("autosync", true)) {
                // 寫log
                logclass.setLOGtoDB(SettingsActivity.this, logJobType,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new java.util.Date()), "同步功能打開");
            } else {
                // 寫log
                logclass.setLOGtoDB(SettingsActivity.this, logJobType,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new java.util.Date()), "同步功能關閉");
            }
        } else if (key.equals("notification")) {
            // 判斷通知設定產生對應內容
            if (mPerferences.getBoolean("notification", true)) {
                // 寫log
                logclass.setLOGtoDB(SettingsActivity.this, logJobType,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new java.util.Date()), "通知功能打開");
            } else {
                // 寫log
                logclass.setLOGtoDB(SettingsActivity.this, logJobType,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new java.util.Date()), "通知功能關閉");
            }
        }
    }

    // 設定選單被點選時，過濾是否是點選登出，如果是就做登出動作
    @Override
    public boolean onPreferenceClick(Preference preference) {
        // 取得是選單是否點選
        blnLogoutClicked = mPreferences.getBoolean("logoutKey", true);

        if (blnLogoutClicked) {
            // 宣告LOG物件，並決定工作類型
            LOGClass logclass = new LOGClass();
            String logJobType = "帳密登出";

            // 寫log
            logclass.setLOGtoDB(SettingsActivity.this, logJobType,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date()), "清空讀者檔及借閱資料");

            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(SettingsActivity.this);

            // 先清空讀者及借閱資料表
            dbHelper.doEmptyPartonLoanTable();
            dbHelper.doEmptyPartonTable();

            // 也清空通知紀錄表
            dbHelper.doEmptyNotificationLog();

            // 清空後直接跳轉到登入畫面
            Intent intent = new Intent();
            intent.setClass(SettingsActivity.this, MainActivity.class);
            startActivity(intent);

            // 關掉這個活動
            finish();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_activity, menu);
        return true;
    }

    // 選單鈕被按住後會出現在動作
    @Override
    public boolean onOptionsItemSelected(MenuItem Item) {
        switch (Item.getItemId()) {
        case R.id.action_cirlog:
            try {
                Intent intent = new Intent();
                intent.setClass(SettingsActivity.this,
                        CirculationLogActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        case R.id.action_systemlog:
            Intent intent1 = new Intent();
            intent1.setClass(SettingsActivity.this, SystemLogActivity.class);
            startActivity(intent1);
            return true;
        case android.R.id.home:

            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new
                // task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }

            return true;
            // default:
            // return false;
        }
        return super.onOptionsItemSelected(Item);

    }

    /**
     * 登出按鈕的動作，直接清空資料庫，並回到登入畫面
     * 
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private OnClickListener btListener = new OnClickListener() {
        public void onClick(View v) {
            // 宣告LOG物件，並決定工作類型
            LOGClass logclass = new LOGClass();
            String logJobType = "帳密登出";

            // 寫log
            logclass.setLOGtoDB(SettingsActivity.this, logJobType,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date()), "清空讀者檔及借閱資料");

            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(SettingsActivity.this);

            // 先清空讀者及借閱資料表
            dbHelper.doEmptyPartonLoanTable();
            dbHelper.doEmptyPartonTable();

            // 清空後直接跳轉到登入畫面
            Intent intent = new Intent();
            intent.setClass(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);

            // 關掉這個活動
            finish();

        }
    };
}
// /:~
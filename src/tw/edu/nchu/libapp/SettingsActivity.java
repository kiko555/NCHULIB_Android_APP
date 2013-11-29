//: object/SettingsActivity.java
package tw.edu.nchu.libapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * 設定畫面所需程式
 * 
 * @author kiko
 * @version 1.0
 */
public class SettingsActivity extends ActionBarActivity {
    /**
     *  btLogout 登出按鈕
     */
    private Button btLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_settings);

        // 登出鈕鈕
        btLogout = (Button) findViewById(R.id.button1);

        // 監聽登出鈕的動作
        btLogout.setOnClickListener(btListener);

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
        default:
            return false;
        }
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
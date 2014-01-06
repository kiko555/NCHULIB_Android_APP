//: object/SystemLogActivity.java
package tw.edu.nchu.libapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * 系統操作紀錄
 * 
 * @author kiko
 * @version 1.0
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SystemLogActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_systemlog);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // 此外為了有ActionBar的頁面，再多補上一個快速回到首頁的功能鈕 for > 4.1 (API level 16)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 隱藏讀取鈕
        setSupportProgressBarIndeterminateVisibility(false);

        // 帶入系統紀錄
        drawTable();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(SystemLogActivity.this);

            int intCountPartonTable = dbHelper.doCountPartonTable();

            // 關閉資料庫
            dbHelper.close();

            if (intCountPartonTable != 1) {
                // 在資料庫中登入紀錄不對，選擇可以看到系統紀錄的選單，但其它不行
                getMenuInflater().inflate(R.menu.menu_systemlog_only_login,
                        menu);
            } else {
                // 在資料庫中有一筆登入紀錄，選擇可以看到全部選單
                getMenuInflater().inflate(R.menu.menu_systemlog, menu);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    // 選單鈕被按住後會出現在動作
    @Override
    public boolean onOptionsItemSelected(MenuItem Item) {
        switch (Item.getItemId()) {
        case R.id.action_cirlog:
            try {
                Intent intent = new Intent();
                intent.setClass(SystemLogActivity.this,
                        CirculationLogActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        case R.id.action_settings:
            Intent intent = new Intent();
            intent.setClass(SystemLogActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        case R.id.action_login:
            Intent intent1 = new Intent();
            intent1.setClass(SystemLogActivity.this, LoginActivity.class);
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
        }
        return super.onOptionsItemSelected(Item);
    }

    public void drawTable() {

        ListView list = (ListView) findViewById(R.id.SystemLogListView1);

        DBHelper dbHelper = new DBHelper(this);

        // 將操作紀錄透過HashMap方式儲存，最後再以arraylist的方式提供給listview介面使用
        ArrayList<HashMap<String, String>> listSystemLog = dbHelper
                .getAllSystemLog(SystemLogActivity.this);

        // listview的中介層
        SimpleAdapter mSystemLogAdapter = new SimpleAdapter(this,
                listSystemLog, R.layout.activity_systemlog_row, new String[] {
                        "JobType", "Time", "ExecuteStatus" }, new int[] {
                        R.id.CELL_JobType, R.id.CELL_Time,
                        R.id.CELL_ExecuteStatus });
        list.setAdapter(mSystemLogAdapter);

        dbHelper.close();
    }

}
// /:~
//: object/SystemLogActivity.java
package tw.edu.nchu.libapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
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
public class SystemLogActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_systemlog);

        // 帶入系統紀錄
        drawTable();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_systemlog, menu);
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
        default:
            return false;
        }
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

        // Cursor percentages = dbHelper.getPercentage(db);
        // ... // do something with the Cursors
        // ..
        // finally

        // percentages.close();
        // dbHelper.closeDb(db);
    }

}
// /:~
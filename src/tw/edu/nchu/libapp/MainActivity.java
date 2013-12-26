//: object/MainActivity.java
package tw.edu.nchu.libapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
    // 用來判斷服務是否已被帶起來了
    static long longCheckService = 0;
    // 建立取用資料庫的物件
    DBHelper dbHelper = new DBHelper(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        int intCountPartonTable = dbHelper.doCountPartonTable();
        if (intCountPartonTable == 1) {
            if (longCheckService == 0) {
                // 建背景更新程式
                Intent TaskServiceIntent = new Intent(getApplicationContext(),
                        TaskServiceClass.class);
                startService(TaskServiceIntent);

                longCheckService++;
            }

            // 在資料庫中有登入紀錄，立刻跳轉到借閱紀錄畫面
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, CirculationLogActivity.class);

            startActivity(intent);
            finish();
        } else {
            // 在資料庫中無登入紀錄，立刻跳轉到登入畫面
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
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
                finish();
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
                finish();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        case R.id.action_settings:
            Toast.makeText(MainActivity.this, Item.getTitle(),
                    Toast.LENGTH_LONG).show();
            finish();
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

    @Override
    protected void onStart() {
        super.onStart();
        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();
    }

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
        DBHelper dbHelper = new DBHelper(MainActivity.this);

        // 取得讀者資料表筆數
        int intCountPartonTable = dbHelper.doCountPartonTable();

        // 在資料庫中有登入紀錄，立刻跳轉到流通紀錄畫面
        if (intCountPartonTable == 1) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, CirculationLogActivity.class);
            startActivity(intent);
            finish();
        }

        // 資料庫關閉
        dbHelper.close();

    }

}

//: object/MainActivity.java
package tw.edu.nchu.libapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        // 右上角顯示讀取中圖示
        // setSupportProgressBarIndeterminateVisibility(true);

        int intCountPartonTable = dbHelper.doCountPartonTable();
        if (intCountPartonTable == 1) {
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

}

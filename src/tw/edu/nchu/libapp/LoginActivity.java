//: object/LoginActivity.java
package tw.edu.nchu.libapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_login);

        // JSON資料接收鈕
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
        case R.id.action_exit:
            finish();
            return true;
        default:
            return false;
        }
    }

    /**
     * HTTP連線的整個過程
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    class RetreiveHTTPTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // Initialize progress
            setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                // 透過keystore來解SSL
                KeyStore trustStore = KeyStore.getInstance(KeyStore
                        .getDefaultType());
                InputStream instream = getResources()
                        .openRawResource(R.raw.api);
                try {
                    trustStore.load(instream, null);
                } finally {
                    instream.close();
                }
                SSLSocketFactory socketFactory = new SSLSocketFactory(
                        trustStore);
                Scheme sch = new Scheme("https", socketFactory, 443);

                // 初始apache的httpclient class
                HttpClient client = new DefaultHttpClient();

                // 將keystore及SSLSocketFactory指回來給httoclient使用
                client.getConnectionManager().getSchemeRegistry().register(sch);

                // 給予連線的網址
                HttpPost httppost = new HttpPost(
                        "https://api.lib.nchu.edu.tw/php/appagent/");

                // 帶入POST要傳的參數
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                        2);
                nameValuePairs.add(new BasicNameValuePair("op", "AccAuth"));
                nameValuePairs.add(new BasicNameValuePair("sid", txID
                        .getEditableText().toString()));
                nameValuePairs.add(new BasicNameValuePair("pwd", txPassword
                        .getEditableText().toString()));

                // 產生DeviceID
                DeviceClass deviceclass = new DeviceClass();
                String strDeviceToken = deviceclass.doMakeDeviceToken(txID
                        .getEditableText().toString());

                // 取得設備解析度
                String strDisplayMetrics;
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                strDisplayMetrics = dm.heightPixels + " * " + dm.widthPixels;

                // 取得設備 SDK 版本
                String strSdkVersion = Build.VERSION.SDK;

                // 键address的值是对象，所以又要创建一个对象
                JSONObject deviceinfo = new JSONObject();
                deviceinfo.put("DeviceToken", strDeviceToken);
                deviceinfo.put("AndroidVersion", strSdkVersion);
                deviceinfo.put("Resolution", strDisplayMetrics);

                nameValuePairs.add(new BasicNameValuePair("deviceinfo",
                        deviceinfo.toString()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = client.execute(httppost);

                // TODO: add exception handle
                // 確認回傳是否異常
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != 200) {
                    // throw new IOException("Invalid response from server: " +
                    // status.toString());
                    Toast.makeText(LoginActivity.this, R.string.Check_Network,
                            Toast.LENGTH_SHORT).show();
                    super.cancel(true);
                    return null;
                } else {
                    // 將回傳值丟進buffer
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity()
                                    .getContent()));

                    // 有值才將buffer內的值彙整起來
                    String line = "";
                    String allline = "";
                    while ((line = rd.readLine()) != null) {
                        allline += line;
                    }

                    // 確認是否沒有收到資料，如果是空的就丟訊息提醒，並停止後續處理
                    if (allline.equals("")) {
                        Toast.makeText(LoginActivity.this,
                                R.string.Check_Network, Toast.LENGTH_SHORT)
                                .show();
                        super.cancel(true);
                        return null;
                    } else {
                        return allline;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, R.string.Check_Network,
                        Toast.LENGTH_SHORT).show();
                super.cancel(true);
                return null;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO: check this.exception
            // TODO: do something with the feed

            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(LoginActivity.this);

            try {

                // 確認是否沒有收到資料
                if (result.equals("")) {
                    Toast.makeText(LoginActivity.this,
                            R.string.JSON_Data_downlaod_fail,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // 測試回傳的內容
                    // Toast.makeText(LoginActivity.this, result.toString(),
                    // Toast.LENGTH_SHORT).show();

                    // 抓取JSON物件中的特定陣列
                    JSONArray jsonResult1Array;
                    JSONArray jsonResult2Array;

                    String strOpResult, strAuthResult, strPatronName, strPID;
                    String strPatronBarCode, strPatronToken, strErrorInfo;

                    JSONArray jsonResultTitleArray;
                    JSONArray jsonResultBarcodeArray;
                    JSONArray jsonResultDataTypeArray;
                    JSONArray jsonResultEndDateArray;

                    strOpResult = new JSONObject(result).getString("op_result");
                    // 如果系統運作正常才繼續下去
                    if (strOpResult.equals("success")) {
                        strAuthResult = new JSONObject(result)
                                .getString("auth_result");
                        // 如果認證成功才執行
                        if (strAuthResult.equals("success")) {
                            strPatronName = new JSONObject(result)
                                    .getString("PatronName");
                            strPID = new JSONObject(result).getString("PID");
                            strPatronBarCode = new JSONObject(result)
                                    .getString("PatronBarCode");
                            strPatronToken = new JSONObject(result)
                                    .getString("PatronToken");

                            // 先清空讀者資料表
                            dbHelper.doEmptyPartonTable();
                            // 寫入讀者資料
                            dbHelper.doInsertPartonTable(strPID,
                                    strPatronBarCode, strPatronName,
                                    strPatronToken);

                            jsonResultTitleArray = new JSONObject(result)
                                    .getJSONObject("PatronLoan").getJSONArray(
                                            "Z13_TITLE");
                            jsonResultBarcodeArray = new JSONObject(result)
                                    .getJSONObject("PatronLoan").getJSONArray(
                                            "Z30_BARCODE");
                            jsonResultDataTypeArray = new JSONObject(result)
                                    .getJSONObject("PatronLoan").getJSONArray(
                                            "DATA_TYPE");
                            jsonResultEndDateArray = new JSONObject(result)
                                    .getJSONObject("PatronLoan").getJSONArray(
                                            "END_DATE");

                            // 判斷所得JSON格式是否有錯
                            if (jsonResultTitleArray == null
                                    || jsonResultEndDateArray == null) {
                                Toast.makeText(LoginActivity.this,
                                        R.string.JSON_Data_error,
                                        Toast.LENGTH_SHORT).show();
                            } else {

                                // 先清空讀者借閱資料表
                                dbHelper.doEmptyPartonLoanTable();

                                // 取出JSON陣列內所有內容
                                for (int i = 0; i < jsonResultTitleArray
                                        .length(); i++) {
                                    // 寫入讀者借閱資料
                                    dbHelper.doInsertPartonLoanTable(
                                            jsonResultTitleArray.get(i)
                                                    .toString(),
                                            jsonResultBarcodeArray.get(i)
                                                    .toString(),
                                            jsonResultDataTypeArray.get(i)
                                                    .toString(),
                                            jsonResultEndDateArray.get(i)
                                                    .toString());

                                    // 登入成功，也成功寫入資料庫，立刻跳轉到借閱紀錄畫面
                                    Intent intent = new Intent();
                                    intent.setClass(LoginActivity.this,
                                            CirculationLogActivity.class);
                                    startActivity(intent);
                                }

                            }
                        }
                    } else {
                        strErrorInfo = new JSONObject(result)
                                .getString("error_info");
                        Toast.makeText(LoginActivity.this, strErrorInfo,
                                Toast.LENGTH_SHORT).show();
                    }

                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 資料抓取完畢將讀取鈕移除
            setSupportProgressBarIndeterminateVisibility(false);
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
             *  retreivehttpask HTTP連線工作
             */

            // Toast.makeText(MainActivity.this,
            // R.string.JSON_DataLoading,Toast.LENGTH_SHORT).show();

            // 呼叫非同步架構抓取http資料
            RetreiveHTTPTask retreivehttpask;
            try {
                retreivehttpask = (RetreiveHTTPTask) new RetreiveHTTPTask()
                        .execute("https://api.lib.nchu.edu.tw/php/appagent/");

                // 倘若失敗時的動作
                if (retreivehttpask == null) {
                    Toast.makeText(LoginActivity.this, R.string.Check_Network,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
         *  dbHelper 資料庫的物件
         *  intCountPartonTable 讀者資料表筆數
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

        // 關閉資料庫
        dbHelper.close();
    }
}
// /:~
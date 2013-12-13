//: object/AuthClass.java
package tw.edu.nchu.libapp;

import java.util.concurrent.ExecutionException;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.widget.Toast;

/**
 * 這是用來處理認證的物件 1.Token認證，回傳布林
 * 
 * @author kiko
 * @version 1.0
 */
public class AuthClass {
    /**
     * 物件進入點
     * 
     */
    public AuthClass() {
    }

    /**
     * 帳密認證
     * 
     * @return strReturnContent Server回傳的內容
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public String doPasswordAuth(Context context, String strSID,
            String strPassword, String strDeviceInfo) {
        /**
         * strPid 讀者證號 m_szUniqueID 設備唯一辨識碼
         */
        // 宣告LOG物件，並決定工作類型
        LOGClass logclass = new LOGClass();
        String logJobType = "帳密登入";

        // 宣告處理JSON的物件
        JSONClass jsonClass = new JSONClass();

        // 寫log
        logclass.setLOGtoDB(context, logJobType,
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date()), "1.開始認證程序");

        // 建立取用資料庫的物件
        DBHelper dbHelper = new DBHelper(context);

        String[] aryPatron = dbHelper.getPartonTable();

        // 關閉資料庫
        dbHelper.close();

        // 產生DeviceToken
        DeviceClass deviceclass = new DeviceClass();
        String strDeviceToken = deviceclass.doMakeDeviceToken(strSID);

        // 呼叫http連線物件，並填入所需相關資料
        HTTPTaskClass httpTaskClass = new HTTPTaskClass();
        httpTaskClass.instream = context.getResources().openRawResource(
                R.raw.api);

        // 設定HTTP Post 帳密參數
        httpTaskClass.nameValuePairs
                .add(new BasicNameValuePair("op", "AccAuth"));
        httpTaskClass.nameValuePairs.add(new BasicNameValuePair("SID", strSID));
        httpTaskClass.nameValuePairs.add(new BasicNameValuePair("PWD",
                strPassword));
        httpTaskClass.nameValuePairs.add(new BasicNameValuePair("DeviceInfo",
                strDeviceInfo));

        // 寫log
        logclass.setLOGtoDB(context, logJobType,
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date()), "2.開始連線");

        String strReturnContent = null;
        try {
            // 進行連線
            AsyncTask<String, Void, String> asyncTask = httpTaskClass
                    .execute("https://api.lib.nchu.edu.tw/php/appagent/");

            strReturnContent = asyncTask.get();
        } catch (Exception e) {
            // 寫log
            logclass.setLOGtoDB(context, logJobType,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date()),
                    "3.連線抓取資料其它異常，請確認網路連線是否正常。");
            Toast.makeText(context, "連線抓取資料其它異常，請確認網路連線是否正常。",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // 寫log
        logclass.setLOGtoDB(context, logJobType,
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date()), "4.連線結束");

        return strReturnContent;
    }

    /**
     * Token 認證
     * 
     * @return strReturnContent Server回傳的內容
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public String doTokenAuth(Context context) {
        /**
         * strPid 讀者證號 m_szUniqueID 設備唯一辨識碼
         */
        // 宣告LOG物件，並決定工作類型
        LOGClass logclass = new LOGClass();
        String logJobType = "Token登入";

        // 宣告處理JSON的物件
        JSONClass jsonClass = new JSONClass();

        // 寫log
        logclass.setLOGtoDB(context, logJobType,
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date()), "1.開始認證程序");

        // 建立取用資料庫的物件
        DBHelper dbHelper = new DBHelper(context);

        String[] aryPatron = dbHelper.getPartonTable();

        // 關閉資料庫
        dbHelper.close();

        // 產生DeviceToken
        DeviceClass deviceclass = new DeviceClass();
        String strDeviceToken = deviceclass.doMakeDeviceToken(aryPatron[1]
                .toString());

        // 呼叫http連線物件，並填入所需相關資料
        HTTPTaskClass httpTaskClass = new HTTPTaskClass();
        httpTaskClass.instream = context.getResources().openRawResource(
                R.raw.api);

        // 設定HTTP Post 帳密參數
        httpTaskClass.nameValuePairs.add(new BasicNameValuePair("op",
                "TokenAuth"));
        httpTaskClass.nameValuePairs.add(new BasicNameValuePair("PatronToken",
                aryPatron[3].toString()));
        httpTaskClass.nameValuePairs.add(new BasicNameValuePair("DeviceToken",
                strDeviceToken));

        // 寫log
        logclass.setLOGtoDB(context, logJobType,
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date()), "2.開始連線");

        String strReturnContent = null;
        try {
            // 進行連線
            AsyncTask<String, Void, String> asyncTask = httpTaskClass
                    .execute("https://api.lib.nchu.edu.tw/php/appagent/");

            strReturnContent = asyncTask.get();
        } catch (Exception e) {
            // 寫log
            logclass.setLOGtoDB(context, logJobType,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date()),
                    "3.連線抓取資料其它異常，請確認網路連線是否正常。");
            Toast.makeText(context, "連線抓取資料其它異常，請確認網路連線是否正常。",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }

        // 寫log
        logclass.setLOGtoDB(context, logJobType,
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new java.util.Date()), "4.連線結束");

        return strReturnContent;
    }

}

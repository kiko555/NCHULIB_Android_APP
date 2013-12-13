//: object/JSONClass.java
package tw.edu.nchu.libapp;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * 處理JSON
 * 
 * @author kiko
 * @version 1.0
 */
public class JSONClass {
    /**
     * 
     */
    // private Context context;

    /**
     * 物件進入點
     * 
     */
    public JSONClass() {
    }

    /**
     * 直接將登入後收到的JSON處理後，直接寫入DB，並回傳狀態
     * 
     * @param LoginJSON
     *            登入後回傳的JSON
     * 
     * @param context
     *            使用者的PID
     * 
     * @return aryReturnResult 將系統狀態、認證狀態回傳
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public HashMap<String, String> setLoginJSONtoDB(String LoginJSON,
            Context context) {
        /**
         * 
         */
        // 宣告LOG物件，並決定工作類型
        LOGClass logclass = new LOGClass();
        String logJobType = "JSON處理";

        String strLoginJSON = LoginJSON;

        // 回傳所需的hashmap
        HashMap<String, String> hmReturnResult = new HashMap<String, String>();

        String strPatronName = null, strPID = null;
        String strPatronBarCode = null, strPatronToken = null;

        JSONArray jsonResultTitleArray = null;
        JSONArray jsonResultBarcodeArray = null;
        JSONArray jsonResultDataTypeArray = null;
        JSONArray jsonResultEndDateArray = null;

        // 建立取用資料庫的物件
        DBHelper dbHelper = new DBHelper(context);

        // 先將JSON內解出來放到各個陣列中
        try {
            // 寫log
            logclass.setLOGtoDB(context, logJobType,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date()), "1.JSON解析");

            hmReturnResult.put("OpResult", new JSONObject(strLoginJSON)
                    .getJSONObject("Status").getString("OpResult"));
            hmReturnResult.put("OpInfo", new JSONObject(strLoginJSON)
                    .getJSONObject("Status").getString("OpInfo"));
            hmReturnResult.put("AuthResult", new JSONObject(strLoginJSON)
                    .getJSONObject("Status").getString("AuthResult"));
            hmReturnResult.put("AuthInfo", new JSONObject(strLoginJSON)
                    .getJSONObject("Status").getString("AuthInfo"));
            hmReturnResult.put("SysStatus", new JSONObject(strLoginJSON)
                    .getJSONObject("Status").getString("SysStatus"));
            hmReturnResult.put("SysInfo", new JSONObject(strLoginJSON)
                    .getJSONObject("Status").getString("SysInfo"));
            hmReturnResult.put("AppStableVersion", new JSONObject(strLoginJSON)
                    .getJSONObject("Status").getString("AppStableVersion"));

            // 如果認證有過才去解析它的資料
            if (hmReturnResult.get("AuthResult").equals("Success")) {
                // 寫log
                logclass.setLOGtoDB(context, logJobType,
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new java.util.Date()), "2.認證成功進一步解析");

                strPatronName = new JSONObject(strLoginJSON).getJSONObject(
                        "PatronInfo").getString("PatronName");
                strPID = new JSONObject(strLoginJSON).getJSONObject(
                        "PatronInfo").getString("PID");
                strPatronBarCode = new JSONObject(strLoginJSON).getJSONObject(
                        "PatronInfo").getString("PatronBarCode");
                strPatronToken = new JSONObject(strLoginJSON).getJSONObject(
                        "PatronInfo").getString("PatronToken");

                jsonResultTitleArray = new JSONObject(strLoginJSON)
                        .getJSONObject("PatronLoan").getJSONArray("Z13_TITLE");
                jsonResultBarcodeArray = new JSONObject(strLoginJSON)
                        .getJSONObject("PatronLoan")
                        .getJSONArray("Z30_BARCODE");
                jsonResultDataTypeArray = new JSONObject(strLoginJSON)
                        .getJSONObject("PatronLoan").getJSONArray("DATA_TYPE");
                jsonResultEndDateArray = new JSONObject(strLoginJSON)
                        .getJSONObject("PatronLoan").getJSONArray("END_DATE");
            }
        } catch (JSONException e) {
            // 寫log
            logclass.setLOGtoDB(context, logJobType,
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new java.util.Date()), "2.認證失敗不進行解析");
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            // 如果系統運作正常才繼續下去，並在回傳陣列寫入系統狀態
            if (hmReturnResult.get("OpResult").equals("Success")) {
                // 如果認證成功才執行
                if (hmReturnResult.get("AuthResult").equals("Success")) {

                    // 判斷所得JSON是否有內容
                    if (jsonResultTitleArray == null
                            || jsonResultEndDateArray == null) {
                        // 寫log
                        logclass.setLOGtoDB(context, logJobType,
                                new java.text.SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss")
                                        .format(new java.util.Date()),
                                "3.借閱資料為空的");
                    } else {
                        // 寫log
                        logclass.setLOGtoDB(context, logJobType,
                                new java.text.SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss")
                                        .format(new java.util.Date()),
                                "3.借閱資料寫入DB");

                        // 先清空讀者資料表
                        dbHelper.doEmptyPartonTable();

                        // 先清空讀者借閱資料表
                        dbHelper.doEmptyPartonLoanTable();

                        // 寫入讀者資料
                        dbHelper.doInsertPartonTable(strPID, strPatronBarCode,
                                strPatronName, strPatronToken);

                        // 取出讀者借閱資料JSON陣列內所有內容
                        for (int i = 0; i < jsonResultTitleArray.length(); i++) {
                            try {

                                // 寫入讀者借閱資料
                                dbHelper.doInsertPartonLoanTable(
                                        jsonResultTitleArray.get(i).toString(),
                                        jsonResultBarcodeArray.get(i)
                                                .toString(),
                                        jsonResultDataTypeArray.get(i)
                                                .toString(),
                                        jsonResultEndDateArray.get(i)
                                                .toString());
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }

                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return hmReturnResult;
    }
}
// /:~
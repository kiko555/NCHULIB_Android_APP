//: object/JSONClass.java
package tw.edu.nchu.libapp;

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
     *  @param context
     *            使用者的PID          
     * 
     * @return aryReturnResult 將系統狀態、認證狀態回傳
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public String[] setLoginJSONtoDB(String LoginJSON, Context context) {
        /**
         * strPid 讀者證號 m_szUniqueID 設備唯一辨識碼
         */
        String strLoginJSON = LoginJSON;
        String[] aryReturnResult = new String[3];

        String strOpResult = null, strAuthResult = null, strPatronName = null, strPID = null;
        String strPatronBarCode = null, strPatronToken = null;

        JSONArray jsonResultTitleArray = null;
        JSONArray jsonResultBarcodeArray = null;
        JSONArray jsonResultDataTypeArray = null;
        JSONArray jsonResultEndDateArray = null;

        // 建立取用資料庫的物件
        DBHelper dbHelper = new DBHelper(context);

        // 先將JSON內解出來放到各個陣列中
        try {
            strOpResult = new JSONObject(strLoginJSON).getString("op_result");
            strAuthResult = new JSONObject(strLoginJSON)
                    .getString("auth_result");
            strPatronName = new JSONObject(strLoginJSON)
                    .getString("PatronName");
            strPID = new JSONObject(strLoginJSON).getString("PID");
            strPatronBarCode = new JSONObject(strLoginJSON)
                    .getString("PatronBarCode");
            strPatronToken = new JSONObject(strLoginJSON)
                    .getString("PatronToken");

            jsonResultTitleArray = new JSONObject(strLoginJSON).getJSONObject(
                    "PatronLoan").getJSONArray("Z13_TITLE");
            jsonResultBarcodeArray = new JSONObject(strLoginJSON)
                    .getJSONObject("PatronLoan").getJSONArray("Z30_BARCODE");
            jsonResultDataTypeArray = new JSONObject(strLoginJSON)
                    .getJSONObject("PatronLoan").getJSONArray("DATA_TYPE");
            jsonResultEndDateArray = new JSONObject(strLoginJSON)
                    .getJSONObject("PatronLoan").getJSONArray("END_DATE");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 如果系統運作正常才繼續下去，並在回傳陣列寫入系統狀態
        aryReturnResult[0] = strOpResult;
        if (strOpResult.equals("success")) {

            aryReturnResult[1] = strAuthResult;
            // 如果認證成功才執行
            if (strAuthResult.equals("success")) {
                // 判斷所得JSON是否有內容
                if (jsonResultTitleArray == null
                        || jsonResultEndDateArray == null) {
                    // TODO: 有錯就寫log
                } else {
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
                                    jsonResultBarcodeArray.get(i).toString(),
                                    jsonResultDataTypeArray.get(i).toString(),
                                    jsonResultEndDateArray.get(i).toString());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                }
            }
        }
        return aryReturnResult;
    }

}
// /:~
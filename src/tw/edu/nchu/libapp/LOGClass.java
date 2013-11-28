//: object/LogClass.java
package tw.edu.nchu.libapp;

import android.content.Context;

/**
 * Log的物件，用以提供系統寫入交易紀錄
 * 
 * @author kiko
 * @version 1.0
 */
public class LOGClass {
    /**
     * 物件進入點
     * 
     */
    public LOGClass() {
    }

    /**
     * 直接將登入後收到的JSON處理後，直接寫入DB，並回傳狀態
     * 
     * @param LoginJSON
     *            登入後回傳的JSON
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public void setLOGtoDB(Context context, String JobType, String Time,
            String ExecuteStatus) {
        /**
         * 
         */

        // 建立取用資料庫的物件
        DBHelper dbHelper = new DBHelper(context);

        try {
            // 寫入讀者借閱資料
            dbHelper.doInsertSystemLogTable(JobType, Time, ExecuteStatus);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 關閉資料庫
        dbHelper.close();
    }
}
// /:~

//: object/DBHelper.java
package tw.edu.nchu.libapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 這是一個取得設備資料的物件 1.產生設備唯一辨識碼
 * 
 * @author kiko
 * @version 1.0
 */
public class DBHelper extends SQLiteOpenHelper {
    /**
     * intDBVersion 資料庫版本
     * 
     * strDBName 準備取用的資料庫名
     */
    private final static int intDBVersion = 2;
    private final static String strDBName = "nchulib.db";

    public DBHelper(Context context, String DBname, CursorFactory factory,
            int version) {
        super(context, DBname, factory, version);
        // TODO Auto-generated constructor stub
    }

    public DBHelper(Context context, String DBname) {
        this(context, DBname, null, intDBVersion);
    }

    public DBHelper(String DBname) {
        this(null, DBname, null, intDBVersion);
    }

    public DBHelper(Context context) {
        this(context, strDBName, null, intDBVersion);
    }

    public DBHelper(Context context, String DBname, int version) {
        this(context, DBname, null, version);
    }

    // 每次成功打開數據庫後首先被執行
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // TODO 每次成功打開數據庫後首先被執行
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

        // 建立讀者資料表
        String strTB_Patron_sql = "CREATE TABLE IF NOT EXISTS patron( "
                + "PID TEXT NOT NULL, " + "PatronBarCode TEXT NOT NULL, "
                + "PatronName TEXT NOT NULL, " + "PatronToken TEXT NOT NULL );";
        db.execSQL(strTB_Patron_sql);

        // 建立讀者借閱紀錄表
        String strTB_PatronLoan_sql = "CREATE TABLE IF NOT EXISTS patronloan( "
                + "Title TEXT NOT NULL, " + "Barcode TEXT NOT NULL, "
                + "DataType TEXT NOT NULL, " + "EndDate TEXT NOT NULL );";
        db.execSQL(strTB_PatronLoan_sql);

        // 建立系統紀錄表
        String strTB_SystemLog_sql = "CREATE TABLE IF NOT EXISTS SystemLog( "
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + "JobType TEXT NOT NULL, " + "Time TEXT NOT NULL, "
                + "ExecuteStatus TEXT NOT NULL );";
        db.execSQL(strTB_SystemLog_sql);

        // 建立系統設定紀錄表
        String strTB_SystemSet_sql = "CREATE TABLE IF NOT EXISTS SystemSet( "
                + "SysStatusDes TEXT NOT NULL, "
                + "SysStatus INTEGER NOT NULL );";
        db.execSQL(strTB_SystemSet_sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            db.beginTransaction();// 建立交易

            boolean success = false;// 判斷參數

            // 由之前不用的版本，可做不同的動作
            switch (oldVersion) {
            case 1:
                // 因log紀錄方式變動，所以欄位變動，刪除StartTime及EndTime改成一個Time
                db.execSQL("CREATE TEMPORARY TABLE SystemLog_backup("
                        + "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "JobType TEXT NOT NULL, " + "Time TEXT NOT NULL, "
                        + "ExecuteStatus TEXT NOT NULL );");
                db.execSQL("INSERT INTO SystemLog_backup SELECT ID,JobType,StartTime,ExecuteStatus FROM SystemLog;");
                db.execSQL("DROP TABLE SystemLog;");
                db.execSQL("CREATE TABLE IF NOT EXISTS SystemLog( "
                        + "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "JobType TEXT NOT NULL, " + "Time TEXT NOT NULL, "
                        + "ExecuteStatus TEXT NOT NULL );");
                db.execSQL("INSERT INTO SystemLog SELECT ID,JobType,Time,ExecuteStatus FROM SystemLog_backup;");
                db.execSQL("DROP TABLE SystemLog_backup;");
                oldVersion++;

                success = true;
                break;
            }

            if (success) {
                db.setTransactionSuccessful();// 正確交易才成功
            }
            db.endTransaction();
        } else {
            onCreate(db);
        }
    }

    public SQLiteDatabase openReadDb() {
        return this.getReadableDatabase(); // Use Readable because you're not
                                           // actually writing any values into
                                           // your db
    }

    public void closeDb(SQLiteDatabase db) {
        db.close();
    }

    /**
     * 將讀者資料寫入 Parton 表格
     * 
     * @param PID
     *            使用者的PID
     * @param PatronBarCode
     *            使用者的PID
     * @param PatronName
     *            使用者名字
     * @param PatronToken
     *            使用者的特殊代碼
     * @throws exceptions
     *             No exceptions thrown
     */
    public void doInsertPartonTable(String PID, String PatronBarCode,
            String PatronName, String PatronToken) {
        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronHelper = getWritableDatabase();
            // 寫入資料庫的內容
            ContentValues rec = new ContentValues();

            rec.put("PID", PID);
            rec.put("PatronBarCode", PatronBarCode);
            rec.put("PatronName", PatronName);
            rec.put("PatronToken", PatronToken);
            db_PatronHelper.insert("patron", null, rec);

            // 關閉資料庫
            db_PatronHelper.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 清空 Parton 表格
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public void doEmptyPartonTable() {
        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronHelper = getWritableDatabase();

            db_PatronHelper.delete("patron", null, null);

            // 關閉資料庫
            db_PatronHelper.close();

        } catch (SQLiteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 取得 Parton 表格的筆數，用以判斷是否有登入資料
     * 
     * @return intPartonCount 回傳總借閱的筆數
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public int doCountPartonTable() {
        int intPartonCount = 0;

        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronHelper = getReadableDatabase();
            String strSql = "Select * from patron";
            Cursor recSet = db_PatronHelper.rawQuery(strSql, null);

            intPartonCount = recSet.getCount();

            // 關閉資料庫
            db_PatronHelper.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return intPartonCount;
    }

    /**
     * 將讀者資料寫入 PartonLoan 表格
     * 
     * @param Title
     *            借閱資料的題名
     * @param BarCode
     *            借閱資料的登錄號
     * @param DataType
     *            借閱資料類型
     * @param EndData
     *            借閱資料到期日
     * @throws exceptions
     *             No exceptions thrown
     */
    public void doInsertPartonLoanTable(String Title, String BarCode,
            String DataType, String EndDate) {
        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronLoanHelper = getWritableDatabase();
            // 寫入資料庫的內容
            ContentValues rec = new ContentValues();

            rec.put("Title", Title);
            rec.put("Barcode", BarCode);
            rec.put("DataType", DataType);
            rec.put("EndDate", EndDate);
            db_PatronLoanHelper.insert("patronloan", null, rec);

            // 關閉資料庫
            db_PatronLoanHelper.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 清空 PartonLoan 表格
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public void doEmptyPartonLoanTable() {
        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronLoanHelper = getWritableDatabase();

            db_PatronLoanHelper.delete("patronloan", null, null);

            // 關閉資料庫
            db_PatronLoanHelper.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 取得 PartonLoan 表格的借閱筆數
     * 
     * @return intRecCount 回傳總借閱的筆數
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public int doCountPartonLoanTable() {
        int intRecCount = 0;

        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronLoanHelper = getReadableDatabase();
            String strSql = "Select * from patronloan where DataType='LOAN'";
            Cursor recSet = db_PatronLoanHelper.rawQuery(strSql, null);

            intRecCount = recSet.getCount();

            // 關閉資料庫
            db_PatronLoanHelper.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return intRecCount;
    }

    /**
     * 取得 PartonLoan 表格的預約筆數
     * 
     * @return intRequestCount 回傳總借閱的筆數
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public int doCountPartonLoanTable_Request() {
        int intRequestCount = 0;

        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronLoanHelper = getReadableDatabase();
            String strSql = "Select * from patronloan where DataType='REQUEST'";
            Cursor recSet = db_PatronLoanHelper.rawQuery(strSql, null);

            intRequestCount = recSet.getCount();

            // 關閉資料庫
            db_PatronLoanHelper.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return intRequestCount;
    }

    /**
     * 取得 PartonLoan 表格中的借閱內容
     * 
     * @param context
     *            活動
     * 
     * @param type
     *            用於控制要回傳的是0全部、1倒數或2過期的書目
     * 
     * @return aryRec 回傳借閱資料的陣列
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public ArrayList<HashMap<String, String>> getPartonLoanTable(
            Context context, Integer partonloantype) {

        // 宣告列表所需的格式
        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();

        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronLoanHelper = getReadableDatabase();
            String strSql = null;

            // 利用判斷天數來控制清單的內容
            SimpleDateFormat smdf = new SimpleDateFormat("yyyyMMdd");

            // 取得今天日期
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.HOUR_OF_DAY, 0); // anything 0 - 23
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date dateToday = cal.getTime();

            // 將到期日轉成特定格式
            String strToday = smdf.format(dateToday);

            switch (partonloantype) {
            case 0:
                strSql = "Select Title,EndDate from patronloan where DataType='LOAN'"
                        + " ORDER BY EndDate DESC";
                break;
            case 1:
                // 往前回推X天
                long longDueDay = dateToday.getTime()
                        + (7 * 24 * 60 * 60 * 1000);
                Date dateDueDay = new Date(longDueDay);

                // 將到期日轉成特定格式
                String strDueDay = smdf.format(dateDueDay);

                strSql = "Select Title,EndDate from patronloan where DataType='LOAN'"
                        + " and EndDate >= "
                        + strToday
                        + " and EndDate <= "
                        + strDueDay + " ORDER BY EndDate DESC";
                break;
            case 2:
                strSql = "Select Title,EndDate from patronloan where DataType='LOAN'"
                        + " and EndDate < "
                        + strToday
                        + " ORDER BY EndDate DESC";
                break;
            default:
                break;
            }

            Cursor recSet = db_PatronLoanHelper.rawQuery(strSql, null);

            // 先把表頭帶入
            map.put("Title",
                    (String) context.getResources().getText(
                            R.string.ActivityCirculationLog_lvTitle));
            map.put("Time",
                    (String) context.getResources().getText(
                            R.string.ActivityCirculationLog_lvDuedate));
            mylist.add(map);

            // 把表格內的每一筆資料都寫入hashmap中
            for (recSet.moveToFirst(); !recSet.isAfterLast(); recSet
                    .moveToNext()) {
                map = new HashMap<String, String>();

                // 把借閱內容帶入hash
                map.put("Title", recSet.getString(0));

                map.put("Time", recSet.getString(1));

                mylist.add(map);
            }

            // 關閉資料庫
            db_PatronLoanHelper.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mylist;
    }

    /**
     * 取得 PartonLoan 表格中的預約內容
     * 
     * @return aryRec 回傳預約資料的陣列
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public ArrayList<HashMap<String, String>> getPartonLoanTable_Request(
            Context context) {

        // 宣告列表所需的格式
        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();

        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronLoanHelper = getReadableDatabase();
            String strSql = "Select Title,EndDate from patronloan "
                    + "where DataType='REQUEST' ORDER BY EndDate DESC";
            // 建立查詢元件
            Cursor recSet = db_PatronLoanHelper.rawQuery(strSql, null);

            // 先把表頭帶入
            map.put("Title",
                    (String) context.getResources().getText(
                            R.string.ActivityCirculationLog_lvTitle));
            map.put("Time",
                    (String) context.getResources().getText(
                            R.string.ActivityCirculationLog_lvRequestHoldUntilDate));
            mylist.add(map);

            // 把表格內的每一筆資料都寫入hashmap中
            for (recSet.moveToFirst(); !recSet.isAfterLast(); recSet
                    .moveToNext()) {
                map = new HashMap<String, String>();

                // 把題名內容帶入hash
                map.put("Title", recSet.getString(0));

                // 將到館日帶入hash
                map.put("Time", recSet.getString(1));

                mylist.add(map);
            }

            // 關閉資料庫
            db_PatronLoanHelper.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mylist;
    }

    /**
     * 將LOG寫入 SystemLog 表格
     * 
     * @param JobType
     *            工作的類型
     * @param StartTime
     *            工作開始執行的時間
     * @param EndTime
     *            工作結束的時間
     * @param ExecuteStatus
     *            執行狀態
     * @throws exceptions
     *             No exceptions thrown
     */
    public void doInsertSystemLogTable(String JobType, String Time,
            String ExecuteStatus) {
        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_PatronHelper = getWritableDatabase();
            // 寫入資料庫的內容
            ContentValues rec = new ContentValues();

            rec.put("JobType", JobType);
            rec.put("Time", Time);
            rec.put("ExecuteStatus", ExecuteStatus);
            db_PatronHelper.insert("SystemLog", null, rec);

            // 關閉資料庫
            db_PatronHelper.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 取得 SystemLog 表格的內容
     * 
     * @return mylist 操作紀錄透過HashMap方式儲存，最後再以arraylist的方式回傳
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public ArrayList<HashMap<String, String>> getAllSystemLog(Context context) {
        // 宣告列表所需的格式
        ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();

        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_SystemLogHelper = getReadableDatabase();

            // select query
            String selectQuery = "SELECT JobType,Time,ExecuteStatus FROM SystemLog"
                    + " ORDER BY Time DESC,JobType,ExecuteStatus Desc";

            // 先把表頭帶入
            map.put("JobType",
                    (String) context.getResources().getText(
                            R.string.ActivitySystemLog_tvJobType));
            map.put("Time",
                    (String) context.getResources().getText(
                            R.string.ActivitySystemLog_tvTime));
            map.put("ExecuteStatus",
                    (String) context.getResources().getText(
                            R.string.ActivitySystemLog_tvNote));
            mylist.add(map);

            // 建立查詢元件
            Cursor cursor = db_SystemLogHelper.rawQuery(selectQuery, null);

            // 把表格內的每一筆資料都寫入hashmap中
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                map = new HashMap<String, String>();

                // 把Log內容帶入hash
                map.put("JobType", cursor.getString(0));
                map.put("Time", cursor.getString(1));
                map.put("ExecuteStatus", cursor.getString(2));

                mylist.add(map);
            }

            // 關閉資料庫
            db_SystemLogHelper.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mylist;

    }

    /**
     * 清空全部表格
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public void doEmptyAllTable() {
        try {
            // SQLiteDatabase對象
            SQLiteDatabase db_Helper = getWritableDatabase();

            db_Helper.delete("patron", null, null);
            db_Helper.delete("patronloan", null, null);
            db_Helper.delete("SystemLog", null, null);
            db_Helper.delete("SystemSet", null, null);

            // 關閉資料庫
            db_Helper.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
// /:~
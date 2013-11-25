//: object/DBHelper.java
package tw.edu.nchu.libapp;

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
    private final static int intDBVersion = 1;
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
    public synchronized void close() {
        super.close();
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
                + "JobType TEXT NOT NULL, " + "StartTime TEXT NOT NULL, "
                + "EndTime TEXT NOT NULL, " + "ExecuteStatus TEXT NOT NULL );";
        db.execSQL(strTB_SystemLog_sql);

        // 建立系統設定紀錄表
        String strTB_SystemSet_sql = "CREATE TABLE IF NOT EXISTS SystemSet( "
                + "SysStatusDes TEXT NOT NULL, "
                + "SysStatus INTEGER NOT NULL );";
        db.execSQL(strTB_SystemSet_sql);
        

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS patron");
        db.execSQL("DROP TABLE IF EXISTS patronloan");
        db.execSQL("DROP TABLE IF EXISTS SystemLog");
        db.execSQL("DROP TABLE IF EXISTS SystemSet");
        onCreate(db);

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
            SQLiteDatabase db_PatronLoanHelper = getReadableDatabase();
            String strSql = "Select * from patron";
            Cursor recSet = db_PatronLoanHelper.rawQuery(strSql, null);

            intPartonCount = recSet.getCount();


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


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return intRequestCount;
    }

    /**
     * 取得 PartonLoan 表格中的借閱內容
     * 
     * @return aryRec 回傳借閱資料的陣列
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public String[][] getPartonLoanTable() {
        // SQLiteDatabase對象
        SQLiteDatabase db_PatronLoanHelper = getReadableDatabase();
        String strSql = "Select * from patronloan where DataType='LOAN'";
        Cursor recSet = db_PatronLoanHelper.rawQuery(strSql, null);

        // 取得資料筆數
        int intRecCount = recSet.getCount();

        // 宣告符合需求的陣列大小
        String[][] aryRec = new String[2][intRecCount];

        while (recSet.moveToNext()) {
            int intRecCursor = recSet.getPosition();
            aryRec[0][intRecCursor] = recSet.getString(0);
            aryRec[1][intRecCursor] = recSet.getString(3);

        }



        return aryRec;
    }

    /**
     * 取得 PartonLoan 表格中的預約內容
     * 
     * @return aryRec 回傳預約資料的陣列
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public String[][] getPartonLoanTable_Request() {
        // SQLiteDatabase對象
        SQLiteDatabase db_PatronLoanHelper = getReadableDatabase();
        String strSql = "Select * from patronloan where DataType='REQUEST'";
        Cursor recSet = db_PatronLoanHelper.rawQuery(strSql, null);

        // 取得資料筆數
        int intRecCount = recSet.getCount();

        // 宣告符合需求的陣列大小
        String[][] aryRec = new String[2][intRecCount];

        while (recSet.moveToNext()) {
            int intRecCursor = recSet.getPosition();
            aryRec[0][intRecCursor] = recSet.getString(0);
            aryRec[1][intRecCursor] = recSet.getString(3);

        }



        return aryRec;
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


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
// /:~
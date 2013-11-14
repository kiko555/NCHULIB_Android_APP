//: object/DBHelper.java
package tw.edu.nchu.libapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 這是一個取得設備資料的物件 1.產生設備唯一辨識碼
 * 
 * @author kiko
 * @version 1.0
 */
public class DBHelper extends SQLiteOpenHelper {
	private final static int DBVersion = 1; // <-- 版本
	private final static String strDBName = "nchulib"; // 準備取用的資料庫名

	// private final static String DBName = "nchulib.db"; // <-- db name
	// private final static String TableName = "patronloan"; // <-- table name

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public DBHelper(Context context, String name) {
		this(context, name, null, DBVersion);
	}

	public DBHelper(String name) {
		this(null, name, null, DBVersion);
	}

	public DBHelper(Context context, String name, int version) {
		this(context, name, null, version);
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

		// 建立讀者借閱紀錄表
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

	// 取回資料表有幾筆資料
	// public int RecCount() {
	// SQLiteDatabase db = getWritableDatabase();
	// String sql = "SELECT * FROM " + TableName;
	// Cursor recSet = db.rawQuery(sql, null);
	// return recSet.getCount();
	// }

	/**
	 * 將讀者資料寫入 Parton 表格
	 * 
	 * @param PID 使用者的PID
	 * @param PatronBarCode 使用者的PID
	 * @param PatronName 使用者名字
	 * @param PatronToken 使用者的特殊代碼
	 * @throws exceptions
	 *             No exceptions thrown
	 */
	public void doInsertPartonTable(String PID, String PatronBarCode,
			String PatronName, String PatronToken) {
		/** 無需先宣告的變數 */
		try {
			// SQLiteDatabase對象
			SQLiteDatabase db_PatronHelper;

			// 準備取用的表格名
			String strPatronTableNname = "patron";

			// 輔助類名
			DBHelper dbPatronHelper = new DBHelper(strPatronTableNname);

			// 取得資料庫可寫入對象
			db_PatronHelper = dbPatronHelper.getWritableDatabase();

			// 寫入資料庫
			ContentValues rec = new ContentValues();
			rec.put("PID", "123");
			rec.put("PatronBarCode", "2dddd");
			rec.put("PatronName", "3eeee");
			rec.put("DataType", "4ffff");
			rec.put("PatronToken", "5gggg");
			db_PatronHelper.insert(strPatronTableNname, null, rec);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
// /:~
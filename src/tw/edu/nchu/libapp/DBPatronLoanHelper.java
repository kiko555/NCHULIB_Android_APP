package tw.edu.nchu.libapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBPatronLoanHelper extends SQLiteOpenHelper {
	private final static int DBVersion = 1; // <-- 版本

	// private final static String DBName = "nchulib.db"; // <-- db name
	// private final static String TableName = "patronloan"; // <-- table name

	public DBPatronLoanHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public DBPatronLoanHelper(Context context, String name) {
		this(context, name, null, DBVersion);
	}

	public DBPatronLoanHelper(Context context, String name, int version) {
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
				+ "PID TEXT NOT NULL, PatronBarCode TEXT NOT NULL, "
				+ "PatronName TEXT NOT NULL, LoanCnt TEXT NOT NULL, "
				+ "RequestCnt TEXT NOT NULL, PatronToken TEXT NOT NULL );"; // <-- table name
		db.execSQL(strTB_Patron_sql);

		// 建立讀者借閱紀錄表
		String strTB_patronloan_sql = "CREATE TABLE IF NOT EXISTS patronloan( "
				+ "ID TEXT NOT NULL, Title TEXT NOT NULL, "
				+ "Barcode TEXT NOT NULL, DataType TEXT NOT NULL, "
				+ "EndDate TEXT NOT NULL );"; // <-- table name
		db.execSQL(strTB_patronloan_sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS patronloan");
		onCreate(db);
	}

	// 取回資料表有幾筆資料
	// public int RecCount() {
	// SQLiteDatabase db = getWritableDatabase();
	// String sql = "SELECT * FROM " + TableName;
	// Cursor recSet = db.rawQuery(sql, null);
	// return recSet.getCount();
	// }

}

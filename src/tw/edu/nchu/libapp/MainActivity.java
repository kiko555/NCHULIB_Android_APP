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

public class MainActivity extends ActionBarActivity {
	private Button btGetJSON;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	// SQLiteDatabase對象
	SQLiteDatabase db;

	// 資料庫名
	public String strDBName = "nchulib";

	// 表格名
	public String strTableNname = "patronloan";

	// 輔助類名
	DBHelper dbHelper = new DBHelper(
			MainActivity.this, strDBName);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 設定讀取圖示
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_main);

		// JSON資料接收鈕
		btGetJSON = (Button) findViewById(R.id.button1);

		// 監聽ok鈕的動作
		btGetJSON.setOnClickListener(btListener);

		// 取得資料庫可寫入對象
		db = dbHelper.getWritableDatabase();

		// 右上角顯示讀取中圖示
		// setSupportProgressBarIndeterminateVisibility(true);

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

	// 建非同步模式架構
	class RetreiveHTTPTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			// Initialize progress
			setSupportProgressBarIndeterminateVisibility(true);
		}

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
				HttpGet request = new HttpGet(urls[0]);

				HttpResponse response = client.execute(request);

				// TODO: add exception handle
				// 確認回傳是否異常
				StatusLine status = response.getStatusLine();
				if (status.getStatusCode() != 200) {
					// throw new IOException("Invalid response from server: " +
					// status.toString());
					Toast.makeText(MainActivity.this, R.string.Check_Network,
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
						Toast.makeText(MainActivity.this,
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
				Toast.makeText(MainActivity.this, R.string.Check_Network,
						Toast.LENGTH_SHORT).show();
				super.cancel(true);
				return null;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			// TODO: check this.exception
			// TODO: do something with the feed

			// tlJOSN_list.setStretchAllColumns(true);
			// TableLayout.LayoutParams row_layout = new
			// TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT);
			// TableRow.LayoutParams view_layout = new
			// TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT);

			try {

				// 確認是否沒有收到資料
				if (result.equals("")) {
					Toast.makeText(MainActivity.this,
							R.string.JSON_Data_downlaod_fail,
							Toast.LENGTH_SHORT).show();
				} else {
					// Toast.makeText(MainActivity.this,
					// result.toString(),Toast.LENGTH_SHORT).show();

					// 抓取JSON物件中的特定陣列
					JSONArray jsonResult1Array;
					JSONArray jsonResult2Array;

					JSONArray jsonResultIDArray;
					JSONArray jsonResultTitleArray;
					JSONArray jsonResultBarcodeArray;
					JSONArray jsonResultDataTypeArray;
					JSONArray jsonResultEndDateArray;

					jsonResultIDArray = new JSONObject(result)
							.getJSONArray("Z36_ID");
					jsonResultTitleArray = new JSONObject(result)
							.getJSONArray("Z13_TITLE");
					jsonResultBarcodeArray = new JSONObject(result)
							.getJSONArray("Z30_BARCODE");
					jsonResultDataTypeArray = new JSONObject(result)
							.getJSONArray("DATA_TYPE");
					jsonResultEndDateArray = new JSONObject(result)
							.getJSONArray("END_DATE");

					jsonResult1Array = new JSONObject(result)
							.getJSONArray("Z13_TITLE");
					jsonResult2Array = new JSONObject(result)
							.getJSONArray("END_DATE");

					// 判斷所得JSON格式是否有錯
					if (jsonResult1Array == null || jsonResult2Array == null) {
						Toast.makeText(MainActivity.this,
								R.string.JSON_Data_error, Toast.LENGTH_SHORT)
								.show();
					} else {

						TableLayout tlJOSN_list = (TableLayout) findViewById(R.id.TableLayout01);
						// tlJOSN_list.setStretchAllColumns(false);

						// 先清空借閱紀錄資料表
						db.execSQL("DELETE FROM " + strTableNname + ";");

						// 取出JSON陣列內所有內容
						for (int i = 0; i < jsonResult1Array.length(); i++) {
							// 讀出JSON的內容後直接寫到畫面上
							// 並且以table layout的方式呈現
							// ****不要用兩個textview疊加的方式，看能不能用tablelayout直接切割
							TableRow tr = new TableRow(MainActivity.this);
							tr.setGravity(Gravity.CENTER_HORIZONTAL);

							TextView tvSEARCH_QUERY = new TextView(
									MainActivity.this);
							tvSEARCH_QUERY.setText(jsonResult1Array.get(i)
									.toString());
							tvSEARCH_QUERY.setMaxEms(12);
							tvSEARCH_QUERY.setTextSize(20);
							tvSEARCH_QUERY
									.setEllipsize(TextUtils.TruncateAt.END);
							tvSEARCH_QUERY.setGravity(Gravity.LEFT);

							TextView tvCOUNT_SEARCH_QUERY = new TextView(
									MainActivity.this);
							tvCOUNT_SEARCH_QUERY.setText(jsonResult2Array
									.get(i).toString());

							tr.addView(tvSEARCH_QUERY);
							tr.addView(tvCOUNT_SEARCH_QUERY);
							tlJOSN_list.addView(tr,
									new TableLayout.LayoutParams(WC, WC));

							try {
								// 寫入資料庫
								ContentValues rec = new ContentValues();
								rec.put("ID", jsonResultIDArray.get(i)
										.toString());
								rec.put("Title", jsonResultTitleArray.get(i)
										.toString());
								rec.put("Barcode", jsonResultBarcodeArray
										.get(i).toString());
								rec.put("DataType", jsonResultDataTypeArray
										.get(i).toString());
								rec.put("EndDate", jsonResultEndDateArray
										.get(i).toString());
								db.insert(strTableNname, null, rec);

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						// db.close();

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

	private OnClickListener btListener = new OnClickListener() {
		public void onClick(View v) {

			// Toast.makeText(MainActivity.this,
			// R.string.JSON_DataLoading,Toast.LENGTH_SHORT).show();

			// 呼叫非同步架構抓取http資料
			RetreiveHTTPTask retreivehttpask = (RetreiveHTTPTask) new RetreiveHTTPTask()
					.execute("https://api.lib.nchu.edu.tw/php/appagent/getrecord.php");

			// 倘若失敗時的動作
			if (retreivehttpask == null) {
				Toast.makeText(MainActivity.this, R.string.Check_Network,
						Toast.LENGTH_SHORT).show();
			}
		}
	};

}

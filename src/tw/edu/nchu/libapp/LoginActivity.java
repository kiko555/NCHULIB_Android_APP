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

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends ActionBarActivity {
	private Button btLogin;
	private EditText txID;
	private EditText txPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
				intent.setClass(LoginActivity.this, LoginActivity.class);
				startActivity(intent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case R.id.action_cirlog:
			try {
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, CirculationLogActivity.class);
				startActivity(intent);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case R.id.action_settings:
			Toast.makeText(LoginActivity.this, Item.getTitle(),
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

			// tlJOSN_list.setStretchAllColumns(true);
			// TableLayout.LayoutParams row_layout = new
			// TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT);
			// TableRow.LayoutParams view_layout = new
			// TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT);

			// 建立取用資料庫的物件
			DBHelper dbHelper = new DBHelper(LoginActivity.this);

			try {

				// 確認是否沒有收到資料
				if (result.equals("")) {
					Toast.makeText(LoginActivity.this,
							R.string.JSON_Data_downlaod_fail,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(LoginActivity.this, result.toString(),
							Toast.LENGTH_SHORT).show();

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
					if (strOpResult.equals("success")) {
						strAuthResult = new JSONObject(result)
								.getString("auth_result");
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

							jsonResult1Array = new JSONObject(result)
									.getJSONObject("PatronLoan").getJSONArray(
											"Z13_TITLE");
							jsonResult2Array = new JSONObject(result)
									.getJSONObject("PatronLoan").getJSONArray(
											"END_DATE");

							// 判斷所得JSON格式是否有錯
							if (jsonResult1Array == null
									|| jsonResult2Array == null) {
								Toast.makeText(LoginActivity.this,
										R.string.JSON_Data_error,
										Toast.LENGTH_SHORT).show();
							} else {

								TableLayout tlJOSN_list = (TableLayout) findViewById(R.id.TableLayout01);
								// tlJOSN_list.setStretchAllColumns(false);

								// 先清空讀者借閱資料表
								dbHelper.doEmptyPartonLoanTable();

								// 取出JSON陣列內所有內容
								for (int i = 0; i < jsonResult1Array.length(); i++) {
									// 讀出JSON的內容後直接寫到畫面上
									// 並且以table layout的方式呈現
									// ****不要用兩個textview疊加的方式，看能不能用tablelayout直接切割
									TableRow tr = new TableRow(
											LoginActivity.this);
									tr.setGravity(Gravity.CENTER_HORIZONTAL);

									TextView tvSEARCH_QUERY = new TextView(
											LoginActivity.this);
									tvSEARCH_QUERY.setText(jsonResult1Array
											.get(i).toString());
									tvSEARCH_QUERY.setMaxEms(12);
									tvSEARCH_QUERY.setTextSize(20);
									tvSEARCH_QUERY
											.setEllipsize(TextUtils.TruncateAt.END);
									tvSEARCH_QUERY.setGravity(Gravity.LEFT);

									TextView tvCOUNT_SEARCH_QUERY = new TextView(
											LoginActivity.this);
									tvCOUNT_SEARCH_QUERY
											.setText(jsonResult2Array.get(i)
													.toString());

									tr.addView(tvSEARCH_QUERY);
									tr.addView(tvCOUNT_SEARCH_QUERY);
									// tlJOSN_list.addView(tr,
									// new TableLayout.LayoutParams(WC, WC));

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
									intent.setClass(LoginActivity.this, CirculationLogActivity.class);
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

	private OnClickListener btListener = new OnClickListener() {
		public void onClick(View v) {

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
}
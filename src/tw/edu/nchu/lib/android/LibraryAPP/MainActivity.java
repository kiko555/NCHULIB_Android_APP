package tw.edu.nchu.lib.android.LibraryAPP;

import java.io.BufferedReader;
import java.io.IOException;
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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button btSure;
	private TextView tvShHttpGet;
	private TableLayout tlJOSN_list;
	//private ProgressBar pbLoadingData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
        setProgressBarIndeterminateVisibility(false);
		
		btSure = (Button)findViewById(R.id.button1);
		tlJOSN_list = (TableLayout)findViewById(R.id.tableLayout1);
		//pbLoadingData = (ProgressBar)findViewById(R.id.progressBar1);
		
		//監聽ok鈕的動作
		btSure.setOnClickListener(btListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//建非同步模式架構
	class RetreiveHTTPTask extends AsyncTask<String, Void, String> {

	    private Exception exception;
	    
		@Override
		protected void onPreExecute() {
		// Initialize progress
		setProgressBarIndeterminateVisibility(true);
		} 

	    protected String doInBackground(String... urls) {
	        try {
	        	//透過keystore來解SSL
	        	KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());          
				InputStream instream = getResources().openRawResource(R.raw.api);
				try {  
				    trustStore.load(instream, null);  
				} finally {  
				    instream.close();  
				}  
				SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);  
				Scheme sch = new Scheme("https", socketFactory, 443);
				
	        	//初始apache的httpclient class
	        	HttpClient client = new DefaultHttpClient();
	        	
	        	//將keystore及SSLSocketFactory指回來給httoclient使用
	        	client.getConnectionManager().getSchemeRegistry().register(sch);
	        	
				//給予連線的網址
	        	HttpGet request = new HttpGet(urls[0]);
				
				HttpResponse response = client.execute(request); 
				
				// TODO: add exception handle 
				//確認回傳是否異常
			    StatusLine status = response.getStatusLine();
			    if (status.getStatusCode() != 200) {
			    	//throw new IOException("Invalid response from server: " + status.toString());
			    	Toast.makeText(MainActivity.this,R.string.Check_Network,Toast.LENGTH_SHORT).show();
					super.cancel(true);
		            return null;
			    } else {
			    	//將回傳值丟進buffer
					BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					
					//有值才將buffer內的值彙整起來
					String line = "";
					String allline = "";
					while ((line = rd.readLine()) != null) {
							allline += line;
					}
					
					//確認是否沒有收到資料，如果是空的就丟訊息提醒，並停止後續處理
					if (allline.equals("")) {
						Toast.makeText(MainActivity.this,R.string.Check_Network,Toast.LENGTH_SHORT).show();
						super.cancel(true);
			            return null;
				    } else {
				    	return allline;
				    }	
			    }
			    			   
				
	        } catch (Exception e) {
	            this.exception = e;
	            Toast.makeText(MainActivity.this,R.string.Check_Network,Toast.LENGTH_SHORT).show();
	            super.cancel(true);
	            return null;
	        }
			
	    }
    	
	    @Override
	    protected void onPostExecute(String result) {
	        // TODO: check this.exception 
	        // TODO: do something with the feed
	    	
	    	tlJOSN_list.setStretchAllColumns(true);
            TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams view_layout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	
        	try {

        		//確認是否沒有收到資料
        		if (result.equals("")) {
        			Toast.makeText(MainActivity.this,R.string.JSON_Data_downlaod_fail,Toast.LENGTH_SHORT).show();
        		} else {
        			//抓取JSON物件中的特定陣列
	        		JSONArray jsonResult1Array;
	        		JSONArray jsonResult2Array;
	        		
	        		jsonResult1Array = new JSONObject(result).getJSONArray("Z69_SEARCH_QUERY");
	        		jsonResult2Array = new JSONObject(result).getJSONArray("COUNT_SEARCH_QUERY");
	        		
	        		//判斷所得JSON格式是否有錯
	        		if (jsonResult1Array == null || jsonResult2Array == null){
	        			Toast.makeText(MainActivity.this,R.string.JSON_Data_error,Toast.LENGTH_SHORT).show();
	        		}
	        		else{
	        			//取出JSON陣列內所有內容
		        		for(int i = 0;i < jsonResult1Array.length(); i++){
			        		//讀出JSON的內容後直接寫到畫面上
		        			//並且以table layout的方式呈現
			        		TableRow tr = new TableRow(MainActivity.this);
			        		tr.setLayoutParams(row_layout);
			        		tr.setGravity(Gravity.CENTER_HORIZONTAL);
			
			        		TextView tvSEARCH_QUERY = new TextView(MainActivity.this);
			        		tvSEARCH_QUERY.setText(jsonResult1Array.get(i).toString());
			        		tvSEARCH_QUERY.setLayoutParams(view_layout);
			
			        		TextView tvCOUNT_SEARCH_QUERY = new TextView(MainActivity.this);
			        		tvCOUNT_SEARCH_QUERY.setText(jsonResult2Array.get(i).toString());
			        		tvCOUNT_SEARCH_QUERY.setLayoutParams(view_layout);
			
			        		tr.addView(tvSEARCH_QUERY);
			        		tr.addView(tvCOUNT_SEARCH_QUERY);
			        		tlJOSN_list.addView(tr);
		        		}	
	        		}
        		}
    		} catch (JSONException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
    		}
	    	//資料抓取完畢將讀取鈕移除
	    	setProgressBarIndeterminateVisibility(false);
	    }
	}
	
	private OnClickListener btListener = new OnClickListener(){
		public void onClick(View v){

			//Toast.makeText(MainActivity.this,R.string.DataLoading,Toast.LENGTH_SHORT).show();
			
			//呼叫非同步架構抓取http資料
			RetreiveHTTPTask retreivehttpask = (RetreiveHTTPTask) new RetreiveHTTPTask().execute("https://api.lib.nchu.edu.tw/php/hotkeyword/index.php");
			
			//倘若失敗時的動作
			if (retreivehttpask == null) {
				Toast.makeText(MainActivity.this,R.string.Check_Network,Toast.LENGTH_SHORT).show();
            }
		}
	};
}

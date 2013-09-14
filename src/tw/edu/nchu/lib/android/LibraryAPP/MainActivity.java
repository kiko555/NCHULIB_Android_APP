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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button btSure;
	private TextView tvShHttpGet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btSure = (Button)findViewById(R.id.button1);
		tvShHttpGet = (TextView)findViewById(R.id.textView3);
		
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

	    protected String doInBackground(String... urls) {
	        try {
	        	//透過keystore來解SSL
	        	KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());          
				InputStream instream = getResources().openRawResource(R.raw.aleph_console);
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
				
				//確認回傳是否異常
			    StatusLine status = response.getStatusLine();
			    if (status.getStatusCode() != 200) {
			    	  throw new IOException("Invalid response from server: " + status.toString());
			    }
			    			   
				//將回傳值丟進buffer
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				//有值才寫入，沒收到資料就提醒
				String line = "";
				String allline = "";
				if (rd.readLine() != null) {
					//將buffer內的值彙整起來，並拋出去
					while ((line = rd.readLine()) != null) {
						allline += line;
					}
					return allline;
			    } else {
			    	allline = "沒有收到資料，請確認網路有通！";
			    	return allline;
			    }
				
				
	        } catch (Exception e) {
	            this.exception = e;
	            return "沒有收到資料，請確認網路有通！";
	        }
	    }
    	
	    @Override
	    protected void onPostExecute(String result) {
	        // TODO: check this.exception 
	        // TODO: do something with the feed
	    	
	    	//收完資料後直接寫到畫面上
	    	tvShHttpGet.setText(result);
	    }
	}
	
	private OnClickListener btListener = new OnClickListener(){
		public void onClick(View v){
			
			
			
			/*KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());          
			InputStream instream = getResources().openRawResource(R.raw.mis);
			try {  
			    trustStore.load(instream, null);  
			} finally {  
			    instream.close();  
			}  
			  
			SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);  
			Scheme sch = new Scheme("https", socketFactory, 443);*/
			
			//httpclient.getConnectionManager().getSchemeRegistry().register(sch);
			
			

			//呼叫非同步架構抓取http資料
			RetreiveHTTPTask retreivehttpask = (RetreiveHTTPTask) new RetreiveHTTPTask().execute("https://aleph-console.lib.nchu.edu.tw/api/hotkeyword/");
			
			//倘若失敗時的動作
			if (retreivehttpask == null) {
				tvShHttpGet.setText("請確認網路有通！");
            }
			

			//Toast.makeText(MainActivity.this,"123",Toast.LENGTH_SHORT).show();
		}
	};
}

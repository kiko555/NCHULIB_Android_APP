package tw.edu.nchu.lib.android.LibraryAPP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
	        	//初始apache的httpclient class
	        	HttpClient client = new DefaultHttpClient();
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
				
				//將buffer內的值彙整起來，並拋出去
				String line = "";
				String allline = "";
				while ((line = rd.readLine()) != null) {
					allline += line;
				}
				return allline;
				
	        } catch (Exception e) {
	            this.exception = e;
	            return null;
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
			RetreiveHTTPTask retreivehttpask = (RetreiveHTTPTask) new RetreiveHTTPTask().execute("http://api.lib.nchu.edu.tw/php/hotkeyword/index.php");
			
			//倘若失敗時的動作
			if (retreivehttpask == null) {
				tvShHttpGet.setText("請確認網路有通！");
            }
			

			//Toast.makeText(MainActivity.this,"123",Toast.LENGTH_SHORT).show();
		}
	};
}

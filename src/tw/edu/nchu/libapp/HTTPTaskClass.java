//: object/HTTPTaskClass.java
package tw.edu.nchu.libapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;

/**
 * 供程式http傳輸使用
 * 
 * @author kiko
 * @version 1.0
 */
public class HTTPTaskClass {
    InputStream instream;
    Scheme sch;
    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    String TAG = "HTTPTaskClass-AsyncTask";

    /**
     * 帳密認證
     * 
     * @return strReturnContent Server回傳的內容
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public String doPostWork(String url) {
        try {
            // 透過keystore來解SSL
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());

            try {
                trustStore.load(instream, null);
            } finally {
                instream.close();
            }
            SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
            sch = new Scheme("https", socketFactory, 443);

        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // HTTP 連線回傳的內容
        String strAllLine = "";

        // 初始apache的httpclient class
        HttpClient client = new DefaultHttpClient();

        // 將keystore及SSLSocketFactory指回來給httoclient使用
        client.getConnectionManager().getSchemeRegistry().register(sch);

        // 給予連線的網址
        HttpPost httppost = new HttpPost(url);

        // 給予POST所需傳遞的參數
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // while (!isCancelled()) {
        try {

            // Execute HTTP Post Request
            HttpResponse response = client.execute(httppost);

            // 確認回傳是否異常
            StatusLine status = response.getStatusLine();

            if (status.getStatusCode() != 200) {
                throw new IOException("Invalid response from server: "
                        + status.toString());
            } else {
                // 將回傳值丟進buffer
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                // 有值才將buffer內的值彙整起來
                String strline = "";

                while ((strline = rd.readLine()) != null) {
                    strAllLine += strline;
                }

                Log.i("test", strAllLine);

                // super.cancel(true);
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // }

        // super.cancel(true);
        // 確認是否沒有收到資料，如果是空的就丟訊息提醒，並停止後續處理
        if (strAllLine.equals("")) {

            return null;
        } else {
            return strAllLine;

        }
    }

}
// /:~
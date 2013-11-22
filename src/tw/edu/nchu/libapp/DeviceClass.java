//: object/DeviceClass.java
package tw.edu.nchu.libapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Window;

/** 這是一個取得設備資料的物件
 * 1.產生設備唯一辨識碼
 * 
 * @author kiko
 * @version 1.0
 */
public class DeviceClass {
	/** 物件進入點
	* 
	*/
	public DeviceClass() {
	}
		
	/** 產生設備唯一辨識碼
	* 
	* @param PID 使用者的PID
	* 
	* @return m_szUniqueID 設備唯一辨識碼
	* 
	* @throws exceptions No exceptions thrown
	*/
	public String doMakeDeviceToken(String PID) {
		/**
		 *  strPid 讀者證號
		 *  m_szUniqueID 設備唯一辨識碼
		 */
		String strPid = PID;
		String m_szUniqueID = new String();
		
		try {
			// 建立一組 IMEI
			String m_szDevIDShort = "99" + Build.BOARD.length() % 10
					+ Build.BRAND.length() % 10 + Build.CPU_ABI.length()
					% 10 + Build.DEVICE.length() % 10
					+ Build.DISPLAY.length() % 10 + Build.HOST.length()
					% 10 + Build.ID.length() % 10
					+ Build.MANUFACTURER.length() % 10
					+ Build.MODEL.length() % 10 + Build.PRODUCT.length()
					% 10 + Build.TAGS.length() % 10 + Build.TYPE.length()
					% 10 + Build.USER.length() % 10;

			// 混合讀者證號
			String m_szLongID = m_szDevIDShort + strPid;
			// compute md5
			MessageDigest m = null;
			try {
				m = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
			// get md5 bytes
			byte p_md5Data[] = m.digest();
			// create a hex string
			
			for (int i = 0; i < p_md5Data.length; i++) {
				int b = (0xFF & p_md5Data[i]);
				// if it is a single digit, make sure it have 0 in front
				// (proper
				// padding)
				if (b <= 0xF)
					m_szUniqueID += "0";
				// add number to string
				m_szUniqueID += Integer.toHexString(b);
			} // hex string to uppercase
			m_szUniqueID = m_szUniqueID.toUpperCase();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return m_szUniqueID;
	}
	
	
	/** 取得設備解析度
	* 
	* @return strDeviceDispaly 設備解析度
	* @throws exceptions No exceptions thrown
	*/
	public String getDeviceDispaly() {
		/**
		 *  strDeviceDispaly 設備解析度 
		 */
		String strDeviceDispaly = new String();
		
		try {
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return strDeviceDispaly;
	}
}

///:~
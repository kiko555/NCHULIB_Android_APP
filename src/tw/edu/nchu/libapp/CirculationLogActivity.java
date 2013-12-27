//: object/CirculationLogActivity.java
package tw.edu.nchu.libapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 流通資料畫面所需程式
 * 
 * @author kiko
 * @version 1.0
 */
public class CirculationLogActivity extends ActionBarActivity {
    /**
     * GroupData 定義第一層清單
     * 
     * ChildrenData 定義第二層清單
     */
    private List<String> GroupData;
    private List<List<String>> ChildrenData;
    SimpleAdapter saPartonLoan_RequestAdapter;
    protected SimpleExpandableListAdapter mySimpleExpandableListAdapter = null;
    ExpandableListView myExpandableListView;

    // 宣告自訂的廣播接受器
    mServiceReceiver mServiceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_cirlog);

        // 隱藏讀取鈕
        setSupportProgressBarIndeterminateVisibility(false);

        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();

        // 使用擴展選單來呈現流通資料
        myExpandableListView = (ExpandableListView) findViewById(R.id.expandableCirLogListView1);

        // 載入資料庫的內容
        LoadListData();

        try {
            // 把清單附加上去
            myExpandableListView.setAdapter(mySimpleExpandableListAdapter);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
      //宣告一個自訂的BroadcastReceiver , 稍後我們會在onResume() 動態註冊
        mServiceReceiver = new mServiceReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();

        // 載入資料庫的內容
        LoadListData();

        try {
            // 把清單附加上去
            myExpandableListView.setAdapter(mySimpleExpandableListAdapter);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
      //宣告一個IntentFilter並使用我們之前自訂的action
        IntentFilter IFilter = new IntentFilter();
        IFilter.addAction("tw.edu.nchu.libapp.Auth_Message");
        
      //動態註冊BroadcastReceiver
        registerReceiver(mServiceReceiver,IFilter);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
      //動態註銷BroadcastReceiver
        unregisterReceiver(mServiceReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TaskServeice", "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.circulation_log_activity, menu);
        return true;
    }

    // 選單鈕被按住後會出現在動作
    @Override
    public boolean onOptionsItemSelected(MenuItem Item) {
        switch (Item.getItemId()) {
        case R.id.action_refresh:
            setSupportProgressBarIndeterminateVisibility(true);
            UpdateCirLogData("Token登入");
            return true;
        case R.id.action_settings:
            Intent intent = new Intent();
            intent.setClass(CirculationLogActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return true;
        case R.id.action_systemlog:
            Intent intent1 = new Intent();
            intent1.setClass(CirculationLogActivity.this,
                    SystemLogActivity.class);
            startActivity(intent1);
            finish();
            return true;
        case R.id.action_exit:
            finish();
            return true;
        default:
            return false;
        }
    }

    /**
     * 檢查資料是否沒有資料，如果是就跳轉到登入畫面
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private void CheckIfDBEmpty() {
        try {
            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(CirculationLogActivity.this);

            int intCountPartonTable = dbHelper.doCountPartonTable();

            // 關閉資料庫
            dbHelper.close();

            if (intCountPartonTable != 1) {
                // 在資料庫中無登入紀錄，立刻跳轉到登入畫面
                Intent intent = new Intent();
                intent.setClass(CirculationLogActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 呼叫資料庫帶出借閱或預約資料，並將其轉入擴展清單功能所需參數中
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    @SuppressLint("SimpleDateFormat")
    private void LoadListData() {
        try {
            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(CirculationLogActivity.this);

            // 建立子清單
            ArrayList<ArrayList<HashMap<String, String>>> menusubitems = new ArrayList<ArrayList<HashMap<String, String>>>();

            // 將回傳的全部的借閱資料陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan 中
            ArrayList<HashMap<String, String>> arylistPartonLoan = dbHelper
                    .getPartonLoanTable(CirculationLogActivity.this, 0);

            // 將回傳的借閱資料快到期陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan_Due 中
            ArrayList<HashMap<String, String>> arylistPartonLoan_Due = dbHelper
                    .getPartonLoanTable(CirculationLogActivity.this, 1);

            // 將回傳的借閱資料已過期陣列透過HashMap方式儲存， 最後轉入arylistPartonLoan_Due 中
            ArrayList<HashMap<String, String>> arylistPartonLoan_OverDue = dbHelper
                    .getPartonLoanTable(CirculationLogActivity.this, 2);

            // 將回傳的全部的預約資料透過HashMap方式儲存，最後再以arraylist的方式提供給listview介面使用
            ArrayList<HashMap<String, String>> arylistPartonLoan_Request = dbHelper
                    .getPartonLoanTable_Request(CirculationLogActivity.this);

            menusubitems.add(arylistPartonLoan);
            menusubitems.add(arylistPartonLoan_Due);
            menusubitems.add(arylistPartonLoan_OverDue);
            menusubitems.add(arylistPartonLoan_Request);

            // 建立母清單，計有四個項目，並填入預設值
            // 母清單在子之下，是為了拿到子的數量
            ArrayList<HashMap<String, String>> menuitems = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> item1 = new HashMap<String, String>();
            HashMap<String, String> item2 = new HashMap<String, String>();
            HashMap<String, String> item3 = new HashMap<String, String>();
            HashMap<String, String> item4 = new HashMap<String, String>();
            item1.put(
                    "items",
                    (String) this.getResources().getText(
                            R.string.ActivityCirculationLog_lvLoanList)
                            + " (" + (arylistPartonLoan.size() - 1) + ")");
            item2.put(
                    "items",
                    (String) this.getResources().getText(
                            R.string.ActivityCirculationLog_lvDueList)
                            + " (" + (arylistPartonLoan_Due.size() - 1) + ")");
            item3.put(
                    "items",
                    (String) this.getResources().getText(
                            R.string.ActivityCirculationLog_lvOverduesList)
                            + " ("
                            + (arylistPartonLoan_OverDue.size() - 1)
                            + ")");
            item4.put(
                    "items",
                    (String) this.getResources().getText(
                            R.string.ActivityCirculationLog_lvRequestList)
                            + " ("
                            + (arylistPartonLoan_Request.size() - 1)
                            + ")");
            menuitems.add(item1);
            menuitems.add(item2);
            menuitems.add(item3);
            menuitems.add(item4);

            // 將清單附加上UI
            mySimpleExpandableListAdapter = new SimpleExpandableListAdapter(
                    this, menuitems, R.layout.activity_cirlog_item,
                    new String[] { "items" },
                    new int[] { R.id.CirLogListView }, menusubitems,
                    R.layout.activity_cirlog_subitem, new String[] { "Title",
                            "Time" }, new int[] { R.id.CirLogListView_Title,
                            R.id.CirLogListView_Time });

            // 關閉資料庫
            dbHelper.close();

        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 更新借閱資料
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    private void UpdateCirLogData(String JobType) {
        try {
            // 宣告LOG物件，並決定工作類型
            LOGClass logclass = new LOGClass();
            String logJobType = JobType;

            // 宣告處理JSON的物件
            JSONClass jsonClass = new JSONClass();

            
            
         // 建立連線服務完成認證工作
            Intent HTTPServiceIntent = new Intent(CirculationLogActivity.this,
                    HTTPServiceClass.class);
            
            
            // HTTP服務所要送出的值
            HTTPServiceIntent.putExtra("OP", "TokenAuth");
            // 啟動HTTP服務
            startService(HTTPServiceIntent);
            
            
//            // 呼叫Token認證程序
//            AuthClass authclass = new AuthClass();
//            String strReturnContent = authclass.doTokenAuth(
//                    CirculationLogActivity.this, logJobType);
//
//            if (strReturnContent != null) {
//                // 呼叫jsonClass處理JSON並寫入資料庫，會回傳交易狀態的各項值
//                HashMap<String, String> hmOpResult = jsonClass
//                        .setTokenResultJSONtoDB(strReturnContent,
//                                CirculationLogActivity.this);
//
//                try {
//                    // 如果系統運作正常才繼續下去
//                    if (hmOpResult.get("OpResult").equals("Success")) {
//                        // 如果認證成功才執行
//                        if (hmOpResult.get("AuthResult").equals("Success")) {
//                            // 寫log
//                            logclass.setLOGtoDB(CirculationLogActivity.this,
//                                    logJobType, new java.text.SimpleDateFormat(
//                                            "yyyy-MM-dd HH:mm:ss")
//                                            .format(new java.util.Date()),
//                                    "5.認證成功");
//                        } else {
//                            // 寫log
//                            logclass.setLOGtoDB(CirculationLogActivity.this,
//                                    logJobType, new java.text.SimpleDateFormat(
//                                            "yyyy-MM-dd HH:mm:ss")
//                                            .format(new java.util.Date()),
//                                    "5.認證失敗-Patron或Device Token錯誤");
//                            // 認證失敗就丟個警告
//                            Toast.makeText(
//                                    CirculationLogActivity.this,
//                                    R.string.ActivityCirculationLog_toastTokenFail,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        // TODO: 增加系統狀態的判斷
//                    }
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 自訂的廣播接受器類別
     * 
     * @throws exceptions
     *             No exceptions thrown
     */
    public class mServiceReceiver extends BroadcastReceiver
    {
      @Override
      public void onReceive(Context context, Intent intent)
      {
       
       if(intent.getAction().equals("tw.edu.nchu.libapp.Auth_Message"))
       {
           LOGClass logclass = new LOGClass();
           String logJobType = "Token登入";

        // 資料抓取完畢將讀取鈕移除
           setSupportProgressBarIndeterminateVisibility(false);
           
           if(intent.getStringExtra("AuthResult").equals("Success")){
               // 更新畫面
               LoadListData();
               
               // 寫log
             logclass.setLOGtoDB(CirculationLogActivity.this,
                     logJobType, new java.text.SimpleDateFormat(
                             "yyyy-MM-dd HH:mm:ss")
                             .format(new java.util.Date()),
                     "5.認證成功");
          // 認證成功就丟通知
             Toast.makeText(CirculationLogActivity.this,
                     R.string.ActivityCirculationLog_toastTokenSuccess,
                     Toast.LENGTH_SHORT).show();
           }else{
            // 寫log
             logclass.setLOGtoDB(CirculationLogActivity.this,
                     logJobType, new java.text.SimpleDateFormat(
                             "yyyy-MM-dd HH:mm:ss")
                             .format(new java.util.Date()),
                     "5.認證失敗-Patron或Device Token錯誤");
             
            // 認證失敗就丟個警告
               Toast.makeText(CirculationLogActivity.this,
                       R.string.ActivityCirculationLog_toastTokenFail,
                       Toast.LENGTH_SHORT).show();
           }
       }
      }
    }

    private class ExpandableAdapter extends BaseExpandableListAdapter {
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return ChildrenData.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            TextView myText = null;
            if (convertView != null) {
                myText = (TextView) convertView;
                myText.setText(ChildrenData.get(groupPosition).get(
                        childPosition));
            } else {
                myText = createView(ChildrenData.get(groupPosition).get(
                        childPosition));
            }
            return myText;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return ChildrenData.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return GroupData.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return GroupData.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            TextView myText = null;
            if (convertView != null) {
                myText = (TextView) convertView;
                myText.setText(GroupData.get(groupPosition));
            } else {
                myText = createView(GroupData.get(groupPosition));
            }
            return myText;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        private TextView createView(String content) {
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 80);
            TextView myText = new TextView(CirculationLogActivity.this);
            myText.setLayoutParams(layoutParams);
            myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            myText.setPadding(80, 0, 0, 0);
            myText.setText(content);
            return myText;
        }
    }

}
// /:~
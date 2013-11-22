//: object/CirculationLogActivity.java
package tw.edu.nchu.libapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定讀取圖示
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_cirlog);

        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();

        // 載入資料庫的內容
        LoadListDate();

        // 使用擴展選單來呈現流通資料
        ExpandableListView myExpandableListView = (ExpandableListView) findViewById(R.id.expandableCirLogListView1);
        myExpandableListView.setAdapter(new ExpandableAdapter());

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 確認資料庫是否有資料，如無跳轉到登入畫面
        CheckIfDBEmpty();
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
        case R.id.action_settings:
            Intent intent = new Intent();
            intent.setClass(CirculationLogActivity.this, SettingsActivity.class);
            startActivity(intent);
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
            if (intCountPartonTable != 1) {
                // 在資料庫中無登入紀錄，立刻跳轉到登入畫面
                Intent intent = new Intent();
                intent.setClass(CirculationLogActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                finish();
            }

            // 關閉資料庫
            dbHelper.close();
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
    @SuppressWarnings("unchecked")
    @SuppressLint("SimpleDateFormat")
    private void LoadListDate() {
        try {
            // 建立取用資料庫的物件
            DBHelper dbHelper = new DBHelper(CirculationLogActivity.this);

            // 將回傳的全部的借閱資料陣列轉入 aryPartonLoan 中
            String[][] aryPartonLoan = dbHelper.getPartonLoanTable();

            // 將回傳的全部的預約資料陣列轉入 aryPartonLoan 中
            String[][] aryPartonLoan_Request = dbHelper
                    .getPartonLoanTable_Request();

            // 建立子擴展清單功能所需的陣列
            String[] aryChildPartonLoan = new String[aryPartonLoan[0].length];
            @SuppressWarnings("rawtypes")
            Vector vectorChildPartonLoan_Due = new Vector();
            @SuppressWarnings("rawtypes")
            Vector vectorChildPartonLoan_OverDue = new Vector();
            String[] aryChildPartonLoan_Request = null;

            // 將 aryPartonLoan 全部借閱資料陣列的值合併，並將其中內容帶入"，到期日："，最後整合至
            // aryChildPartonLoan中
            for (int i = 0; i < aryPartonLoan[0].length; i++) {
                try {
                    aryChildPartonLoan[i] = aryPartonLoan[0][i].toString()
                            + (String) this.getResources().getText(
                                    R.string.ActivityCirculationLog_lvDuedate)
                            + aryPartonLoan[1][i].toString();

                    // 利用判斷天數來控制清單的內容
                    SimpleDateFormat smdf = new SimpleDateFormat("yyyyMMdd");

                    // 取得今天日期
                    Calendar cal = new GregorianCalendar();
                    cal.set(Calendar.HOUR_OF_DAY, 0); // anything 0 - 23
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    Date dateToday = cal.getTime();

                    // 將到期日轉成特定格式
                    Date dateDue = smdf.parse(aryPartonLoan[1][i].toString());

                    //Date dateTestDue = smdf.parse("20131231");

                    // 計算到期日跟今天差幾天
                    long longDay = dateDue.getTime() - dateToday.getTime();
                    longDay = longDay / (24 * 60 * 60 * 1000);

                    // 判斷是過期還是快過期
                    if (longDay >= 0 && longDay < 20) {
                        vectorChildPartonLoan_Due.add(aryPartonLoan[0][i]
                                .toString() + ",倒數：" + longDay);
                    } else if (longDay < 0) {
                        vectorChildPartonLoan_OverDue.add(aryPartonLoan[0][i]
                                .toString() + ",過期：" + longDay);
                    }

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // 擴展清單所需的陣列清單
            GroupData = new ArrayList<String>();

            // 帶入讀者借閱資料表筆數，供擴展清單用
            GroupData.add((String) this.getResources().getText(
                    R.string.ActivityCirculationLog_lvLoanList)
                    + " (" + dbHelper.doCountPartonLoanTable() + ")");

            // 帶入讀者借閱到期資料表筆數，供擴展清單用
            GroupData.add((String) this.getResources().getText(
                    R.string.ActivityCirculationLog_lvDueList)
                    + " (" + vectorChildPartonLoan_Due.size() + ")");//
            
            // 帶入讀者借閱過期資料表筆數，供擴展清單用
            GroupData.add((String) this.getResources().getText(
                    R.string.ActivityCirculationLog_lvOverduesList)
                    + " (" + vectorChildPartonLoan_OverDue.size() + ")");

            // 帶入讀者預約資料表筆數，供擴展清單用
            GroupData.add((String) this.getResources().getText(
                    R.string.ActivityCirculationLog_lvRequestList)
                    + " (" + dbHelper.doCountPartonLoanTable_Request() + ")");

            ChildrenData = new ArrayList<List<String>>();

            // 將借閱資料陣列轉為arraylist格式，供UI使用
            List<String> ChildPartonLoanlist = Arrays
                    .asList(aryChildPartonLoan);
            ChildrenData.add(ChildPartonLoanlist);

            // 將即將到期資料陣列轉為arraylist格式，供UI使用
            List<String> ChildPartonLoanDuelist;
            // 判斷是否為空陣列，來決定list的內容
            if (vectorChildPartonLoan_Due.size() > 0) {
                ChildPartonLoanDuelist = new ArrayList<String>(
                        vectorChildPartonLoan_Due);
                ChildrenData.add(ChildPartonLoanDuelist);
            } else {
                ChildPartonLoanDuelist = new ArrayList<String>();
                ChildrenData.add(ChildPartonLoanDuelist);
            }

            // 將過期資料陣列轉為arraylist格式，供UI使用
            List<String> ChildPartonLoanOverDuelist;
            // 判斷是否為空陣列，來決定list的內容
            if (vectorChildPartonLoan_OverDue.size() > 0) {
                ChildPartonLoanOverDuelist = new ArrayList<String>(
                        vectorChildPartonLoan_OverDue);
                ChildrenData.add(ChildPartonLoanOverDuelist);
            } else {
                ChildPartonLoanOverDuelist = new ArrayList<String>();
                ChildrenData.add(ChildPartonLoanOverDuelist);
            }

            // 將 aryPartonLoan_Request 全部借預約資料陣列的值合併，並將其中內容帶入"，到館日："，最後整合至
            // ChildPartonLoan_Requestlist 中
            // TODO 修改到期字樣，改成預約書到館日
            for (int i = 0; i < aryPartonLoan_Request[0].length; i++) {
                aryChildPartonLoan_Request[i] = aryPartonLoan_Request[0][i]
                        .toString()
                        + (String) this.getResources().getText(
                                R.string.ActivityCirculationLog_lvDuedate)
                        + aryPartonLoan_Request[1][i].toString();
            }

            // 將array轉為arraylist格式，供UI使用
            List<String> ChildPartonLoan_Requestlist;
            // 判斷是否為空陣列，來決定list的內容
            if (aryPartonLoan_Request[0].length > 0) {
                ChildPartonLoan_Requestlist = Arrays
                        .asList(aryChildPartonLoan_Request);
                ChildrenData.add(ChildPartonLoan_Requestlist);
            } else {
                ChildPartonLoan_Requestlist = new ArrayList<String>();
                ChildrenData.add(ChildPartonLoan_Requestlist);
            }

            // 關閉資料庫
            dbHelper.close();
        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
package tw.edu.nchu.libapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class CirculationLogActivity extends ActionBarActivity {
	private List<String> GroupData;// 定义组数据
	private List<List<String>> ChildrenData;// 定义组中的子数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 設定讀取圖示
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_cirlog);

		LoadListDate();

		ExpandableListView myExpandableListView = (ExpandableListView) findViewById(R.id.expandableCirLogListView1);
		myExpandableListView.setAdapter(new ExpandableAdapter());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void LoadListDate() {
		// 建立取用資料庫的物件
		DBHelper dbHelper = new DBHelper(CirculationLogActivity.this);

		// 取得讀者借閱資料表筆數，並帶入畫面中
		GroupData = new ArrayList<String>();
		GroupData.add((String) this.getResources().getText(
				R.string.ActivityCirculationLog_lvLoanList)
				+" ("+ dbHelper.doCountPartonLoanTable() + ")");
		GroupData.add((String) this.getResources().getText(
				R.string.ActivityCirculationLog_lvDueList)
				+" ("+ dbHelper.doCountPartonLoanTable() + ")");
		GroupData.add((String) this.getResources().getText(
				R.string.ActivityCirculationLog_lvOverduesList)
				+" ("+ dbHelper.doCountPartonLoanTable() + ")");
		GroupData.add((String) this.getResources().getText(
				R.string.ActivityCirculationLog_lvRequestList)
				+" ("+ dbHelper.doCountPartonLoanTable() + ")");

		ChildrenData = new ArrayList<List<String>>();

		// 帶入全部的借閱資料
		String[][] Child1 = dbHelper.getPartonLoanTable();
		List<String> Child1list = Arrays.asList(Child1[0]);  

		ChildrenData.add(Child1list);

		List<String> Child2 = new ArrayList<String>();

		ChildrenData.add(Child2);
		List<String> Child3 = new ArrayList<String>();
		Child3.add("cccccc");
		ChildrenData.add(Child3);
		List<String> Child4 = new ArrayList<String>();
		Child4.add("dddddd");
		ChildrenData.add(Child4);
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

package tools.dannyhe.wang.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
	//#ifdef KEY
	//#expand public static String SDK_KEY = "%KEY%";
//@	public static String SDK_KEY = "%KEY%";
	//#endif
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				StringBuilder sb = new StringBuilder();
				//#ifdef FREE
//@				sb.append(" FREE");
				//#endif
				//#ifdef XIAOMI
				sb.append(" XIAOMI");
				//#endif
				//#ifdef HUAWEI
//@				sb.append(" HUAWEI");
				//#endif
				//#ifdef VIP
				sb.append(" VIP");
				//#endif
				//#if VERSION >= 5
//@				sb.append(" VERSION >= 5");
				//#endif
				//#if PRINT && FREE
//@				sb.append(" PRINT & FREE");
				//#endif
				Snackbar.make(view, sb.toString(), Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});


		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}

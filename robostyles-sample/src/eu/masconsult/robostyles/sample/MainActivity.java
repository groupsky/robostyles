package eu.masconsult.robostyles.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import eu.masconsult.robostyles.RoboStyles;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public View onCreateView(View parent, String name, Context context,
			AttributeSet attrs) {
		View view = RoboStyles.onCreateView(parent, name, context, attrs);
		if (view != null) {
			return view;
		}
		return super.onCreateView(parent, name, context, attrs);
	}

}

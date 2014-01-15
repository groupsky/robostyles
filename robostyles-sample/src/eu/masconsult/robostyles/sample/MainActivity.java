package eu.masconsult.robostyles.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import eu.masconsult.robostyles.RoboStyles;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RoboStyles.initialize(this);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

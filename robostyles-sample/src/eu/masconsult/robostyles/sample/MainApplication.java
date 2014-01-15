package eu.masconsult.robostyles.sample;

import android.app.Application;
import eu.masconsult.robostyles.RoboStyles;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		RoboStyles.initialize(this);
	}
}

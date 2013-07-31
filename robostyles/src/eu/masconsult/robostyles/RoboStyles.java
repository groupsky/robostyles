package eu.masconsult.robostyles;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

public class RoboStyles {

	private static final String TAG = "RoboStyles";

	/**
	 * marker for when we are in primary context and we need to style it, or the
	 * second call for view creation
	 */
	private static boolean inflating = false;

	public static View onCreateView(View parent, String name, Context context,
			AttributeSet attrs) {
		// if the context is already themed, no need to do it again
		if (inflating) {
			return null;
		}

		// get the defined styles on the view
		String styles = getStyles(context, attrs);
		Log.v(TAG, name + ".styles=" + styles);

		// if none are defined, we have nothing to do
		if (TextUtils.isEmpty(styles)) {
			return null;
		}

		// parse the styles
		Integer[] stylesRes = evaluateStyles(context, styles);
		// no styles - no work
		if (stylesRes.length == 0) {
			return null;
		}

		// create a new context that is themed with the defined styles
		for (int styleRes : stylesRes) {
			context = new ContextThemeWrapper(context, styleRes);
		}

		View view;
		inflating = true;
		try {
			// ask the themed context to create the view
			view = createViewInContext(parent, name, context, attrs);
		} finally {
			inflating = false;
		}
		return view;
	}

	private static Integer[] evaluateStyles(Context context, String stylesStr) {
		int resId;
		String[] styles = TextUtils.split(stylesStr, " +");
		ArrayList<Integer> stylesRes = new ArrayList<Integer>(styles.length);
		for (String style : styles) {
			if (TextUtils.isEmpty(style)) {
				continue;
			}

			resId = context.getResources().getIdentifier(style, "style",
					context.getPackageName());
			Log.d(TAG, style + "=" + Integer.toHexString(resId));
			if (resId != 0) {
				stylesRes.add(resId);
			}
		}
		Log.d(TAG, "found " + stylesRes.size() + " styles");
		return stylesRes.toArray(new Integer[] {});
	}

	private static String getStyles(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.View, 0, 0);
		try {
			return a.getString(R.styleable.View_styles);
		} finally {
			a.recycle();
		}
	}

	private static View createViewInContext(View parent, String name,
			Context context, AttributeSet attrs) {
		// using some reflection we call the layout inflater of the context as
		// if it was loading a layout
		LayoutInflater inflater = LayoutInflater.from(context);
		try {
			// we need to hack some values to private fields
			Field field = LayoutInflater.class
					.getDeclaredField("mConstructorArgs");
			field.setAccessible(true);
			Object[] mConstructorArgs = (Object[]) field.get(inflater);
			mConstructorArgs[0] = context;

			// then we call the hidden method to create the view
			Method method = LayoutInflater.class.getDeclaredMethod(
					"createViewFromTag", View.class, String.class,
					AttributeSet.class);
			method.setAccessible(true);
			return (View) method.invoke(inflater, parent, name, attrs);
		} catch (Exception e) {
			// probably we'd better not crash the app, but let's see when this
			// happens
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}

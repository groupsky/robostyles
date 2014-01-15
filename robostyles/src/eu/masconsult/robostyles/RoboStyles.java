package eu.masconsult.robostyles;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import se.fishtank.css.selectors.Selector;
import se.fishtank.css.selectors.Specifier;
import se.fishtank.css.selectors.scanner.Scanner;
import se.fishtank.css.selectors.scanner.ScannerException;
import se.fishtank.css.selectors.specifier.AttributeSpecifier;
import se.fishtank.css.selectors.specifier.AttributeSpecifier.Match;
import android.content.Context;
import android.content.res.Resources.Theme;
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

	private static ArrayList<Style> styles = new ArrayList<Style>();

	public static void initialize(Context context) {
		loadStyles(context);
		injectFactory(context);
	}

	private static void injectFactory(Context context) {
		LayoutInflater.from(context).setFactory2(new LayoutInflater.Factory2() {

			@Override
			public View onCreateView(String name, Context context,
					AttributeSet attrs) {
				throw new UnsupportedOperationException(
						"Not supporting android pre-3.0 for the moment!");
			}

			@Override
			public View onCreateView(View parent, String name, Context context,
					AttributeSet attrs) {
				return RoboStyles.onCreateView(parent, name, context, attrs);
			}
		});
	}

	private static void loadStyles(Context context) {
		Theme theme = context.getTheme();
		int styleId;
		String styleName;
		TypedArray a;
		try {
			Class<?> classStyle = context.getClassLoader().loadClass(
					context.getPackageName() + ".R$style");
			Field[] styleFields = classStyle.getFields();
			for (Field styleField : styleFields) {
				try {
					styleId = styleField.getInt(null);
				} catch (Exception e) {
					continue;
				}
				styleName = context.getResources()
						.getResourceEntryName(styleId);
				Log.d(TAG, String.format("processing R.style.%s: %s",
						styleName, styleField.getType().getSimpleName()));

				a = theme.obtainStyledAttributes(styleId,
						new int[] { R.attr.robostyle_selector });
				try {
					String selector = a.getString(0);
					Log.d(TAG, String.format("R.style.%s = %s", styleName,
							selector));

					registerStyle(styleName, selector, styleId);
				} finally {
					a.recycle();
				}
			}

			String[] res = context.getResources().getAssets().list("");
			for (String s : res) {
				Log.d(TAG, "assets=/" + s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void registerStyle(String name, String selector, int styleId) {
		if (TextUtils.isEmpty(selector)) {
			return;
		}
		try {
			List<List<Selector>> groups = new Scanner(selector).scan();
			if (groups.isEmpty()) {
				return;
			}

			styles.add(new Style(name, selector, styleId, groups));
		} catch (ScannerException e) {
			Log.w(TAG, String
					.format("Can't parse selector for style %s: \"%s\"", name,
							selector), e);
		}
	}

	private static View onCreateView(View parent, String name, Context context,
			AttributeSet attrs) {
		// if the context is already themed, no need to do it again
		if (inflating) {
			return null;
		}

		// get the defined styles on the view
		String classes = getClasses(context, attrs);
		Log.v(TAG, name + ".classes=" + classes);

		// parse the styles
		List<Integer> stylesRes = evaluateStyles(parent, name, context, attrs,
				classes);
		// no styles - no work
		if (stylesRes.size() == 0) {
			return null;
		}

		// create a new context that is themed with the defined styles
		context = new ContextThemeWrapper(context, stylesRes.get(0));
		Theme theme = context.getTheme();
		for (int styleRes : stylesRes) {
			theme.applyStyle(styleRes, true);
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

	private static List<Integer> evaluateStyles(View parent, String name,
			Context context, AttributeSet attrs, String stylesStr) {
		// int resId;
		// String[] styles = TextUtils.split(stylesStr, " +");
		ArrayList<Integer> stylesRes = new ArrayList<Integer>();

		for (Style style : RoboStyles.styles) {
			if (style.matches(parent, name, context, attrs, stylesStr)) {
				stylesRes.add(style.styleId);
			}
		}

		Log.d(TAG, "found " + stylesRes.size() + " styles");
		return stylesRes;
	}

	private static String getClasses(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				new int[] { R.attr.robostyle_classes }, 0, 0);
		try {
			return a.getString(0);
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

	private static final class Style {
		final String name;
		final String selector;
		final int styleId;
		final List<List<Selector>> groups;

		public Style(String name, String selector, int styleId,
				List<List<Selector>> groups) {
			this.name = name;
			this.selector = selector;
			this.styleId = styleId;
			this.groups = groups;
		}

		public boolean matches(View parent, String viewName, Context context,
				AttributeSet attrs, String classes) {
			Log.d(TAG, String.format(
					"matching %s(%s) for %s within %s and classes %s", name,
					selector, viewName, parent != null ? parent.getClass()
							.getSimpleName() : parent, classes));

			Selector selector;

			for (List<Selector> group : groups) {
				for (int i = group.size() - 1; i >= 0; i--) {
					selector = group.get(i);

					// check tag
					if (!TextUtils.equals(Selector.UNIVERSAL_TAG,
							selector.getTagName())) {
						if (!viewName.endsWith(selector.getTagName())) {
							return false;
						}
					}

					// check specifiers
					if (selector.hasSpecifiers()) {
						for (Specifier spec : selector.getSpecifiers()) {
							switch (spec.getType()) {
							case ATTRIBUTE: {
								AttributeSpecifier attributeSpec = (AttributeSpecifier) spec;

								// check id
								if (TextUtils.equals("id",
										attributeSpec.getName())) {
									int id;
									TypedArray a = context
											.getTheme()
											.obtainStyledAttributes(
													attrs,
													new int[] { android.R.attr.id },
													0, 0);
									try {
										id = a.getResourceId(0, View.NO_ID);
									} finally {
										a.recycle();
									}
									// no id, and the attribute requires id
									if (id == View.NO_ID) {
										return false;
									}

									String idName = context.getResources()
											.getResourceEntryName(id);

									if (!matchAttributeSpec(idName,
											attributeSpec.getMatch(),
											attributeSpec.getValue())) {
										return false;
									}
								} else if (TextUtils.equals("class",
										attributeSpec.getName())) {
									if (!matchAttributeSpec(classes,
											attributeSpec.getMatch(),
											attributeSpec.getValue())) {
										return false;
									}
								} else {
									Log.w(TAG,
											String.format(
													"[%s %s %s] - unsupported attribute!",
													attributeSpec.getName(),
													attributeSpec.getMatch(),
													attributeSpec.getValue()));
								}
								break;
							}
							default: {
								Log.w(TAG, String.format(
										"%s - unsupported specification!",
										spec.getType()));
								break;
							}
							}
						}
					}

					// check combinator
					switch (selector.getCombinator()) {
					case DESCENDANT:
						if (i == 0) {
							break;
						} else {
							Log.w(TAG, String
									.format("%s - unsupported combinator!"
											+ selector.getCombinator()));
						}
					default:
						Log.w(TAG,
								String.format("%s - unsupported combinator!"
										+ selector.getCombinator()));
						break;
					}
				}
			}

			return true;
		}

		private boolean matchAttributeSpec(String actual, Match match,
				String expected) {
			if (actual == null) {
				return false;
			}

			switch (match) {
			case EXACT:
				return TextUtils.equals(actual, expected);
			case LIST: {
				String[] items = TextUtils.split(actual, " +");
				for (String item : items) {
					if (TextUtils.equals(item, expected)) {
						return true;
					}
				}
				return false;
			}
			default:
				Log.w(TAG, String.format("%s - unsupported match type!", match));
				return false;
			}
		}
	}

}

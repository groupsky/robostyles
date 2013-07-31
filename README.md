robostyles [![Build Status](https://travis-ci.org/groupsky/robostyles.png?branch=master)](https://travis-ci.org/groupsky/robostyles)
==========
An android library for applying multiple styles on android view

example
-------
```xml
    <TextView xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:robostyles="http://schemas.android.com/apk/res-auto"
        style="@style/MyText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="#00f"
        android:text="@string/hello_world"
        robostyles:styles="view text label secondary_label" />
```


info
----
The library allows to specify multiple styles (plain old android styles) to be applied on a view. Currently only application styles can be used (no ```@android:styles/...```).
Styles are specified only by their name, so instead of writing ```@style/MyText```, only ```MyText``` is necessary. The order in which styles are applied is:
 1. Context theme
 2. Styles (the new robostyles attribute)
 3. View style (the original style attribute)
 4. View attributes


how to use
----------
 1. Clone the library
 2. Add as android library to your project
 3. In your activities override the onCreateView:
```java
	@Override
	public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
		View view = RoboStyles.onCreateView(parent, name, context, attrs);
		if (view != null) {
			return view;
		}
		return super.onCreateView(parent, name, context, attrs);
	}
```

 4. Declare the namespace in each layout ```xmlns:robostyles="http://schemas.android.com/apk/res-auto"```
 5. Add ```robostyles:styles="style1 style2"``` to your views
 6. Enjoy


limitations
-----------
Currently the lib works for honeycomp(3.0+) and newer.



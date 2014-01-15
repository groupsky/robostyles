robostyles
==========
[![Build Status](https://travis-ci.org/groupsky/robostyles.png?branch=master)](https://travis-ci.org/groupsky/robostyles)

Android styling with CSS3 selectors.

**Note:** The library is still in early alpha, so use at your own risk!

example
-------
```xml
    <!-- Standard views may contain additional attribute classes to equivalent of HTML class attribute -->
    <TextView xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:robostyles="http://schemas.android.com/apk/res-auto"
        android:id="@+id/label"
        style="@style/MyText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="#00f"
        android:text="@string/hello_world"
        robostyles:classes="view text label secondary_label" />
    
    <!-- Styles have additional attribute robostyle_selector where CSS3 selector is written -->
    <style name="Selector_Tag">
        <item name="robostyle_selector">Label</item>
        <item name="android:textSize">10sp</item>
    </style>
    
    <!-- The name of the style is not used, but is required by Android -->
    <style name="Selector_Class">
        <item name="robostyle_selector">.main-label</item>
        <item name="android:textSize">15sp</item>
    </style>
    
    <!-- id selector uses the value in android:id tag -->
    <style name="Selector_ID">
        <item name="robostyle_selector">#label</item>
        <item name="android:background">#c33</item>
        <item name="android:buttonStyle">@style/Selector_ID</item>
    </style>

```


info
----
The library allows to use CSS3 selectors to apply styles on arbitrary views. Styles are normal android styles with additional item **robostyle_selector**. 
The order in which attribute value is resolved is:
 1. View attributes
 2. View style (the original style attribute)
 3. Styles matched using robostyle_selector
 4. Context theme
The first instance found is used.

**Note:** the styles are applied as part of the theme, so for styling button you need to use *android:buttonStyle* with reference to a style to be used, which may be the same style:
```xml
    <style name="ButtonStyle">
        <item name="robostyle_selector">Button</item>
        <item name="android:buttonStyle">@style/ButtonStyle</item>
        <item name="android:background">#c33</item>
    </style>
```

how to use
----------
 1. Clone the library
 2. Add as android library to your project
 3. Add to your *Application#OnCreate*:

```java
	@Override
	public void onCreate() {
		super.onCreate();
		RoboStyles.initialize(this);
		
		...
	}
```
 4. Add at the beggining of each of your *Activity#OnCreate*:

```java
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RoboStyles.inject(this);

		...
		setContentView(R.layout.activity_main);
		...
	}
```
 5. Enjoy


limitations
-----------
- Currently the lib works for honeycomp(3.0+) and newer.
- Only a small subset of CSS3 selectors is supported, but work is under way to achieve nearly 100% support.




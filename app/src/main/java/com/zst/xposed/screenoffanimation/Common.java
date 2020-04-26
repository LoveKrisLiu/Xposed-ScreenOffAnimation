package com.zst.xposed.screenoffanimation;

import android.os.Build;

public class Common {
	// Main
	public static final String PACKAGE_THIS = Common.class.getPackage().getName();
	public final static String BROADCAST_REFRESH_SETTINGS = PACKAGE_THIS + ".REFRESH_SETTINGS";
	public final static String BROADCAST_TEST_OFF_ANIMATION = PACKAGE_THIS + ".TEST_OFF_ANIMATION";
	public final static String BROADCAST_TEST_ON_ANIMATION = PACKAGE_THIS + ".TEST_ON_ANIMATION";
	public final static String EXTRA_TEST_ANIMATION = "animation";
	
	// Animation Numbers
	public static class Anim {
		public final static int UNKNOWN = 0;
		public final static int FADE = 1;
		public final static int CRT = 2;
		public final static int CRT_VERTICAL = 3;
		public final static int SCALE = 4;
		public final static int TV_BURN = 5;
		public final static int LG_OPTIMUS_G = 6;
		public final static int FADE_TILES = 7;
		public final static int VERTU_SIG_TOUCH = 8;
		public final static int LOLLIPOP_FADE_OUT = 9;
		public final static int SCALE_BOTTOM = 10;
		public final static int BOUNCE = 11;
		public final static int FLIP = 12;
		public final static int WP8 = 13;
		public final static int FLIP_TILES = 14;
		public final static int SIDE_FILL = 15;
		public final static int RANDOM = 99;
	}
	
	// Preferences
	public static class Pref {
		public final static String PREF_MAIN = "pref_main";
		
		// Preference Keys
		public static class Key {
			public final static String ENABLED = "anim_enabled";
			public final static String EFFECT = "anim_effect";
			public final static String SPEED = "anim_speed";
			public final static String RANDOM_LIST = "anim_random";
			
			public final static String ON_ENABLED = "wake_enabled";
			public final static String ON_EFFECT = "wake_effect";
			public final static String ON_SPEED = "wake_speed";
			public final static String ON_RANDOM_LIST = "wake_random";

			//custom delay before screen on animation
			public final static String ENABLE_DEFAULT_DELAY_OVERRIDE = "override_delay_enabled";
			public final static String DELAY_OVERRIDE_SPEED = "override_delay_speed";

		}
		
		// Preference Default Values
		public static class Def {
			public final static boolean ENABLED = false;
			public final static int EFFECT = Anim.FADE;
			public final static int SPEED = 300;
			public final static int STOCK_DELAY = 600;

			public final static String RANDOM_LIST = "";

		}
	}
	
	// Others
	public final static String LOG_TAG = "Xposed-ScreenOffAnimation" + " (SDK" + Build.VERSION.SDK_INT + ")" + ": ";
}

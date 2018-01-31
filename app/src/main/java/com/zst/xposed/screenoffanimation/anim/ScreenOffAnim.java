package com.zst.xposed.screenoffanimation.anim;

import com.zst.xposed.screenoffanimation.MainXposed;
import com.zst.xposed.screenoffanimation.helpers.TouchConsumer;
import com.zst.xposed.screenoffanimation.helpers.Utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import static android.view.WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;

public abstract class ScreenOffAnim {
	// Hidden in API
	public static final int TYPE_SECURE_SYSTEM_OVERLAY = FIRST_SYSTEM_WINDOW + 15;
	
	private final static WindowManager.LayoutParams LAYOUT_PARAM = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
			TYPE_SECURE_SYSTEM_OVERLAY,
			0 | WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN |
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
				WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			PixelFormat.TRANSLUCENT);
	
	private final MethodHookParam mMethodParam;
	private final WindowManager mWM;
	private final PowerManager mPM;
	private final Context mContext;
	
	FrameLayout mFrame;
	TouchConsumer mConsumer;
	
	public ScreenOffAnim(Context ctx, WindowManager wm, final MethodHookParam param) {
		mWM = wm;
		mContext = ctx;
		mMethodParam = param;
		mPM = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		
		mFrame = new FrameLayout(ctx);
		mFrame.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		mConsumer = new TouchConsumer(ctx, wm);
		/* The system forces windows of TYPE_SECURE_SYSTEM_OVERLAY to be non-focusable
		 * or touchable. Thus, the user would be able to interact with their apps while
		 * the animation is playing. (Accidental touches such as putting the phone into
		 * the pocket while the animation is playing might be registered too).
		 * 
		 * We thus add another view of TYPE_SYSTEM_ERROR that is completely transparent
		 * to consume all the touches. We will remove them after the animation finishes.
		 */
	}
	
	public void showScreenOffView(View animating_view) {
		MainXposed.mAnimationRunning = true;
		
		mConsumer.start();
		
		mFrame.removeAllViews();
		mFrame.addView(animating_view);
		
		try {
			if (Build.VERSION.SDK_INT >= 19) {
				/* On Kitkat onwards, MATCH_PARENT by default doesn't fill
				 * up the entire screen even though it has the permissions
				 * to overlay everything.
				 * We find the height and width and set them accordingly.
				 * Furthermore, Gravity.CENTER seems to be slightly above 
				 * the actual center. Thus, showing a few pixels of the 
				 * nav bar. Setting to TOP | LEFT fixes that. */
				DisplayMetrics displayMetrics = new DisplayMetrics();
				mWM.getDefaultDisplay().getRealMetrics(displayMetrics);
				
				WindowManager.LayoutParams params = new WindowManager.LayoutParams();
				params.copyFrom(LAYOUT_PARAM);
				params.width = displayMetrics.widthPixels;
				params.height = displayMetrics.heightPixels;
				params.gravity = Gravity.TOP | Gravity.LEFT;
				
				mWM.addView(mFrame, params);
			} else {
				mWM.addView(mFrame, LAYOUT_PARAM);
			}
			animateScreenOffView();
		} catch (Exception e) {
			Utils.log("(ScreenOffAnim) Error adding view to WindowManager", e);
		}
	}
	
	public abstract void animateScreenOffView();
	
	public void finishScreenOffAnim() {
		MainXposed.mAnimationRunning = false;
		try {
			if (mMethodParam != null)
				Utils.callOriginal(mMethodParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (mMethodParam != null && 
				mMethodParam.method.getName().equals("goToSleepNoUpdateLocked")) {
			XposedHelpers.callMethod(mMethodParam.thisObject, "updatePowerStateLocked");
			// By the time the animation finishes, the system thinks the phone
			// is already timed-out and sleeping. We must call the update method
		}
		
		if (!mPM.isScreenOn() || mMethodParam == null) {
			new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					hideScreenOffView();
				}
			}, 500);
			// Give the system enough time to turn off the screen
		} else {
			/* (This only happens on ICS/JB)
			 * If user taps on the screen when the animation has just
			 * finished, some systems would register it & cancel the
			 * screen off event.
			 * 
			 * If screen is still on after calling native method, use
			 * PowerManager's public API to attempt screen of again.
			 */
			
			MainXposed.mDontAnimate = true;
			// set to not animate so the animation hook will not
			// be called again and go in an infinite loop
			
			mPM.goToSleep(SystemClock.uptimeMillis());
			
			new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					MainXposed.mDontAnimate = false;
					Utils.logcat("(ScreenOffAnim) Reattempt Screen Off (Removed)");
					hideScreenOffView();
				}
			}, 750);
			// More delay than above is needed because we are calling through a binder
		}
	}
	
	private void hideScreenOffView() {
		try {
			mWM.removeView(mFrame);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mConsumer.stop();
	}
}

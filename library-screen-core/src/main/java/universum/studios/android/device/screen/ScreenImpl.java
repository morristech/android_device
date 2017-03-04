/*
 * =================================================================================================
 *                             Copyright (C) 2016 Universum Studios
 * =================================================================================================
 *         Licensed under the Apache License, Version 2.0 or later (further "License" only).
 * -------------------------------------------------------------------------------------------------
 * You may use this file only in compliance with the License. More details and copy of this License
 * you may obtain at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * You can redistribute, modify or publish any part of the code written within this file but as it
 * is described in the License, the software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES or CONDITIONS OF ANY KIND.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 * =================================================================================================
 */
package universum.studios.android.device.screen;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

/**
 * A {@link Screen} implementation.
 *
 * @author Martin Albedinsky
 */
final class ScreenImpl implements Screen {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "ScreenImpl";

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Lock used for synchronized operations.
	 */
	private static final Object LOCK = new Object();

	/**
	 * ScreenImpl singleton instance.
	 */
	@SuppressLint("StaticFieldLeak")
	private static ScreenImpl sInstance;

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Display metrics.
	 */
	private final DisplayMetrics mMetrics = new DisplayMetrics();

	/**
	 * Application context.
	 */
	private final Context mContext;

	/**
	 * "Default" width of the current Android device's screen.
	 */
	private final int mWidth;

	/**
	 * "Default" height of the current Android device's screen.
	 */
	private final int mHeight;

	/**
	 * Window manager service.
	 */
	private final WindowManager mWindowManager;

	/**
	 * Default orientation of the current Android device's screen.
	 */
	private final int mDefaultOrientation;

	/**
	 * Type of the current Android device's screen.
	 */
	private ScreenType mType = ScreenType.UNKNOWN;

	/**
	 * Density of the current Android device's screen.
	 */
	private ScreenDensity mDensity = ScreenDensity.UNKNOWN;

	/**
	 * Raw density of the current Android device's screen.
	 */
	private int mRawDensity = -1;

	/**
	 * Actual orientation of the current Android device's screen.
	 */
	private int mCurrentOrientation = ORIENTATION_UNSPECIFIED;

	/**
	 * Actual rotation of the current Android device's screen.
	 */
	private ScreenRotation mCurrentRotation = ScreenRotation.UNKNOWN;

	/**
	 * Actual width of the current Android device's screen. Depends on the actual screen orientation.
	 */
	private int mCurrentWidth;

	/**
	 * Actual height of the current Android device's screen. Depends on the actual screen orientation.
	 */
	private int mCurrentHeight;

	/**
	 * Flag indicating whether the current Android device's screen orientation is currently locked or
	 * not.
	 */
	private boolean mOrientationLocked;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ScreenImpl.
	 *
	 * @param applicationContext Application context used to access system services.
	 */
	private ScreenImpl(Context applicationContext) {
		this.mContext = applicationContext;
		this.mWindowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
		this.refresh();
		// Resolve default orientation.
		switch (mCurrentRotation) {
			case ROTATION_0:
			case ROTATION_180:
				switch (mCurrentOrientation) {
					case ORIENTATION_LANDSCAPE:
					case ORIENTATION_REVERSE_LANDSCAPE:
						this.mDefaultOrientation = ORIENTATION_LANDSCAPE;
						break;
					case ORIENTATION_PORTRAIT:
					case ORIENTATION_REVERSE_PORTRAIT:
						this.mDefaultOrientation = ORIENTATION_PORTRAIT;
						break;
					default:
						this.mDefaultOrientation = ORIENTATION_UNSPECIFIED;
						break;
				}
				break;
			case ROTATION_90:
			case ROTATION_270:
				switch (mCurrentOrientation) {
					case ORIENTATION_LANDSCAPE:
					case ORIENTATION_REVERSE_LANDSCAPE:
						this.mDefaultOrientation = ORIENTATION_PORTRAIT;
						break;
					case ORIENTATION_PORTRAIT:
					case ORIENTATION_REVERSE_PORTRAIT:
						this.mDefaultOrientation = ORIENTATION_LANDSCAPE;
						break;
					default:
						this.mDefaultOrientation = ORIENTATION_UNSPECIFIED;
						break;
				}
				break;
			default:
				this.mDefaultOrientation = ORIENTATION_UNSPECIFIED;
				break;
		}
		this.mDensity = ScreenDensity.resolve(mRawDensity = mMetrics.densityDpi);
		// Resolve screen type.
		float defaultWidthDP, defaultHeightDP;
		switch (mCurrentOrientation) {
			case ORIENTATION_LANDSCAPE:
			case ORIENTATION_REVERSE_LANDSCAPE:
				defaultWidthDP = pixelToDP(mMetrics.heightPixels);
				defaultHeightDP = pixelToDP(mMetrics.widthPixels);
				break;
			case ORIENTATION_PORTRAIT:
			case ORIENTATION_REVERSE_PORTRAIT:
				defaultWidthDP = pixelToDP(mMetrics.widthPixels);
				defaultHeightDP = pixelToDP(mMetrics.heightPixels);
				break;
			case ORIENTATION_UNSPECIFIED:
			default:
				defaultWidthDP = pixelToDP(mMetrics.widthPixels);
				defaultHeightDP = pixelToDP(mMetrics.heightPixels);
				break;
		}
		this.mType = ScreenType.resolve(defaultWidthDP, defaultHeightDP);
		// Resolve actual screen metrics.
		final boolean reverse;
		switch (mDefaultOrientation) {
			case ORIENTATION_PORTRAIT:
				reverse = mCurrentOrientation != ORIENTATION_PORTRAIT && mCurrentOrientation != ORIENTATION_REVERSE_PORTRAIT;
				break;
			case ORIENTATION_LANDSCAPE:
				reverse = mCurrentOrientation != ORIENTATION_LANDSCAPE && mCurrentOrientation != ORIENTATION_REVERSE_LANDSCAPE;
				break;
			default:
				reverse = false;
				break;
		}
		// Initialize display actual width and height.
		this.mCurrentWidth = mMetrics.widthPixels;
		this.mCurrentHeight = mMetrics.heightPixels;
		this.mWidth = reverse ? mCurrentHeight : mCurrentWidth;
		this.mHeight = reverse ? mCurrentWidth : mCurrentHeight;
	}

	/**
	 * Returns or creates a new singleton instance of ScreenImpl.
	 *
	 * @param context Context used by the screen implementation to access system services.
	 * @return Screen implementation with actual screen data available.
	 */
	@NonNull
	static ScreenImpl getsInstance(@NonNull Context context) {
		synchronized (LOCK) {
			if (sInstance == null) sInstance = new ScreenImpl(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean isOn() {
		final PowerManager power = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		return power != null && power.isScreenOn();
	}

	/**
	 */
	@Override
	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	public boolean isInteractive() {
		final PowerManager power = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		return power != null && power.isInteractive();
	}

	/**
	 */
	@Override
	public boolean lockOrientation(@NonNull Activity activity) {
		return requestOrientation(activity, ORIENTATION_CURRENT);
	}

	/**
	 */
	@Override
	public void unlockOrientation(@NonNull Activity activity) {
		requestOrientation(activity, ORIENTATION_USER);
	}

	/**
	 */
	@Override
	@SuppressWarnings("WrongConstant")
	public boolean requestOrientation(@NonNull Activity activity, @Orientation int orientation) {
		switch (orientation) {
			case ORIENTATION_CURRENT:
				activity.setRequestedOrientation(getCurrentOrientation());
				break;
			case ORIENTATION_UNSPECIFIED:
				break;
			default:
				activity.setRequestedOrientation(orientation);
				break;
		}
		return mOrientationLocked = (orientation != ORIENTATION_USER);
	}

	/**
	 */
	@Override
	@Orientation
	@SuppressWarnings("ResourceType")
	public int getRequestedOrientation(@NonNull Activity activity) {
		return activity.getRequestedOrientation();
	}

	/**
	 */
	@NonNull
	@Override
	public Display getDisplay() {
		return mWindowManager.getDefaultDisplay();
	}

	/**
	 */
	@Px
	@Override
	@IntRange(from = 0)
	public int getWidth() {
		return mWidth;
	}

	/**
	 */
	@Px
	@Override
	@IntRange(from = 0)
	public int getHeight() {
		return mHeight;
	}

	/**
	 */
	@Px
	@Override
	@IntRange(from = 0)
	public int getCurrentWidth() {
		refresh();
		return mCurrentWidth;
	}

	/**
	 */
	@Px
	@Override
	@IntRange(from = 0)
	public int getCurrentHeight() {
		refresh();
		return mCurrentHeight;
	}

	/**
	 */
	@NonNull
	@Override
	public DisplayMetrics getMetrics() {
		refresh();
		return mMetrics;
	}

	/**
	 */
	@NonNull
	@Override
	public ScreenDensity getDensity() {
		return mDensity;
	}

	/**
	 */
	@Override
	public int getRawDensity() {
		return mRawDensity;
	}

	/**
	 */
	@NonNull
	@Override
	public ScreenType getType() {
		return mType;
	}

	/**
	 */
	@Override
	@Orientation
	public int getCurrentOrientation() {
		refresh();
		return mCurrentOrientation;
	}

	/**
	 */
	@Override
	@Orientation
	public int getDefaultOrientation() {
		return mDefaultOrientation;
	}

	/**
	 */
	@NonNull
	@Override
	public ScreenRotation getCurrentRotation() {
		refresh();
		return mCurrentRotation;
	}

	/**
	 */
	@Override
	@FloatRange(from = 0)
	public float getDiagonalDistanceInInches() {
		refresh();
		// Calculation in inches will depends on the exact pixel per inch in each axis (x and y) of the device display.
		return (float) Math.sqrt(Math.pow((mWidth / mMetrics.xdpi), 2) + Math.pow((mHeight / mMetrics.ydpi), 2));
	}

	/**
	 */
	@Px
	@Override
	@IntRange(from = 0)
	@SuppressWarnings("SuspiciousNameCombination")
	public int getDiagonalDistanceInPixels() {
		refresh();
		return Math.round((float) Math.sqrt(Math.pow(mWidth, 2) + Math.pow(mHeight, 2)));
	}

	/**
	 */
	@Override
	public float getRefreshRate() {
		return getDisplay().getRefreshRate();
	}

	/**
	 */
	@Override
	@SuppressWarnings("ResourceType")
	public int getBrightness(@NonNull Activity activity) {
		final Window window = activity.getWindow();
		// Get the brightness from the current application window settings.
		return Math.round(window.getAttributes().screenBrightness * 100);
	}

	/**
	 */
	@Override
	public void setBrightness(@NonNull Activity activity, @IntRange(from = 0, to = 100) int brightness) {
		if (brightness < 0 || brightness > 100) {
			throw new IllegalArgumentException("Brightness value(" + brightness + ") is out of the range [0, 100].");
		}
		// Create new window parameters.
		final Window window = activity.getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.screenBrightness = ((brightness == 0) ? ++brightness : brightness) / 100f;
		// Set new brightness to the current application window.
		window.setAttributes(layoutParams);
	}

	/**
	 */
	@Override
	public int getSystemBrightness() {
		float brightness = 0;
		try {
			brightness = System.getInt(mContext.getContentResolver(), System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return Math.round(brightness / 255 * 100);
	}

	/**
	 */
	@Override
	public boolean isOrientationLocked() {
		return mOrientationLocked;
	}

	/**
	 */
	@Override
	public float pixelToDP(@Px int pixel) {
		// From the Android developers documentation:
		// px = dp * (dpi / 160)
		// So modified equation is:
		// dp = px / (dpi / 160)
		return pixel / (mDensity.value / 160);
	}

	/**
	 */
	@Px
	@Override
	public int dpToPixel(float dp) {
		// From the Android developers documentation:
		// px = dp * (dpi / 160)
		return Math.round(dp * (mDensity.value / 160));
	}

	/**
	 */
	@Override
	public float getScreenDP() {
		// From the Android developers documentation:
		// The density-independent pixel is equivalent to one physical pixel on
		// a 160 dpi screen, which is the baseline density assumed by the system
		// for a "medium" density screen.
		// px = dp * (dpi / 160)
		// So modified equation is:
		// dp = px / (dpi / 160)
		// and finally to determine for 1 px:
		return mDensity.value / 160;
	}

	/**
	 * Called to refresh the current data of this screen wrapper instance. This should be called whenever
	 * the current data are requested.
	 */
	private void refresh() {
		getDisplay().getMetrics(mMetrics);
		// Refresh when:
		// 1) screen orientation change occurred
		if (mCurrentWidth != mMetrics.widthPixels || mCurrentHeight != mMetrics.heightPixels) {
			onRefresh();
		}
	}

	/**
	 * Invoked to refresh/update the current data. This is invoked only in case, that the current
	 * Android device's screen orientation was changed.
	 */
	private void onRefresh() {
		// Update orientation to the actual. This call also updates the screen rotation.
		int orientation = ORIENTATION_UNSPECIFIED;
		this.mCurrentRotation = ScreenRotation.resolve(getDisplay().getRotation());
		switch (mContext.getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				// Check screen rotation.
				switch (mCurrentRotation) {
					case ROTATION_0:
						// Tablet in landscape mode.
					case ROTATION_90:
						// Phone in landscape mode.
						orientation = ORIENTATION_LANDSCAPE;
						break;
					case ROTATION_180:
						// Tablet in reverse landscape mode.
					case ROTATION_270:
						// Phone in reverse landscape mode.
						orientation = ORIENTATION_REVERSE_LANDSCAPE;
						break;
					default:
						// Unknown current landscape rotation.
						break;
				}
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				// Check screen rotation.
				switch (mCurrentRotation) {
					case ROTATION_0:
						// Phone in portrait mode.
					case ROTATION_270:
						// Tablet in portrait mode.
						orientation = ORIENTATION_PORTRAIT;
						break;
					case ROTATION_90:
						// Tablet in reverse portrait mode.
					case ROTATION_180:
						// Phone in reverse portrait mode.
						orientation = ORIENTATION_REVERSE_PORTRAIT;
						break;
					default:
						// Unknown current portrait rotation.
						break;
				}
				break;
			case Configuration.ORIENTATION_UNDEFINED:
			default:
				// Nothing to refresh.
				break;
		}
		this.mCurrentOrientation = orientation;
		// Get screen metrics to handle actual ones.
		this.mCurrentWidth = mMetrics.widthPixels;
		this.mCurrentHeight = mMetrics.heightPixels;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}

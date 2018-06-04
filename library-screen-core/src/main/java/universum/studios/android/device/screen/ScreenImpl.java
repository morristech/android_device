/*
 * *************************************************************************************************
 *                                 Copyright 2016 Universum Studios
 * *************************************************************************************************
 *                  Licensed under the Apache License, Version 2.0 (the "License")
 * -------------------------------------------------------------------------------------------------
 * You may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 * *************************************************************************************************
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
 * @since 1.0
 */
final class ScreenImpl implements Screen {

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ScreenImpl";

	/*
	 * Interface ===================================================================================
	 */

	/*
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
	private static ScreenImpl instance;

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Display metrics.
	 */
	private final DisplayMetrics metrics = new DisplayMetrics();

	/**
	 * Application context.
	 */
	private final Context context;

	/**
	 * "Default" width of the current Android device's screen.
	 */
	private final int width;

	/**
	 * "Default" height of the current Android device's screen.
	 */
	private final int height;

	/**
	 * Window manager service.
	 */
	private final WindowManager windowManager;

	/**
	 * Default orientation of the current Android device's screen.
	 */
	private final int defaultOrientation;

	/**
	 * Type of the current Android device's screen.
	 */
	private final ScreenType type;

	/**
	 * Density of the current Android device's screen.
	 */
	private final ScreenDensity density;

	/**
	 * Raw density of the current Android device's screen.
	 */
	private final int rawDensity;

	/**
	 * Actual orientation of the current Android device's screen.
	 */
	private int currentOrientation = ORIENTATION_UNSPECIFIED;

	/**
	 * Actual rotation of the current Android device's screen.
	 */
	private ScreenRotation currentRotation = ScreenRotation.UNKNOWN;

	/**
	 * Actual width of the current Android device's screen. Depends on the actual screen orientation.
	 */
	private int currentWidth;

	/**
	 * Actual height of the current Android device's screen. Depends on the actual screen orientation.
	 */
	private int currentHeight;

	/**
	 * Flag indicating whether the current Android device's screen orientation is currently locked or
	 * not.
	 */
	private boolean orientationLocked;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ScreenImpl.
	 *
	 * @param applicationContext Application context used to access system services.
	 */
	private ScreenImpl(final Context applicationContext) {
		this.context = applicationContext;
		this.windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
		this.refresh();
		// Resolve default orientation.
		switch (currentRotation) {
			case ROTATION_0:
			case ROTATION_180:
				switch (currentOrientation) {
					case ORIENTATION_LANDSCAPE:
					case ORIENTATION_REVERSE_LANDSCAPE:
						this.defaultOrientation = ORIENTATION_LANDSCAPE;
						break;
					case ORIENTATION_PORTRAIT:
					case ORIENTATION_REVERSE_PORTRAIT:
						this.defaultOrientation = ORIENTATION_PORTRAIT;
						break;
					default:
						this.defaultOrientation = ORIENTATION_UNSPECIFIED;
						break;
				}
				break;
			case ROTATION_90:
			case ROTATION_270:
				switch (currentOrientation) {
					case ORIENTATION_LANDSCAPE:
					case ORIENTATION_REVERSE_LANDSCAPE:
						this.defaultOrientation = ORIENTATION_PORTRAIT;
						break;
					case ORIENTATION_PORTRAIT:
					case ORIENTATION_REVERSE_PORTRAIT:
						this.defaultOrientation = ORIENTATION_LANDSCAPE;
						break;
					default:
						this.defaultOrientation = ORIENTATION_UNSPECIFIED;
						break;
				}
				break;
			default:
				this.defaultOrientation = ORIENTATION_UNSPECIFIED;
				break;
		}
		this.density = ScreenDensity.resolve(rawDensity = metrics.densityDpi);
		// Resolve screen type.
		float defaultWidthDP, defaultHeightDP;
		switch (currentOrientation) {
			case ORIENTATION_LANDSCAPE:
			case ORIENTATION_REVERSE_LANDSCAPE:
				defaultWidthDP = pixelToDP(metrics.heightPixels);
				defaultHeightDP = pixelToDP(metrics.widthPixels);
				break;
			case ORIENTATION_PORTRAIT:
			case ORIENTATION_REVERSE_PORTRAIT:
				defaultWidthDP = pixelToDP(metrics.widthPixels);
				defaultHeightDP = pixelToDP(metrics.heightPixels);
				break;
			case ORIENTATION_UNSPECIFIED:
			default:
				defaultWidthDP = pixelToDP(metrics.widthPixels);
				defaultHeightDP = pixelToDP(metrics.heightPixels);
				break;
		}
		this.type = ScreenType.resolve(defaultWidthDP, defaultHeightDP);
		// Resolve actual screen metrics.
		final boolean reverse;
		switch (defaultOrientation) {
			case ORIENTATION_PORTRAIT:
				reverse = currentOrientation != ORIENTATION_PORTRAIT && currentOrientation != ORIENTATION_REVERSE_PORTRAIT;
				break;
			case ORIENTATION_LANDSCAPE:
				reverse = currentOrientation != ORIENTATION_LANDSCAPE && currentOrientation != ORIENTATION_REVERSE_LANDSCAPE;
				break;
			default:
				reverse = false;
				break;
		}
		// Initialize display actual width and height.
		this.currentWidth = metrics.widthPixels;
		this.currentHeight = metrics.heightPixels;
		this.width = reverse ? currentHeight : currentWidth;
		this.height = reverse ? currentWidth : currentHeight;
	}

	/**
	 * Returns or creates a new singleton instance of ScreenImpl.
	 *
	 * @param context Context used by the screen implementation to access system services.
	 * @return Screen implementation with actual screen data available.
	 */
	@NonNull static ScreenImpl getsInstance(@NonNull final Context context) {
		synchronized (LOCK) {
			if (instance == null) instance = new ScreenImpl(context.getApplicationContext());
		}
		return instance;
	}

	/**
	 */
	@SuppressWarnings("deprecation")
	@Override public boolean isOn() {
		final PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return power != null && power.isScreenOn();
	}

	/**
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	@Override public boolean isInteractive() {
		final PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return power != null && power.isInteractive();
	}

	/**
	 */
	@Override public boolean lockOrientation(@NonNull final Activity activity) {
		return requestOrientation(activity, ORIENTATION_CURRENT);
	}

	/**
	 */
	@Override public void unlockOrientation(@NonNull final Activity activity) {
		requestOrientation(activity, ORIENTATION_USER);
	}

	/**
	 */
	@SuppressWarnings("WrongConstant")
	@Override public boolean requestOrientation(@NonNull final Activity activity, @Orientation final int orientation) {
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
		return orientationLocked = (orientation != ORIENTATION_USER);
	}

	/**
	 */
	@SuppressWarnings("ResourceType")
	@Override @Orientation public int getRequestedOrientation(@NonNull final Activity activity) {
		return activity.getRequestedOrientation();
	}

	/**
	 */
	@Override @NonNull public Display getDisplay() {
		return windowManager.getDefaultDisplay();
	}

	/**
	 */
	@Override @Px @IntRange(from = 0) public int getWidth() {
		return width;
	}

	/**
	 */
	@Override @Px @IntRange(from = 0) public int getHeight() {
		return height;
	}

	/**
	 */
	@Override @Px @IntRange(from = 0) public int getCurrentWidth() {
		refresh();
		return currentWidth;
	}

	/**
	 */
	@Override @Px @IntRange(from = 0) public int getCurrentHeight() {
		refresh();
		return currentHeight;
	}

	/**
	 */
	@Override @NonNull public DisplayMetrics getMetrics() {
		refresh();
		return metrics;
	}

	/**
	 */
	@Override @NonNull public ScreenDensity getDensity() {
		return density;
	}

	/**
	 */
	@Override public int getRawDensity() {
		return rawDensity;
	}

	/**
	 */
	@Override @NonNull public ScreenType getType() {
		return type;
	}

	/**
	 */
	@Override @Orientation public int getCurrentOrientation() {
		refresh();
		return currentOrientation;
	}

	/**
	 */
	@Override @Orientation public int getDefaultOrientation() {
		return defaultOrientation;
	}

	/**
	 */
	@Override @NonNull public ScreenRotation getCurrentRotation() {
		refresh();
		return currentRotation;
	}

	/**
	 */
	@Override @FloatRange(from = 0) public float getDiagonalDistanceInInches() {
		refresh();
		// Calculation in inches will depends on the exact pixel per inch in each axis (x and y) of the device display.
		return (float) Math.sqrt(Math.pow((width / metrics.xdpi), 2) + Math.pow((height / metrics.ydpi), 2));
	}

	/**
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	@Override @Px @IntRange(from = 0) public int getDiagonalDistanceInPixels() {
		refresh();
		return Math.round((float) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)));
	}

	/**
	 */
	@Override public float getRefreshRate() {
		return getDisplay().getRefreshRate();
	}

	/**
	 */
	@SuppressWarnings("ResourceType")
	@Override public int getBrightness(@NonNull final Activity activity) {
		final Window window = activity.getWindow();
		// Get the brightness from the current application window settings.
		return Math.round(window.getAttributes().screenBrightness * 100);
	}

	/**
	 */
	@Override public void setBrightness(@NonNull final Activity activity, @IntRange(from = 0, to = 100) final int brightness) {
		if (brightness < 0 || brightness > 100) {
			throw new IllegalArgumentException("Brightness value(" + brightness + ") is out of the range [0, 100].");
		}
		// Create new window parameters.
		final Window window = activity.getWindow();
		final WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.screenBrightness = Math.max(1, brightness) / 100f;
		// Set new brightness to the current application window.
		window.setAttributes(layoutParams);
	}

	/**
	 */
	@Override public int getSystemBrightness() {
		float brightness = 0;
		try {
			brightness = System.getInt(context.getContentResolver(), System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return Math.round(brightness / 255 * 100);
	}

	/**
	 */
	@Override public boolean isOrientationLocked() {
		return orientationLocked;
	}

	/**
	 */
	@Override public float pixelToDP(@Px final int pixel) {
		// From the Android developers documentation:
		// px = dp * (dpi / 160)
		// So modified equation is:
		// dp = px / (dpi / 160)
		return pixel / (density.value / 160);
	}

	/**
	 */
	@Override @Px public int dpToPixel(final float dp) {
		// From the Android developers documentation:
		// px = dp * (dpi / 160)
		return Math.round(dp * (density.value / 160));
	}

	/**
	 */
	@Override public float getScreenDP() {
		// From the Android developers documentation:
		// The density-independent pixel is equivalent to one physical pixel on
		// a 160 dpi screen, which is the baseline density assumed by the system
		// for a "medium" density screen.
		// px = dp * (dpi / 160)
		// So modified equation is:
		// dp = px / (dpi / 160)
		// and finally to determine for 1 px:
		return density.value / 160;
	}

	/**
	 * Called to refresh the current data of this screen wrapper instance. This should be called whenever
	 * the current data are requested.
	 */
	private void refresh() {
		getDisplay().getMetrics(metrics);
		// Refresh when:
		// 1) screen orientation change occurred
		if (currentWidth != metrics.widthPixels || currentHeight != metrics.heightPixels) {
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
		this.currentRotation = ScreenRotation.resolve(getDisplay().getRotation());
		switch (context.getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				// Check screen rotation.
				switch (currentRotation) {
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
				switch (currentRotation) {
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
		this.currentOrientation = orientation;
		// Get screen metrics to handle actual ones.
		this.currentWidth = metrics.widthPixels;
		this.currentHeight = metrics.heightPixels;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
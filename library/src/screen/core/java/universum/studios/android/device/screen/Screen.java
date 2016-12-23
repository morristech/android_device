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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Screen interface specifies API through which an actual information about the Android device's
 * screen may be accessed.
 * <p>
 * A screen implementation may be obtained via {@link Screen.Provider#getScreen(Context) Screen.PROVIDER.getScreen(Context)}.
 * <p>
 * Below are listed methods provided by the this interface:
 * <ul>
 * <li>{@link #getType()}</li>
 * <li>{@link #getDensity()}</li>
 * <li>{@link #getRawDensity()}</li>
 * <li>{@link #getCurrentRotation()}</li>
 * <li>{@link #getDefaultOrientation()}</li>
 * <li>{@link #getCurrentOrientation()}</li>
 * <li>{@link #requestOrientation(Activity, int)}</li>
 * <li>{@link #getRequestedOrientation(Activity)}</li>
 * <li>{@link #lockOrientation(Activity)}</li>
 * <li>{@link #isOrientationLocked()}</li>
 * <li>{@link #unlockOrientation(Activity)}</li>
 * <li>{@link #getWidth()}</li>
 * <li>{@link #getHeight()}</li>
 * <li>{@link #getCurrentWidth()}</li>
 * <li>{@link #getCurrentHeight()}</li>
 * <li>{@link #getMetrics()}</li>
 * <li>{@link #getScreenDP()}</li>
 * <li>{@link #getDiagonalDistanceInInches()}</li>
 * <li>{@link #getDiagonalDistanceInPixels()}</li>
 * <li>{@link #getBrightness(Activity)}</li>
 * <li>{@link #setBrightness(Activity, int)}</li>
 * <li>{@link #setBrightness(Activity, int)}</li>
 * <li>{@link #getSystemBrightness()}</li>
 * <li>{@link #getRefreshRate()}</li>
 * <li>{@link #dpToPixel(float)}</li>
 * <li>{@link #pixelToDP(int)}</li>
 * <li>{@link #isOn()}</li>
 * <li>{@link #isInteractive()}</li>
 * </ul>
 *
 * @author Martin Albedinsky
 */
public interface Screen {

	/**
	 * Provider ====================================================================================
	 */

	/**
	 * Interface for provider that may be used to access implementation of {@link Screen}.
	 *
	 * @author Martin Albedinsky
	 */
	interface Provider {

		/**
		 * Provides a singleton implementation of {@link Screen}.
		 *
		 * @param context Context used by the screen implementation to access actual screen data.
		 * @return Screen implementation with actual screen data already available.
		 */
		@NonNull
		Screen getScreen(@NonNull Context context);
	}

	/**
	 * A {@link Provider} implementation that may be used to access implementation of {@link Screen}.
	 */
	Provider PROVIDER = new Provider() {

		/**
		 */
		@NonNull
		@Override
		public Screen getScreen(@NonNull Context context) {
			return ScreenImpl.getsInstance(context);
		}
	};

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Defines an annotation for determining set of available screen orientations.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({
			ORIENTATION_CURRENT, ORIENTATION_UNSPECIFIED,
			ORIENTATION_LANDSCAPE, ORIENTATION_PORTRAIT,
			ORIENTATION_USER, ORIENTATION_BEHIND,
			ORIENTATION_SENSOR, ORIENTATION_SENSOR_LANDSCAPE, ORIENTATION_SENSOR_PORTRAIT,
			ORIENTATION_REVERSE_LANDSCAPE, ORIENTATION_REVERSE_PORTRAIT,
			ORIENTATION_FULL_SENSOR
	})
	@interface Orientation {
	}

	/**
	 * Orientation flag that is used to lock screen on the current orientation.
	 */
	int ORIENTATION_CURRENT = -2;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_UNSPECIFIED} flag for better access.
	 */
	int ORIENTATION_UNSPECIFIED = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_LANDSCAPE} flag for better access.
	 */
	int ORIENTATION_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_PORTRAIT} flag for better access.
	 */
	int ORIENTATION_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_USER} flag for better access.
	 */
	int ORIENTATION_USER = ActivityInfo.SCREEN_ORIENTATION_USER;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_BEHIND} flag for better access.
	 */
	int ORIENTATION_BEHIND = ActivityInfo.SCREEN_ORIENTATION_BEHIND;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_SENSOR} flag for better access.
	 */
	int ORIENTATION_SENSOR = ActivityInfo.SCREEN_ORIENTATION_SENSOR;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_SENSOR_LANDSCAPE} flag for better access.
	 */
	int ORIENTATION_SENSOR_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_SENSOR_PORTRAIT} flag for better access.
	 */
	int ORIENTATION_SENSOR_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_REVERSE_LANDSCAPE} flag for better access.
	 */
	int ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_REVERSE_PORTRAIT} flag for better access.
	 */
	int ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

	/**
	 * Copy of {@link ActivityInfo#SCREEN_ORIENTATION_FULL_SENSOR} flag for better access.
	 */
	int ORIENTATION_FULL_SENSOR = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;

	/**
	 * Enums =======================================================================================
	 */

	/**
	 * Represents density of the Android device's screen.
	 *
	 * @author Martin Albedinsky
	 * @see #resolve(float)
	 */
	enum ScreenDensity {

		/**
		 * Indicates that the screen density is unknown due to some error or the current screen data
		 * are unavailable.
		 */
		UNKNOWN(0, 0),
		/**
		 * <b>Low</b> density per inch:
		 * <ul>
		 * <li>density: <b>120</b></li>
		 * <li>scale ratio: <b>.75</b></li>
		 * </ul>
		 */
		LDPI(120.0f, 0.75f),
		/**
		 * <b>Medium</b> density per inch.
		 * <ul>
		 * <li>density: <b>160</b></li>
		 * <li>scale ratio: <b>1</b></li>
		 * </ul>
		 */
		MDPI(160.0f, 1f),
		/**
		 * <b>Height</b> density per inch.
		 * <ul>
		 * <li>density: <b>240</b></li>
		 * <li>scale ratio: <b>1.5</b></li>
		 * </ul>
		 */
		HDPI(240f, 1.5f),
		/**
		 * <b>Extra-Height</b> density per inch.
		 * <ul>
		 * <li>density: <b>320</b></li>
		 * <li>scale ratio: <b>2</b></li>
		 * </ul>
		 */
		XHDPI(320f, 2.00f),
		/**
		 * <b>Extra-Extra-Eeight</b> density per inch.
		 * <ul>
		 * <li>density: <b>480</b></li>
		 * <li>scale ratio: <b>3</b></li>
		 * </ul>
		 */
		XXHDPI(480f, 3f),
		/**
		 * <b>Extra-Extra-Extra-Height</b> density per inch.
		 * <ul>
		 * <li>density: <b>640</b></li>
		 * <li>scale ratio: <b>4</b></li>
		 * </ul>
		 */
		XXXHDPI(640f, 4f);

		/**
		 * The value of this screen density in DPI units.
		 */
		public final float value;

		/**
		 * The value of scale ratio specific for this screen density.
		 */
		public final float scaleRatio;

		/**
		 */
		private static final float DENSITY_CHECK_OFFSET = 10f;

		/**
		 * Creates a new instance of ScreenDensity with the given value and scale ratio.
		 *
		 * @param value      Value of density.
		 * @param scaleRatio Scale ratio for density.
		 */
		ScreenDensity(float value, float scaleRatio) {
			this.value = value;
			this.scaleRatio = scaleRatio;
		}

		/**
		 * Resolves an instance of ScreenDensity according to the given <var>densityDpi</var>.
		 *
		 * @param densityDpi The value {@link #value} of the desired density to resolve.
		 * @return Resolved screen density instance or {@link ScreenDensity#UNKNOWN} if there is no
		 * screen density with the requested density value.
		 */
		@NonNull
		public static ScreenDensity resolve(float densityDpi) {
			for (ScreenDensity density : values()) {
				if ((densityDpi + DENSITY_CHECK_OFFSET) >= density.value && (densityDpi - DENSITY_CHECK_OFFSET) <= density.value) return density;
			}
			return UNKNOWN;
		}
	}

	/**
	 * Represents type of the Android device's screen.
	 *
	 * @author Martin Albedinsky
	 * @see #resolve(float, float)
	 */
	enum ScreenType {

		/**
		 * Indicates that the screen type is unknown due to some error or the current screen data are
		 * unavailable.
		 */
		UNKNOWN(-1, -1),
		/**
		 * Type indicating <b>Small</b> screen.
		 * <h3>Native dimensions (in DP):</h3>
		 * <ul>
		 * <li><b>Native width: </b> <i>320</i></li>
		 * <li><b>Native height: </b> <i>426</i></li>
		 * </ul>
		 */
		SMALL(320f, 426f),
		/**
		 * Type indicating <b>Normal</b> screen.
		 * <h3>Native dimensions (in DP):</h3>
		 * <ul>
		 * <li><b>Native width: </b> <i>320</i></li>
		 * <li><b>Native height: </b> <i>470</i></li>
		 * </ul>
		 */
		NORMAL(320f, 470f),
		/**
		 * Type indicating <b>Large</b> screen.
		 * <h3>Native dimensions (in DP):</h3>
		 * <ul>
		 * <li><b>Native width: </b> <i>480</i></li>
		 * <li><b>Native height: </b> <i>640</i></li>
		 * </ul>
		 */
		LARGE(480f, 640f),
		/**
		 * Type indicating <b>X-Large</b> screen.
		 * <h3>Native dimensions (in DP):</h3>
		 * <ul>
		 * <li><b>Native width: </b> <i>720</i></li>
		 * <li><b>Native height: </b> <i>960</i></li>
		 * </ul>
		 */
		XLARGE(720f, 960f);

		/**
		 * Reversed array with all ScreenType values.
		 */
		private static final ScreenType[] REVERSED_VALUES = {
				XLARGE, LARGE, NORMAL, SMALL, UNKNOWN
		};

		/**
		 * Native dimension in DP units.
		 */
		public final float nativeWidthDp, nativeHeightDp;

		/**
		 * Creates a new instance of ScreenType with the specified native dimensions.
		 *
		 * @param nativeWidthDp  Native width specific for this screen type.
		 * @param nativeHeightDp Native height specific for this screen type.
		 */
		ScreenType(float nativeWidthDp, float nativeHeightDp) {
			this.nativeWidthDp = nativeWidthDp;
			this.nativeHeightDp = nativeHeightDp;
		}

		/**
		 * Resolves an instance of ScreenType according to the given native screen dimensions from the
		 * current set of ScreenType values.
		 *
		 * @param widthDp  The value of native screen width, <b>no current width</b>.
		 * @param heightDp The value of native screen height, <b>no current height</b>.
		 * @return Resolved screen type instance or {@link ScreenType#UNKNOWN} if there is no screen
		 * type with the given dimensions specified.
		 */
		@NonNull
		public static ScreenType resolve(float widthDp, float heightDp) {
			for (ScreenType type : REVERSED_VALUES) {
				if (widthDp >= type.nativeWidthDp && heightDp >= type.nativeHeightDp) return type;
			}
			return UNKNOWN;
		}
	}

	/**
	 * Represents rotation of the Android device's screen.
	 * <p>
	 * <b>Note: </b>there is difference between tablet default rotation and phone default rotation.
	 *
	 * @author Martin Albedinsky
	 * @see #resolve(int)
	 */
	enum ScreenRotation {

		/**
		 * Indicates that the screen rotation is unknown due to some error or the current screen data
		 * are unavailable.
		 */
		UNKNOWN(-1, -1),
		/**
		 * Rotation indicating <b>default</b> screen rotation by <b>0 degrees</b>:
		 * <ul>
		 * <li><b>phone:</b> {@link #ORIENTATION_PORTRAIT PORTRAIT} orientation</li>
		 * <li><b>tablet:</b> {@link #ORIENTATION_LANDSCAPE LANDSCAPE} orientation</li>
		 * </ul>
		 * System constant: <b>{@link Surface#ROTATION_0}</b>
		 */
		ROTATION_0(0, Surface.ROTATION_0),
		/**
		 * Rotation indicating screen rotation by <b>90 degrees</b>:
		 * <ul>
		 * <li><b>phone:</b> {@link #ORIENTATION_LANDSCAPE LANDSCAPE} orientation</li>
		 * <li><b>tablet:</b> {@link #ORIENTATION_REVERSE_PORTRAIT REVERSE_PORTRAIT} orientation</li>
		 * </ul>
		 * System constant: <b>{@link Surface#ROTATION_90}</b>
		 */
		ROTATION_90(90, Surface.ROTATION_90),
		/**
		 * Rotation indicating screen rotation by <b>180 degrees</b>:
		 * <p>
		 * <ul>
		 * <li><b>phone:</b> {@link #ORIENTATION_REVERSE_PORTRAIT REVERSE_PORTRAIT} orientation</li>
		 * <li><b>tablet:</b> {@link #ORIENTATION_REVERSE_LANDSCAPE REVERSE_LANDSCAPE} orientation</li>
		 * </ul>
		 * System constant: <b>{@link Surface#ROTATION_180}</b>
		 */
		ROTATION_180(180, Surface.ROTATION_180),
		/**
		 * Rotation indicating screen rotation by <b>270 degrees</b>:
		 * <ul>
		 * <li><b>phone:</b> {@link #ORIENTATION_REVERSE_LANDSCAPE REVERSE_LANDSCAPE} orientation</li>
		 * <li><b>tablet:</b> {@link #ORIENTATION_PORTRAIT PORTRAIT} orientation</li>
		 * </ul>
		 * System constant: <b>{@link Surface#ROTATION_270}</b>
		 */
		ROTATION_270(270, Surface.ROTATION_270);

		/**
		 * The value of degrees specific for this screen rotation.
		 */
		public final int degrees;

		/**
		 * The flag provided by {@link Surface} for this screen rotation.
		 */
		public final int systemConstant;

		/**
		 * Creates a new instance of ScreenRotation with the given value of degrees.
		 *
		 * @param degreesValue The value of degrees specific for this screen rotation.
		 * @param flag         Id of rotation as flag provided by {@link Surface}.
		 */
		ScreenRotation(int degreesValue, int flag) {
			this.degrees = degreesValue;
			this.systemConstant = flag;
		}

		/**
		 * Resolves an instance of ScreenRotation according to the given <var>systemConstant</var> from
		 * the current set of ScreenRotation values.
		 *
		 * @param systemConstant The id ({@link #systemConstant}) of the desired screen rotation to
		 *                       resolve.
		 * @return Resolved screen rotation instance or {@link ScreenRotation#UNKNOWN} if there is
		 * no screen rotation with the requested constant.
		 */
		@NonNull
		public static ScreenRotation resolve(int systemConstant) {
			for (ScreenRotation rotation : values()) {
				if (rotation.systemConstant == systemConstant) return rotation;
			}
			return UNKNOWN;
		}
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Checks whether the Android device's screen is currently on or off.
	 *
	 * @return {@code True} if screen is on, {@code false} otherwise.
	 */
	boolean isOn();

	/**
	 * Checks whether the Android device's screen is interactive or not.
	 * <p>
	 * <b>Note, that this is supported only for {@link android.os.Build.VERSION_CODES#KITKAT_WATCH KITKAT_WATCH}
	 * and above</b>.
	 *
	 * @return {@code True} if screen is interactive, {@code false} otherwise.
	 */
	boolean isInteractive();

	/**
	 * Returns default display of the Android device.
	 *
	 * @return The default display obtained by {@link android.view.WindowManager#getDefaultDisplay()}.
	 */
	@NonNull
	Display getDisplay();

	/**
	 * Returns the "default" width of the Android device's screen.
	 *
	 * @return Screen width in pixels.
	 */
	@Px
	@IntRange(from = 0)
	int getWidth();

	/**
	 * Returns the "default" height of the Android device's screen.
	 *
	 * @return Screen height in pixels.
	 */
	@Px
	@IntRange(from = 0)
	int getHeight();

	/**
	 * Returns the current width of the Android device's screen, which depends on the current screen
	 * orientation.
	 *
	 * @return Screen current width in pixels.
	 */
	@Px
	@IntRange(from = 0)
	int getCurrentWidth();

	/**
	 * Returns the current height of the Android device's screen, which depends on the current screen
	 * orientation.
	 *
	 * @return Screen current height in pixels.
	 */
	@Px
	@IntRange(from = 0)
	int getCurrentHeight();

	/**
	 * Returns the current display metrics of the Android device's screen.
	 *
	 * @return Display metrics.
	 */
	@NonNull
	DisplayMetrics getMetrics();

	/**
	 * Returns the density of the Android device's screen.
	 *
	 * @return One of {@link Screen.ScreenDensity} values or {@link Screen.ScreenDensity#UNKNOWN UNKNOWN}
	 * if the current screen data are unavailable.
	 * @see #getRawDensity()
	 */
	@NonNull
	ScreenDensity getDensity();

	/**
	 * Returns the raw density of the Android device's screen obtained from the screen metrics.
	 *
	 * @return Value of raw density
	 * @see #getDensity()
	 */
	int getRawDensity();

	/**
	 * Returns the type of the Android device's screen.
	 *
	 * @return One of {@link Screen.ScreenType} values or {@link Screen.ScreenType#UNKNOWN UNKNOWN}
	 * if the current screen data are unavailable.
	 */
	@NonNull
	ScreenType getType();

	/**
	 * Returns the default orientation of the Android device's screen.
	 *
	 * @return {@link #ORIENTATION_PORTRAIT} for <b>phone</b>, {@link #ORIENTATION_LANDSCAPE}
	 * for <b>tablet</b> or {@link #ORIENTATION_UNSPECIFIED UNSPECIFIED} if the current screen data
	 * are unavailable or orientation is unknown.
	 */
	@Orientation
	int getDefaultOrientation();

	/**
	 * Returns the current orientation of the Android device's screen.
	 *
	 * @return One of {@link #ORIENTATION_PORTRAIT}, {@link #ORIENTATION_LANDSCAPE}, {@link #ORIENTATION_REVERSE_PORTRAIT}
	 * {@link #ORIENTATION_REVERSE_LANDSCAPE}, {@link #ORIENTATION_USER}, {@link #ORIENTATION_SENSOR},
	 * {@link #ORIENTATION_SENSOR_PORTRAIT}, {@link #ORIENTATION_SENSOR_LANDSCAPE}, {@link #ORIENTATION_FULL_SENSOR},
	 * {@link #ORIENTATION_BEHIND}, or {@link #ORIENTATION_UNSPECIFIED} if the current screen data
	 * are unavailable or orientation is unknown.
	 */
	@Orientation
	int getCurrentOrientation();

	/**
	 * Returns the current rotation of the Android device's screen.
	 *
	 * @return One of {@link Screen.ScreenRotation} values or {@link Screen.ScreenRotation#UNKNOWN UNKNOWN}
	 * if the current screen data are unavailable or orientation is unknown.
	 */
	@NonNull
	ScreenRotation getCurrentRotation();

	/**
	 * Returns the diagonal distance of the Android device's screen.
	 *
	 * @return Diagonal distance in the inches.
	 */
	@FloatRange(from = 0)
	float getDiagonalDistanceInInches();

	/**
	 * Returns the diagonal distance of the Android device's screen.
	 *
	 * @return Diagonal distance in the raw pixels.
	 */
	@Px
	@IntRange(from = 0)
	int getDiagonalDistanceInPixels();

	/**
	 * Returns the back-light brightness of the Android device's screen for the current application
	 * window from the range <b>[0, 100]</b>.
	 *
	 * @param activity Currently visible activity.
	 * @return The value of brightness from the specified range or negative number if the current
	 * application window doesn't have set custom brightness value or the given activity or its window
	 * is invalid.
	 */
	@IntRange(from = -1, to = 100)
	int getBrightness(@NonNull Activity activity);

	/**
	 * Sets the back-light brightness of the Android device's screen. Brightness will be applied only
	 * for the current application window, not for the whole device.
	 * <p>
	 * Note, that the platform will automatically take care of changing the brightness as the user moves
	 * in and out of the application with custom brightness.
	 *
	 * @param activity   Currently visible activity.
	 * @param brightness The value of brightness for the application window from the range <b>[0, 100]</b>.
	 * @throws IllegalArgumentException When the given brightness value is out of the specified range.
	 */
	void setBrightness(@NonNull Activity activity, @IntRange(from = 0, to = 100) int brightness);

	/**
	 * Returns the back-light brightness of the Android device's screen.
	 *
	 * @return The value of system brightness as an Android user set it in the settings.
	 */
	@IntRange(from = 0, to = 100)
	int getSystemBrightness();

	/**
	 * Requests the specified orientation as orientation of the Android device's screen for the given
	 * activity.
	 *
	 * @param activity    Currently visible activity.
	 * @param orientation Orientation that you would like to set for the activity. One of the orientation
	 *                    flags specified within this interface.
	 * @return {@code True} if orientation was successfully requested, {@code false} otherwise.
	 * @see #isOrientationLocked()
	 * @see #lockOrientation(Activity)
	 */
	boolean requestOrientation(@NonNull Activity activity, @Orientation int orientation);

	/**
	 * Returns the orientation that was requested for the specified <var>activity</var> via
	 * {@link #requestOrientation(Activity, int)} or directly {@link Activity#setRequestedOrientation(int)}.
	 *
	 * @param activity Currently visible activity of which requested orientation to obtain.
	 * @return One of the orientation flags specified within this interface.
	 */
	@Orientation
	int getRequestedOrientation(@NonNull Activity activity);

	/**
	 * Locks orientation of the Android device's screen at the current orientation for the given
	 * activity.
	 *
	 * @param activity Currently visible activity.
	 * @return {@code True} if orientation was locked, {@code false} otherwise.
	 * @see #isOrientationLocked()
	 * @see #unlockOrientation(Activity)
	 * @see #requestOrientation(Activity, int)
	 */
	boolean lockOrientation(@NonNull Activity activity);

	/**
	 * Unlocks orientation of the Android device's screen for the given activity. This will actually
	 * requests {@link ActivityInfo#SCREEN_ORIENTATION_USER SCREEN_ORIENTATION_USER} orientation.
	 *
	 * @param activity Currently visible activity.
	 * @see #isOrientationLocked()
	 * @see #lockOrientation(Activity)
	 */
	void unlockOrientation(@NonNull Activity activity);

	/**
	 * Returns flag indicating whether orientation of the Android device's screen is currently locked
	 * or not. <b>Note, that this flag is useful only while you are within activity which requests to
	 * lock/unlock screen orientation.</b>
	 *
	 * @return {@code True} if screen orientation is currently locked by one of {@link #lockOrientation(Activity)}
	 * or {@link #requestOrientation(Activity, int)}, {@code false} otherwise.
	 * @see #lockOrientation(Activity)
	 * @see #requestOrientation(Activity, int)
	 */
	boolean isOrientationLocked();

	/**
	 * Transforms raw pixel to density pixel. Transformation will be performed according to density
	 * of the Android device's screen.
	 *
	 * @param pixel The pixel value which you want to transform to density pixel value.
	 * @return Density pixel value transformed from the given pixel value.
	 * @see #dpToPixel(float)
	 */
	float pixelToDP(@Px int pixel);

	/**
	 * Transforms density pixel to raw pixel. Transformation will be performed according to density
	 * of the Android device's screen.
	 *
	 * @param dp The density pixel value which you want to transform to pixel raw value.
	 * @return Pixel value transformed from the given density pixel value.
	 * @see #pixelToDP(int)
	 */
	@Px
	int dpToPixel(float dp);

	/**
	 * Returns the value of density pixel for 1 pixel for the Android device's screen.
	 * <p>
	 * This value depends on the screen's density.
	 *
	 * @return Density pixel value of screen.
	 * @see #dpToPixel(float)
	 * @see #pixelToDP(int)
	 */
	float getScreenDP();

	/**
	 * Returns refresh rate of the Android device's screen.
	 *
	 * @return Refresh rate in frames per second.
	 * @see Display#getRefreshRate()
	 */
	float getRefreshRate();
}

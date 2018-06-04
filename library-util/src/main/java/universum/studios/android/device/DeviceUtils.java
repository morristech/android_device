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
package universum.studios.android.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import universum.studios.android.device.screen.Screen;

/**
 * Utility class that may be used to check whether the Android device is tablet or not via
 * {@link #isTablet(Context)}.
 *
 * @author Martin Albedinsky
 */
@SuppressWarnings("unused")
public final class DeviceUtils {

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "DeviceUtils";

	/**
	 * Identifier for <b>UNKNOWN</b> type of Android powered device.
	 * <p>
	 * Constant value: <b>0</b>
	 */
	public static final int UNKNOWN = 0x00;

	/**
	 * Identifier for <b>PHONE</b> type of Android powered device.
	 * <p>
	 * Constant value: <b>1</b>
	 */
	public static final int PHONE = 0x01;

	/**
	 * Identifier for <b>PHABLET</b> type of Android powered device.
	 * <p>
	 * Constant value: <b>2</b>
	 */
	public static final int PHABLET = 0x02;

	/**
	 * Identifier for <b>TABLET</b> type of Android powered device.
	 * <p>
	 * Constant value: <b>3</b>
	 */
	public static final int TABLET = 0x03;

	/**
	 * Identifier for <b>TELEVISION</b> type of Android powered device.
	 * <p>
	 * Constant value: <b>4</b>
	 */
	public static final int TELEVISION = 0x04;

	/**
	 * Identifier for <b>WEARABLE</b> type of Android powered device.
	 * <p>
	 * Constant value: <b>5</b>
	 */
	public static final int WEARABLE = 0x05;

	/**
	 * Amount of points for TABLET device when performing check {@link #isTablet(Context)} for default
	 * screen orientation.
	 */
	private static final int TABLET_MATCH_SCREEN_DEFAULT_ORIENTATION_POINTS = 60;

	/**
	 * Amount of points for TABLET device when performing check {@link #isTablet(Context)} for screen
	 * type.
	 */
	private static final int TABLET_MATCH_SCREEN_TYPE_POINTS = 10;

	/**
	 * Amount of points for TABLET device when performing check {@link #isTablet(Context)} for screen
	 * density.
	 */
	private static final int TABLET_MATCH_SCREEN_DENSITY_POINTS = 10;

	/**
	 * Amount of points for TABLET device when performing check {@link #isTablet(Context)} for screen
	 * diagonal distance.
	 */
	private static final int TABLET_MATCH_SCREEN_DIAGONAL_POINTS = 10;

	/**
	 * Minimum percentage value to match tablet device.
	 */
	private static final int MINIMUM_TABLET_PERCENTAGE_MATCH = 85;

	/**
	 * Minimum screen diagonal distance to match tablet device in inches.
	 */
	private static final float MINIMUM_TABLET_DIAGONAL_DISTANCE = 6.5f;

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/*
	 * Constructors ================================================================================
	 */

	/**
	 */
	private DeviceUtils() {
		// Not allowed to be instantiated publicly.
		throw new UnsupportedOperationException();
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Checks whether the current Android device matches tablet specifications or not.
	 * <p>
	 * <b>Note</b>, that this information is only approximate and shouldn't be used for core logic
	 * of any application.
	 * <h3>Checked specifications:</h3>
	 * <ul>
	 * <li>{@link Screen#getDefaultOrientation()}</li>
	 * <li>{@link Screen#getType()}</li>
	 * <li>{@link Screen#getDiagonalDistanceInInches()}</li>
	 * <li>{@link Screen#getDensity()}</li>
	 * </ul>
	 *
	 * @return {@code True} if the current Android device matches tablet specifications, {@code false}
	 * otherwise.
	 */
	@SuppressLint("SwitchIntDef")
	public static boolean isTablet(@NonNull final Context context) {
		final Screen screen = Screen.PROVIDER.getScreen(context);
		int percentageMatch = 0;
		// Check the default screen orientation.
		// LANDSCAPE => tablet, PORTRAIT => phone
		switch (screen.getDefaultOrientation()) {
			case Screen.ORIENTATION_LANDSCAPE:
				percentageMatch += TABLET_MATCH_SCREEN_DEFAULT_ORIENTATION_POINTS;
				break;
			default:
				// No percentage improvement.
				break;
		}
		// Check screen type. Most of the Android tablet devices have display type LARGE or X-LARGE.
		switch (screen.getType()) {
			case LARGE:
			case XLARGE:
				percentageMatch += TABLET_MATCH_SCREEN_TYPE_POINTS;
				break;
			default:
				// No percentage improvement.
				break;
		}
		// Check screen diagonal distance.
		if (screen.getDiagonalDistanceInInches() >= MINIMUM_TABLET_DIAGONAL_DISTANCE) {
			percentageMatch += TABLET_MATCH_SCREEN_DIAGONAL_POINTS;
		}
		// Check screen density.
		// Most of Android tablet devices have MDPI or HDPI screen density (in some cases XHDPI too).
		switch (screen.getDensity()) {
			case MDPI:
			case HDPI:
			case XHDPI:
				percentageMatch += TABLET_MATCH_SCREEN_DENSITY_POINTS;
				break;
			default:
				// No percentage improvement.
				break;
		}
		return percentageMatch >= MINIMUM_TABLET_PERCENTAGE_MATCH;
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
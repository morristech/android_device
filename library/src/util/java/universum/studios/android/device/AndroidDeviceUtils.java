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
package universum.studios.android.device;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * todo: description
 *
 * @author Martin Albedinsky
 */
public final class AndroidDeviceUtils {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "AndroidDeviceUtils";

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

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Checks whether this Android device match tablet specifications or not. <b>Note</b>, that this
	 * information is only accurate and shouldn't be used for core logic of your application.
	 * <h3>Specifications to check:</h3>
	 * <ul>
	 * <li>Screen orientation</li>
	 * <li>{@link universum.studios.android.device.screen.Screen.ScreenRotation}</li>
	 * <li>{@link universum.studios.android.device.screen.Screen.ScreenType}</li>
	 * <li>{@link universum.studios.android.device.screen.Screen.ScreenDensity}</li>
	 * <li><b>Screen diagonal distance</b></li>
	 * </ul>
	 *
	 * @return {@code True} if this Android device matches tablet specifications, {@code false}
	 * otherwise.
	 */
	public static boolean isTablet(@NonNull Context context) {
		// todo:
		/*final Screen screen = getScreen();
		int percentageMatch = 0;

		// Check the default screen orientation.
		// LANDSCAPE => tablet, PORTRAIT => phone
		switch (screen.getDefaultOrientation()) {
			case Screen.ORIENTATION_LANDSCAPE:
				percentageMatch += TABLET_MATCH_SCREEN_DEFAULT_ORIENTATION_POINTS;
		}

		// Check screen type. Most of the Android tablet devices have display type LARGE or X-LARGE.
		switch (screen.getType()) {
			case LARGE:
			case XLARGE:
				percentageMatch += TABLET_MATCH_SCREEN_TYPE_POINTS;
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
		}
		return percentageMatch >= MINIMUM_TABLET_PERCENTAGE_MATCH;*/
		return false;
	}

	/**
	 * Inner classes ===============================================================================
	 */
}

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

import android.os.Build;

/**
 * todo
 *
 * @author Martin Albedinsky
 */
public class AndroidDevice {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "AndroidDevice";

	/**
	 * Android version that powers this Android device.
	 * <p>
	 * See {@link Build.VERSION#RELEASE} for additional info.
	 */
	public static final String ANDROID_VERSION = Build.VERSION.RELEASE;

	/**
	 * Name of the Android version that powers this Android device.
	 * <p>
	 * See {@link Build.VERSION_CODES} for additional info.
	 */
	public static final String ANDROID_VERSION_NAME;

	// Set up version name depending on the current SDK version.
	static {
		switch (Build.VERSION.SDK_INT) {
			case Build.VERSION_CODES.BASE:
				ANDROID_VERSION_NAME = "Base";
				break;
			case Build.VERSION_CODES.BASE_1_1:
				ANDROID_VERSION_NAME = "Base 1.1";
				break;
			case Build.VERSION_CODES.CUPCAKE:
				ANDROID_VERSION_NAME = "Cupcake";
				break;
			case Build.VERSION_CODES.DONUT:
				ANDROID_VERSION_NAME = "Donut";
				break;
			case Build.VERSION_CODES.ECLAIR:
				ANDROID_VERSION_NAME = "Eclair";
				break;
			case Build.VERSION_CODES.ECLAIR_0_1:
				ANDROID_VERSION_NAME = "Eclair 2.0.1";
				break;
			case Build.VERSION_CODES.ECLAIR_MR1:
				ANDROID_VERSION_NAME = "Eclair MR1";
				break;
			case Build.VERSION_CODES.FROYO:
				ANDROID_VERSION_NAME = "Froyo";
				break;
			case Build.VERSION_CODES.GINGERBREAD:
				ANDROID_VERSION_NAME = "Gingerbread";
				break;
			case Build.VERSION_CODES.GINGERBREAD_MR1:
				ANDROID_VERSION_NAME = "Gingerbread MR1";
				break;
			case Build.VERSION_CODES.HONEYCOMB:
				ANDROID_VERSION_NAME = "Honeycomb";
				break;
			case Build.VERSION_CODES.HONEYCOMB_MR1:
				ANDROID_VERSION_NAME = "Honeycomb MR1";
				break;
			case Build.VERSION_CODES.HONEYCOMB_MR2:
				ANDROID_VERSION_NAME = "Honeycomb MR2";
				break;
			case Build.VERSION_CODES.ICE_CREAM_SANDWICH:
				ANDROID_VERSION_NAME = "Ice Cream Sandwich";
				break;
			case Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1:
				ANDROID_VERSION_NAME = "Ice Cream Sandwich MR1";
				break;
			case Build.VERSION_CODES.JELLY_BEAN:
				ANDROID_VERSION_NAME = "Jelly Bean";
				break;
			case Build.VERSION_CODES.JELLY_BEAN_MR1:
				ANDROID_VERSION_NAME = "Jelly Bean MR1";
				break;
			case Build.VERSION_CODES.JELLY_BEAN_MR2:
				ANDROID_VERSION_NAME = "Jelly Bean MR2";
				break;
			case Build.VERSION_CODES.KITKAT:
				ANDROID_VERSION_NAME = "KitKat";
				break;
			case Build.VERSION_CODES.KITKAT_WATCH:
				ANDROID_VERSION_NAME = "KitKat Watch";
				break;
			case Build.VERSION_CODES.LOLLIPOP:
				ANDROID_VERSION_NAME = "Lollipop";
				break;
			case Build.VERSION_CODES.LOLLIPOP_MR1:
				ANDROID_VERSION_NAME = "Lollipop MR1";
				break;
			case Build.VERSION_CODES.M:
				ANDROID_VERSION_NAME = "Marshmallow";
				break;
			case Build.VERSION_CODES.N:
				ANDROID_VERSION_NAME = "Nougat";
				break;
			case Build.VERSION_CODES.N_MR1:
				ANDROID_VERSION_NAME = "Nougat MR1";
				break;
			default:
				ANDROID_VERSION_NAME = "UNSPECIFIED";
		}
	}

	/**
	 * Android SDK version that is available within this Android device.
	 * <p>
	 * See {@link Build.VERSION#SDK_INT} for additional info.
	 */
	public static final int ANDROID_SDK_VERSION = Build.VERSION.SDK_INT;

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
	 * Inner classes ===============================================================================
	 */
}

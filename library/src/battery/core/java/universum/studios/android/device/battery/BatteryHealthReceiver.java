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
package universum.studios.android.device.battery;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import universum.studios.android.device.AndroidDevice;

/**
 * A {@link android.content.BroadcastReceiver} used by {@link Battery} implementation to receive
 * information about the current health state of the Android device's battery.
 *
 * @author Martin Albedinsky
 */
public class BatteryHealthReceiver extends Battery.BatteryBroadcastReceiver {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Id of this receiver.
	 */
	public static final int RECEIVER_ID = 0x10002;

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Returns the intent filter for {@link Intent#ACTION_BATTERY_OKAY} and {@link Intent#ACTION_BATTERY_LOW}
	 * actions which should be used when registering this instance of battery receiver.
	 */
	@NonNull
	@Override
	public final IntentFilter newIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_OKAY);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		return filter;
	}

	/**
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		final Battery battery = AndroidDevice.getInstance(context).getBattery();
		battery.processBroadcast(context, intent, RECEIVER_ID);
	}
}

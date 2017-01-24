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

/**
 * A {@link Battery.BatteryBroadcastReceiver} used by {@link Battery} implementation to receive actual
 * information about the current battery health state.
 *
 * @author Martin Albedinsky
 */
public class BatteryHealthReceiver extends Battery.BatteryBroadcastReceiver {

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
		intent.putExtra(EXTRA_RECEIVER_CLASS, getClass());
		BatteryImpl.getInstance(context).handleBroadcast(context, intent);
	}
}

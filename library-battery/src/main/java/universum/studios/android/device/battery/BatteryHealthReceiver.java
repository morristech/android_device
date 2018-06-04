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
 * @since 1.0
 */
public class BatteryHealthReceiver extends Battery.BatteryBroadcastReceiver {

	/**
	 * Returns the intent filter for {@link Intent#ACTION_BATTERY_OKAY} and {@link Intent#ACTION_BATTERY_LOW}
	 * actions which should be used when registering this instance of battery receiver.
	 */
	@Override @NonNull public final IntentFilter newIntentFilter() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_OKAY);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		return filter;
	}

	/**
	 */
	@Override public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
		intent.putExtra(EXTRA_RECEIVER_CLASS, getClass());
		BatteryImpl.getInstance(context).handleBroadcast(context, intent);
	}
}
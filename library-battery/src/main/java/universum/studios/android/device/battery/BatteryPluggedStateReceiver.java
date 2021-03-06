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
 * information about the current battery plugged state.
 * <p>
 * This broadcast receiver receives only data about:
 * <ul>
 * <li>plugged state</li>
 * <li>voltage</li>
 * <li>technology: {@link Battery.BatteryTechnology}</li>
 * </ul>
 * All other data about the battery will be unknown at the time when the intent for this receiver is
 * being processed by {@link Battery} implementation, if there is not {@link BatteryStatusReceiver}
 * registered.
 *
 * @author Martin Albedinsky
 * @since 1.0
 */
public class BatteryPluggedStateReceiver extends Battery.BatteryBroadcastReceiver {

	/**
	 * Returns the intent filter for {@link Intent#ACTION_POWER_CONNECTED} and {@link Intent#ACTION_POWER_DISCONNECTED}
	 * actions which should be used when registering this instance of battery receiver.
	 *
	 * @return New instance of {@link IntentFilter}.
	 */
	@Override @NonNull public final IntentFilter newIntentFilter() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		return filter;
	}

	/**
	 */
	@Override
	public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
		intent.putExtra(EXTRA_RECEIVER_CLASS, getClass());
		BatteryImpl.getInstance(context).handleBroadcast(context, intent);
	}
}
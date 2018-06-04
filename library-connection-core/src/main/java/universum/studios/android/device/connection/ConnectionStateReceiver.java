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
package universum.studios.android.device.connection;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

/**
 * A {@link BroadcastReceiver} used by {@link Connection} implementation to receive actual information
 * about network connection state.
 *
 * @author Martin Albedinsky
 * @since 1.0
 */
public class ConnectionStateReceiver extends BroadcastReceiver {

	/**
	 * Returns the intent filter for {@link ConnectivityManager#CONNECTIVITY_ACTION} action which
	 * should be used when registering this instance of connection receiver.
	 *
	 * @return New instance of {@link IntentFilter}.
	 */
	@NonNull public final IntentFilter newIntentFilter() {
		return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
	}

	/**
	 */
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
		ConnectionImpl.getsInstance(context).handleBroadcast(context, intent);
	}
}
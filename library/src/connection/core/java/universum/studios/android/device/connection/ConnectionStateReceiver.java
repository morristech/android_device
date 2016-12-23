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
package universum.studios.android.device.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import universum.studios.android.device.AndroidDevice;

/**
 * A {@link BroadcastReceiver} used by {@link Connection} implementation to receive
 * information about the current state of the Android device's network connection.
 *
 * @author Martin Albedinsky
 */
public class ConnectionStateReceiver extends BroadcastReceiver {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Id of this receiver.
	 */
	public static final int RECEIVER_ID = 0x00001;

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Returns the intent filter for {@link ConnectivityManager#CONNECTIVITY_ACTION}
	 * action which should be used when registering this instance of connection receiver.
	 *
	 * @return New instance of {@link IntentFilter}.
	 */
	@NonNull
	public final IntentFilter newIntentFilter() {
		return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
	}

	/**
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		final Connection connection = AndroidDevice.getInstance(context).getConnection();
		connection.processBroadcast(context, intent, RECEIVER_ID);
	}
}

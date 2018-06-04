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
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

/**
 * Connection utility class specifying API allowing to check whether the Android device has some network
 * connection established or to access info of the current established network connection.
 *
 * @author Martin Albedinsky
 */
public final class ConnectionUtils {

	/**
	 * Checks whether there is some connection currently established or not.
	 * <p>
	 * See {@link ConnectivityManager#getActiveNetworkInfo()} for additional info.
	 *
	 * @param context Context used to access {@link ConnectivityManager}.
	 * @return {@code True} if there is some connection available and it is currently established,
	 * {@code false} otherwise.
	 * @see #isConnectionEstablished(Context, int)
	 */
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectionEstablished(@NonNull final Context context) {
		final NetworkInfo info = obtainEstablishedConnectionInfo(context);
		return info != null && info.isConnected();
	}

	/**
	 * Checks whether there is connection of the requested <var>connectionType</var> currently established
	 * or not.
	 * <p>
	 * See {@link NetworkInfo#isConnected()} for additional info.
	 *
	 * @param context        Context used to access {@link ConnectivityManager}.
	 * @param connectionType Type of the desired connection of which current connectivity to check.
	 * @return {@code True} if there is connection of the requested type available and it is
	 * currently established, {@code false} otherwise.
	 * @see #isConnectionEstablished(Context)
	 * @see #isConnectionAvailable(Context, int)
	 */
	@SuppressWarnings("deprecation")
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectionEstablished(@NonNull final Context context, final int connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = accessManager(context).getNetworkInfo(connectionType);
		return info != null && info.isConnected();
	}

	/**
	 * Checks whether there is connection of the requested <var>connectionType</var> available or not.
	 * <p>
	 * See {@link NetworkInfo#isAvailable()} for additional info.
	 *
	 * @param context        Context used to access {@link ConnectivityManager}.
	 * @param connectionType Type of the desired connection of which availability to check.
	 * @return {@code True} if connection of the requested type available, {@code false}
	 * otherwise.
	 * @see #isConnectionEstablished(Context, int)
	 */
	@SuppressWarnings("deprecation")
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	public static boolean isConnectionAvailable(@NonNull final Context context, final int connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = accessManager(context).getNetworkInfo(connectionType);
		return info != null && info.isAvailable();
	}

	/**
	 * Obtains type of the currently established connection.
	 * <p>
	 * See {@link ConnectivityManager#getActiveNetworkInfo()} for additional info.
	 *
	 * @param context Context used to access {@link ConnectivityManager}.
	 * @return Type provided by {@link ConnectivityManager} or {@code -1} if
	 * there is no connection currently established.
	 * @see #obtainEstablishedConnectionInfo(Context)
	 * @see #isConnectionEstablished(Context)
	 */
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	public static int obtainEstablishedConnectionType(@NonNull final Context context) {
		final NetworkInfo info = obtainEstablishedConnectionInfo(context);
		return info == null ? -1 : info.getType();
	}

	/**
	 * Obtains info of the currently established connection.
	 *
	 * @param context Context used to access {@link ConnectivityManager}.
	 * @return Info provided by {@link ConnectivityManager} or {@code null} if there
	 * is on connection currently established.
	 * @see #obtainEstablishedConnectionType(Context)
	 * @see #isConnectionEstablished(Context)
	 */
	@Nullable
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	public static NetworkInfo obtainEstablishedConnectionInfo(@NonNull final Context context) {
		return accessManager(context).getActiveNetworkInfo();
	}

	/**
	 * Accesses the ConnectivityManager service using the given <var>context</var>.
	 *
	 * @param context Context from which ConnectivityManager should be accessed.
	 * @return An instance of ConnectivityManager.
	 */
	private static ConnectivityManager accessManager(final Context context) {
		return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 */
	private ConnectionUtils() {
		// Not allowed to be instantiated publicly.
		throw new UnsupportedOperationException();
	}
}
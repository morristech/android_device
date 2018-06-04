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
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link Connection} implementation.
 *
 * @author Martin Albedinsky
 * @since 1.0
 */
final class ConnectionImpl implements Connection {

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ConnectionImpl";

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Static members ==============================================================================
	 */

	/**
	 * Lock used for synchronized operations.
	 */
	private static final Object LOCK = new Object();

	/**
	 * ConnectionImpl singleton instance.
	 */
	private static ConnectionImpl instance;

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Connectivity manager which provides current connection info.
	 */
	private final ConnectivityManager connectivityManager;

	/**
	 * Default connection receiver (broadcast receiver) to receive connection changes.
	 */
	private ConnectionStateReceiver receiver;

	/**
	 * Flag indicating whether the {@link ConnectionStateReceiver} is already registered or not.
	 */
	private final AtomicBoolean receiverRegistered = new AtomicBoolean(false);

	/**
	 * Current connection status.
	 */
	private ActualConnection actualConnection = new ActualConnection(ConnectionType.UNAVAILABLE, false);

	/**
	 * List of connection listeners.
	 */
	private final List<OnConnectionListener> listeners = new ArrayList<>(2);

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ConnectionImpl.
	 *
	 * @param applicationContext Application context used to access system services.
	 */
	private ConnectionImpl(final Context applicationContext) {
		this.connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Returns or creates a new singleton instance of ConnectionImpl.
	 *
	 * @param context Context used by the connection implementation to access system services.
	 * @return Connection implementation with actual connection data available.
	 */
	@NonNull static ConnectionImpl getsInstance(@NonNull final Context context) {
		synchronized (LOCK) {
			if (instance == null) instance = new ConnectionImpl(context.getApplicationContext());
		}
		return instance;
	}

	/**
	 */
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override public boolean isConnectedOrConnecting() {
		final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}

	/**
	 */
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override public boolean isConnected() {
		final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}

	/**
	 */
	@SuppressWarnings("deprecation")
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override public boolean isConnectedOrConnecting(@NonNull final ConnectionType connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = connectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isConnectedOrConnecting();
	}

	/**
	 */
	@SuppressWarnings("deprecation")
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override public boolean isConnected(@NonNull final ConnectionType connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = connectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isConnected();
	}

	/**
	 */
	@SuppressWarnings("deprecation")
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override public boolean isAvailable(@NonNull final ConnectionType connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = connectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isAvailable();
	}

	/**
	 */
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override @NonNull public ConnectionType getConnectionType() {
		final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return info == null || !info.isConnected() ?
				ConnectionType.UNAVAILABLE :
				ConnectionType.resolve(info.getType());
	}

	/**
	 */
	@SuppressWarnings("deprecation")
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	@Override @Nullable public NetworkInfo getConnectionInfo(@NonNull final ConnectionType type) {
		// todo: Implement not deprecated approach ...
		return connectivityManager.getNetworkInfo(type.systemConstant);
	}

	/**
	 */
	@Override public void registerOnConnectionListener(@NonNull final OnConnectionListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) listeners.add(listener);
		}
	}

	/**
	 */
	@Override public void unregisterOnConnectionListener(@NonNull final OnConnectionListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 */
	@Override public void registerConnectionReceiver(@NonNull final Context context) {
		synchronized (LOCK) {
			if (!receiverRegistered.get()) {
				this.receiverRegistered.set(true);
				context.registerReceiver(
						receiver = new ConnectionStateReceiver(),
						receiver.newIntentFilter()
				);
			}
		}
	}

	/**
	 */
	@Override public boolean isConnectionReceiverRegistered() {
		return receiverRegistered.get();
	}

	/**
	 */
	@Override public void unregisterConnectionReceiver(@NonNull final Context context) {
		synchronized (LOCK) {
			if (receiverRegistered.get()) {
				context.unregisterReceiver(receiver);
				this.receiverRegistered.set(false);
				this.receiver = null;
			}
		}
	}

	/**
	 * Handles a broadcast received by {@link ConnectionStateReceiver}.
	 *
	 * @param context Application context.
	 * @param intent  The intent containing the broadcasted data.
	 */
	@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
	void handleBroadcast(@NonNull final Context context, @NonNull final Intent intent) {
		if (isConnected()) {
			final ConnectionType type = getConnectionType();
			final ActualConnection connection = new ActualConnection(type, true);
			if (!connection.equals(actualConnection)) {
				this.actualConnection = connection;
				final NetworkInfo info = getConnectionInfo(type);
				synchronized (listeners) {
					if (!listeners.isEmpty()) {
						for (final OnConnectionListener listener : listeners) {
							listener.onConnectionEstablished(context, type, info);
						}
					}
				}
			}
		} else if (actualConnection != null) {
			final ConnectionType lostType = ConnectionType.values()[actualConnection.type.ordinal()];
			this.actualConnection = new ActualConnection(ConnectionType.UNAVAILABLE, false);
			synchronized (listeners) {
				if (!listeners.isEmpty()) {
					for (final OnConnectionListener listener : listeners) {
						listener.onConnectionLost(context, lostType);
					}
				}
			}
		}
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * Simple holder for the actual connection data.
	 */
	private static final class ActualConnection {

		/**
		 * Connection type.
		 */
		ConnectionType type;

		/**
		 * Flag indicating whether the connection is established or not.
		 */
		boolean connected;

		/**
		 * Creates a new instance of CurrentConnection with the given parameters.
		 *
		 * @param type      Type  current connection type.
		 * @param connected {@code True} if connection is established, {@code false} otherwise.
		 */
		ActualConnection(final ConnectionType type, final boolean connected) {
			this.type = type;
			this.connected = connected;
		}

		/**
		 */
		@Override public int hashCode() {
			int hash = connected ? 1 : 0;
			hash = 31 * hash + type.hashCode();
			return hash;
		}

		/**
		 */
		@Override public boolean equals(@Nullable final Object other) {
			if (other == this) return true;
			if (!(other instanceof ActualConnection)) return false;
			final ActualConnection connection = (ActualConnection) other;
			if (!type.equals(connection.type)) {
				return false;
			}
			return connected == connection.connected;
		}
	}
}
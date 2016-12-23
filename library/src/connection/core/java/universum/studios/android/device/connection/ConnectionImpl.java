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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import universum.studios.android.device.DeviceConfig;

/**
 * A {@link Connection} implementation.
 *
 * @author Martin Albedinsky
 */
final class ConnectionImpl implements Connection {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "ConnectionImpl";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Lock used for synchronized operations.
	 */
	private static final Object LOCK = new Object();

	/**
	 * ConnectionImpl singleton instance.
	 */
	private static ConnectionImpl sInstance;

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Connectivity manager which provides current connection info.
	 */
	private ConnectivityManager mConnectivityManager;

	/**
	 * Default connection receiver (broadcast receiver) to receive connection changes.
	 */
	private ConnectionStateReceiver mReceiver;

	/**
	 * Flag indicating whether the {@link ConnectionStateReceiver} is already registered or not.
	 */
	private final AtomicBoolean mReceiverRegistered = new AtomicBoolean(false);

	/**
	 * Current connection status.
	 */
	private ActualConnection mActualConnection = new ActualConnection(ConnectionType.UNAVAILABLE, false);

	/**
	 * Set of connection listeners.
	 */
	private List<OnConnectionListener> mListeners;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ConnectionImpl.
	 *
	 * @param applicationContext Application context used to access system services.
	 */
	private ConnectionImpl(Context applicationContext) {
		this.mConnectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Returns or creates a new singleton instance of ConnectionImpl.
	 *
	 * @param context Context used by the connection implementation to access system services.
	 * @return Connection implementation with actual connection data available.
	 */
	@NonNull
	static ConnectionImpl getsInstance(@NonNull Context context) {
		synchronized (LOCK) {
			if (sInstance == null) sInstance = new ConnectionImpl(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	 */
	@Override
	public boolean isConnectedOrConnecting() {
		final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}

	/**
	 */
	@Override
	public boolean isConnected() {
		final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}

	/**
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean isConnectedOrConnecting(@NonNull ConnectionType connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = mConnectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isConnectedOrConnecting();
	}

	/**
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean isConnected(@NonNull ConnectionType connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = mConnectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isConnected();
	}

	/**
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean isAvailable(@NonNull ConnectionType connectionType) {
		// todo: Implement not deprecated approach ...
		final NetworkInfo info = mConnectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isAvailable();
	}

	/**
	 */
	@NonNull
	@Override
	public ConnectionType getConnectionType() {
		final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return ConnectionType.resolve(info.getType());
		}
		return ConnectionType.UNAVAILABLE;
	}

	/**
	 */
	@Nullable
	@Override
	@SuppressWarnings("deprecation")
	public NetworkInfo getConnectionInfo(@NonNull ConnectionType type) {
		// todo: Implement not deprecated approach ...
		return mConnectivityManager.getNetworkInfo(type.systemConstant);
	}

	/**
	 */
	@Override
	public void registerOnConnectionListener(@NonNull OnConnectionListener listener) {
		if (mListeners == null) this.mListeners = new ArrayList<>(1);
		if (!mListeners.contains(listener)) mListeners.add(listener);
	}

	/**
	 */
	@Override
	public void unregisterOnConnectionListener(@NonNull OnConnectionListener listener) {
		if (mListeners != null) mListeners.remove(listener);
	}

	/**
	 */
	@Override
	public void registerConnectionReceiver(@NonNull Context context) {
		synchronized (LOCK) {
			if (mReceiverRegistered.get()) {
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Connection receiver already registered.");
				}
			} else {
				this.mReceiverRegistered.set(true);
				context.registerReceiver(
						mReceiver = new ConnectionStateReceiver(),
						mReceiver.newIntentFilter()
				);
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Connection receiver successfully registered.");
				}
			}
		}
	}

	/**
	 */
	@Override
	public boolean isConnectionReceiverRegistered() {
		return mReceiverRegistered.get();
	}

	/**
	 */
	@Override
	public void unregisterConnectionReceiver(@NonNull Context context) {
		synchronized (LOCK) {
			if (!mReceiverRegistered.get()) {
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Cannot un-register not registered connection receiver.");
				}
			} else {
				context.unregisterReceiver(mReceiver);
				this.mReceiverRegistered.set(false);
				this.mReceiver = null;
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Connection receiver successfully unregistered.");
				}
			}
		}
	}

	/**
	 * Handles a broadcast received by {@link ConnectionStateReceiver}.
	 *
	 * @param context Application context.
	 * @param intent  The intent with the broadcasted data.
	 */
	void handleBroadcast(@NonNull Context context, @NonNull Intent intent) {
		if (isConnected()) {
			final ConnectionType type = getConnectionType();
			final ActualConnection connection = new ActualConnection(type, true);
			if (!connection.equals(mActualConnection)) {
				this.mActualConnection = connection;
				notifyConnectionEstablished(context, type, getConnectionInfo(type));
			}
		} else if (mActualConnection != null) {
			final ConnectionType lostType = ConnectionType.values()[mActualConnection.type.ordinal()];
			this.mActualConnection = new ActualConnection(ConnectionType.UNAVAILABLE, false);
			notifyConnectionLost(context, lostType);
		}
	}

	/**
	 * Notifies all connection listeners, that new connection is available.
	 *
	 * @param context Current context.
	 * @param type    Type of the connection.
	 * @param info    current info of the passed connection type.
	 */
	private void notifyConnectionEstablished(Context context, ConnectionType type, NetworkInfo info) {
		if (mListeners != null && mListeners.size() > 0) {
			for (OnConnectionListener listener : mListeners) {
				listener.onConnectionEstablished(context, type, info);
			}
		}
	}

	/**
	 * Notifies all connection listeners, that connection was lost.
	 *
	 * @param context Current context.
	 * @param type    Type of the connection.
	 */
	private void notifyConnectionLost(Context context, ConnectionType type) {
		if (mListeners != null && mListeners.size() > 0) {
			for (OnConnectionListener listener : mListeners) {
				listener.onConnectionLost(context, type);
			}
		}
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Simple holder for the actual connection data.
	 */
	private static final class ActualConnection {

		/**
		 * Connection type.
		 */
		ConnectionType type = ConnectionType.UNAVAILABLE;

		/**
		 * Flag indicating whether the connection is established or not.
		 */
		boolean connected = false;

		/**
		 * Creates a new instance of CurrentConnection with the given parameters.
		 *
		 * @param type      Type  current connection type.
		 * @param connected {@code True} if connection is established, {@code false} otherwise.
		 */
		ActualConnection(ConnectionType type, boolean connected) {
			this.type = type;
			this.connected = connected;
		}

		/**
		 */
		@Override
		public int hashCode() {
			int hash = connected ? 1 : 0;
			hash = 31 * hash + type.hashCode();
			return hash;
		}

		/**
		 */
		@Override
		public boolean equals(Object other) {
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

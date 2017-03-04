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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link Connection} implementation.
 *
 * @author Martin Albedinsky
 */
final class ConnectionImpl implements Connection {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "ConnectionImpl";

	/**
	 * Interface ===================================================================================
	 */

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
	 * List of connection listeners.
	 */
	private final List<OnConnectionListener> mListeners = new ArrayList<>(2);

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
		return info == null || !info.isConnected() ?
				ConnectionType.UNAVAILABLE :
				ConnectionType.resolve(info.getType());
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
		synchronized (mListeners) {
			if (!mListeners.contains(listener)) mListeners.add(listener);
		}
	}

	/**
	 */
	@Override
	public void unregisterOnConnectionListener(@NonNull OnConnectionListener listener) {
		synchronized (mListeners) {
			mListeners.remove(listener);
		}
	}

	/**
	 */
	@Override
	public void registerConnectionReceiver(@NonNull Context context) {
		synchronized (LOCK) {
			if (!mReceiverRegistered.get()) {
				this.mReceiverRegistered.set(true);
				context.registerReceiver(
						mReceiver = new ConnectionStateReceiver(),
						mReceiver.newIntentFilter()
				);
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
			if (mReceiverRegistered.get()) {
				context.unregisterReceiver(mReceiver);
				this.mReceiverRegistered.set(false);
				this.mReceiver = null;
			}
		}
	}

	/**
	 * Handles a broadcast received by {@link ConnectionStateReceiver}.
	 *
	 * @param context Application context.
	 * @param intent  The intent containing the broadcasted data.
	 */
	void handleBroadcast(@NonNull Context context, @NonNull Intent intent) {
		if (isConnected()) {
			final ConnectionType type = getConnectionType();
			final ActualConnection connection = new ActualConnection(type, true);
			if (!connection.equals(mActualConnection)) {
				this.mActualConnection = connection;
				final NetworkInfo info = getConnectionInfo(type);
				synchronized (mListeners) {
					if (!mListeners.isEmpty()) {
						for (final OnConnectionListener listener : mListeners) {
							listener.onConnectionEstablished(context, type, info);
						}
					}
				}
			}
		} else if (mActualConnection != null) {
			final ConnectionType lostType = ConnectionType.values()[mActualConnection.type.ordinal()];
			this.mActualConnection = new ActualConnection(ConnectionType.UNAVAILABLE, false);
			synchronized (mListeners) {
				if (!mListeners.isEmpty()) {
					for (final OnConnectionListener listener : mListeners) {
						listener.onConnectionLost(context, lostType);
					}
				}
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
		boolean connected;

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

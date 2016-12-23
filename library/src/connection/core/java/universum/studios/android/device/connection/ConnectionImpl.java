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
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import universum.studios.android.device.receiver.ConnectionStateReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link Connection} API for {@link AndroidDevice AndroidDevice}.
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
	 * The key value for the user preferred connections saved in the shared preferences.
	 */
	private static final String PREFS_USER_NETWORKS = "universum.studios.android.device.ConnectionImpl.PREFERENCE.UserNetworks";

	/**
	 * Separator used preferred connections saving.
	 */
	private static final String NET_SEPARATOR = ":";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Lock used for synchronized operations.
	 */
	private final Object LOCK = new Object();

	/**
	 * Connectivity manager which provides current connection info.
	 */
	private ConnectivityManager mConnectivityManager;

	/**
	 * Default connection receiver (broadcast receiver) to receive connection changes.
	 */
	private ConnectionStateReceiver mNetworkReceiver;

	/**
	 * Current connection status.
	 */
	private CurrentConnection mCurrentConnection = new CurrentConnection(ConnectionType.UNAVAILABLE, false);

	/**
	 * Set of connection listeners.
	 */
	private List<OnConnectionListener> mListeners;

	/**
	 * Flag indicating whether the {@link universum.studios.android.device.receiver.ConnectionStateReceiver} is already registered or not.
	 */
	private boolean mReceiverRegistered;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of ConnectionImpl wrapper with also initialized {@link ConnectivityManager}.
	 *
	 * @param context Application context or activity context.
	 */
	ConnectionImpl(Context context) {
		this.mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Methods =====================================================================================
	 */

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
	public boolean isConnectedOrConnecting(@NonNull ConnectionType connectionType) {
		final NetworkInfo info = mConnectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isConnectedOrConnecting();
	}

	/**
	 */
	@Override
	public boolean isConnected(@NonNull ConnectionType connectionType) {
		final NetworkInfo info = mConnectivityManager.getNetworkInfo(connectionType.systemConstant);
		return info != null && info.isConnected();
	}

	/**
	 */
	@Override
	public boolean isAvailable(@NonNull ConnectionType connectionType) {
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
	public NetworkInfo getConnectionInfo(@NonNull ConnectionType type) {
		return mConnectivityManager.getNetworkInfo(type.systemConstant);
	}

	/**
	 */
	@Override
	public void registerOnConnectionListener(@NonNull OnConnectionListener listener) {
		if (mListeners == null) {
			this.mListeners = new ArrayList<>(1);
			this.mListeners.add(listener);
		} else if (!mListeners.contains(listener)) {
			this.mListeners.add(listener);
		}
	}

	/**
	 */
	@Override
	public void unregisterOnConnectionListener(@NonNull OnConnectionListener listener) {
		if (hasListeners()) mListeners.remove(listener);
	}

	/**
	 */
	@Override
	public void registerConnectionReceiver(@NonNull Context context) {
		synchronized (LOCK) {
			if (mReceiverRegistered) {
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Connection receiver already registered.");
				}
			} else {
				this.mReceiverRegistered = true;
				context.registerReceiver(
						mNetworkReceiver = new ConnectionStateReceiver(),
						mNetworkReceiver.newIntentFilter()
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
	public void unregisterConnectionReceiver(@NonNull Context context) {
		synchronized (LOCK) {
			if (!mReceiverRegistered) {
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Cannot un-register not registered connection receiver.");
				}
			} else {
				context.unregisterReceiver(mNetworkReceiver);
				dispatchConnectionReceiverUnregistered();
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Connection receiver successfully unregistered.");
				}
			}
		}
	}

	/**
	 */
	@Override
	public void dispatchConnectionReceiverUnregistered() {
		this.mReceiverRegistered = false;
		this.mNetworkReceiver = null;
	}

	/**
	 */
	@Override
	public boolean isConnectionReceiverRegistered() {
		return mReceiverRegistered;
	}

	/**
	 */
	@Override
	public void saveUserPreferredConnections(@NonNull ConnectionType[] types, @NonNull SharedPreferences preferences) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < types.length; i++) {
			builder.append(types[i].systemConstant);
			if (i != (types.length - 1)) {
				builder.append(NET_SEPARATOR);
			}
		}
		preferences.edit().putString(PREFS_USER_NETWORKS, builder.toString()).apply();
	}

	/**
	 */
	@NonNull
	@Override
	public ConnectionType[] getUserPreferredConnections(@NonNull SharedPreferences preferences) {
		ConnectionType[] types = new ConnectionType[0];
		final String connections = preferences.getString(PREFS_USER_NETWORKS, null);
		if (connections != null) {
			final String[] data = connections.split(NET_SEPARATOR);
			types = new ConnectionType[data.length];
			for (int i = 0; i < data.length; i++) {
				types[i] = ConnectionType.resolve(Integer.parseInt(data[i]));
			}
		}
		return types;
	}

	/**
	 */
	@Override
	public void processBroadcast(@NonNull Context context, @NonNull Intent intent, int receiverId) {
		if (isConnected()) {
			final ConnectionType type = getConnectionType();
			final CurrentConnection connection = new CurrentConnection(type, true);
			if (!connection.equals(mCurrentConnection)) {
				// Save connection state.
				this.mCurrentConnection = connection;
				notifyConnectionEstablished(context, type, getConnectionInfo(type));
			}
		} else {
			final ConnectionType lostType = (mCurrentConnection == null) ? null : ConnectionType.values()[mCurrentConnection.type.ordinal()];
			// Save connection state.
			this.mCurrentConnection = new CurrentConnection(ConnectionType.UNAVAILABLE, false);
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
		if (hasListeners()) {
			for (OnConnectionListener listener : mListeners) {
				listener.onConnectionEstablished(type, info, context);
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
		if (hasListeners()) {
			for (OnConnectionListener listener : mListeners) {
				listener.onConnectionLost(type, context);
			}
		}
	}

	/**
	 * Checks whether there are some connection listener presented or not.
	 *
	 * @return {@code True} if this connection implementation has some listeners set, {@code false}
	 * otherwise.
	 */
	private boolean hasListeners() {
		return mListeners != null && mListeners.size() > 0;
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Simple wrapper for current connection data.
	 */
	private static final class CurrentConnection {

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
		CurrentConnection(ConnectionType type, boolean connected) {
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
			if (!(other instanceof CurrentConnection)) return false;
			final CurrentConnection connection = (CurrentConnection) other;
			if (!type.equals(connection.type)) {
				return false;
			}
			return connected == connection.connected;
		}
	}
}

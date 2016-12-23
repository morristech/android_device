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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Connection interface specifies API through which an actual information about the Android device's
 * screen may be accessed.
 * <p>
 * A connection implementation may be obtained via {@link Connection.Provider#getConnection(Context) Connection.PROVIDER.getConnection(Context)}.
 * <p>
 * Below are listed methods provided by the this interface:
 * <ul>
 * <li>{@link #getConnectionType()}</li>
 * <li>{@link #getConnectionInfo(Connection.ConnectionType)}</li>
 * <li>{@link #isConnected()}</li>
 * <li>{@link #isConnected(Connection.ConnectionType)}</li>
 * <li>{@link #isConnectedOrConnecting()}</li>
 * <li>{@link #isConnectedOrConnecting(Connection.ConnectionType)}</li>
 * <li>{@link #isAvailable(Connection.ConnectionType)}</li>
 * </ul>
 *
 * <h3>Listening for changes in the connection</h3>
 * The following code snippet shows, how to properly register connection receivers and where to handle
 * the desired callbacks.
 * <pre>
 * public class ConnectionActivity extends Activity {
 *
 *      // Connection listener.
 *      private final Connection.OnConnectionListener CONNECTION_LISTENER = new Connection.OnConnectionListener() {
 *
 *          &#064;Override
 *          public void onConnectionEstablished(&#064;NonNull ConnectionType type, &#064;Nullable NetworkInfo info, &#064;NonNull Context context) {
 *              // Handle here new established connection.
 *          }
 *
 *          &#064;Override
 *          public void onConnectionLost(&#064;NonNull ConnectionType type, &#064;NonNull Context context) {
 *              // Handle here lost connection.
 *          }
 *      }
 *
 *      // Connection implementation.
 *      private Connection mConnection;
 *
 *      &#064;Override
 *      protected void onCreate(Bundle savedInstanceState) {
 *          super.onCreate(savedInstanceState);
 *           // ...
 *           // Get the connection API implementation.
 *           this.mConnection = Connection.PROVIDER.getConnection(this);
 *           // Register connection listener to handle changes in network connection.
 *           mConnection.registerOnConnectionListener(CONNECTION_LISTENER);
 *           // ...
 *      }
 *
 *      &#064;Override
 *      protected void onResume() {
 *          super.onResume();
 *          // Register connection broadcast receiver so the Connection implementation will receive
 *          // info about the established/lost connection.
 *          mConnection.registerConnectionReceiver(this);
 *      }
 *
 *      &#064;Override
 *      protected void onPause() {
 *          super.onPause();
 *          // Un-register all registered receivers.
 *          mConnection.unregisterConnectionReceiver(this);
 *      }
 *
 *      &#064;Override
 *      protected void onDestroy() {
 *          super.onDestroy();
 *          // Un-register connection listener.
 *          mConnection.unregisterOnConnectionListener(CONNECTION_LISTENER);
 *      }
 * }
 * </pre>
 *
 * @author Martin Albedinsky
 */
public interface Connection {

	/**
	 * Provider ====================================================================================
	 */

	/**
	 * Interface for provider that may be used to access implementation of {@link Connection}.
	 *
	 * @author Martin Albedinsky
	 */
	interface Provider {

		/**
		 * Provides a singleton implementation of {@link Connection}.
		 *
		 * @param context Context used by the connection implementation to access actual connection
		 *                data.
		 * @return Connection implementation with actual connection data already available.
		 */
		@NonNull
		Connection getConnection(@NonNull Context context);
	}

	/**
	 * A {@link Provider} implementation that may be used to access implementation of {@link Connection}.
	 */
	Provider PROVIDER = new Provider() {

		/**
		 */
		@NonNull
		@Override
		public Connection getConnection(@NonNull Context context) {
			return ConnectionImpl.getsInstance(context);
		}
	};

	/**
	 * Listeners ===================================================================================
	 */

	/**
	 * Listener that can receive callback with info about changed network connection.
	 *
	 * @author Martin Albedinsky
	 */
	interface OnConnectionListener {

		/**
		 * Invoked whenever registered {@link ConnectionStateReceiver} revive an Intent for the
		 * {@link ConnectivityManager#CONNECTIVITY_ACTION} and there is some connection established.
		 * <p>
		 * <b>Note:</b> this is also fired while connection receiver is being registered via
		 * {@link #registerConnectionReceiver(Context)} and there is currently established some connection.
		 *
		 * @param context Current application context.
		 * @param type    Type of the connection which was just now established or was already established
		 *                while this callback was being registered.
		 * @param info    Info about the current established connection.
		 */
		void onConnectionEstablished(@NonNull Context context, @NonNull ConnectionType type, @Nullable NetworkInfo info);

		/**
		 * Invoked whenever registered {@link ConnectionStateReceiver} receive an Intent for the
		 * {@link ConnectivityManager#CONNECTIVITY_ACTION} and there is no connection available.
		 * <p>
		 * <b>Note:</b> this is also fired while connection receiver is being registered via
		 * {@link #registerConnectionReceiver(Context)} and there isn't currently any connection available.
		 *
		 * @param context Current application context.
		 * @param type    Type of the connection which the Android device just lost or {@link ConnectionType#UNAVAILABLE UNAVAILABLE}
		 *                if there was no connection available while this callback was being registered.
		 */
		void onConnectionLost(@NonNull Context context, @NonNull ConnectionType type);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Enums =======================================================================================
	 */

	/**
	 * Represents type of the Android device's connection.
	 * <p>
	 * See {@link ConnectivityManager} for types supported from the
	 * {@link android.os.Build.VERSION_CODES#HONEYCOMB_MR2 API level 13} and higher.
	 *
	 * @author Martin Albedinsky
	 */
	@SuppressWarnings("deprecation")
	enum ConnectionType {

		/**
		 * Connection type indicating that there is no connection currently available.
		 * <p>
		 * <i>Constant value:</i> <b>-1</b>
		 * <ul>
		 * <li><i>Original name:</i> <b>Unavailable</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.UNAVAILABLE</b></li>
		 * </ul>
		 */
		UNAVAILABLE(-1, "Unavailable", "NETWORK_TYPE.UNAVAILABLE"),
		/**
		 * Connection type indicating that the current connection is established trough <b>bluetooth</b>.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_BLUETOOTH}
		 * <ul>
		 * <li><i>Original name:</i> <b>Bluetooth</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.BLUETOOTH</b></li>
		 * </ul>
		 *
		 * @since {@link android.os.Build.VERSION_CODES#HONEYCOMB_MR2 API level 13}
		 */
		BLUETOOTH(ConnectivityManager.TYPE_BLUETOOTH, "Bluetooth", "NETWORK_TYPE.BLUETOOTH"),
		/**
		 * Connection type indicating that the current connection is <b>dummy</b>.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_DUMMY}
		 * <ul>
		 * <li><i>Original name:</i> <b>Dummy</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.DUMMY</b></li>
		 * </ul>
		 *
		 * @since {@link android.os.Build.VERSION_CODES#HONEYCOMB_MR2 API level 13}
		 */
		DUMMY(ConnectivityManager.TYPE_DUMMY, "Dummy", "NETWORK_TYPE.DUMMY"),
		/**
		 * Connection type indicating that the current connection is established trough <b>ethernet</b>.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_ETHERNET}
		 * <ul>
		 * <li><i>Original name:</i> <b>Ethernet</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.ETHERNET</b></li>
		 * </ul>
		 *
		 * @since {@link android.os.Build.VERSION_CODES#HONEYCOMB_MR2 API level 13}
		 */
		ETHERNET(ConnectivityManager.TYPE_ETHERNET, "Ethernet", "NETWORK_TYPE.ETHERNET"),
		/**
		 * Connection type indicating that the current connection is established trough common
		 * <b>mobile</b> network (<i>3G, ...</i>).
		 * <p>
		 * From {@link ConnectivityManager#TYPE_MOBILE}
		 * <ul>
		 * <li><i>Original name:</i> <b>Mobile</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.MOBILE</b></li>
		 * </ul>
		 */
		MOBILE(ConnectivityManager.TYPE_MOBILE, "Mobile", "NETWORK_TYPE.MOBILE"),
		/**
		 * Connection type indicating that the current connection is established trough <b>mobile</b>
		 * network (<i>DUN traffic</i>).
		 * <p>
		 * From {@link ConnectivityManager#TYPE_MOBILE_DUN}
		 * <ul>
		 * <li><i>Original name:</i> <b>Mobile (DUN)</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.MOBILE_DUN</b></li>
		 * </ul>
		 */
		MOBILE_DUN(ConnectivityManager.TYPE_MOBILE_DUN, "Mobile (DUN)", "NETWORK_TYPE.MOBILE_DUN"),
		/**
		 * Connection type indicating that the current connection is established trough <b>mobile</b>
		 * network.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_MOBILE_HIPRI}
		 * <ul>
		 * <li><i>Original name:</i> <b>Mobile (HIPRI)</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.MOBILE_HIPRI</b></li>
		 * </ul>
		 */
		@Deprecated
		MOBILE_HIPRI(ConnectivityManager.TYPE_MOBILE_HIPRI, "Mobile (HIPRI)", "NETWORK_TYPE.MOBILE_HIPRI"),
		/**
		 * Connection type indicating that the current connection is established trough <b>mobile</b>
		 * network specific for Multimedia Messaging Services.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_MOBILE_MMS}
		 * <ul>
		 * <li><i>Original name:</i> <b>Mobile (MMS)</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.MOBILE_MMS</b></li>
		 * </ul>
		 */
		@Deprecated
		MOBILE_MMS(ConnectivityManager.TYPE_MOBILE_MMS, "Mobile (MMS)", "NETWORK_TYPE.MOBILE_MMS"),
		/**
		 * Connection type indicating that the current connection is established trough <b>mobile</b>
		 * network which can be used to use Secure User Plane Location servers.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_MOBILE_SUPL}
		 * <ul>
		 * <li><i>Original name:</i> <b>Mobile (SUPL)</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.MOBILE_SUPL</b></li>
		 * </ul>
		 */
		@Deprecated
		MOBILE_SUPL(ConnectivityManager.TYPE_MOBILE_SUPL, "Mobile (SUPL)", "NETWORK_TYPE.MOBILE_SUPL"),
		/**
		 * Connection type indicating that the current connection is established trough <b>Wi-Fi</b>
		 * network access point.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_WIFI}
		 * <ul>
		 * <li><i>Original name:</i> <b>Wi-Fi</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.WIFI</b></li>
		 * </ul>
		 */
		WIFI(ConnectivityManager.TYPE_WIFI, "Wi-Fi", "NETWORK_TYPE.WIFI"),
		/**
		 * Connection type indicating that the current connection is established trough <b>WiMAX</b>
		 * specific for data connection.
		 * <p>
		 * From {@link ConnectivityManager#TYPE_WIMAX}
		 * <ul>
		 * <li><i>Original name:</i> <b>WiMAX</b></li>
		 * <li><i>Preferences key:</i> <b>NETWORK_TYPE.WIMAX</b></li>
		 * </ul>
		 */
		WIMAX(ConnectivityManager.TYPE_WIMAX, "WiMAX", "NETWORK_TYPE.WIMAX");

		/**
		 * The flag provided by {@link ConnectivityManager} for this connectivity type.
		 */
		public final int systemConstant;

		/**
		 * Original name of this connection type. Can be used to present this connection type by
		 * its name in an Android application's UI.
		 */
		public final String originalName;

		/**
		 * The key under which may be this connection type saved within shared preferences.
		 */
		public final String preferencesKey;

		/**
		 * Creates a new instance of ConnectionType with the given ConnectivityManager flag, original
		 * name and preferences key.
		 * name.
		 *
		 * @param id       Id of connectivity type as flag provided by {@link ConnectivityManager}.
		 * @param origName Original name of this connectivity type.
		 * @param prefsKey The key under which may be this connectivity type saved into shared preferences.
		 */
		ConnectionType(int id, String origName, String prefsKey) {
			this.systemConstant = id;
			this.preferencesKey = prefsKey;
			this.originalName = origName;
		}

		/**
		 * Resolves an instance of ConnectionType according to the given <var>typeId</var> from the
		 * current set of ConnectionType values.
		 *
		 * @param systemConstant An id ({@link #systemConstant}) of the the desired connection type
		 *                       to resolve.
		 * @return Resolved connection type instance or {@link ConnectionType#UNAVAILABLE} if there
		 * is no connection type with the requested constant.
		 */
		@NonNull
		public static ConnectionType resolve(int systemConstant) {
			for (ConnectionType type : ConnectionType.values()) {
				if (type.systemConstant == systemConstant) return type;
			}
			return UNAVAILABLE;
		}
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Checks whether the Android device has some connection or is currently in the process to establish
	 * some connection.
	 * <p>
	 * See {@link NetworkInfo#isConnectedOrConnecting()} for additional info.
	 *
	 * @return {@code True} if some connection is established or will be established in a while,
	 * {@code false} otherwise.
	 * @see #isConnected()
	 * @see #getConnectionType()
	 */
	boolean isConnectedOrConnecting();

	/**
	 * Checks whether the Android device has some connection currently established.
	 * <p>
	 * See {@link NetworkInfo#isConnected()} for additional info.
	 *
	 * @return {@code True} if some connection is established, {@code false} otherwise.
	 * @see #isConnectedOrConnecting()
	 * @see #getConnectionType()
	 */
	boolean isConnected();

	/**
	 * Checks whether the Android device has established the connection or is currently in the process
	 * to establish the connection for the requested <var>connectionType</var>.
	 * <p>
	 * See {@link NetworkInfo#isConnectedOrConnecting()} for additional info.
	 *
	 * @param connectionType Type of the connection of which the current state should be checked.
	 * @return {@code True} if connection is established or is in the process of being established,
	 * {@code false} otherwise.
	 */
	boolean isConnectedOrConnecting(@NonNull ConnectionType connectionType);

	/**
	 * Checks whether the Android device has established the connection for the requested <var>connectionType</var>.
	 * <p>
	 * See {@link NetworkInfo#isConnected()} for additional info.
	 *
	 * @param connectionType Type of the connection of which the current state should be checked.
	 * @return {@code True} if connection is established, {@code false} otherwise.
	 * @see #isConnectedOrConnecting(Connection.ConnectionType)
	 */
	boolean isConnected(@NonNull ConnectionType connectionType);

	/**
	 * Checks whether the Android device can establish connection for the requested <var>connectionType</var>
	 * <p>
	 * See {@link NetworkInfo#isAvailable()} for additional info.
	 *
	 * @param connectionType Type of the connection of which availability should be checked.
	 * @return {@code True} if the requested connection can be established, {@code false}
	 * if establishing of the requested connection is not possible due to current network conditions.
	 */
	boolean isAvailable(@NonNull ConnectionType connectionType);

	/**
	 * Returns type of the current established connection.
	 *
	 * @return One of {@link Connection.ConnectionType} values or  {@link Connection.ConnectionType#UNAVAILABLE}
	 * if there is no connection currently available.
	 */
	@NonNull
	ConnectionType getConnectionType();

	/**
	 * Returns the current info about the requested <var>connectionType</var>.
	 *
	 * @param connectionType Type of the connection of which current info should be obtained.
	 * @return Info of the requested connection type.
	 * @see #getConnectionType()
	 */
	@Nullable
	NetworkInfo getConnectionInfo(@NonNull ConnectionType connectionType);

	/**
	 * Registers a callback to be invoked when some connection change occur.
	 *
	 * @param listener Callback to register.
	 * @see #registerConnectionReceiver(Context)
	 */
	void registerOnConnectionListener(@NonNull OnConnectionListener listener);

	/**
	 * Un-registers the given connection callback.
	 *
	 * @param listener Callback to un-register.
	 */
	void unregisterOnConnectionListener(@NonNull OnConnectionListener listener);

	/**
	 * Registers {@link ConnectionStateReceiver} to receive broadcasts about the current connection
	 * state.
	 *
	 * @param context The main activity of application.
	 * @see #registerOnConnectionListener(Connection.OnConnectionListener)
	 * @see #unregisterConnectionReceiver(Context)
	 */
	void registerConnectionReceiver(@NonNull Context context);

	/**
	 * Returns flag indicating whether {@link ConnectionStateReceiver} is currently registered or not.
	 *
	 * @return {@code True} if receiver is registered, {@code false} otherwise.
	 */
	boolean isConnectionReceiverRegistered();

	/**
	 * Un-registers registered {@link ConnectionStateReceiver}.
	 *
	 * @param context Context in which was connection receiver registered before.
	 * @see #registerConnectionReceiver(Context)
	 */
	void unregisterConnectionReceiver(@NonNull Context context);
}

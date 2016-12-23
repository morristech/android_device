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
package universum.studios.android.device.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import universum.studios.android.device.BroadcastProcessor;

/**
 * The Battery interface specifies API allowing to access current information about status, plugged
 * state, health state of the current Android device's battery and more. An implementation of this
 * API can be obtained from the {@link AndroidDevice} via {@link AndroidDevice#getBattery()}.
 * <p>
 * Below are specified methods provided by this API to access the current battery's data:
 * <ul>
 * <li>{@link #getStatus()}</li>
 * <li>{@link #getStrength()}</li>
 * <li>{@link #getPluggedState()}</li>
 * <li>{@link #getHealth()}</li>
 * <li>{@link #getVoltage()}</li>
 * <li>{@link #getTechnology()}</li>
 * </ul>
 *
 * <h3>Listening for changes in the battery's data</h3>
 * The following code snipped shows, how to properly register battery receivers and where to handle
 * the desired callbacks.
 * <pre>
 * public class BatteryActivity extends Activity {
 *
 *      // Battery status listener.
 *      private final Battery.OnStatusListener STATUS_LISTENER = new Battery.OnStatusListener() {
 *
 *          &#064;Override
 *          public void onStatusChange(&#064;NonNull Battery battery, &#064;NonNull Context context) {
 *              // Handle here battery status changes.
 *          }
 *      }
 *
 *      // Battery health status listener.
 *      private final Battery.OnHealthListener HEALTH_LISTENER = new Battery.OnHealthListener() {
 *
 *          &#064;Override
 *          public void onHealthOk(&#064;NonNull Battery battery, &#064;NonNull Context context) {
 *              // Handle here battery health OK status.
 *          }
 *
 *          &#064;Override
 *          public void onHealthLow(&#064;NonNull Battery battery, &#064;NonNull Context context) {
 *              // Handle here battery health LOW status.
 *          }
 *      }
 *
 *      // Battery plugged state listener.
 *      private final Battery.OnPluggedStateListener PLUGGED_STATE_LISTENER = new Battery.OnPluggedStateListener() {
 *
 *          &#064;Override
 *          public void onPluggedToPowerSource(&#064;NonNull Battery battery, &#064;NonNull Context context) {
 *              // Handle here battery charging status.
 *          }
 *
 *          &#064;Override
 *          public void onUnpluggedFromPowerSource(&#064;NonNull Battery battery, &#064;NonNull Context context) {
 *              // Handle here battery discharging status.
 *          }
 *      }
 *
 *      // Device battery wrapper.
 *      private Battery mBattery;
 *
 *      &#064;Override
 *      protected void onCreate(Bundle savedInstanceState) {
 *          super.onCreate(savedInstanceState);
 *
 *          // ...
 *
 *          // Get the battery API implementation.
 *          this.mBattery = AndroidDevice.getInstance(this).getBattery();
 *          // Register battery listeners to handle battery status changes.
 *          mBattery.registerOnBatteryListener(STATUS_LISTENER);
 *          mBattery.registerOnBatteryListener(HEALTH_LISTENER);
 *          mBattery.registerOnBatteryListener(PLUGGED_STATE_LISTENER);
 *
 *          // ...
 *      }
 *
 *      &#064;Override
 *      protected void onResume() {
 *          super.onResume();
 *
 *          // Register only needed battery broadcast receivers so the Battery wrapper will receive
 *          // info about the current battery status, plugged state or health.
 *          mBattery.registerBatteryReceiver(this, Battery.RECEIVER_BATTERY_STATUS);
 *          mBattery.registerBatteryReceiver(this, Battery.RECEIVER_BATTERY_HEALTH);
 *          mBattery.registerBatteryReceiver(this, Battery.RECEIVER_BATTERY_PLUGGED_STATE);
 *          // ... or
 *          mBattery.registerAllBatteryReceivers(this);
 *      }
 *
 *      &#064;Override
 *      protected void onPause() {
 *          super.onPause();
 *
 *          // Un-register all registered receivers.
 *          mBattery.unregisterBatteryReceiver(this, Battery.RECEIVER_BATTERY_STATUS);
 *          mBattery.unregisterBatteryReceiver(this, Battery.RECEIVER_BATTERY_HEALTH);
 *          mBattery.unregisterBatteryReceiver(this, Battery.RECEIVER_BATTERY_PLUGGED_STATE);
 *          // ... or
 *          mBattery.unregisterAllBatteryReceivers(this);
 *      }
 *
 *      &#064;Override
 *      protected void onDestroy() {
 *          super.onDestroy();
 *
 *          // Un-register battery listeners.
 *          mBattery.unregisterOnBatteryListener(STATUS_LISTENER);
 *          mBattery.unregisterOnBatteryListener(HEALTH_LISTENER);
 *          mBattery.unregisterOnBatteryListener(PLUGGED_STATE_LISTENER);
 *      }
 * }
 * </pre>
 *
 * @author Martin Albedinsky
 */
public interface Battery extends BroadcastProcessor {

	/**
	 * Provider ====================================================================================
	 */

	/**
	 * Interface for provider that may be used to access implementation of {@link Battery}.
	 *
	 * @author Martin Albedinsky
	 */
	interface Provider {

		/**
		 * Provides a singleton implementation of {@link Battery}.
		 *
		 * @param context Context used by the battery implementation to access actual battery data.
		 * @return Battery implementation with actual battery data already available.
		 */
		@NonNull
		Battery getBattery(@NonNull Context context);
	}

	/**
	 * A {@link Provider} implementation that may be used to access implementation of {@link Battery}.
	 */
	Provider PROVIDER = new Provider() {

		/**
		 */
		@NonNull
		@Override
		public Battery getBattery(@NonNull Context context) {
			return BatteryImpl.getInstance(context);
		}
	};

	/**
	 * Listeners ===================================================================================
	 */

	/**
	 * Parent battery listener. Only for internal purpose.
	 */
	interface BatteryListener {
	}

	/**
	 * Listener that can receive callback about changed battery's status.
	 *
	 * @author Martin Albedinsk
	 */
	interface OnStatusListener extends BatteryListener {

		/**
		 * Invoked whenever the status change of this Android device's battery occurs.
		 * <p>
		 * <b>Note</b>, that there need to be registered {@link BatteryStatusReceiver}, for example
		 * by {@link #registerBatteryReceiver(Context, int)} with {@link #RECEIVER_BATTERY_STATUS}
		 * to receive this callback.
		 *
		 * @param battery Battery with actual data.
		 * @param context Current application context.
		 */
		void onStatusChange(@NonNull Battery battery, @NonNull Context context);
	}

	/**
	 * Listener that can receive callback about changed battery's plugged state.
	 *
	 * @author Martin Albedinsky
	 */
	interface OnPluggedStateListener extends BatteryListener {

		/**
		 * Invoked whenever this Android device's battery has been plugged into some power source.
		 * The current plugged state can be obtained via {@link Battery#getPluggedState()}.
		 * <p>
		 * <b>Note</b>, that there need to be registered {@link BatteryPluggedStateReceiver}, for
		 * example by {@link #registerBatteryReceiver(Context, int)} with {@link #RECEIVER_BATTERY_PLUGGED_STATE}
		 * to receive this callback.
		 *
		 * @param battery Battery with actual data.
		 * @param context Current application context.
		 */
		void onPluggedToPowerSource(@NonNull Battery battery, @NonNull Context context);

		/**
		 * Invoked whenever this Android device's battery has been unplugged from its power source.
		 * <p>
		 * <b>Note</b>, that there need to be registered {@link BatteryPluggedStateReceiver}, for
		 * example by {@link #registerBatteryReceiver(Context, int)} with {@link #RECEIVER_BATTERY_PLUGGED_STATE}
		 * to receive this callback.
		 *
		 * @param battery Battery with actual data.
		 * @param context Current application context.
		 */
		void onUnpluggedFromPowerSource(@NonNull Battery battery, @NonNull Context context);
	}

	/**
	 * Listener that can receive callback about changed battery's health (LOW/OK).
	 *
	 * @author Martin Albedinsky
	 */
	interface OnHealthListener extends BatteryListener {

		/**
		 * Invoked whenever the current strength of this Android device's battery comes from the
		 * <b>LOW</b> level to the <b>OK</b> one. The current health can be obtained via {@link Battery#getHealth()}.
		 * <p>
		 * <b>Note</b>, that there need to be registered {@link BatteryHealthReceiver}, for example
		 * by {@link #registerBatteryReceiver(Context, int)} with {@link #RECEIVER_BATTERY_HEALTH}
		 * to receive this callback.
		 *
		 * @param battery Battery with actual data.
		 * @param context Current application context.
		 * @see #setHealthLowLevel(int)
		 * @see #setHealthOkLevel(int)
		 */
		void onHealthOk(@NonNull Battery battery, @NonNull Context context);

		/**
		 * Invoked whenever the current strength of this Android device's battery comes from the <b>OK</b>
		 * level to the <b>LOW</b>. The current health can be obtained via {@link Battery#getHealth()}.
		 * <p>
		 * <b>Note</b>, that there need to be registered {@link BatteryHealthReceiver}, for example
		 * by {@link #registerBatteryReceiver(Context, int)} with {@link #RECEIVER_BATTERY_HEALTH}
		 * to receive this callback.
		 *
		 * @param battery Battery with actual data.
		 * @param context Current application context.
		 * @see #setHealthLowLevel(int)
		 * @see #setHealthOkLevel(int)
		 */
		void onHealthLow(@NonNull Battery battery, @NonNull Context context);
	}

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Defines an annotation for determining set of allowed battery receiver ids.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({RECEIVER_BATTERY_STATUS, RECEIVER_BATTERY_HEALTH, RECEIVER_BATTERY_PLUGGED_STATE})
	@interface Receiver {
	}

	/**
	 * The id of {@link universum.studios.android.device.receiver.BatteryStatusReceiver} copied from
	 * {@link universum.studios.android.device.receiver.BatteryStatusReceiver#RECEIVER_ID}.
	 */
	int RECEIVER_BATTERY_STATUS = BatteryStatusReceiver.RECEIVER_ID;

	/**
	 * The id of {@link universum.studios.android.device.receiver.BatteryHealthReceiver} copied from
	 * {@link universum.studios.android.device.receiver.BatteryHealthReceiver#RECEIVER_ID}.
	 */
	int RECEIVER_BATTERY_HEALTH = BatteryHealthReceiver.RECEIVER_ID;

	/**
	 * The id of {@link universum.studios.android.device.receiver.BatteryPluggedStateReceiver} copied from
	 * {@link universum.studios.android.device.receiver.BatteryPluggedStateReceiver#RECEIVER_ID}.
	 */
	int RECEIVER_BATTERY_PLUGGED_STATE = BatteryPluggedStateReceiver.RECEIVER_ID;

	/**
	 * Defines an annotation for determining set of available battery statuses.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({STATUS_UNKNOWN, STATUS_CHARGING, STATUS_DISCHARGING, STATUS_NOT_CHARGING, STATUS_FULL})
	@interface Status {
	}

	/**
	 * Copy of {@link BatteryManager#BATTERY_STATUS_UNKNOWN} flag for better access.
	 */
	int STATUS_UNKNOWN = BatteryManager.BATTERY_STATUS_UNKNOWN;

	/**
	 * Copy of {@link BatteryManager#BATTERY_STATUS_CHARGING} flag for better access.
	 */
	int STATUS_CHARGING = BatteryManager.BATTERY_STATUS_CHARGING;

	/**
	 * Copy of {@link BatteryManager#BATTERY_STATUS_DISCHARGING} flag for better access.
	 */
	int STATUS_DISCHARGING = BatteryManager.BATTERY_STATUS_DISCHARGING;

	/**
	 * Copy of {@link BatteryManager#BATTERY_STATUS_NOT_CHARGING} flag for better access.
	 */
	int STATUS_NOT_CHARGING = BatteryManager.BATTERY_STATUS_NOT_CHARGING;

	/**
	 * Copy of {@link BatteryManager#BATTERY_STATUS_FULL} flag for better access.
	 */
	int STATUS_FULL = BatteryManager.BATTERY_STATUS_FULL;

	/**
	 * Defines an annotation for determining set of available battery plugged states.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({PLUGGED_UNKNOWN, PLUGGED_NONE, PLUGGED_AC, PLUGGED_USB, PLUGGED_WIRELESS})
	@interface PluggedState {
	}

	/**
	 * Plugged state flag indicating that the battery plugged state is unknown due to some error or
	 * the current battery data are unavailable.
	 */
	int PLUGGED_UNKNOWN = -1;

	/**
	 * Plugged state flag indicating that the battery isn't connected to any power source.
	 */
	int PLUGGED_NONE = 0;

	/**
	 * Copy of {@link BatteryManager#BATTERY_PLUGGED_AC} flag for better access.
	 */
	int PLUGGED_AC = BatteryManager.BATTERY_PLUGGED_AC;

	/**
	 * Copy of {@link BatteryManager#BATTERY_PLUGGED_USB} flag for better access.
	 */
	int PLUGGED_USB = BatteryManager.BATTERY_PLUGGED_USB;

	/**
	 * Copy of {@link BatteryManager#BATTERY_PLUGGED_WIRELESS} flag for better access.
	 */
	int PLUGGED_WIRELESS = /*BatteryManager.BATTERY_PLUGGED_WIRELESS*/4;

	/**
	 * Default value of the battery health indicating <b>LOW</b> level.
	 * <p>
	 * Constant value: <b>15</b>
	 */
	int HEALTH_LOW_LEVEL = 15;

	/**
	 * Default value of the battery health indicating <b>OK</b> level.
	 * <p>
	 * Constant value: <b>20</b>
	 */
	int HEALTH_OK_LEVEL = 20;

	/**
	 * Defines an annotation for determining set of available battery healths.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({HEALTH_UNKNOWN, HEALTH_GOOD, HEALTH_OVERHEAT, HEALTH_DEAD,
			HEALTH_OVER_VOLTAGE, HEALTH_UNSPECIFIED_FAILURE, HEALTH_COLD})
	@interface Health {
	}

	/**
	 * Copy of {@link BatteryManager#BATTERY_HEALTH_UNKNOWN} flag for better access.
	 */
	int HEALTH_UNKNOWN = BatteryManager.BATTERY_HEALTH_UNKNOWN;

	/**
	 * Copy of {@link BatteryManager#BATTERY_HEALTH_GOOD} flag for better access.
	 */
	int HEALTH_GOOD = BatteryManager.BATTERY_HEALTH_GOOD;

	/**
	 * Copy of {@link BatteryManager#BATTERY_HEALTH_OVERHEAT} flag for better access.
	 */
	int HEALTH_OVERHEAT = BatteryManager.BATTERY_HEALTH_OVERHEAT;

	/**
	 * Copy of {@link BatteryManager#BATTERY_HEALTH_DEAD} flag for better access.
	 */
	int HEALTH_DEAD = BatteryManager.BATTERY_HEALTH_DEAD;

	/**
	 * Copy of {@link BatteryManager#BATTERY_HEALTH_OVER_VOLTAGE} flag for better access.
	 */
	int HEALTH_OVER_VOLTAGE = BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE;

	/**
	 * Copy of {@link BatteryManager#BATTERY_HEALTH_UNSPECIFIED_FAILURE} flag for better access.
	 */
	int HEALTH_UNSPECIFIED_FAILURE = BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE;

	/**
	 * Copy of {@link BatteryManager#BATTERY_HEALTH_COLD} flag for better access.
	 */
	int HEALTH_COLD = BatteryManager.BATTERY_HEALTH_COLD;

	/**
	 * Enums =======================================================================================
	 */

	/**
	 * Represents technology of the Android device's battery.
	 *
	 * @author Martin Albedinsky
	 * @see #resolve(String)
	 */
	enum BatteryTechnology {

		/**
		 * Indicates that the current Android device's battery technology can't be parsed by the tagged
		 * name provided by {@link BatteryManager} or the current battery data are unavailable.
		 * <ul>
		 * <li><i>Tag:</i> <b>unknown</b></li>
		 * <li><i>Original name:</i> <b>unknown</b></li>
		 * </ul>
		 */
		UNKNOWN("unknown", "unknown"),
		/**
		 * <b>Lithium Ion</b> battery technology.
		 * <ul>
		 * <li><i>Tag:</i> <b>Li-ion</b></li>
		 * <li><i>Original name:</i> <b>Li-ion</b></li>
		 * </ul>
		 */
		Li_ion("Li-ion", "Li-ion"),
		/**
		 * <b>Lithium Polymer</b> battery technology.
		 * <ul>
		 * <li><i>Tag:</i> <b>Li-poly</b></li>
		 * <li><i>Original name:</i> <b>Li-poly</b></li>
		 * </ul>
		 */
		Li_poly("Li-poly", "Li-poly"),
		/**
		 * <b>Silver Zinc</b> battery technology.
		 * <ul>
		 * <li><i>Tag:</i> <b>AgZn</b></li>
		 * <li><i>Original name:</i> <b>Ag-Zn</b></li>
		 * </ul>
		 */
		Ag_Zn("AgZn", "Ag-Zn"),
		/**
		 * <b>Nickel Zinc</b> battery technology.
		 * <ul>
		 * <li><i>Tag:</i> <b>NiZn</b></li>
		 * <li><i>Original name:</i> <b>Ni-Zn</b></li>
		 * </ul>
		 */
		Ni_Zn("NiZn", "Ni-Zn"),
		/**
		 * <b>Nickel Metal Hydrid</b> battery technology.
		 * <ul>
		 * <li><i>Tag:</i> <b>NiMH</b></li>
		 * <li><i>Original name:</i> <b>Ni-MH</b></li>
		 * </ul>
		 */
		Ni_MH("NiMH", "Ni-MH"),
		/**
		 * <b>LEAD ACID</b> battery technology.
		 * <ul>
		 * <li><i>Tag:</i> <b>?</b></li>
		 * <li><i>Original name:</i> <b>Lead-acid</b></li>
		 * </ul>
		 */
		LEAD_ACID(/*todo:*/"?", "Lead-acid"),
		/**
		 * <b>Nickel Cadmium</b> battery technology.
		 * <ul>
		 * <li><i>Tag:</i> <b>NiCd</b></li>
		 * <li><i>Original name:</i> <b>Ni-Cd</b></li>
		 * </ul>
		 */
		Ni_Cd("NiCd", "Ni-Cd");

		/**
		 * The tag name provided by {@link BatteryManager} for this battery technology.
		 */
		public final String tagName;

		/**
		 * Original name of this battery technology. Can be used to present this technology by its
		 * name in an Android application's UI.
		 */
		public final String originalName;

		/**
		 * Creates a new instance of BatteryTechnology with the given BatteryManager tag and original
		 * name.
		 *
		 * @param tagName  Tag of this technology provided by {@link BatteryManager}.
		 * @param origName Original name of this technology.
		 */
		BatteryTechnology(String tagName, String origName) {
			this.tagName = tagName;
			this.originalName = origName;
		}

		/**
		 * Resolves an instance of BatteryTechnology according to the given <var>tagName</var> from the
		 * current set of BatteryTechnology values.
		 *
		 * @param tagName The name ({@link #tagName}) of the desired battery technology to resolve.
		 * @return Resolved battery technology instance or {@link BatteryTechnology#UNKNOWN}
		 * if there is no battery technology with the requested tag.
		 */
		@NonNull
		public static BatteryTechnology resolve(@NonNull String tagName) {
			for (BatteryTechnology tech : BatteryTechnology.values()) {
				if (tech.tagName.equalsIgnoreCase(tagName)) {
					return tech;
				}
			}
			return UNKNOWN;
		}
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Registers all battery receivers to receive all possible battery states for the given context.
	 *
	 * @param context Current activity or application context.
	 * @see #unregisterAllBatteryReceivers(Context)
	 * @see #registerBatteryReceiver(Context, int)
	 */
	void registerAllBatteryReceivers(@NonNull Context context);

	/**
	 * Registers single battery receiver for the given context.
	 *
	 * @param context    Current activity or application context.
	 * @param receiverId One of {@link #RECEIVER_BATTERY_STATUS}, {@link #RECEIVER_BATTERY_HEALTH},
	 *                   {@link #RECEIVER_BATTERY_PLUGGED_STATE} ids of the receiver to register.
	 * @see #registerAllBatteryReceivers(Context)
	 */
	void registerBatteryReceiver(@NonNull Context context, @Receiver int receiverId);

	/**
	 * Un-registers all battery receivers from the given context.
	 *
	 * @param context Context in which were all battery receivers registered before.
	 * @see #registerAllBatteryReceivers(Context)
	 * @see #unregisterBatteryReceiver(Context, int)
	 */
	void unregisterAllBatteryReceivers(@NonNull Context context);

	/**
	 * Un-registers single battery receiver from the given context.
	 *
	 * @param context    Context in which was battery receiver registered before.
	 * @param receiverId The id of battery receiver to un-register. One of {@link #RECEIVER_BATTERY_STATUS},
	 *                   {@link #RECEIVER_BATTERY_HEALTH}, {@link #RECEIVER_BATTERY_PLUGGED_STATE}.
	 */
	void unregisterBatteryReceiver(@NonNull Context context, @Receiver int receiverId);

	/**
	 * Called to dispatch message that one of the registered {@link Battery.BatteryBroadcastReceiver}
	 * was currently unregistered. This should be called immediately after successful un-registration
	 * of battery receiver with the specified <var>receiverId</var>.
	 *
	 * @param receiverId The id of the battery receiver which was un-registered. One of {@link #RECEIVER_BATTERY_STATUS},
	 *                   {@link #RECEIVER_BATTERY_HEALTH}, {@link #RECEIVER_BATTERY_PLUGGED_STATE}.
	 */
	void dispatchBatteryReceiverUnregistered(@Receiver int receiverId);

	/**
	 * Returns flag indicating whether the battery receiver with the specified <var>receiverId</var>
	 * is currently registered or not.
	 *
	 * @param receiverId The id of the battery receiver. One of {@link #RECEIVER_BATTERY_STATUS},
	 *                   {@link #RECEIVER_BATTERY_HEALTH}, {@link #RECEIVER_BATTERY_PLUGGED_STATE}.
	 * @return {@code True} if battery receiver is registered, {@code false} otherwise.
	 */
	boolean isBatteryReceiverRegistered(@Receiver int receiverId);

	/**
	 * Registers a callback to be invoked when some of the battery states changes.
	 *
	 * @param listener Callback to register. One of {@link Battery.OnStatusListener},
	 *                 {@link Battery.OnHealthListener},
	 *                 {@link Battery.OnPluggedStateListener}.
	 * @see #unregisterOnBatteryListener(Battery.BatteryListener)
	 */
	void registerOnBatteryListener(@NonNull BatteryListener listener);

	/**
	 * Un-registers the given battery listener callback.
	 *
	 * @param listener Callback to un-register.
	 * @see #registerOnBatteryListener(Battery.BatteryListener)
	 */
	void unregisterOnBatteryListener(@NonNull BatteryListener listener);

	/**
	 * Returns the current strength of life of this Android device's battery.
	 *
	 * @return The value of strength in the range {@code [0, 100]} or negative number if the current
	 * battery data are unavailable.
	 */
	@IntRange(from = 0, to = 100)
	int getStrength();

	/**
	 * Returns the current status of this Android device's battery.
	 *
	 * @return One of {@link #STATUS_CHARGING}, {@link #STATUS_DISCHARGING}, {@link #STATUS_NOT_CHARGING}
	 * {@link #STATUS_FULL} or {@link #STATUS_UNKNOWN} if the current battery data are unavailable.
	 */
	@Status
	int getStatus();

	/**
	 * Returns the current plugged state of this Android device's battery.
	 *
	 * @return One of {@link #PLUGGED_AC}, {@link #PLUGGED_USB}, {@link #PLUGGED_WIRELESS}, {@link #PLUGGED_NONE}
	 * or {@link #PLUGGED_UNKNOWN} if the current battery data are unavailable.
	 */
	@PluggedState
	int getPluggedState();

	/**
	 * Returns the current health of this Android device's battery.
	 *
	 * @return One of {@link #HEALTH_GOOD}, {@link #HEALTH_LOW_LEVEL}, {@link #HEALTH_OK_LEVEL},
	 * {@link #HEALTH_DEAD}, {@link #HEALTH_OVERHEAT}, {@link #HEALTH_OVER_VOLTAGE} or {@link #HEALTH_UNKNOWN}
	 * if the current battery data are unavailable.
	 */
	@Health
	int getHealth();

	/**
	 * Returns the technology of this Android device's battery.
	 *
	 * @return One of {@link BatteryTechnology} values or {@link BatteryTechnology#UNKNOWN BatteryTechnology.UNKNOWN}
	 * if technology can't be parsed due to unknown technology tag name or if the current battery data
	 * are unavailable.
	 */
	@NonNull
	BatteryTechnology getTechnology();

	/**
	 * Returns the current temperature of this Android device's battery.
	 *
	 * @return Current temperature in degree <b>Centigrade</b> (°C) or negative number if the current
	 * battery data are unavailable.
	 */
	float getTemperature();

	/**
	 * Returns the current voltage of this Android device's battery.
	 *
	 * @return Current voltage in <b>milli-Volts</b> (mV) or negative number if current voltage is
	 * unknown or the current battery data are unavailable.
	 */
	int getVoltage();

	/**
	 * Returns flag indicating whether this Android device's battery is currently being charging or not.
	 * <p>
	 * This is similar to {@link #isPlugged()} but here is checked the battery current status.
	 *
	 * @return {@code True} if battery is currently being charging
	 * (status == {@link #STATUS_CHARGING} || {@link #STATUS_FULL}), {@code false} otherwise.
	 * @see #isPlugged()
	 */
	boolean isCharging();

	/**
	 * Returns flag indicating whether this Android device's battery is currently plugged to some
	 * power source or not.
	 * <p>
	 * This is similar to {@link #isCharging()} but here is checked the battery current plugged state.
	 *
	 * @return {@code True} if battery is plugged to some power source, {@code false} otherwise.
	 * @see #getPluggedState()
	 * @see #isCharging()
	 */
	boolean isPlugged();

	/**
	 * Returns flag indicating whether the current strength of this Android device's battery is below
	 * the <b>LOW</b> ({@link #getHealthLowLevel()}) level value or not.
	 *
	 * @return {@code True} if the current strength is less then or equal to the current <b>LOW</b>
	 * level value, {@code false} otherwise.
	 * @see #getStrength()
	 * @see #getHealth()
	 */
	boolean isLow();

	/**
	 * Sets value of the battery health <b>LOW</b> level. This value will be used to determine whether
	 * the battery health change should be fired for {@link Battery.OnHealthListener}
	 * with {@code true} flag for {@code isLow} parameter.
	 * <p>
	 * <b>Note</b> that this level is checked only in case of received intent for {@link BatteryStatusReceiver}
	 * which must be registered.
	 *
	 * @param level Level from the range {@code [0, 100]}.
	 * @see #getHealthLowLevel()
	 * @see #registerBatteryReceiver(Context, int)
	 */
	void setHealthLowLevel(@IntRange(from = 0, to = 100) int level);

	/**
	 * Returns value of the battery current health <b>LOW</b> level.
	 *
	 * @return The value of <b>LOW</b> level in the range {@code [0, 100]}.
	 * @see #setHealthLowLevel(int)
	 * @see #isLow()
	 */
	@IntRange(from = 0, to = 100)
	int getHealthLowLevel();

	/**
	 * Sets value of the battery health <b>OK</b> level. This value will be used to determine whether
	 * the battery health change should be fired for {@link Battery.OnHealthListener}
	 * with {@code false} flag for {@code isLow} parameter.
	 * <p>
	 * <b>Note</b> that this level is checked only in case of received intent for {@link BatteryStatusReceiver}
	 * which must be registered.
	 *
	 * @param level Level from the range {@code [0, 100]}.
	 * @see #getHealthOkLevel()
	 * @see #registerBatteryReceiver(Context, int)
	 */
	void setHealthOkLevel(int level);

	/**
	 * Returns value of the battery current health <b>OK</b> level.
	 *
	 * @return The value of <b>OK</b> level in the range {@code [0, 100]}.
	 * @see #setHealthOkLevel(int)
	 */
	int getHealthOkLevel();

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Base battery status receiver.
	 */
	abstract class BatteryBroadcastReceiver extends BroadcastReceiver {

		/**
		 * Implementation in subclass should return new intent filter specific for its receiver action.
		 *
		 * @return New intent filter.
		 */
		@NonNull
		public abstract IntentFilter newIntentFilter();
	}
}

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

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import universum.studios.android.device.DeviceConfig;

/**
 * Implementation of {@link Battery} API for {@link AndroidDevice AndroidDevice}.
 *
 * @author Martin Albedinsky
 */
final class BatteryImpl implements Battery {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "BatteryImpl";

	/**
	 * Lock used for synchronized operations.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * todo:
	 */
	private static BatteryImpl sInstance;

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Application context obtained from the context passed during initialization of this wrapper.
	 */
	private final Context mContext;

	/**
	 * Value of the battery strength to indicate low battery level.
	 */
	private int mHealthLowLevel = HEALTH_LOW_LEVEL;

	/**
	 * Value of the battery strength to indicate OK battery level.
	 */
	private int mHealthOkLevel = HEALTH_OK_LEVEL;

	/**
	 * Battery status receiver (broadcast receiver) to receive battery status changes.
	 */
	private BatteryStatusReceiver mStatusReceiver;

	/**
	 * Battery health receiver (broadcast receiver) to receive battery health changes.
	 */
	private BatteryHealthReceiver mHealthReceiver;

	/**
	 * Battery plugged state receiver (broadcast receiver) to receive battery plugged state changes.
	 */
	private BatteryPluggedStateReceiver mPluggedStateReceiver;

	/**
	 * Current battery data. This is only for battery status changes management.
	 */
	private BatteryInfo mInfo = new BatteryInfo();

	/**
	 * Previous battery data. This is only for battery status changes management.
	 */
	private BatteryInfo mPrevInfo;

	/**
	 * Array with status listeners.
	 */
	private List<OnStatusListener> mStatusListeners;

	/**
	 * Array with health listeners.
	 */
	private List<OnHealthListener> mHealthListeners;

	/**
	 * Array with charge listeners.
	 */
	private List<OnPluggedStateListener> mPluggedStateListeners;

	/**
	 * Flag indicating whether the receiver is registered or not.
	 */
	private boolean mStatusReceiverRegistered, mHealthReceiverRegistered, mPluggedStateReceiverRegistered;

	/**
	 * Flag indicating whether the persistent data about battery are initialized or not.
	 */
	private boolean mPersistentDataInitialized;

	/**
	 * Flag indicating whether the current battery data are initialized or not.
	 */
	private boolean mDataInitialized;

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of BatteryImpl wrapper.
	 *
	 * @param applicationContext Application context or activity context.
	 */
	private BatteryImpl(Context applicationContext) {
		this.mContext = applicationContext.getApplicationContext();
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * todo:
	 *
	 * @param context
	 * @return
	 */
	static BatteryImpl getInstance(@NonNull Context context) {
		synchronized (LOCK) {
			if (sInstance == null) sInstance = new BatteryImpl(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	 */
	@Override
	public void registerAllBatteryReceivers(@NonNull Context context) {
		registerBatteryReceiver(context, BatteryImpl.RECEIVER_BATTERY_STATUS);
		registerBatteryReceiver(context, BatteryImpl.RECEIVER_BATTERY_HEALTH);
		registerBatteryReceiver(context, BatteryImpl.RECEIVER_BATTERY_PLUGGED_STATE);
	}

	/**
	 */
	@Override
	public void registerBatteryReceiver(@NonNull Context context, @Receiver int receiverId) {
		final String receiverName = getReceiverName(receiverId);
		synchronized (LOCK) {
			// Check already registered receiver.
			if (isBatteryReceiverRegistered(receiverId)) {
				if (DeviceConfig.LOG_ENABLED)
					Log.v(TAG, "Battery receiver(" + receiverName + ") already registered.");
			} else {
				final BatteryBroadcastReceiver receiver = getReceiver(receiverId);
				context.registerReceiver(receiver, receiver.newIntentFilter());
				switch (receiverId) {
					case RECEIVER_BATTERY_HEALTH:
						this.mHealthReceiverRegistered = true;
						break;
					case RECEIVER_BATTERY_PLUGGED_STATE:
						this.mPluggedStateReceiverRegistered = true;
						break;
					case RECEIVER_BATTERY_STATUS:
						this.mStatusReceiverRegistered = true;
						break;
				}
				if (DeviceConfig.LOG_ENABLED)
					Log.v(TAG, "Battery receiver(" + receiverName + ") successfully registered.");
			}
		}
	}

	/**
	 */
	@Override
	public void unregisterAllBatteryReceivers(@NonNull Context context) {
		unregisterBatteryReceiver(context, BatteryImpl.RECEIVER_BATTERY_STATUS);
		unregisterBatteryReceiver(context, BatteryImpl.RECEIVER_BATTERY_HEALTH);
		unregisterBatteryReceiver(context, BatteryImpl.RECEIVER_BATTERY_PLUGGED_STATE);
	}

	/**
	 */
	@Override
	public void unregisterBatteryReceiver(@NonNull Context context, @Receiver int receiverId) {
		final String receiverName = getReceiverName(receiverId);
		synchronized (LOCK) {
			// Check if is receiver registered.
			if (!isBatteryReceiverRegistered(receiverId)) {
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Cannot un-register not registered battery receiver(" + receiverName + ").");
				}
			} else {
				final BatteryBroadcastReceiver receiver = getReceiver(receiverId);
				context.unregisterReceiver(receiver);
				dispatchBatteryReceiverUnregistered(receiverId);
				if (DeviceConfig.LOG_ENABLED) {
					Log.v(TAG, "Battery receiver(" + receiverName + ") successfully unregistered.");
				}
			}
		}
	}

	/**
	 */
	@Override
	public boolean isBatteryReceiverRegistered(@Receiver int receiverId) {
		switch (receiverId) {
			case RECEIVER_BATTERY_STATUS:
				return mStatusReceiverRegistered;
			case RECEIVER_BATTERY_HEALTH:
				return mHealthReceiverRegistered;
			case RECEIVER_BATTERY_PLUGGED_STATE:
				return mPluggedStateReceiverRegistered;
		}
		return false;
	}

	/**
	 */
	@Override
	public final void dispatchBatteryReceiverUnregistered(@Receiver int receiverId) {
		switch (receiverId) {
			case RECEIVER_BATTERY_STATUS:
				this.mStatusReceiverRegistered = false;
				break;
			case RECEIVER_BATTERY_HEALTH:
				this.mHealthReceiverRegistered = false;
				break;
			case RECEIVER_BATTERY_PLUGGED_STATE:
				this.mPluggedStateReceiverRegistered = false;
				break;
		}
		onBatteryReceiverUnregistered(receiverId);
	}

	/**
	 * Invoked when a battery receiver with the specified <var>receiverId</var> was currently un-registered.
	 *
	 * @param receiverId The id of currently un-registered receiver.
	 */
	private void onBatteryReceiverUnregistered(int receiverId) {
		switch (receiverId) {
			case RECEIVER_BATTERY_STATUS:
				// Dispatch to reset status data.
				mInfo.resetStatusData();
				mPrevInfo.resetStatusData();
				// Reset all status receiver flags.
				this.mPersistentDataInitialized = this.mDataInitialized = false;
				// Dispatch to reset both plugged and health state.
				mInfo.resetHealthState();
				mInfo.resetPluggedState();
				mPrevInfo.resetHealthState();
				mPrevInfo.resetPluggedState();
				break;
			case RECEIVER_BATTERY_HEALTH:
				mInfo.resetHealthState();
				mPrevInfo.resetHealthState();
				break;
			case RECEIVER_BATTERY_PLUGGED_STATE:
				mInfo.resetPluggedState();
				mPrevInfo.resetPluggedState();
				break;
		}
	}

	/**
	 */
	@Override
	public void registerOnBatteryListener(@NonNull BatteryListener listener) {
		this.registerUnregisterListener(listener, true);
	}

	/**
	 */
	@Override
	public void unregisterOnBatteryListener(@NonNull BatteryListener listener) {
		this.registerUnregisterListener(listener, false);
	}

	/**
	 * Handles registering/un-registering of the given listener.
	 *
	 * @param listener Listener to register/un-register.
	 * @param register {@code True} to register, {@code false} to un-register listener.
	 */
	private void registerUnregisterListener(BatteryListener listener, boolean register) {
		if (listener instanceof OnStatusListener) {
			if (register) {
				this.addStatusListener((OnStatusListener) listener);
			} else {
				this.removeStatusListener((OnStatusListener) listener);
			}
		} else if (listener instanceof OnPluggedStateListener) {
			if (register) {
				this.addPluggedListener((OnPluggedStateListener) listener);
			} else {
				this.removePluggedListener((OnPluggedStateListener) listener);
			}
		} else if (listener instanceof OnHealthListener) {
			if (register) {
				this.addHealthListener((OnHealthListener) listener);
			} else {
				this.removeHealthListener((OnHealthListener) listener);
			}
		} else if (DeviceConfig.LOG_ENABLED) {
			Log.v(TAG, "Unknown battery listener type(" + listener.getClass().getSimpleName()
					+ ") when registering/un-registering BatteryListener.");
		}
	}

	/**
	 * Adds the given listener into the current set of plugged state listeners.
	 *
	 * @param listener Listener to add.
	 */
	private void addPluggedListener(OnPluggedStateListener listener) {
		if (mPluggedStateListeners == null) {
			this.mPluggedStateListeners = new ArrayList<>();
			mPluggedStateListeners.add(listener);
			return;
		}
		if (!mPluggedStateListeners.contains(listener)) {
			mPluggedStateListeners.add(listener);
		}
	}

	/**
	 * Removes the given listener from the current ste of plugged state listeners.
	 *
	 * @param listener Listener to remove.
	 */
	private void removePluggedListener(OnPluggedStateListener listener) {
		if (mPluggedStateListeners != null) mPluggedStateListeners.remove(listener);
	}

	/**
	 * Adds the given listener into the current set of status listeners.
	 *
	 * @param listener Listener to add.
	 */
	private void addStatusListener(OnStatusListener listener) {
		if (mStatusListeners == null) {
			this.mStatusListeners = new ArrayList<>();
			mStatusListeners.add(listener);
			if (mDataInitialized) {
				// Fire initial callback.
				notifyBatteryStatusChange(getApplicationContext());
			}
			return;
		}
		if (!mStatusListeners.contains(listener)) {
			mStatusListeners.add(listener);
			if (mDataInitialized) {
				// Fire initial callback.
				notifyBatteryStatusChange(getApplicationContext());
			}
		}
	}

	/**
	 * Removes the given listener from the current set of status listeners.
	 *
	 * @param listener Listener to remove.
	 */
	private void removeStatusListener(OnStatusListener listener) {
		if (mStatusListeners != null) mStatusListeners.remove(listener);
	}

	/**
	 * Adds the given listener into the current set of health listeners.
	 *
	 * @param listener Listener to add.
	 */
	private void addHealthListener(OnHealthListener listener) {
		if (mHealthListeners == null) {
			this.mHealthListeners = new ArrayList<>();
			mHealthListeners.add(listener);
			return;
		}
		if (!mHealthListeners.contains(listener)) {
			mHealthListeners.add(listener);
		}
	}

	/**
	 * Removes the given listener from the current set of health listeners.
	 *
	 * @param listener Listener to remove.
	 */
	private void removeHealthListener(OnHealthListener listener) {
		if (mHealthListeners != null) mHealthListeners.remove(listener);
	}

	/**
	 */
	@Override
	public boolean isCharging() {
		this.checkDataInitialization("charging state");
		final int status = getStatus();
		return (status == STATUS_CHARGING || status == STATUS_FULL);
	}

	/**
	 */
	@Override
	public boolean isPlugged() {
		this.checkDataInitialization("plugged state");
		final int state = getPluggedState();
		return (state != PLUGGED_UNKNOWN && state != PLUGGED_NONE);
	}

	/**
	 */
	@Override
	public boolean isLow() {
		return (getStrength() <= getHealthLowLevel());
	}

	/**
	 */
	@Override
	public void processBroadcast(@NonNull Context context, @NonNull Intent intent, int receiverId) {
		// Obtain intent action.
		final String action = intent.getAction();
		if (action == null) {
			return;
		}
		// Resolve which receiver receives broadcast.
		switch (receiverId) {
			case RECEIVER_BATTERY_STATUS:
				// Update data to actual ones.
				this.bindBatteryData(intent, receiverId);
				// Fire callback for listener.
				notifyBatteryStatusChange(context);
				break;
			case RECEIVER_BATTERY_HEALTH:
				// This is fired whenever the battery level reach the LOW level or
				// OK level value of its strength.
				// Intent is not fired repeatedly, but only when the battery reach
				// low level from the OK level or on the contrary when the battery
				// reach OK level from the low level.
				// =================================================================
				// Fire callback for listeners.
				notifyBatteryHealthChange(context, action.equals(Intent.ACTION_BATTERY_LOW));
				break;
			case RECEIVER_BATTERY_PLUGGED_STATE:
				// This is fired whenever the power is connected/disconnected
				// to/from the battery. We want to just determine if this is connect
				// or disconnect.
				// =================================================================
				// Fire callback for listeners.
				notifyBatteryPluggedStateChange(context, action.equals(Intent.ACTION_POWER_CONNECTED));
				break;
		}
	}

	/**
	 * Returns an application context obtained from the context passed during initialization of this
	 * wrapper.
	 *
	 * @return Application context.
	 */
	public Context getApplicationContext() {
		return mContext;
	}

	/**
	 */
	@IntRange(from = 0, to = 100)
	@Override
	public int getStrength() {
		this.checkDataInitialization("strength");
		return mInfo.strength();
	}

	/**
	 */
	@Status
	@Override
	public int getStatus() {
		this.checkDataInitialization("status");
		return mInfo.status;
	}

	/**
	 */
	@PluggedState
	@Override
	public int getPluggedState() {
		this.checkDataInitialization("plugged state");
		return mInfo.pluggedState;
	}

	/**
	 */
	@Health
	@Override
	public int getHealth() {
		this.checkDataInitialization("health");
		return mInfo.health;
	}

	/**
	 */
	@NonNull
	@Override
	public BatteryTechnology getTechnology() {
		this.checkDataInitialization("technology");
		return mInfo.technology;
	}

	/**
	 */
	@Override
	public float getTemperature() {
		this.checkDataInitialization("temperature");
		return mInfo.temperature / 10.0f;
	}

	/**
	 */
	@Override
	public int getVoltage() {
		this.checkDataInitialization("voltage");
		return mInfo.voltage;
	}

	/**
	 * Checks whether the current battery data are initialized or not.
	 *
	 * @param batteryInfo Cause, why the initialization state of the battery data is being checked.
	 * @return {@code True} if data are initialized, {@code false} otherwise.
	 */
	private boolean checkDataInitialization(String batteryInfo) {
		if (!mDataInitialized && !mStatusReceiverRegistered) {
			if (DeviceConfig.DEBUG_LOG_ENABLED) {
				Log.d(TAG, "Cannot determine " + batteryInfo + " of the battery. The BatteryStatusReceiver is not registered.");
			}
		}
		return mDataInitialized;
	}

	/**
	 */
	@Override
	public void setHealthLowLevel(@IntRange(from = 0, to = 100) int level) {
		if (level >= 0 && level <= 100) this.mHealthLowLevel = level;
	}

	/**
	 */
	@Override
	@IntRange(from = 0, to = 100)
	public int getHealthLowLevel() {
		return mHealthLowLevel;
	}

	/**
	 */
	@Override
	public void setHealthOkLevel(int level) {
		if (level >= 0 && level <= 100) this.mHealthOkLevel = level;
	}

	/**
	 */
	@Override
	public int getHealthOkLevel() {
		return mHealthOkLevel;
	}

	/**
	 * Notifies all current {@link universum.studios.android.device.Battery.OnHealthListener} listeners, that the health state of battery
	 * was changed.
	 *
	 * @param context Current application context.
	 * @param low     {@code True} if the current health of battery is low, {@code false} otherwise.
	 */
	private void notifyBatteryHealthChange(Context context, boolean low) {
		for (OnHealthListener listener : mHealthListeners) {
			if (listener != null) {
				if (low) {
					listener.onHealthLow(this, context);
				} else {
					listener.onHealthOk(this, context);
				}
			}
		}
		// Dispatch to reset status data.
		mInfo.resetStatusData();
		mPrevInfo.resetStatusData();
	}

	/**
	 * Notifies all current {@link universum.studios.android.device.Battery.OnPluggedStateListener}
	 * listeners, that the plugged state of battery was changed.
	 *
	 * @param context Current application context.
	 * @param plugged {@code True} if battery is currently plugged to some source,
	 *                {@code false} otherwise.
	 */
	private void notifyBatteryPluggedStateChange(Context context, boolean plugged) {
		for (OnPluggedStateListener listener : mPluggedStateListeners) {
			if (listener != null) {
				if (plugged) {
					listener.onPluggedToPowerSource(this, context);
				} else {
					listener.onUnpluggedFromPowerSource(this, context);
				}
			}
		}
	}

	/**
	 * Returns the battery receiver associated with the requested <var>receiverId</var>.
	 *
	 * @param receiverId Id of the battery receiver.
	 * @return One of battery receivers.
	 */
	private BatteryBroadcastReceiver getReceiver(int receiverId) {
		BatteryBroadcastReceiver receiver = null;
		synchronized (LOCK) {
			switch (receiverId) {
				case RECEIVER_BATTERY_STATUS:
					receiver = (mStatusReceiver == null) ? mStatusReceiver = new BatteryStatusReceiver() : mStatusReceiver;
					break;
				case RECEIVER_BATTERY_HEALTH:
					receiver = (mHealthReceiver == null) ? mHealthReceiver = new BatteryHealthReceiver() : mHealthReceiver;
					break;
				case RECEIVER_BATTERY_PLUGGED_STATE:
					receiver = (mPluggedStateReceiver == null) ? mPluggedStateReceiver = new BatteryPluggedStateReceiver() : mPluggedStateReceiver;
					break;
			}
		}
		return receiver;
	}

	/**
	 * Returns a name of the battery receiver associated with the requested <var>receiverId</var>.
	 *
	 * @param receiverId Id of the battery receiver which name to obtain.
	 * @return Battery receiver's name.
	 */
	private String getReceiverName(int receiverId) {
		String name = "";
		switch (receiverId) {
			case RECEIVER_BATTERY_STATUS:
				name = "BATTERY_STATUS_RECEIVER";
				break;
			case RECEIVER_BATTERY_HEALTH:
				name = "BATTERY_HEALTH_RECEIVER";
				break;
			case RECEIVER_BATTERY_PLUGGED_STATE:
				name = "BATTERY_PLUGGED_STATE_RECEIVER";
				break;
		}
		return name;
	}

	/**
	 * Binds the current battery data with that ones (actual) from the given intent.
	 *
	 * @param intent     Intent with the actual battery data.
	 * @param receiverId Id of the battery receiver which receives the given intent.
	 */
	private void bindBatteryData(Intent intent, int receiverId) {
		// Save previous battery info.
		this.mPrevInfo = new BatteryInfo(mInfo);
		// Initialize persistent data only if this is first binding.
		if (!mPersistentDataInitialized) {
			final String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
			mInfo.technology = BatteryTechnology.resolve(technology);
			mInfo.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, mInfo.voltage);
			this.mPersistentDataInitialized = true;
		}
		/**
		 * Update data to actual ones.
		 */
		// Status.
		mInfo.status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, mInfo.status);
		// Temperature.
		mInfo.temperature = intent.getIntExtra(
				BatteryManager.EXTRA_TEMPERATURE,
				mInfo.temperature);
		// Strength.
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
		final float strength = level / (float) scale;
		if (strength >= 0) {
			mInfo.strength = strength;
		}
		// Health.
		mInfo.health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, mInfo.health);
		// Plugged state.
		mInfo.pluggedState = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, mInfo.pluggedState);
		// Save initialization state.
		this.mDataInitialized = true;
		mInfo.logCurrentData();
	}

	/**
	 * Notifies all current {@link universum.studios.android.device.Battery.OnStatusListener} listeners, that some of the battery status
	 * values was changed.
	 *
	 * @param context Current application context.
	 */
	private void notifyBatteryStatusChange(Context context) {
		for (OnStatusListener listener : mStatusListeners) {
			if (listener != null) {
				listener.onStatusChange(this, context);
			}
		}
		// Check for changed health or plugged status.
		if (mPrevInfo != null) {
			if (!mHealthReceiverRegistered) {
				switch (mPrevInfo.getHealthStatus(mInfo.strength())) {
					case BatteryInfo.HEALTH_LEVEL_STATUS_UNCHANGED:
						break;
					case BatteryInfo.HEALTH_LEVEL_STATUS_LOW:
						notifyBatteryHealthChange(context, true);
						break;
					case BatteryInfo.HEALTH_LEVEL_STATUS_OK:
						notifyBatteryHealthChange(context, false);
						break;
				}
			}
			if (!mPluggedStateReceiverRegistered) {
				if (mPrevInfo.hasPluggedStateChanged(mInfo.pluggedState)) {
					notifyBatteryPluggedStateChange(context, isPlugged());
				}
			}
		}
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * Battery info data holder.
	 */
	private final class BatteryInfo {

		/**
		 * Flag indicating, that battery health level status in unchanged.
		 */
		private static final int HEALTH_LEVEL_STATUS_UNCHANGED = 0x00;

		/**
		 * Flag indicating, that battery health reaches OK level.
		 */
		private static final int HEALTH_LEVEL_STATUS_OK = 0x01;

		/**
		 * Flag indicating, that battery health drops below OK level to LOW level.
		 */
		private static final int HEALTH_LEVEL_STATUS_LOW = 0x02;

		/**
		 * Status of the current Android device's battery like {@link #STATUS_CHARGING}.
		 */
		int status = STATUS_UNKNOWN;

		/**
		 * Status of the current Android device's battery plugged state like {@link #PLUGGED_USB}.
		 */
		int pluggedState = PLUGGED_UNKNOWN;

		/**
		 * Technology of the current Android device's battery.
		 */
		BatteryTechnology technology = BatteryTechnology.UNKNOWN;

		/**
		 * Status of the current Android device's battery health state like {@link #HEALTH_GOOD}.
		 */
		int health = HEALTH_UNKNOWN;

		/**
		 * Strength of the current Android device's battery life as percentage value.
		 */
		float strength = -0.01f;

		/**
		 * Temperature of the current Android device's battery.
		 */
		int temperature = -1;

		/**
		 * Voltage of the current Android device's battery.
		 */
		int voltage = -1;

		/**
		 * Creates a new empty instance of BatteryInfo unknown data for all battery parameters.
		 */
		BatteryInfo() {
		}

		/**
		 * Creates a new instance of BatteryInfo with the data copied from the given <var>other</var>
		 * battery info holder.
		 *
		 * @param other Battery info holder of which data to copy.
		 */
		BatteryInfo(BatteryInfo other) {
			this.health = other.health;
			this.status = other.status;
			this.pluggedState = other.pluggedState;
			this.technology = BatteryTechnology.resolve(other.technology.tagName);
			this.strength = other.strength;
			this.temperature = other.temperature;
			this.voltage = other.voltage;
		}

		/**
		 * Resets the current value of health state of this info to {@link #HEALTH_UNKNOWN}.
		 */
		private void resetHealthState() {
			if (!mStatusReceiverRegistered) {
				// We don't know anymore the actual health state of the battery.
				this.health = HEALTH_UNKNOWN;
			}
		}

		/**
		 * Resets the current value of plugged state of this info to {@link #PLUGGED_UNKNOWN}.
		 */
		final void resetPluggedState() {
			// We don't know anymore the actual plugged state of the battery.
			if (!mStatusReceiverRegistered || !mPluggedStateReceiverRegistered) {
				this.pluggedState = PLUGGED_UNKNOWN;
			}
		}

		/**
		 * Resets the current value of status to {@link #STATUS_UNKNOWN} and value of strength
		 * to {@code -1} of this info.
		 */
		final void resetStatusData() {
			if (!mStatusReceiverRegistered) {
				// Reset all dynamically changing states.
				this.status = STATUS_UNKNOWN;
				this.strength = this.temperature = -1;
			}
		}

		/**
		 * Checks whether the current plugged state of this info is different from
		 * {@link #PLUGGED_UNKNOWN} and the given <var>currentStatus</var>.
		 *
		 * @param currentStatus Current status to compare with the current plugged state of this info.
		 * @return {@code True} if changed, {@code false} otherwise.
		 */
		final boolean hasPluggedStateChanged(int currentStatus) {
			return pluggedState != PLUGGED_UNKNOWN && pluggedState != currentStatus;
		}

		/**
		 * Returns the current strength value in %.
		 *
		 * @return Current strength multiplied by {@code 100}.
		 */
		final int strength() {
			return (int) (mInfo.strength * 100);
		}

		/**
		 * @param currentHealthStrength The value of the current Android device's battery strength.
		 * @return On of the {@link #HEALTH_LEVEL_STATUS_UNCHANGED}, {@link #HEALTH_LEVEL_STATUS_LOW},
		 * {@link #HEALTH_LEVEL_STATUS_OK}.
		 */
		final int getHealthStatus(int currentHealthStrength) {
			int status = HEALTH_LEVEL_STATUS_UNCHANGED;
			int prevHealthStrength = strength();
			if (prevHealthStrength != currentHealthStrength) {
				/**
				 * Check if the battery strength gets below/above the
				 * LOW/OK level like so:
				 * --------------------------------------------------
				 * -- battery is getting OK
				 * OK level ======================
				 * -- neutral area
				 * LOW level =====================
				 * -- battery is getting LOW
				 * --------------------------------------------------
				 */
				if (prevHealthStrength == (mHealthLowLevel + 1) && currentHealthStrength <= mHealthLowLevel) {
					status = HEALTH_LEVEL_STATUS_LOW;
				} else if (prevHealthStrength == mHealthOkLevel && currentHealthStrength > mHealthOkLevel) {
					status = HEALTH_LEVEL_STATUS_OK;
				}
			}
			return status;
		}

		/**
		 * Logs the current data of this info to log-cat console.
		 */
		final void logCurrentData() {
			if (DeviceConfig.DEBUG_LOG_ENABLED) Log.d(TAG, toString());
		}

		/**
		 */
		@Override
		@SuppressWarnings("StringBufferReplaceableByString")
		public String toString() {
			final StringBuilder builder = new StringBuilder("BatteryInfo{");
			builder.append("status: ");
			builder.append(status);
			builder.append(", health: ");
			builder.append(health);
			builder.append(", strength: ");
			builder.append(getStrength());
			builder.append(", voltage: ");
			builder.append(getVoltage());
			builder.append(", technology: ");
			builder.append(technology.name());
			builder.append(", pluggedState: ");
			builder.append(pluggedState);
			builder.append(", temperature: ");
			builder.append(getTemperature());
			return builder.append("}").toString();
		}
	}
}

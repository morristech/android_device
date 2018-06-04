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
package universum.studios.android.device.battery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Battery} implementation.
 *
 * @author Martin Albedinsky
 * @since 1.0
 */
final class BatteryImpl implements Battery {

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "BatteryImpl";

	/**
	 * Boolean flag indicating whether to print debug output or not.
	 */
	static final boolean DEBUG = false;

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
	 * BatteryImpl singleton instance.
	 */
	@SuppressLint("StaticFieldLeak")
	private static BatteryImpl instance;

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Value of the battery strength to indicate low battery level.
	 */
	private int healthLowLevel = HEALTH_LOW_LEVEL;

	/**
	 * Value of the battery strength to indicate OK battery level.
	 */
	private int healthOkLevel = HEALTH_OK_LEVEL;

	/**
	 * Battery status receiver (broadcast receiver) to receive battery status changes.
	 */
	private BatteryStatusReceiver statusReceiver;

	/**
	 * Battery health receiver (broadcast receiver) to receive battery health changes.
	 */
	private BatteryHealthReceiver healthReceiver;

	/**
	 * Battery plugged state receiver (broadcast receiver) to receive battery plugged state changes.
	 */
	private BatteryPluggedStateReceiver pluggedStateReceiver;

	/**
	 * Current battery data. This is only for battery status changes management.
	 */
	private final BatteryInfo info = new BatteryInfo();

	/**
	 * Previous battery data. This is only for battery status changes management.
	 */
	private BatteryInfo prevInfo;

	/**
	 * List of status listeners.
	 */
	private final List<OnStatusListener> statusListeners = new ArrayList<>(2);

	/**
	 * AList of health listeners.
	 */
	private final List<OnHealthListener> healthListeners = new ArrayList<>(2);

	/**
	 * List of plugged state listeners.
	 */
	private final List<OnPluggedStateListener> pluggedStateListeners = new ArrayList<>(2);

	/**
	 * Integer holding state determining which battery receivers are registered.
	 */
	private final AtomicInteger registeredReceiversCount = new AtomicInteger(0);

	/**
	 * Flag indicating whether the persistent data about battery are initialized or not.
	 */
	private boolean persistentDataInitialized;

	/**
	 * Flag indicating whether the current battery data are initialized or not.
	 */
	private boolean dataInitialized;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of BatteryImpl.
	 */
	private BatteryImpl() {}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Returns or creates a new singleton instance of BatteryImpl.
	 *
	 * @param context Context used by the battery implementation to access system services.
	 * @return Battery implementation with actual battery data available.
	 */
	@NonNull static BatteryImpl getInstance(@NonNull final Context context) {
		synchronized (LOCK) {
			if (instance == null) instance = new BatteryImpl();
		}
		return instance;
	}

	/**
	 */
	@Override public boolean isCharging() {
		this.checkDataInitialization("charging state");
		final int status = getStatus();
		return (status == STATUS_CHARGING || status == STATUS_FULL);
	}

	/**
	 */
	@Override public boolean isPlugged() {
		this.checkDataInitialization("plugged state");
		final int state = getPluggedState();
		return (state != PLUGGED_UNKNOWN && state != PLUGGED_NONE);
	}

	/**
	 */
	@Override public boolean isLow() {
		return (getStrength() <= getHealthLowLevel());
	}

	/**
	 */
	@Override @IntRange(from = 0, to = 100) public int getStrength() {
		this.checkDataInitialization("strength");
		return info.strength();
	}

	/**
	 */
	@Override @Status public int getStatus() {
		this.checkDataInitialization("status");
		return info.status;
	}

	/**
	 */
	@Override @PluggedState public int getPluggedState() {
		this.checkDataInitialization("plugged state");
		return info.pluggedState;
	}

	/**
	 */
	@Override @Health public int getHealth() {
		this.checkDataInitialization("health");
		return info.health;
	}

	/**
	 */
	@Override @NonNull public BatteryTechnology getTechnology() {
		this.checkDataInitialization("technology");
		return info.technology;
	}

	/**
	 */
	@Override public float getTemperature() {
		this.checkDataInitialization("temperature");
		return info.temperature / 10.0f;
	}

	/**
	 */
	@Override public int getVoltage() {
		this.checkDataInitialization("voltage");
		return info.voltage;
	}

	/**
	 * Checks whether the current battery data are initialized or not.
	 *
	 * @param batteryInfo Cause, why the initialization state of the battery data is being checked.
	 * @return {@code True} if data are initialized, {@code false} otherwise.
	 */
	private boolean checkDataInitialization(final String batteryInfo) {
		if (!dataInitialized && !isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS) && DEBUG) {
			Log.d(TAG, "Cannot determine " + batteryInfo + " of the battery. The BatteryStatusReceiver is not registered.");
		}
		return dataInitialized;
	}

	/**
	 */
	@Override public void setHealthLowLevel(@IntRange(from = 0, to = 100) final int level) {
		if (level >= 0 && level <= 100) this.healthLowLevel = level;
	}

	/**
	 */
	@Override @IntRange(from = 0, to = 100) public int getHealthLowLevel() {
		return healthLowLevel;
	}

	/**
	 */
	@Override public void setHealthOkLevel(final int level) {
		if (level >= 0 && level <= 100) this.healthOkLevel = level;
	}

	/**
	 */
	@Override public int getHealthOkLevel() {
		return healthOkLevel;
	}

	/**
	 * Binds the current battery data with that ones (actual) from the given intent.
	 *
	 * @param intent Intent with the actual battery data.
	 */
	private void bindBatteryData(final Intent intent) {
		// Save previous battery info.
		this.prevInfo = new BatteryInfo(info);
		// Initialize persistent data only if this is first binding.
		if (!persistentDataInitialized) {
			final String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
			info.technology = BatteryTechnology.resolve(technology);
			info.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, info.voltage);
			this.persistentDataInitialized = true;
		}
		/*
		 * Update data to actual ones.
		 */
		// Status.
		info.status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, info.status);
		// Temperature.
		info.temperature = intent.getIntExtra(
				BatteryManager.EXTRA_TEMPERATURE,
				info.temperature);
		// Strength.
		final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
		final float strength = level / (float) scale;
		if (strength >= 0) {
			info.strength = strength;
		}
		// Health.
		info.health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, info.health);
		// Plugged state.
		info.pluggedState = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, info.pluggedState);
		// Save initialization state.
		this.dataInitialized = true;
		info.logCurrentData();
	}

	/**
	 */
	@Override public void registerOnStatusListener(@NonNull final OnStatusListener listener) {
		synchronized (statusListeners) {
			if (!statusListeners.contains(listener)) statusListeners.add(listener);
		}
	}

	/**
	 */
	@Override public void unregisterOnStatusListener(@NonNull final OnStatusListener listener) {
		synchronized (statusListeners) {
			statusListeners.remove(listener);
		}
	}

	/**
	 */
	@Override public void registerOnHealthListener(@NonNull final OnHealthListener listener) {
		synchronized (healthListeners) {
			if (!healthListeners.contains(listener)) healthListeners.add(listener);
		}
	}

	/**
	 */
	@Override public void unregisterOnHealthListener(@NonNull final OnHealthListener listener) {
		synchronized (healthListeners) {
			healthListeners.remove(listener);
		}
	}

	/**
	 */
	@Override public void registerOnPluggedStateListener(@NonNull final OnPluggedStateListener listener) {
		synchronized (pluggedStateListeners) {
			if (!pluggedStateListeners.contains(listener)) pluggedStateListeners.add(listener);
		}
	}

	/**
	 */
	@Override public void unregisterOnPluggedStateListener(@NonNull final OnPluggedStateListener listener) {
		synchronized (pluggedStateListeners) {
			pluggedStateListeners.remove(listener);
		}
	}

	/**
	 */
	@Override public void registerBatteryReceiver(@NonNull final Context context, @Receiver final int receiverFlag) {
		synchronized (LOCK) {
			if ((receiverFlag & RECEIVER_BATTERY_STATUS) != 0 && !isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
				this.statusReceiver = new BatteryStatusReceiver();
				context.registerReceiver(statusReceiver, statusReceiver.newIntentFilter());
				this.registeredReceiversCount.set(registeredReceiversCount.get() | RECEIVER_BATTERY_STATUS);
			}
			if ((receiverFlag & RECEIVER_BATTERY_HEALTH) != 0 && !isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
				this.healthReceiver = new BatteryHealthReceiver();
				context.registerReceiver(healthReceiver, healthReceiver.newIntentFilter());
				this.registeredReceiversCount.set(registeredReceiversCount.get() | RECEIVER_BATTERY_HEALTH);
			}
			if ((receiverFlag & RECEIVER_BATTERY_PLUGGED_STATE) != 0 && !isBatteryReceiverRegistered(RECEIVER_BATTERY_PLUGGED_STATE)) {
				this.pluggedStateReceiver = new BatteryPluggedStateReceiver();
				context.registerReceiver(pluggedStateReceiver, pluggedStateReceiver.newIntentFilter());
				this.registeredReceiversCount.set(registeredReceiversCount.get() | RECEIVER_BATTERY_PLUGGED_STATE);
			}
		}
	}

	/**
	 */
	@Override public boolean isBatteryReceiverRegistered(@Receiver final int receiverFlag) {
		return (registeredReceiversCount.get() & receiverFlag) != 0;
	}

	/**
	 */
	@Override public void unregisterBatteryReceiver(@NonNull final Context context, @Receiver final int receiverFlag) {
		synchronized (LOCK) {
			if ((receiverFlag & RECEIVER_BATTERY_STATUS) != 0 && isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
				context.unregisterReceiver(statusReceiver);
				this.statusReceiver = null;
				this.registeredReceiversCount.set(registeredReceiversCount.get() & ~RECEIVER_BATTERY_STATUS);
				// Reset status data.
				info.resetStatusData();
				prevInfo.resetStatusData();
				// Reset all status receiver flags.
				this.persistentDataInitialized = this.dataInitialized = false;
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
					info.resetHealthState();
					prevInfo.resetHealthState();
				}
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_PLUGGED_STATE)) {
					info.resetPluggedState();
					prevInfo.resetPluggedState();
				}
			}
			if ((receiverFlag & RECEIVER_BATTERY_HEALTH) != 0 && isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
				context.unregisterReceiver(healthReceiver);
				this.healthReceiver = null;
				this.registeredReceiversCount.set(registeredReceiversCount.get() & ~RECEIVER_BATTERY_HEALTH);
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
					info.resetHealthState();
					prevInfo.resetHealthState();
				}
			}
			if ((receiverFlag & RECEIVER_BATTERY_PLUGGED_STATE) != 0 && isBatteryReceiverRegistered(RECEIVER_BATTERY_PLUGGED_STATE)) {
				context.unregisterReceiver(pluggedStateReceiver);
				this.pluggedStateReceiver = null;
				this.registeredReceiversCount.set(registeredReceiversCount.get() & ~RECEIVER_BATTERY_PLUGGED_STATE);
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
					info.resetPluggedState();
					prevInfo.resetPluggedState();
				}
			}
		}
	}

	/**
	 * Handles a broadcast received by one of {@link BatteryStatusReceiver}, {@link BatteryHealthReceiver}
	 * or {@link BatteryPluggedStateReceiver}.
	 *
	 * @param context Application context.
	 * @param intent  The intent containing the broadcasted data.
	 */
	void handleBroadcast(@NonNull final Context context, @NonNull final Intent intent) {
		// Check whether there is an intent action.
		final String action = intent.getAction();
		if (action == null) {
			return;
		}
		// Resolve which receiver has received the broadcast.
		final Class<?> receiverClass = (Class<?>) intent.getSerializableExtra(BatteryBroadcastReceiver.EXTRA_RECEIVER_CLASS);
		if (BatteryStatusReceiver.class.equals(receiverClass)) {
			// Update data to actual ones.
			this.bindBatteryData(intent);
			// Fire callback for listener.
			notifyBatteryStatusChange(context);
		} else if (BatteryHealthReceiver.class.equals(receiverClass)) {
			// This is fired whenever the battery level reach the LOW level or
			// OK level value of its strength.
			// Intent is not fired repeatedly, but only when the battery reach
			// low level from the OK level or on the contrary when the battery
			// reach OK level from the low level.
			// =================================================================
			// Fire callback for listeners.
			notifyBatteryHealthChange(context, action.equals(Intent.ACTION_BATTERY_LOW));
		} else if (BatteryPluggedStateReceiver.class.equals(receiverClass)) {
			// This is fired whenever the power is connected/disconnected
			// to/from the battery. We want to just determine if this is connect
			// or disconnect.
			// =================================================================
			// Fire callback for listeners.
			notifyBatteryPluggedStateChange(context, action.equals(Intent.ACTION_POWER_CONNECTED));
		}
	}

	/**
	 * Notifies all registered {@link OnStatusListener} listeners that some of the battery status
	 * values has changed.
	 *
	 * @param context Current application context.
	 */
	private void notifyBatteryStatusChange(final Context context) {
		synchronized (statusListeners) {
			if (!statusListeners.isEmpty()) {
				for (final OnStatusListener listener : statusListeners) {
					listener.onStatusChange(context, this);
				}
			}
		}
		// Check for changed health or plugged status.
		if (prevInfo != null) {
			if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
				switch (prevInfo.getHealthStatus(info.strength())) {
					case BatteryInfo.HEALTH_LEVEL_STATUS_LOW:
						notifyBatteryHealthChange(context, true);
						break;
					case BatteryInfo.HEALTH_LEVEL_STATUS_OK:
						notifyBatteryHealthChange(context, false);
						break;
					case BatteryInfo.HEALTH_LEVEL_STATUS_UNCHANGED:
					default:
						break;
				}
			}
			if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_PLUGGED_STATE)) {
				if (prevInfo.hasPluggedStateChanged(info.pluggedState)) {
					notifyBatteryPluggedStateChange(context, isPlugged());
				}
			}
		}
	}

	/**
	 * Notifies all registered {@link OnHealthListener} listeners that the health state of battery
	 * has changed.
	 *
	 * @param context Current application context.
	 * @param low     {@code True} if the current health of battery is low, {@code false} otherwise.
	 */
	private void notifyBatteryHealthChange(final Context context, final boolean low) {
		synchronized (healthListeners) {
			if (!healthListeners.isEmpty()) {
				for (final OnHealthListener listener : healthListeners) {
					if (low) listener.onHealthLow(context, this);
					else listener.onHealthOk(context, this);
				}
			}
		}
	}

	/**
	 * Notifies all registered {@link OnPluggedStateListener} listeners that the plugged state of
	 * battery has changed.
	 *
	 * @param context Current application context.
	 * @param plugged {@code True} if battery is currently plugged to some source, {@code false} otherwise.
	 */
	private void notifyBatteryPluggedStateChange(final Context context, final boolean plugged) {
		synchronized (pluggedStateListeners) {
			if (!pluggedStateListeners.isEmpty()) {
				for (final OnPluggedStateListener listener : pluggedStateListeners) {
					if (plugged) listener.onPluggedToPowerSource(context, this);
					else listener.onUnpluggedFromPowerSource(context, this);
				}
			}
		}
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * Battery info data holder.
	 */
	private final class BatteryInfo {

		/**
		 * Flag indicating, that battery health level status in unchanged.
		 */
		static final int HEALTH_LEVEL_STATUS_UNCHANGED = 0x00;

		/**
		 * Flag indicating, that battery health reaches OK level.
		 */
		static final int HEALTH_LEVEL_STATUS_OK = 0x01;

		/**
		 * Flag indicating, that battery health drops below OK level to LOW level.
		 */
		static final int HEALTH_LEVEL_STATUS_LOW = 0x02;

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
		 * Creates a new empty instance of BatteryInfo.
		 */
		BatteryInfo() {
			// Use default values.
		}

		/**
		 * Creates a new instance of BatteryInfo with the data copied from the given <var>other</var>
		 * battery info holder.
		 *
		 * @param other Battery info holder of which data to copy.
		 */
		BatteryInfo(@NonNull final BatteryInfo other) {
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
		void resetHealthState() {
			// We don't know anymore the actual health state of the battery.
			this.health = HEALTH_UNKNOWN;
		}

		/**
		 * Resets the current value of plugged state of this info to {@link #PLUGGED_UNKNOWN}.
		 */
		void resetPluggedState() {
			// We don't know anymore the actual plugged state of the battery.
			this.pluggedState = PLUGGED_UNKNOWN;
		}

		/**
		 * Resets the current value of status to {@link #STATUS_UNKNOWN} and value of strength
		 * to {@code -1} of this info.
		 */
		void resetStatusData() {
			// Reset all dynamically changing states.
			this.status = STATUS_UNKNOWN;
			this.strength = this.temperature = -1;
		}

		/**
		 * Checks whether the current plugged state of this info is different from
		 * {@link #PLUGGED_UNKNOWN} and the given <var>currentStatus</var>.
		 *
		 * @param currentStatus Current status to compare with the current plugged state of this info.
		 * @return {@code True} if changed, {@code false} otherwise.
		 */
		boolean hasPluggedStateChanged(final int currentStatus) {
			return pluggedState != PLUGGED_UNKNOWN && pluggedState != currentStatus;
		}

		/**
		 * Returns the current strength value in %.
		 *
		 * @return Current strength multiplied by {@code 100}.
		 */
		int strength() {
			return (int) (info.strength * 100);
		}

		/**
		 * @param currentHealthStrength The value of the current Android device's battery strength.
		 * @return On of the {@link #HEALTH_LEVEL_STATUS_UNCHANGED}, {@link #HEALTH_LEVEL_STATUS_LOW},
		 * {@link #HEALTH_LEVEL_STATUS_OK}.
		 */
		int getHealthStatus(final int currentHealthStrength) {
			int status = HEALTH_LEVEL_STATUS_UNCHANGED;
			final int prevHealthStrength = strength();
			if (prevHealthStrength != currentHealthStrength) {
				/*
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
				if (prevHealthStrength == (healthLowLevel + 1) && currentHealthStrength <= healthLowLevel) {
					status = HEALTH_LEVEL_STATUS_LOW;
				} else if (prevHealthStrength == healthOkLevel && currentHealthStrength > healthOkLevel) {
					status = HEALTH_LEVEL_STATUS_OK;
				}
			}
			return status;
		}

		/**
		 * Logs the current data of this info to log-cat console.
		 */
		void logCurrentData() {
			if (DEBUG) Log.d(TAG, toString());
		}

		/**
		 */
		@SuppressWarnings("StringBufferReplaceableByString")
		@Override public String toString() {
			final StringBuilder builder = new StringBuilder(64);
			builder.append(BatteryInfo.class.getSimpleName());
			builder.append("{status: ");
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
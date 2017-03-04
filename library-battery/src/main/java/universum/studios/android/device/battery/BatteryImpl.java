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

import universum.studios.android.device.DeviceConfig;

/**
 * A {@link Battery} implementation.
 *
 * @author Martin Albedinsky
 */
final class BatteryImpl implements Battery {

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "BatteryImpl";

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
	 * BatteryImpl singleton instance.
	 */
	@SuppressLint("StaticFieldLeak")
	private static BatteryImpl sInstance;

	/**
	 * Members =====================================================================================
	 */

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
	 * List of status listeners.
	 */
	private final List<OnStatusListener> mStatusListeners = new ArrayList<>(2);

	/**
	 * AList of health listeners.
	 */
	private final List<OnHealthListener> mHealthListeners = new ArrayList<>(2);

	/**
	 * List of plugged state listeners.
	 */
	private final List<OnPluggedStateListener> mPluggedStateListeners = new ArrayList<>(2);

	/**
	 * Integer holding state determining which battery receivers are registered.
	 */
	private final AtomicInteger mRegisteredReceivers = new AtomicInteger(0);

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
	 * Creates a new instance of BatteryImpl.
	 *
	 * @param applicationContext Application context used to access system services.
	 */
	private BatteryImpl(Context applicationContext) {
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Returns or creates a new singleton instance of BatteryImpl.
	 *
	 * @param context Context used by the battery implementation to access system services.
	 * @return Battery implementation with actual battery data available.
	 */
	@NonNull
	static BatteryImpl getInstance(@NonNull Context context) {
		synchronized (LOCK) {
			if (sInstance == null) sInstance = new BatteryImpl(context.getApplicationContext());
		}
		return sInstance;
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
	@IntRange(from = 0, to = 100)
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
	@Override
	@PluggedState
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
		if (!mDataInitialized && !isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
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
	 * Binds the current battery data with that ones (actual) from the given intent.
	 *
	 * @param intent Intent with the actual battery data.
	 */
	private void bindBatteryData(Intent intent) {
		// Save previous battery info.
		this.mPrevInfo = new BatteryInfo(mInfo);
		// Initialize persistent data only if this is first binding.
		if (!mPersistentDataInitialized) {
			final String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
			mInfo.technology = BatteryTechnology.resolve(technology);
			mInfo.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, mInfo.voltage);
			this.mPersistentDataInitialized = true;
		}
		/*
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
	 */
	@Override
	public void registerOnStatusListener(@NonNull OnStatusListener listener) {
		synchronized (mStatusListeners) {
			if (!mStatusListeners.contains(listener)) mStatusListeners.add(listener);
		}
	}

	/**
	 */
	@Override
	public void unregisterOnStatusListener(@NonNull OnStatusListener listener) {
		synchronized (mStatusListeners) {
			mStatusListeners.remove(listener);
		}
	}

	/**
	 */
	@Override
	public void registerOnHealthListener(@NonNull OnHealthListener listener) {
		synchronized (mHealthListeners) {
			if (!mHealthListeners.contains(listener)) mHealthListeners.add(listener);
		}
	}

	/**
	 */
	@Override
	public void unregisterOnHealthListener(@NonNull OnHealthListener listener) {
		synchronized (mHealthListeners) {
			mHealthListeners.remove(listener);
		}
	}

	/**
	 */
	@Override
	public void registerOnPluggedStateListener(@NonNull OnPluggedStateListener listener) {
		synchronized (mPluggedStateListeners) {
			if (!mPluggedStateListeners.contains(listener)) mPluggedStateListeners.add(listener);
		}
	}

	/**
	 */
	@Override
	public void unregisterOnPluggedStateListener(@NonNull OnPluggedStateListener listener) {
		synchronized (mPluggedStateListeners) {
			mPluggedStateListeners.remove(listener);
		}
	}

	/**
	 */
	@Override
	public void registerBatteryReceiver(@NonNull Context context, @Receiver int receiverFlag) {
		synchronized (LOCK) {
			if ((receiverFlag & RECEIVER_BATTERY_STATUS) != 0 && !isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
				this.mStatusReceiver = new BatteryStatusReceiver();
				context.registerReceiver(mStatusReceiver, mStatusReceiver.newIntentFilter());
				this.mRegisteredReceivers.set(mRegisteredReceivers.get() | RECEIVER_BATTERY_STATUS);
			}
			if ((receiverFlag & RECEIVER_BATTERY_HEALTH) != 0 && !isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
				this.mHealthReceiver = new BatteryHealthReceiver();
				context.registerReceiver(mHealthReceiver, mHealthReceiver.newIntentFilter());
				this.mRegisteredReceivers.set(mRegisteredReceivers.get() | RECEIVER_BATTERY_HEALTH);
			}
			if ((receiverFlag & RECEIVER_BATTERY_PLUGGED_STATE) != 0 && !isBatteryReceiverRegistered(RECEIVER_BATTERY_PLUGGED_STATE)) {
				this.mPluggedStateReceiver = new BatteryPluggedStateReceiver();
				context.registerReceiver(mPluggedStateReceiver, mPluggedStateReceiver.newIntentFilter());
				this.mRegisteredReceivers.set(mRegisteredReceivers.get() | RECEIVER_BATTERY_PLUGGED_STATE);
			}
		}
	}

	/**
	 */
	@Override
	public boolean isBatteryReceiverRegistered(@Receiver int receiverFlag) {
		return (mRegisteredReceivers.get() & receiverFlag) != 0;
	}

	/**
	 */
	@Override
	public void unregisterBatteryReceiver(@NonNull Context context, @Receiver int receiverFlag) {
		synchronized (LOCK) {
			if ((receiverFlag & RECEIVER_BATTERY_STATUS) != 0 && isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
				context.unregisterReceiver(mStatusReceiver);
				this.mStatusReceiver = null;
				this.mRegisteredReceivers.set(mRegisteredReceivers.get() & ~RECEIVER_BATTERY_STATUS);
				// Reset status data.
				mInfo.resetStatusData();
				mPrevInfo.resetStatusData();
				// Reset all status receiver flags.
				this.mPersistentDataInitialized = this.mDataInitialized = false;
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
					mInfo.resetHealthState();
					mPrevInfo.resetHealthState();
				}
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_PLUGGED_STATE)) {
					mInfo.resetPluggedState();
					mPrevInfo.resetPluggedState();
				}
			}
			if ((receiverFlag & RECEIVER_BATTERY_HEALTH) != 0 && isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
				context.unregisterReceiver(mHealthReceiver);
				this.mHealthReceiver = null;
				this.mRegisteredReceivers.set(mRegisteredReceivers.get() & ~RECEIVER_BATTERY_HEALTH);
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
					mInfo.resetHealthState();
					mPrevInfo.resetHealthState();
				}
			}
			if ((receiverFlag & RECEIVER_BATTERY_PLUGGED_STATE) != 0 && isBatteryReceiverRegistered(RECEIVER_BATTERY_PLUGGED_STATE)) {
				context.unregisterReceiver(mPluggedStateReceiver);
				this.mPluggedStateReceiver = null;
				this.mRegisteredReceivers.set(mRegisteredReceivers.get() & ~RECEIVER_BATTERY_PLUGGED_STATE);
				if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_STATUS)) {
					mInfo.resetPluggedState();
					mPrevInfo.resetPluggedState();
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
	void handleBroadcast(@NonNull Context context, @NonNull Intent intent) {
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
	private void notifyBatteryStatusChange(Context context) {
		synchronized (mStatusListeners) {
			if (mStatusListeners.size() > 0) {
				for (final OnStatusListener listener : mStatusListeners) {
					listener.onStatusChange(context, this);
				}
			}
		}
		// Check for changed health or plugged status.
		if (mPrevInfo != null) {
			if (!isBatteryReceiverRegistered(RECEIVER_BATTERY_HEALTH)) {
				switch (mPrevInfo.getHealthStatus(mInfo.strength())) {
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
				if (mPrevInfo.hasPluggedStateChanged(mInfo.pluggedState)) {
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
	private void notifyBatteryHealthChange(Context context, boolean low) {
		synchronized (mHealthListeners) {
			if (mHealthListeners.size() > 0) {
				for (final OnHealthListener listener : mHealthListeners) {
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
	private void notifyBatteryPluggedStateChange(Context context, boolean plugged) {
		synchronized (mPluggedStateListeners) {
			if (mPluggedStateListeners.size() > 0) {
				for (final OnPluggedStateListener listener : mPluggedStateListeners) {
					if (plugged) listener.onPluggedToPowerSource(context, this);
					else listener.onUnpluggedFromPowerSource(context, this);
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
			// We don't know anymore the actual health state of the battery.
			this.health = HEALTH_UNKNOWN;
		}

		/**
		 * Resets the current value of plugged state of this info to {@link #PLUGGED_UNKNOWN}.
		 */
		final void resetPluggedState() {
			// We don't know anymore the actual plugged state of the battery.
			this.pluggedState = PLUGGED_UNKNOWN;
		}

		/**
		 * Resets the current value of status to {@link #STATUS_UNKNOWN} and value of strength
		 * to {@code -1} of this info.
		 */
		final void resetStatusData() {
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

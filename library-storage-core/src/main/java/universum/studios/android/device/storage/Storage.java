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
 *//*
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
 */package universum.studios.android.device.storage;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Storage interface specifies API through which an actual information about the Android device's
 * storage may be accessed. Also this interface may be used to create, delete, copy or move files
 * on the Android devide's file system.
 * <p>
 * A storage implementation may be obtained via {@link Storage.Provider#getStorage(Context) Storage.PROVIDER.getStorage(Context)}.
 * <p>
 * Below are listed methods to manage files on the file system provided by the this interface:
 *
 * <h3>To create files and directories</h3>
 * <ul>
 * <li>{@link #createFile(String)}</li>
 * <li>{@link #createFile(int, String)}</li>
 * <li>{@link #createFiles(String...)}</li>
 * <li>{@link #createFiles(int, String...)}</li>
 * <li>{@link #createDirectory(String)}</li>
 * <li>{@link #createDirectory(int, String)}</li>
 * <li>{@link #createDirectories(String...)}</li>
 * <li>{@link #createDirectories(int, String...)}</li>
 * </ul>
 *
 * <h3>To delete files or directories</h3>
 * <ul>
 * <li>{@link #deleteFile(String)}</li>
 * <li>{@link #deleteFile(int, String)}</li>
 * <li>{@link #deleteFiles(String...)}</li>
 * <li>{@link #deleteFiles(int, String...)}</li>
 * <li>{@link #deleteDirectory(String)}</li>
 * <li>{@link #deleteDirectory(int, String)}</li>
 * <li>{@link #deleteDirectory(int, FileFilter, FilenameFilter, String)}</li>
 * <li>{@link #deleteDirectories(String...)}</li>
 * <li>{@link #deleteDirectories(int, String...)}</li>
 * <li>{@link #deleteDirectories(int, FileFilter, FilenameFilter, String...)}</li>
 * </ul>
 *
 * <h3>To copy files or directories</h3>
 * <ul>
 * <li>{@link #copyFile(int, String, String)}</li>
 * <li>{@link #copyFile(int, int, String, String)}</li>
 * <li>{@link #copyFiles(int, String, String...)}</li>
 * <li>{@link #copyFiles(int, int, String, String...)}</li>
 * <li>{@link #copyDirectory(int, String, String)}</li>
 * <li>{@link #copyDirectory(int, int, String, String)}</li>
 * <li>{@link #copyDirectory(int, int, FileFilter, FilenameFilter, String, String)}</li>
 * <li>{@link #copyDirectories(int, String, String...)}</li>
 * <li>{@link #copyDirectories(int, int, String, String...)}</li>
 * <li>{@link #copyDirectories(int, int, FileFilter, FilenameFilter, String, String...)}</li>
 * </ul>
 *
 * <h3>To move files or directories</h3>
 * <ul>
 * <li>{@link #moveFile(int, String, String)}</li>
 * <li>{@link #moveFile(int, int, String, String)}</li>
 * <li>{@link #moveFiles(int, String, String...)}</li>
 * <li>{@link #moveFiles(int, int, String, String...)}</li>
 * <li>{@link #moveDirectory(int, String, String)}</li>
 * <li>{@link #moveDirectory(int, int, String, String)}</li>
 * <li>{@link #moveDirectory(int, int, FileFilter, FilenameFilter, String, String)}</li>
 * <li>{@link #moveDirectories(int, String, String...)}</li>
 * <li>{@link #moveDirectories(int, int, String, String...)}</li>
 * <li>{@link #moveDirectories(int, int, FileFilter, FilenameFilter, String, String...)}</li>
 * </ul>
 *
 * @author Martin Albedinsky
 */
public interface Storage {

	/*
	 * Provider ====================================================================================
	 */

	/**
	 * Interface for provider that may be used to access implementation of {@link Storage}.
	 *
	 * @author Martin Albedinsky
	 */
	interface Provider {

		/**
		 * Provides a singleton implementation of {@link Storage}.
		 *
		 * @param context Context used by the storage implementation to access actual storage data.
		 * @return Storage implementation with actual storage data already available.
		 */
		@NonNull
		Storage getStorage(@NonNull Context context);
	}

	/**
	 * A {@link Provider} implementation that may be used to access implementation of {@link Storage}.
	 */
	Provider PROVIDER = new Provider() {

		/**
		 */
		@NonNull
		@Override
		public Storage getStorage(@NonNull final Context context) {
			return StorageImpl.getInstance(context);
		}
	};

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Represents an empty set of flags for storage API. This is only for informative purpose for
	 * {@link Storage.Result} objects when there are used storage API methods which does not takes
	 * any flags.
	 */
	int NO_FLAGS = -1;

	/**
	 * Copied flag from {@link StorageEditor#DEFAULT} for better access.
	 */
	int DEFAULT = StorageEditor.DEFAULT;

	/**
	 * Copied flag from {@link StorageEditor#OVERWRITE} for better access.
	 */
	int OVERWRITE = StorageEditor.OVERWRITE;

	/**
	 * Copied flag from {@link StorageEditor#COPY} for better access.
	 */
	int COPY = StorageEditor.COPY;

	/**
	 * Defines an annotation for determining set of allowed flags.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef(flag = true, value = {DEFAULT, OVERWRITE, COPY})
	@interface Flags {
	}

	/**
	 * Permissions ---------------------------------------------------------------------------------
	 */

	/**
	 * Flag indicating whether to allow permission to file to be readable or not.
	 */
	int PERMISSION_READ = 0x00000001;

	/**
	 * Flag indicating whether to allow permission to file to be writable or not.
	 */
	int PERMISSION_WRITE = 0x00000001 << 1;

	/**
	 * Flag indicating whether to allow permission to file to be executable or not.
	 */
	int PERMISSION_EXECUTE = 0x00000001 << 2;

	/**
	 * <b>Read + Write</b> file permissions.
	 * <h3>Flags</h3>
	 * <ul>
	 * <li>read:        <b>true</b></li>
	 * <li>write:       <b>true</b></li>
	 * <li>execute:     <b>false</b></li>
	 * </ul>
	 */
	int PERMISSIONS_READ_WRITE = PERMISSION_READ | PERMISSION_WRITE;

	/**
	 * <b>All</b> file permissions.
	 * <h3>Flags</h3>
	 * <ul>
	 * <li>read:        <b>true</b></li>
	 * <li>write:       <b>true</b></li>
	 * <li>executed:    <b>true</b></li>
	 * </ul>
	 */
	int PERMISSIONS_ALL = PERMISSION_READ | PERMISSION_WRITE | PERMISSION_EXECUTE;

	/**
	 * Defines an annotation for determining set of allowed file permission flags.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef(flag = true, value = {PERMISSION_READ, PERMISSION_WRITE, PERMISSION_EXECUTE})
	@interface FilePermissions {
	}

	/**
	 * Storage identifiers -------------------------------------------------------------------------
	 */

	/**
	 * Identifier for <b>base</b> path on an Android device's file system. This identifier should be
	 * used whenever the passed path to this API represents a full path to the desired file, so should
	 * not be prepended.
	 */
	int BASE = 0x00;

	/**
	 * Identifier for the <b>root</b> directory of an Android device's file system.
	 * <p>
	 * <b>Note</b>, that this is only <b>read-only</b> file storage.
	 * <p>
	 * See {@link android.os.Environment#getRootDirectory()} for additional info.
	 */
	int ROOT = 0x01;

	/**
	 * Identifier for the <b>data</b> directory of an Android device's file system.
	 * <p>
	 * See {@link android.os.Environment#getDataDirectory()} for additional info.
	 */
	int DATA = 0x02;

	/**
	 * Identifier for the <b>cache</b> directory of an Android device's file system.
	 * <p>
	 * <b>Note</b>, that this directory is created for each of Android applications (respectively),
	 * identified by theirs <i>package name</i>, and is meant to be used for caching all necessary
	 * data of the particular Android application. This directory can be accessed/managed only by
	 * application for which was this directory created.
	 * <p>
	 * <b>Files from this directory will be deleted as first when the Android device runs low on storage.</b>
	 * <p>
	 * <b>Once the application is uninstalled, this directory will be deleted.</b>
	 * <p>
	 * See {@link android.content.Context#getCacheDir()} for additional info.
	 */
	int CACHE = 0x04;

	/**
	 * Identifier for the <b>internal</b> directory of an Android device's file system.
	 * <p>
	 * <b>Note</b>, that this directory (like {@link #CACHE}) is created for each of Android applications
	 * (respectively), and is meant to be used for storing all necessary data of the particular Android
	 * application.
	 * <p>
	 * <b>Once the application is uninstalled, this directory will be deleted.</b>
	 * <p>
	 * See {@link android.content.Context#getFilesDir()} for additional info.
	 */
	int INTERNAL = 0x05;

	/**
	 * Identifier for the <b>external</b> directory of an Android device's file system. This will in
	 * most cases point to the mounted <b>SD card</b> if available.
	 * <p>
	 * See {@link android.os.Environment#getExternalStorageDirectory()} for additional info.
	 *
	 * @see #isExternalAvailable()
	 */
	int EXTERNAL = 0x06;

	/**
	 * Identifier for the <b>external-package</b> directory of an Android device's file system created
	 * especially for the particular Android application on the external file storage at path:
	 * /Android/data/<b>app.package.name</b>/.
	 * <p>
	 * <b>Note</b>, that this directory is created when {@link #getStorage(int) getStorage(EXTERNAL_PACKAGE)}
	 * is being first time called and the external storage is at that particular time available.
	 *
	 * @see #isExternalAvailable()
	 */
	int EXTERNAL_PACKAGE = 0x07;

	/**
	 * Defines an annotation for determining set of available storage directories.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({BASE, ROOT, DATA, CACHE, INTERNAL, EXTERNAL, EXTERNAL_PACKAGE})
	@interface StorageDir {
	}

	/**
	 * Actions -------------------------------------------------------------------------------------
	 */

	/**
	 * Flag identifying <b>create</b> action on file system.
	 * <p>
	 * This flag is used by {@link Storage.Result} and {@link Storage.Results} to identify for which
	 * action was the specific result created.
	 */
	int ACTION_CREATE = 0x01;

	/**
	 * Flag identifying <b>delete</b> action on file system.
	 * <p>
	 * This flag is used by {@link Storage.Result} and {@link Storage.Results} to identify for which
	 * action was the specific result created.
	 */
	int ACTION_DELETE = 0x02;

	/**
	 * Flag identifying <b>copy</b> action on file system.
	 * <p>
	 * This flag is used by {@link Storage.Result} and {@link Storage.Results} to identify for which
	 * action was the specific result created.
	 */
	int ACTION_COPY = 0x03;

	/**
	 * Flag identifying <b>move</b> action on file system.
	 * <p>
	 * This flag is used by {@link Storage.Result} and {@link Storage.Results} to identify for which
	 * action was the specific result created.
	 */
	int ACTION_MOVE = 0x04;

	/**
	 * Error codes ---------------------------------------------------------------------------------
	 */

	/**
	 * Code representing no error. Only for internal purpose.
	 */
	int NO_ERROR = 0x00;

	/**
	 * Error set to {@link Storage.Result} or {@link Storage.Results} when there was requested some
	 * storage API action but no paths for such a action were specified.
	 * <p>
	 * Constant value: <b>0</b>
	 */
	int ERROR_EMPTY_REQUEST = -0x0000;

	/**
	 * Error set to {@link Storage.Result} or {@link Storage.Results} when {@link java.io.IOException}
	 * occurred while performing some of the storage API actions.
	 * <p>
	 * Constant value: <b>-1</b>
	 */
	int ERROR_IO = -0x01;

	/**
	 * Error set to {@link Storage.Result} or {@link Storage.Results} when there was requested some
	 * action which is not supported by this storage API.
	 * <p>
	 * This can be for example, when trying to move directory to itself.
	 * <p>
	 * Constant value: <b>-2</b>
	 */
	int ERROR_UNSUPPORTED_OPERATION = -0x02;

	/**
	 * Error set to {@link Storage.Result} or {@link Storage.Results} when there was requested some
	 * action to be performed by this storage API, but this action failed because of the specified
	 * <b>flags</b> for this action or because of constraints of this storage API.
	 * <p>
	 * This can be for example when requesting <b>copy</b> action for some files which should be copied
	 * into the same directory as they are right now, but without {@link #COPY} flag.
	 * <p>
	 * Constant value: <b>-3</b>
	 */
	int ERROR_API = -0x03;

	/**
	 * Error set to {@link Storage.Result} or {@link Storage.Results} whenever there is requested
	 * some action to be performed by this storage API with a file which actually does not exists.
	 * <p>
	 * Constant value: <b>-17</b>
	 */
	int ERROR_FILE_NOT_FOUND = -0x11;

	/**
	 * Error set to {@link Storage.Result} or {@link Storage.Results} whenever there is requested
	 * some action to be performed by this storage API with a file but the destination path points
	 * to a directory.
	 * <p>
	 * This can be for example, when trying to <b>copy</b> a file to another directory where in this
	 * particular directory already exists a directory with the same name as the requested file.
	 * <p>
	 * Constant value: <b>-18</b>
	 */
	int ERROR_FILE_SAME_AS_DIRECTORY = -0x12;

	/**
	 * Error set to {@link Storage.Result} or {@link Storage.Results} whenever there is requested
	 * some action to be performed by this storage API with a directory but the destination path points
	 * to a file.
	 * <p>
	 * This can be for example, when trying to <b>move</b> a directory to another directory where in
	 * this particular directory already exists a file with the same name as the requested directory.
	 * <p>
	 * Constant value: <b>-19</b>
	 */
	int ERROR_DIRECTORY_SAME_AS_FILE = -0x13;

	/**
	 * ---------------------------------------------------------------------------------------------
	 */

	/**
	 * Constant used to increase amount of bits when moving between different storage units by
	 * {@link Storage.Unit}.
	 */
	int KILO = 128;

	/*
	 * Enums =======================================================================================
	 */

	/**
	 * Represents unit to measure files on an Android device's file system.
	 *
	 * @author Martin Albedinsky
	 */
	enum Unit {

		/**
		 * Storage unit size of one <b>bit</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>1</b></li>
		 * <li>bytes: <b>0.125</b></li>
		 * </ul>
		 */
		b("Bit", 1),
		/**
		 * Storage unit size of one <b>byte</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>8</b></li>
		 * <li>bytes: <b>1</b></li>
		 * </ul>
		 */
		B("Byte", 8 * b.bits),
		/**
		 * Storage unit size of one <b>kilo-bit</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>1,024</b></li>
		 * <li>bytes: <b>128</b></li>
		 * </ul>
		 */
		Kb("Kilobit", KILO * B.bits),
		/**
		 * Storage unit size of one <b>kilo-byte</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>8,192</b></li>
		 * <li>bytes: <b>1,024</b></li>
		 * </ul>
		 */
		KB("Kilobyte", B.bits * Kb.bits),
		/**
		 * Storage unit size of one <b>mega-bit</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>1,048,576</b></li>
		 * <li>bytes: <b>131,072</b></li>
		 * </ul>
		 */
		Mb("Megabit", KILO * KB.bits),
		/**
		 * Storage unit size of one <b>mega-byte</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>8,388,608</b></li>
		 * <li>bytes: <b>1,048,576</b></li>
		 * </ul>
		 */
		MB("Megabyte", B.bits * Mb.bits),
		/**
		 * Storage unit size of one <b>giga-bit</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>1,073,741,824</b></li>
		 * <li>bytes: <b>134,217,728</b></li>
		 * </ul>
		 */
		Gb("Gigabit", KILO * MB.bits),
		/**
		 * Storage unit size of one <b>giga-byte</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>8,589,934,592</b></li>
		 * <li>bytes: <b>1,073,741,824</b></li>
		 * </ul>
		 */
		GB("Gigabyte", B.bits * Gb.bits),
		/**
		 * Storage unit size of one <b>tera-bit</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>1,099,511,627,776</b></li>
		 * <li>bytes: <b>137,438,953,472</b></li>
		 * </ul>
		 */
		Tb("Terabit", KILO * GB.bits),
		/**
		 * Storage unit size of one <b>tera-byte</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>8,796,093,022,208</b></li>
		 * <li>bytes: <b>1,099,511,627,776</b></li>
		 * </ul>
		 */
		TB("Terabyte", B.bits * Tb.bits),
		/**
		 * Storage unit size of one <b>peta-bit</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>1,125,899,906,842,624</b></li>
		 * <li>bytes: <b>140,737,488,355,328</b></li>
		 * </ul>
		 */
		Pb("Petabit", KILO * TB.bits),
		/**
		 * Storage unit size of one <b>peta-byte</b>.
		 * <h3>Size:</h3>
		 * <ul>
		 * <li>bits: <b>9,007,199,254,740,992</b></li>
		 * <li>bytes: <b>1,125,899,906,842,624</b></li>
		 * </ul>
		 */
		PB("Petabyte", B.bits * Pb.bits);

		/**
		 * Full name of this storage unit. Like <b>Megabyte</b> for <i>MB</i>.
		 */
		public final String fullName;

		/**
		 * Amount of bits represented by this storage unit.
		 */
		public final long bits;

		/**
		 * Amount of bytes ({@code bits / 8}) represented by this storage unit.
		 */
		public final float bytes;

		/**
		 * Creates a new instance of storage Unit with the given full name and amount of bits.
		 *
		 * @param fullName The name for this storage unit.
		 * @param bits     Amount of bits represented by this unit.
		 */
		Unit(final String fullName, final long bits) {
			this.fullName = fullName;
			this.bits = bits;
			this.bytes = bits / 8.0f;
		}

		/**
		 * Formats the given amount of <var>bits</var> to format specific for this storage unit.
		 * <p>
		 * For example, {@link Unit#MB Unit.MB.formatBits(8388608)} will result in {@code 1} which
		 * can be printed as {@code 1 MB}.
		 *
		 * @param bits The amount of bits to format to this storage unit's representative value.
		 * @return Formatted representative value of this storage unit for the given amount of bits.
		 * @see #formatBytes(long)
		 */
		public float formatBits(final long bits) {
			return bits / (float) this.bits;
		}

		/**
		 * Formats the given amount of <var>bytes</var> to format specific for this storage unit.
		 * <p>
		 * For example, {@link Unit#MB Unit.MB.formatBytes(1048576)} will result in {@code 1} which
		 * can be printed as {@code 1 MB}.
		 *
		 * @param bytes The amount of bytes to format to this storage unit's representative value.
		 * @return Formatted representative value of this storage unit for the given amount of bytes.
		 * @see #formatBits(long)
		 */
		public float formatBytes(final long bytes) {
			return bytes / this.bytes;
		}
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Same as {@link #createFile(int, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result createFile(@NonNull String path);

	/**
	 * Creates a <b>file</b> with the given <var>path</var> on this Android device's storage.
	 * <p>
	 * See {@link StorageUtils#createFile(String)} for additional info.
	 *
	 * @param storage An identifier of the storage of which path to use as {@code base path} for
	 *                the given <var>path</var>. Use {@link #BASE} when the given path represents
	 *                full path to the file which should be created.
	 * @param path    The path to the file which should be created.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result createFile(@StorageDir int storage, @NonNull String path);

	/**
	 * Same as {@link #createFiles(int, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results createFiles(@NonNull String... paths);

	/**
	 * Creates all <b>files</b> with the given <var>paths</var> on this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #createFile(int, String)} method, so the given paths will
	 * be looped to perform the requested action for each of them.
	 *
	 * @param paths Set of paths of files which should be created.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results createFiles(@StorageDir int storage, @NonNull String... paths);

	/**
	 * Same as {@link #createDirectory(int, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result createDirectory(@NonNull String path);

	/**
	 * Creates a <b>directory</b> with the given <var>path</var> on this Android device's storage.
	 * <p>
	 * See {@link StorageUtils#createDirectory(String)} for additional info.
	 *
	 * @param storage An identifier of the storage of which path to use as {@code base path} for
	 *                the given <var>path</var>. Use {@link #BASE} when the given path represents
	 *                full path to the directory which should be created.
	 * @param path    The path to the directory which should be created.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result createDirectory(@StorageDir int storage, @NonNull String path);

	/**
	 * Same as {@link #createDirectories(int, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results createDirectories(@NonNull String... paths);

	/**
	 * Creates all <b>directories</b> with the given <var>paths</var> on this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #createDirectory(int, String)} method, so the given paths
	 * will be looped to perform the requested action for each of them.
	 *
	 * @param paths Set of paths of directories which should be created.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results createDirectories(@StorageDir int storage, @NonNull String... paths);

	/**
	 * Same as {@link #deleteFile(int, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result deleteFile(@NonNull String path);

	/**
	 * Deletes a file at the given <var>path</var> from this Android device's storage.
	 * <p>
	 * See {@link StorageUtils#deleteFile(String)} for additional info.
	 *
	 * @param storage An identifier of the storage of which path to use as {@code base path} for
	 *                the given <var>path</var>. Use {@link #BASE} when the given path represents
	 *                full path to the file which should be deleted.
	 * @param path    The path to the file which should be deleted.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result deleteFile(@StorageDir int storage, @NonNull String path);

	/**
	 * Same as {@link #deleteFiles(int, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results deleteFiles(@NonNull String... paths);

	/**
	 * Deletes all files at the given <var>paths</var> from this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #deleteFile(int, String)} method, so the given paths will
	 * be looped to perform the requested action for each of them.
	 *
	 * @param paths Set of paths to files which should be deleted.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results deleteFiles(@StorageDir int storage, @NonNull String... paths);

	/**
	 * Same as {@link #deleteDirectory(int, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result deleteDirectory(@NonNull String path);

	/**
	 * Same as {@link #deleteDirectory(int, FileFilter, FilenameFilter, String)} with {@code null}
	 * filters.
	 */
	@NonNull
	Result deleteDirectory(@StorageDir int storage, @NonNull String path);

	/**
	 * Deletes a directory at the given <var>path</var> from this Android device's storage.
	 * <p>
	 * <b>Note</b>, that if this method is used with valid <b>filters</b>, sub-directories
	 * (and theirs sub-directories, ...) of the requested directory which has some content left after
	 * deleting process will remain and will be not deleted. This also applies to the requested directory.
	 * This can be used to not delete whole directory, but only the desired files of such a directory.
	 * <p>
	 * See {@link StorageUtils#deleteDirectory(FileFilter, FilenameFilter, String)} for additional
	 * info.
	 *
	 * @param storage    An identifier of the storage of which path to use as {@code base path} for
	 *                   the given <var>path</var>. Use {@link #BASE} when the given path represents
	 *                   full path to the file which should be deleted.
	 * @param filter     If not {@code null}, this filter will be used to delete only files of
	 *                   the requested directory which passes through this filter, all other files
	 *                   will be not deleted.
	 * @param nameFilter If not {@code null}, this filter will be used to delete only files of
	 *                   the requested directory with names which passes through this filter, all
	 *                   other files will be not deleted.
	 * @param path       The path to the directory which should be deleted or of which filtered files
	 *                   should be deleted.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result deleteDirectory(@StorageDir int storage, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String path);

	/**
	 * Same as {@link #deleteDirectories(int, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results deleteDirectories(@NonNull String... paths);

	/**
	 * Same as {@link #deleteDirectories(int, FileFilter, FilenameFilter, String...)} with {@code null}
	 * filters.
	 */
	@NonNull
	Results deleteDirectories(@StorageDir int storage, @NonNull String... paths);

	/**
	 * Deletes all directories at the given <var>paths</var> from this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #deleteDirectory(int, FileFilter, FilenameFilter, String)}
	 * method, so the given paths will be looped to perform the requested action for each of them.
	 *
	 * @param paths Set of paths to directories which should be deleted.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results deleteDirectories(@StorageDir int storage, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String... paths);

	/**
	 * Same as {@link #deleteFileOrDirectory(int, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result deleteFileOrDirectory(@NonNull String path);

	/**
	 * Same as {@link #deleteFileOrDirectory(int, FileFilter, FilenameFilter, String)} with
	 * {@code null} filters.
	 */
	@NonNull
	Result deleteFileOrDirectory(@StorageDir int storage, @NonNull String path);

	/**
	 * Performs {@link #deleteFile(int, String)} or {@link #deleteDirectory(int, FileFilter, FilenameFilter, String)}
	 * depends on whether the given <var>path</var> points to a file or to a directory.
	 */
	@NonNull
	Result deleteFileOrDirectory(@StorageDir int storage, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String path);

	/**
	 * Same as {@link #deleteFilesOrDirectories(int, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results deleteFilesOrDirectories(@NonNull String... paths);

	/**
	 * Same as {@link #deleteFilesOrDirectories(int, FileFilter, FilenameFilter, String...)} with
	 * {@code null} filters.
	 */
	@NonNull
	Results deleteFilesOrDirectories(@StorageDir int storage, @NonNull String... paths);

	/**
	 * Performs {@link #deleteFile(int, String)} or {@link #deleteDirectory(int, FileFilter, FilenameFilter, String)}
	 * for each of the given <var>paths</var> depends on whether the iterated path points to a file
	 * or to a directory.
	 */
	@NonNull
	Results deleteFilesOrDirectories(@StorageDir int storage, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String... paths);

	/**
	 * Same as {@link #copyFile(int, int, String, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result copyFile(@Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Copies a file from the given <var>fromPath</var> to the given <var>toPath</var> on this Android
	 * device's storage.
	 * <p>
	 * See {@link StorageUtils#deleteFile(String)} for additional info.
	 *
	 * @param storage  An identifier of the storage of which path to use as {@code base path} for
	 *                 the given <var>path</var>. Use {@link #BASE} when the given path represents
	 *                 full path to the file which should be copied.
	 * @param flags    Set of flags to be taken into count when performing this action. Use for example
	 *                 {@link #COPY} flag to create copy at the same path as the specified <var>fromPath</var>
	 *                 with {@code -Copy} suffix.
	 * @param toPath   The path to the file where should be the requested file copied. If there is no
	 *                 existing file at this path, a new one will be created.
	 * @param fromPath The path to the file which should be copied.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result copyFile(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #copyFiles(int, int, String, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results copyFiles(@Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Copies all files at the given <var>fromPaths</var> on this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #copyFile(int, int, String, String)} method, so the given
	 * paths will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Set of paths to files which should be copied.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results copyFiles(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #copyFile(int, int, String, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result copyDirectory(@Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #copyDirectory(int, int, FileFilter, FilenameFilter, String, String)} with
	 * {@code null} filters, so whole content of the requested directory will be copied.
	 */
	@NonNull
	Result copyDirectory(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Copies a directory from the given <var>fromPath</var> to the given <var>toPath</var> on this
	 * Android device's storage.
	 * <p>
	 * <b>Note</b>, that if this method is used with valid <b>filters</b>, only files which passes
	 * the given filters will be copied to the requested destination. This can be used to not copy
	 * whole directory, but only the desired files of such a directory.
	 * <p>
	 * See {@link StorageUtils#copyDirectory(int, FileFilter, FilenameFilter, String, String)} for
	 * additional info.
	 *
	 * @param storage    An identifier of the storage of which path to use as {@code base path} for
	 *                   the given paths. Use {@link #BASE} when the given paths represents full paths
	 *                   for the directory which should be copied.
	 * @param flags      Set of flags to be taken into count when performing this action. Use for example
	 *                   {@link #COPY} flag to create copy at the same path as the specified <var>fromPath</var>
	 *                   with {@code -Copy} suffix.
	 * @param filter     If not {@code null}, this filter will be used to copy only files of the
	 *                   requested directory which passes through this filter, all other files will
	 *                   be not copied.
	 * @param nameFilter If not {@code null}, this filter will be used to copy only files of the
	 *                   requested directory with names which passes through this filter, all other
	 *                   files will be not copied.
	 * @param toPath     The path to the directory where should be the requested directory or its filtered
	 *                   files copied. If there is no existing directory at this path, a new one will
	 *                   be created.
	 * @param fromPath   The path to the directory which or of which filtered files should be copied.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result copyDirectory(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #copyDirectories(int, int, String, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results copyDirectories(@Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #copyDirectories(int, int, FileFilter, FilenameFilter, String, String...)} with
	 * {@code null} filters, so whole content of the requested directories will be copied.
	 */
	@NonNull
	Results copyDirectories(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Copies all directories at the given <var>fromPaths</var> on this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #copyDirectory(int, int, FileFilter, FilenameFilter, String, String)}
	 * method, so the given  paths will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Set of paths to directories which should be copied.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results copyDirectories(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #copyFileOrDirectory(int, int, String, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result copyFileOrDirectory(@Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #copyFileOrDirectory(int, int, FileFilter, FilenameFilter, String, String)} with
	 * {@code null} filters.
	 */
	@NonNull
	Result copyFileOrDirectory(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Performs {@link #copyFile(int, int, String, String)} or {@link #copyDirectory(int, int, FileFilter, FilenameFilter, String, String)}
	 * depends on whether the given <var>fromPath</var> points to a file or to a directory.
	 */
	@NonNull
	Result copyFileOrDirectory(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #copyFilesOrDirectories(int, int, String, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results copyFilesOrDirectories(@Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #copyFilesOrDirectories(int, int, FileFilter, FilenameFilter, String, String...)}
	 * with {@code null} filters.
	 */
	@NonNull
	Results copyFilesOrDirectories(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Performs {@link #copyFile(int, int, String, String)} or {@link #copyDirectory(int, int, FileFilter, FilenameFilter, String, String)}
	 * for each of the given <var>fromPaths</var> depends on whether the iterated path points to a file
	 * or to a directory.
	 */
	@NonNull
	Results copyFilesOrDirectories(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #moveFile(int, int, String, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result moveFile(@Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Moves a file from the given <var>fromPath</var> to the given <var>toPath</var> on this Android
	 * device's storage.
	 * <p>
	 * See {@link StorageUtils#moveFile(int, String, String)} for additional info.
	 *
	 * @param storage  An identifier of the storage of which path to use as {@code base path} for
	 *                 the given <var>path</var>. Use {@link #BASE} when the given path represents
	 *                 full path to the file which should be copied.
	 * @param flags    Set of flags to be taken into count when performing this action. Use for example
	 *                 {@link #OVERWRITE} flag to overwrite the existing file at the specified <var>toPath</var>.
	 * @param toPath   The path to the file where should be the requested file copied. If there is no
	 *                 existing file at this path, a new one will be created.
	 * @param fromPath The path to the file which should be copied.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result moveFile(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #moveFiles(int, int, String, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results moveFiles(@Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Moves all files at the given <var>fromPaths</var> on this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #moveFile(int, int, String, String)} method, so the given
	 * paths will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Set of paths to files which should be moved.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results moveFiles(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #moveFile(int, int, String, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result moveDirectory(@Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #moveDirectory(int, int, FileFilter, FilenameFilter, String, String)} with
	 * {@code null} filters, so whole content of the requested directory will be copied.
	 */
	@NonNull
	Result moveDirectory(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Moves a directory from the given <var>fromPath</var> to the given <var>toPath</var> on this
	 * Android device's storage.
	 * <p>
	 * <b>Note</b>, that if this method is used with valid <b>filters</b>, only files which passes
	 * the given filters will be moved to the requested destination. This can be used to not move
	 * whole directory, but only the desired files of such a directory.
	 * <p>
	 * See {@link StorageUtils#moveDirectory(int, String, String)} for additional info.
	 *
	 * @param storage    An identifier of the storage of which path to use as {@code base path} for
	 *                   the given paths. Use {@link #BASE} when the given paths represents full paths
	 *                   for the directory which should be copied.
	 * @param flags      Set of flags to be taken into count when performing this action. Use for example
	 *                   {@link #OVERWRITE} flag to overwrite the existing directory at the specified
	 *                   <var>toPath</var>.
	 * @param filter     If not {@code null}, this filter will be used to move only files of the
	 *                   requested directory which passes through this filter, all other files will
	 *                   be not moved.
	 * @param nameFilter If not {@code null}, this filter will be used to move only files of the
	 *                   requested directory with names which passes through this filter, all other
	 *                   files will be not moved.
	 * @param toPath     The path to the directory where should be the requested directory or its filtered
	 *                   files copied. If there is no existing directory at this path, a new one will
	 *                   be created.
	 * @param fromPath   The path to the directory which or of which filtered files should be moved.
	 * @return Result data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Result moveDirectory(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #moveDirectories(int, int, String, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results moveDirectories(@Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #moveDirectories(int, int, FileFilter, FilenameFilter, String, String...)} with
	 * {@code null} filters, so whole content of the requested directories will be copied.
	 */
	@NonNull
	Results moveDirectories(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Moves all directories at the given <var>fromPaths</var> on this Android device's storage.
	 * <p>
	 * This is a bulk operation for {@link #moveFile(int, int, String, String)} method, so the given
	 * paths will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Set of paths to directories which should be moved.
	 * @return Results data for this action to identify whether this action succeeded or failed.
	 */
	@NonNull
	Results moveDirectories(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #moveFileOrDirectory(int, int, String, String)} for {@link #BASE} storage.
	 */
	@NonNull
	Result moveFileOrDirectory(@Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #moveFileOrDirectory(int, int, FileFilter, FilenameFilter, String, String)} with
	 * {@code null} filters.
	 */
	@NonNull
	Result moveFileOrDirectory(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Performs {@link #moveFile(int, int, String, String)} or {@link #moveDirectory(int, int, FileFilter, FilenameFilter, String, String)}
	 * depends on whether the given <var>fromPath</var> points to a file or to a directory.
	 */
	@NonNull
	Result moveFileOrDirectory(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String fromPath);

	/**
	 * Same as {@link #moveFilesOrDirectories(int, int, String, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	Results moveFilesOrDirectories(@Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #moveFilesOrDirectories(int, int, FileFilter, FilenameFilter, String, String...)}
	 * with {@code null} filters.
	 */
	@NonNull
	Results moveFilesOrDirectories(@StorageDir int storage, @Flags int flags, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Performs {@link #moveFile(int, int, String, String)} or {@link #moveDirectory(int, int, FileFilter, FilenameFilter, String, String)}
	 * for each of the given <var>fromPaths</var> depends on whether the iterated path points to a file
	 * or to a directory.
	 */
	@NonNull
	Results moveFilesOrDirectories(@StorageDir int storage, @Flags int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String... fromPaths);

	/**
	 * Same as {@link #changeFilePermissions(int, String, int, boolean)} for {@link #BASE} storage.
	 */
	boolean changeFilePermissions(@NonNull String path, @FilePermissions int permissions, boolean ownerOnly);

	/**
	 * Changes <b>read/write</b> permissions of a file on the given <var>path</var> of this Android
	 * device's storage.
	 *
	 * @param storage     An identifier of the storage of which path to use as {@code base path} for
	 *                    the given <var>path</var>. Use {@link #BASE} when the given path represents
	 *                    full path to the file of which read/write permissions should be changed.
	 * @param path        The path to the file of which permissions should be changed.
	 * @param permissions The desired read/write/execute permissions. One of {@link #PERMISSION_READ},
	 *                    {@link #PERMISSION_WRITE}, {@link #PERMISSION_EXECUTE} or combination of
	 *                    them.
	 * @param ownerOnly   If {@code true} only owner will have the specified permissions, otherwise
	 *                    any one will have them.
	 * @return {@code True} if all permissions were successfully changed, {@code false}
	 * otherwise.
	 */
	boolean changeFilePermissions(@StorageDir int storage, @NonNull String path, @FilePermissions int permissions, boolean ownerOnly);

	/**
	 * Checks whether there is some external storage mounted to this Android device or not. If this
	 * check returns {@code true}, the current external storage can be used for <b>read + write</b>
	 * access.
	 * <p>
	 * See {@link android.os.Environment#getExternalStorageState()} and {@link android.os.Environment#MEDIA_MOUNTED}
	 * for more info.
	 *
	 * @return {@code True} if some external storage is mounted to this Android device,
	 * {@code false} otherwise.
	 * @see #isExternalReadOnly()
	 * @see #isExternalAvailable()
	 */
	boolean isExternalMounted();

	/**
	 * Checks whether the current mounted external storage is in <b>read only</b> state or not.
	 * <p>
	 * See {@link android.os.Environment#getExternalStorageState()} and {@link android.os.Environment#MEDIA_MOUNTED_READ_ONLY}
	 * for more info.
	 *
	 * @return {@code True} if the currently mounted external storage to this Android device is
	 * in <b>read only</b> state, {@code false} if there is no external storage mounted or the
	 * current one can be used for <b>read + write</b> operations.
	 * @see #isExternalMounted()
	 * @see #isExternalAvailable()
	 */
	boolean isExternalReadOnly();

	/**
	 * Checks whether there is some external storage available on this Android device which can be
	 * used to store some data.
	 *
	 * @return {@code True} if there is some external storage mounted and can be used for
	 * <b>read + write</b> access or at least for <b>read</b> access if it is in {@link android.os.Environment#MEDIA_MOUNTED_READ_ONLY}
	 * state.
	 * @see #isExternalMounted()
	 * @see #isExternalReadOnly()
	 */
	boolean isExternalAvailable();

	/**
	 * Checks whether there is the specified amount of <var>bytes</var> available on the requested
	 * <var>storage</var> type.
	 * <p>
	 * <b>Note</b>, that for {@link #BASE} this will always returns {@code false}, because this
	 * identifier doesn't represents any storage.
	 * <p>
	 * See {@link File#getFreeSpace()} for additional info.
	 *
	 * @param storage An identifier of the storage of which free space to check.
	 * @param bytes   The desired amount of bytes to check against.
	 * @return {@code True} if there is at least the desired amount of bytes available for storing
	 * of new files on the requested storage, {@code false} otherwise.
	 */
	boolean hasFreeSpace(@StorageDir int storage, long bytes);

	/**
	 * Returns the currently available free space on the requested storage type.
	 * <p>
	 * <b>Note</b>, that for {@link #BASE} this will always returns {@code 0}, because this
	 * identifier doesn't represents any storage.
	 *
	 * @param storage An identifier of the storage of which free space to obtain.
	 * @return The amount of bytes which are at this time available for storing of new files on the
	 * requested storage.
	 */
	long getFreeSpace(@StorageDir int storage);

	/**
	 * Returns a file representing the directory on this Android device's file system.
	 * <p>
	 * <b>Note</b>, that for {@link #BASE} this will always returns {@code null}, because this
	 * identifier doesn't represents any storage.
	 *
	 * @param storage An identifier of the storage of which directory to obtain.
	 * @return The directory for the desired storage, or {@code null} unknown storage type was
	 * requested.
	 */
	@Nullable
	File getStorage(@StorageDir int storage);

	/**
	 * Returns a path obtained from the requested storage by {@link #getStorage(int) getStorage(int).getPath()}.
	 *
	 * @return The path of the requested storage type.
	 */
	@NonNull
	String getStoragePath(@StorageDir int storage);

	/**
	 * Returns a file eventually stored on the specified <var>storage</var>.
	 * <p>
	 * <b>Note</b>, that this method only builds the path to the requested file and creates an instance
	 * of File.
	 *
	 * @param storage An identifier of the storage of which path to use as base path for the requested
	 *                file.
	 * @param path    Relative path to the desired file which is stored at storage with the specified
	 *                identifier.
	 * @return A new instance of File with the path created from the path of the given <var>storage</var>
	 * appended by the given relative file <var>path</var>.
	 */
	@NonNull
	File getFile(@StorageDir int storage, @NonNull String path);

	/**
	 * Same as {@link #getDirectoryContent(int, String)} for {@link #BASE} storage.
	 */
	@NonNull
	List<File> getDirectoryContent(@NonNull String path);

	/**
	 * Same as {@link #getDirectoryContent(int, FileFilter, FilenameFilter, String)}
	 * with {@code null} filters.
	 */
	@NonNull
	List<File> getDirectoryContent(@StorageDir int storage, @NonNull String path);

	/**
	 * Lists all files stored within a directory at the given <var>path</var> on this Android device's
	 * storage.
	 * <p>
	 * See {@link File#listFiles(FileFilter)} and {@link File#listFiles(FilenameFilter)}
	 * for additional info.
	 *
	 * @param storage    An identifier of the storage of which path to use as {@code base path} for
	 *                   the given path. Use {@link #BASE} when the given path represents full path
	 *                   to the directory of which files to list.
	 * @param filter     If not {@code null}, this filter will be used to filter the desired
	 *                   types of files.
	 * @param nameFilter If not {@code null}, this filter will be used to filter the desired
	 *                   files by their names.
	 * @param path       The path to the directory of which files to list.
	 * @return List of files listed from the requested directory, or empty list if the directory on
	 * the specified path doesn't exists or is not a directory or just none file passes through the
	 * given filters.
	 */
	@NonNull
	List<File> getDirectoryContent(@StorageDir int storage, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String path);

	/**
	 * Same as {@link #getDirectoriesContent(int, String...)} for {@link #BASE} storage.
	 */
	@NonNull
	List<File> getDirectoriesContent(@NonNull String... paths);

	/**
	 * Same as {@link #getDirectoriesContent(int, FileFilter, FilenameFilter, String...)}
	 * with {@code null} filters.
	 */
	@NonNull
	List<File> getDirectoriesContent(@StorageDir int storage, @NonNull String... paths);

	/**
	 * Lists all files stored within directories at the given <var>paths</var> on this Android
	 * device's storage.
	 * <p>
	 * This is a bulk operation for {@link #getDirectoryContent(int, FileFilter, FilenameFilter, String)}
	 * method, so the given paths will be looped to perform the requested action for each of them.
	 *
	 * @param paths Set of paths to directories of which files should be listed.
	 * @return List with all files obtained from {@link #getDirectoryContent(int, FileFilter, FilenameFilter, String)}
	 * for each of the given <var>paths</var>.
	 */
	@NonNull
	List<File> getDirectoriesContent(@StorageDir int storage, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String... paths);

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * This class represents the base structure for results which may be returned by this storage API
	 * when invoking some of its methods.
	 */
	abstract class BaseResult {

		/**
		 * Identifier of action for which was this result created. One of {@link #ACTION_CREATE},
		 * {@link #ACTION_DELETE}, {@link #ACTION_COPY} or {@link #ACTION_MOVE}.
		 */
		public final int action;

		/**
		 * Message for this result. This can be <b>info</b> message or <b>error</b> message, depends
		 * on for which purpose was this result created.
		 */
		@NonNull
		public final String message;

		/**
		 * Same flags as passed to the storage API method for which was this result created.
		 */
		public final int flags;

		/**
		 * Flag indicating whether this result was created as error or not.
		 * <p>
		 * If {@code true}, the {@link #errorCode} will point to the specific error which occurred
		 * during execution of the requested {@link #action}.
		 */
		public final boolean isError;

		/**
		 * Code specific to error which occurred during execution of the requested {@link #action}.
		 * <p>
		 * See all <b>Storage.ERROR_...</b> which can be raised by this storage API.
		 */
		public final int errorCode;

		/**
		 * Creates a new instance of BaseResult.
		 *
		 * @param action  The action for which is this result being created.
		 * @param message Message for this result.
		 * @param flags   Flags passed to the method which is represented by the given <var>action</var>.
		 * @param error   Error code if this result represents error.
		 */
		BaseResult(final int action, @NonNull final String message, final int flags, final int error) {
			this.action = action;
			this.message = message;
			this.flags = flags;
			this.errorCode = error;
			this.isError = error < 0;
		}
	}

	/**
	 * This class represents structure for result which will be returned by this storage API when
	 * invoking some of its methods which accepts only <b>single path</b> as parameter.
	 */
	final class Result extends BaseResult {

		/**
		 * The path passed to the method for which was this result created.
		 */
		@NonNull
		public final String path;

		/**
		 * Creates a new instance of Result like {@link BaseResult#BaseResult(int, String, int, int)}.
		 *
		 * @param path The single path for which is this result being created.
		 */
		Result(final int action, final String message, @NonNull final String path, final int flags, final int error) {
			super(action, message, flags, error);
			this.path = path;
		}
	}

	/**
	 * This class represents structure for result which will be returned by this storage API when
	 * invoking some of its methods which accepts set of <b>paths</b> as parameter so when requesting
	 * a bulk storage operation.
	 */
	final class Results extends BaseResult {

		/**
		 * The count determining how many operations were successfully performed for the paths passed
		 * to the bulk method for which was this result created.
		 */
		public final int succeeded;

		/**
		 * List of single results where each of them was obtained by invoking storage API method which
		 * accepts <b>single path</b> as parameter for each of the passed paths which were passed to
		 * the method for which was this result created.
		 * <p>
		 * This list of results will be always the same size as the size of provided paths.
		 */
		@NonNull
		public final List<Result> results;

		/**
		 * Creates a new instance of Results like {@link BaseResult#BaseResult(int, String, int, int)}.
		 *
		 * @param succeeded Count of successfully performed operations.
		 * @param results   List of results for each of the paths passed to the method for which is
		 *                  this results object being created.
		 */
		Results(final int action, @NonNull final String message, final int succeeded, @NonNull final List<Result> results, final int flags, final int error) {
			super(action, message, flags, error);
			this.succeeded = succeeded;
			this.results = results;
		}
	}
}

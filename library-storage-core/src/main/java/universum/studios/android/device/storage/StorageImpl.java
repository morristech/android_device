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
package universum.studios.android.device.storage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A {@link Storage} implementation.
 *
 * @author Martin Albedinsky
 */
final class StorageImpl implements Storage {

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "StorageImpl";

	/**
	 * Boolean flag indicating whether to print debug output or not.
	 */
	private static final boolean DEBUG = false;

	/**
	 * Relative path to the directory on the external storage where a directory named by the package
	 * name of the current application should be created, so this application can store there its
	 * own persistent data which will be deleted as soon as the application will be uninstalled.
	 * <p>
	 * Constant value: <b>Android/data</b>
	 */
	private static final String EXTERNAL_PACKAGE_STORAGE_PARENTS_PATH = "Android" + File.separator + "data";

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
	 * StorageImpl singleton instance.
	 */
	@SuppressLint("StaticFieldLeak")
	private static StorageImpl sInstance;

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Helper for create operations on files system.
	 */
	private final StorageAction SA_CREATE = new StorageAction.Create(this);

	/**
	 * Helper for delete operations on file system.
	 */
	private final StorageAction SA_DELETE = new StorageAction.Delete(this);

	/**
	 * Helper for copy operations on file system.
	 */
	private final StorageAction SA_COPY = new StorageAction.Copy(this);

	/**
	 * Helper for move operations on file system.
	 */
	private final StorageAction SA_MOVE = new StorageAction.Move(this);

	/**
	 * Application context obtained from the context passed during initialization of this wrapper.
	 */
	private final Context mContext;

	/**
	 * An application package name to create specific sub-directory in the external storage if it is
	 * available.
	 */
	private final String mPackageName;

	/**
	 * File representing the root directory of the external file system.
	 */
	private File mExternal;

	/**
	 * File representing the directory for the current application to store its data there. This
	 * directory will be created only if {@link #getStorage(int)} is called with {@link #EXTERNAL_PACKAGE}
	 * identifier and the external storage is at that particular time available.
	 */
	private File mExternalPackage;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of StorageImpl.
	 *
	 * @param applicationContext Application context used to access system services.
	 */
	private StorageImpl(final Context applicationContext) {
		this.mContext = applicationContext;
		this.mPackageName = applicationContext.getPackageName();
		this.checkExternalAvailability();
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Returns or creates a new singleton instance of StorageImpl.
	 *
	 * @param context Context used by the storage implementation to access system services.
	 * @return Storage implementation with actual storage data available.
	 */
	@NonNull static StorageImpl getInstance(@NonNull final Context context) {
		synchronized (LOCK) {
			if (sInstance == null) sInstance = new StorageImpl(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	 */
	@Override @NonNull public Result createFile(@NonNull final String path) {
		return createFile(BASE, path);
	}

	/**
	 */
	@Override @NonNull public Result createFile(@StorageDir final int storage, @NonNull final String path) {
		if (StorageUtils.directoryExists(appendBasePath(storage, path))) {
			return StorageAction.createResult(
					ACTION_CREATE,
					"Failed to create file. Directory with this name already exists.",
					path,
					NO_FLAGS,
					ERROR_FILE_SAME_AS_DIRECTORY
			);
		}
		return SA_CREATE.performFileAction(storage, NO_FLAGS, "", path);
	}

	/**
	 */
	@Override @NonNull public Results createFiles(@NonNull final String... paths) {
		return createFiles(BASE, paths);
	}

	/**
	 */
	@Override @NonNull public Results createFiles(@StorageDir final int storage, @NonNull final String... paths) {
		return SA_CREATE.performFilesAction(storage, NO_FLAGS, "", paths);
	}

	/**
	 */
	@Override @NonNull public Result createDirectory(@NonNull final String path) {
		return createDirectory(BASE, path);
	}

	/**
	 */
	@Override @NonNull public Result createDirectory(@StorageDir final int storage, @NonNull final String path) {
		if (StorageUtils.directoryExists(appendBasePath(storage, path))) {
			return StorageAction.createResult(
					ACTION_CREATE,
					"Failed to create file. Directory with this name already exists.",
					path,
					NO_FLAGS,
					ERROR_DIRECTORY_SAME_AS_FILE
			);
		}
		return SA_CREATE.performDirectoryAction(storage, NO_FLAGS, null, null, "", path);
	}

	/**
	 */
	@Override @NonNull public Results createDirectories(@NonNull final String... paths) {
		return createDirectories(BASE, paths);
	}

	/**
	 */
	@Override @NonNull public Results createDirectories(@StorageDir final int storage, @NonNull final String... paths) {
		return SA_CREATE.performDirectoriesAction(storage, NO_FLAGS, null, null, "", paths);
	}

	/**
	 */
	@Override @NonNull public Result deleteFile(@NonNull final String path) {
		return deleteFile(BASE, path);
	}

	/**
	 */
	@Override @NonNull public Result deleteFile(@StorageDir final int storage, @NonNull final String path) {
		return SA_DELETE.performFileAction(storage, NO_FLAGS, "", path);
	}

	/**
	 */
	@Override @NonNull public Results deleteFiles(@NonNull final String... paths) {
		return deleteFiles(BASE, paths);
	}

	/**
	 */
	@Override @NonNull public Results deleteFiles(@StorageDir final int storage, @NonNull final String... paths) {
		return SA_DELETE.performFilesAction(storage, NO_FLAGS, "", paths);
	}

	/**
	 */
	@Override @NonNull public Result deleteDirectory(@NonNull final String path) {
		return deleteDirectory(BASE, path);
	}

	/**
	 */
	@Override @NonNull public Result deleteDirectory(@StorageDir final int storage, @NonNull final String path) {
		return deleteDirectory(storage, null, null, path);
	}

	/**
	 */
	@Override @NonNull public Result deleteDirectory(@StorageDir final int storage, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @NonNull final String path) {
		return SA_DELETE.performDirectoryAction(storage, NO_FLAGS, filter, nameFilter, "", path);
	}

	/**
	 */
	@Override @NonNull public Results deleteDirectories(@NonNull final String... paths) {
		return deleteDirectories(BASE, paths);
	}

	/**
	 */
	@Override @NonNull public Results deleteDirectories(@StorageDir final int storage, @NonNull final String... paths) {
		return deleteDirectories(storage, null, null, paths);
	}

	/**
	 */
	@Override @NonNull public Results deleteDirectories(@StorageDir final int storage, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @NonNull final String... paths) {
		return SA_DELETE.performDirectoriesAction(storage, NO_FLAGS, filter, nameFilter, "", paths);
	}

	/**
	 */
	@Override @NonNull public Result deleteFileOrDirectory(@NonNull final String path) {
		return deleteFileOrDirectory(BASE, path);
	}

	/**
	 */
	@Override @NonNull public Result deleteFileOrDirectory(@StorageDir final int storage, @NonNull final String path) {
		return deleteFileOrDirectory(storage, null, null, path);
	}

	/**
	 */
	@Override @NonNull public Result deleteFileOrDirectory(@StorageDir final int storage, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @NonNull final String path) {
		return SA_DELETE.performFileOrDirectoryAction(storage, NO_FLAGS, filter, nameFilter, "", path);
	}

	/**
	 */
	@Override @NonNull public Results deleteFilesOrDirectories(@NonNull final String... paths) {
		return deleteFilesOrDirectories(BASE, paths);
	}

	/**
	 */
	@Override @NonNull public Results deleteFilesOrDirectories(@StorageDir final int storage, @NonNull final String... paths) {
		return deleteFilesOrDirectories(storage, null, null, paths);
	}

	/**
	 */
	@Override @NonNull public Results deleteFilesOrDirectories(@StorageDir final int storage, @Nullable final FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String... paths) {
		return SA_DELETE.performFilesOrDirectoriesAction(storage, NO_FLAGS, filter, nameFilter, "", paths);
	}

	/**
	 */
	@Override @NonNull public Result copyFile(final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return copyFile(BASE, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result copyFile(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return SA_COPY.performFileAction(storage, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Results copyFiles(@Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return copyFiles(BASE, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results copyFiles(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return SA_DELETE.performFilesAction(storage, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Result copyDirectory(@Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return copyDirectory(BASE, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result copyDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return copyDirectory(storage, flags, null, null, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result copyDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String fromPath) {
		return SA_DELETE.performDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Results copyDirectories(@Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return copyDirectories(BASE, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results copyDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return copyDirectories(storage, flags, null, null, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results copyDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return SA_DELETE.performDirectoriesAction(storage, flags, filter, nameFilter, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Result copyFileOrDirectory(@Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return copyFileOrDirectory(BASE, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result copyFileOrDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return copyFileOrDirectory(storage, flags, null, null, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result copyFileOrDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String fromPath) {
		return SA_COPY.performFileOrDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Results copyFilesOrDirectories(@Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return copyFilesOrDirectories(BASE, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results copyFilesOrDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return copyFilesOrDirectories(storage, flags, null, null, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results copyFilesOrDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return SA_COPY.performFilesOrDirectoriesAction(storage, flags, filter, nameFilter, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Result moveFile(@Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return moveFile(BASE, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result moveFile(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return SA_MOVE.performFileAction(storage, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Results moveFiles(@Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return moveFiles(BASE, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results moveFiles(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return SA_MOVE.performFilesAction(storage, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Result moveDirectory(@Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return moveDirectory(BASE, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result moveDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return moveDirectory(storage, flags, null, null, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result moveDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String fromPath) {
		return SA_MOVE.performDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Results moveDirectories(@Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return moveDirectories(BASE, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results moveDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return moveDirectories(storage, flags, null, null, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results moveDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return SA_MOVE.performDirectoriesAction(storage, flags, filter, nameFilter, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Result moveFileOrDirectory(@Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return moveFileOrDirectory(BASE, flags, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result moveFileOrDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String fromPath) {
		return moveFileOrDirectory(storage, flags, null, null, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Result moveFileOrDirectory(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String fromPath) {
		return SA_MOVE.performFileOrDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
	}

	/**
	 */
	@Override @NonNull public Results moveFilesOrDirectories(@Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return moveFilesOrDirectories(BASE, flags, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results moveFilesOrDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return moveFilesOrDirectories(storage, flags, null, null, toPath, fromPaths);
	}

	/**
	 */
	@Override @NonNull public Results moveFilesOrDirectories(@StorageDir final int storage, @Flags final int flags, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @Nullable final String toPath, @NonNull final String... fromPaths) {
		return SA_MOVE.performFilesOrDirectoriesAction(storage, flags, filter, nameFilter, toPath, fromPaths);
	}

	/**
	 */
	@Override public boolean changeFilePermissions(@NonNull final String path, @FilePermissions final int permissions, final boolean ownerOnly) {
		return changeFilePermissions(BASE, path, permissions, ownerOnly);
	}

	/**
	 */
	@Override public boolean changeFilePermissions(@StorageDir final int storage, @NonNull final String path, @FilePermissions final int permissions, final boolean ownerOnly) {
		final File file = this.newFile(storage, path);
		if (file.isFile() && permissions > 0) {
			if (file.setExecutable((permissions & PERMISSION_EXECUTE) != 0, ownerOnly) &&
					file.setWritable((permissions & PERMISSION_WRITE) != 0, ownerOnly) &&
					file.setReadable((permissions & PERMISSION_READ) != 0, ownerOnly)) {
				return true;
			}
			Log.e(TAG, "Failed to change read/write/execute permissions(" + permissions + ") of file('" + file.getPath() + "').");
		}
		return false;
	}

	/**
	 */
	@Override public boolean hasFreeSpace(@StorageDir final int storage, final long bytes) {
		final File storageFile = getStorage(storage);
		return storageFile != null && storageFile.getFreeSpace() >= bytes;
	}

	/**
	 */
	@Override public long getFreeSpace(@StorageDir final int storage) {
		final File storageFile = getStorage(storage);
		return storageFile == null ? 0 : storageFile.getFreeSpace();
	}

	/**
	 */
	@Override public boolean isExternalReadOnly() {
		return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
	}

	/**
	 */
	@Override public boolean isExternalMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	/**
	 */
	@Override public boolean isExternalAvailable() {
		return checkExternalAvailability();
	}

	/**
	 * Checks whether the external storage is available or not. See {@link Environment#getExternalStorageState()}
	 * for more info.
	 * <p>
	 * <b>Note</b>, that in case that the external storage is available, this will also initialize file
	 * for external dir and also, create sub-directory in the Android/data/ directory for the
	 * current application package name.
	 *
	 * @return {@code True} if external storage is <b>MOUNTED</b> or <b>MOUNTED_READ_ONLY</b>.
	 */
	private boolean checkExternalAvailability() {
		String status;
		boolean available;
		final String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			if (mExternal == null) {
				this.mExternal = Environment.getExternalStorageDirectory();
			}
			status = "available";
			available = true;
		} else {
			this.mExternal = null;
			this.mExternalPackage = null;
			status = "not available";
			available = false;
		}
		this.logMessage("External storage is " + status + ".", false);
		return available;
	}

	/**
	 */
	@Override @NonNull public String getStoragePath(@StorageDir final int storage) {
		if (storage == BASE) {
			return "";
		}
		final File storageFile = getStorage(storage);
		return storageFile == null ? "" : storageFile.getPath();
	}

	/**
	 */
	@Override @Nullable public File getStorage(@StorageDir final int storage) {
		switch (storage) {
			case INTERNAL:
				return mContext.getFilesDir();
			case EXTERNAL:
				if (checkExternalAvailability()) return mExternal;
				else return null;
			case EXTERNAL_PACKAGE:
				if (checkExternalAvailability()) {
					this.ensureExternalPackageDir();
					return mExternalPackage;
				}
				return null;
			case ROOT:
				return Environment.getRootDirectory();
			case DATA:
				return Environment.getDataDirectory();
			case CACHE:
				return mContext.getCacheDir();
			case BASE:
			default:
				return null;
		}
	}

	/**
	 * Ensures that a directory on the external storage for the current application <var>mPackageName</var>
	 * is created if does not exist yet.
	 * <p>
	 * Directory will be created with the path: {@link #EXTERNAL_PACKAGE_STORAGE_PARENTS_PATH} + mPackageName,
	 * so for example for application with package name {@code com.google.docs}, this will create
	 * a directory with path {@code Android/data/com.google.docs/}.
	 *
	 * @return {@code True} if the directory has been successfully created or already exists,
	 * {@code false} if the external storage is not available at this time or permission to write
	 * external directory has not been granted yet.
	 */
	private boolean ensureExternalPackageDir() {
		if (mExternal == null) return false;
		if (mExternalPackage != null && mExternalPackage.isDirectory()) return true;
		if (mExternalPackage == null) {
			// Build the path to the external storage directory specific for the package name of
			// this application.
			this.mExternalPackage = new File(mExternal.getPath() +
					File.separator +
					EXTERNAL_PACKAGE_STORAGE_PARENTS_PATH +
					File.separator +
					mPackageName
			);
		}
		// Check whether we have permission to write on external storage.
		if (mContext.checkPermission(
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Process.myPid(),
				Process.myUid()
		) != PackageManager.PERMISSION_GRANTED) {
			return false;
		}
		// Create if not exists.
		if (mExternalPackage.isDirectory() || mExternalPackage.mkdirs()) {
			return true;
		}
		this.logMessage("Failed to create external directory(" + mExternalPackage.getPath() + ") for package(" + mPackageName + ").", true);
		return false;
	}

	/**
	 */
	@Override @NonNull public File getFile(@StorageDir final int storage, @NonNull final String path) {
		return this.newFile(storage, path);
	}

	/**
	 */
	@Override @NonNull public List<File> getDirectoryContent(@NonNull final String path) {
		return getDirectoryContent(BASE, path);
	}

	/**
	 */
	@Override @NonNull public List<File> getDirectoryContent(@StorageDir final int storage, @NonNull final String path) {
		return getDirectoryContent(storage, null, null, path);
	}

	/**
	 */
	@SuppressWarnings("unchecked")
	@Override @NonNull public List<File> getDirectoryContent(@StorageDir final int storage, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @NonNull final String path) {
		final File dir = this.newFile(storage, path);
		if (dir.isDirectory()) {
			if (filter == null) {
				final File[] files = dir.listFiles(nameFilter);
				return isArrayEmpty(files) ? Collections.EMPTY_LIST : Arrays.asList(files);
			} else {
				// First, filter by file.
				final File[] filteredFiles = dir.listFiles(filter);
				if (isArrayEmpty(filteredFiles)) {
					return Collections.EMPTY_LIST;
				}
				if (nameFilter != null) {
					final List<File> files = new ArrayList<>();
					// Now filter by name.
					for (final File file : filteredFiles) {
						if (nameFilter.accept(file, file.getName())) {
							files.add(file);
						}
					}
					return files;
				}
				return Arrays.asList(filteredFiles);
			}
		} else {
			this.logMessage("Cannot obtain content of directory. Directory(" + dir.getPath() + ") does not exist or it is not a directory.", true);
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * Creates a new file with the path created from the given relative <var>path</var> and path
	 * of the specified <var>storage</var> as base path.
	 *
	 * @param storage Type of storage of which path should be used as base for the requested file's
	 *                path.
	 * @param path    Relative path to be appended to the path of resolved <var>storage</var> type.
	 * @return An instance of file the the requested path.
	 */
	private File newFile(final int storage, final String path) {
		return new File(buildPath(storage, path));
	}

	/**
	 * Builds the full path from the given path and the path of the given <var>storage</var> type.
	 *
	 * @param storage Type of storage of which path should be used as base for the requested path.
	 * @param path    Relative path to be appended to the path of resolved <var>storage</var> type.
	 * @return Full path.
	 */
	private String buildPath(final int storage, final String path) {
		return getStoragePath(storage) + File.separator + path;
	}

	/**
	 * Checks whether the given array is empty or not.
	 *
	 * @param array Array to check.
	 * @return {@code True} if the given array is {@code null} or if it doesn't have any
	 * items within it, {@code false} otherwise.
	 */
	private boolean isArrayEmpty(final Object[] array) {
		return array == null || array.length == 0;
	}

	/**
	 */
	@Override @NonNull public List<File> getDirectoriesContent(@NonNull final String... paths) {
		return getDirectoriesContent(BASE, paths);
	}

	/**
	 */
	@Override @NonNull public List<File> getDirectoriesContent(@StorageDir final int storage, @NonNull final String... paths) {
		return getDirectoriesContent(storage, null, null, paths);
	}

	/**
	 */
	@Override @NonNull public List<File> getDirectoriesContent(@StorageDir final int storage, @Nullable final FileFilter filter, @Nullable final FilenameFilter nameFilter, @NonNull final String... paths) {
		final List<File> files = new ArrayList<>();
		if (paths.length > 0) {
			for (final String path : paths) {
				files.addAll(getDirectoryContent(storage, filter, nameFilter, path));
			}
			return files;
		}
		return files;
	}

	/**
	 * Returns a name of storage type represented by the specified <var>storage</var> identifier.
	 *
	 * @param storage An identifier of the desired storage of which name is requested.
	 * @return Storage name or {@code ""} if unknown type or {@link #BASE} was requested.
	 */
	static String getStorageName(final int storage) {
		switch (storage) {
			case INTERNAL:
				return "INTERNAL";
			case EXTERNAL:
				return "EXTERNAL";
			case EXTERNAL_PACKAGE:
				return "EXTERNAL_PACKAGE";
			case ROOT:
				return "ROOT";
			case DATA:
				return "DATA";
			case CACHE:
				return "CACHE";
			default:
				return "";
		}
	}

	/**
	 * Appends the path obtained by {@link #getStoragePath(int)} by the given <var>path</var>.
	 *
	 * @param storage An identifier of the desired storage of which path to use as base path.
	 * @param path    The desired path which should be appended to the base path.
	 * @return Appended base path of the specified storage with the given path or just the given
	 * <var>path</var> if the path of the specified storage is empty.
	 */
	String appendBasePath(final int storage, final String path) {
		final String basePath = getStoragePath(storage);
		return TextUtils.isEmpty(basePath) ? path : basePath + File.separator + path;
	}

	/**
	 * Logs the given message using {@link Log} util.
	 *
	 * @param message Message to log.
	 * @param error   {@code True} to log message as error, {@code false}otherwise.
	 */
	private void logMessage(final String message, final boolean error) {
		if (error) Log.e(TAG, message);
		else if (DEBUG) Log.d(TAG, message);
	}

	/*
	 * Inner classes ===============================================================================
	 */
}
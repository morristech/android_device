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
package universum.studios.android.device.storage;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Simple utility class specifying API allowing to create, copy, move or delete files on the current
 * Android device's file system.
 * <p>
 * This class wraps instance of {@link universum.studios.android.device.storage.StorageEditor} for
 * all file content related operations, like <b>deleting, copying and moving</b> of file/directory
 * content on the file system. <b>Note</b>, that all exceptions thrown by the editor need to be handled
 * by a caller of the StorageUtils API itself.
 *
 * @author Martin Albedinsky
 */
public final class StorageUtils {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "StorageUtils";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Lock object used for synchronized operations.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Instance of StorageEditor to be used fare editable actions performed with files, like copying
	 * or moving data between files or directories.
	 */
	private static final StorageEditor EDITOR = new StorageEditor();

	/**
	 * Constructors ================================================================================
	 */

	/**
	 */
	private StorageUtils() {
		// Creation of instances of this class is not publicly allowed.
	}

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Checks whether a file at the specified <var>path</var> on the file system exists or not.
	 *
	 * @param path Path of the desired file to check.
	 * @return {@code True} if the given path points to file which at this time exists,
	 * {@code false} if there is no file at the given path or it is a directory.
	 */
	public static boolean fileExists(@NonNull String path) {
		return new File(path).isFile();
	}

	/**
	 * Checks whether a directory at the specified <var>path</var> on the file system exists or not.
	 *
	 * @param path Path of the desired directory to check.
	 * @return {@code True} if the given path points to directory which at this time exists,
	 * {@code false} if there is no directory at the given path or it is a file.
	 */
	public static boolean directoryExists(@NonNull String path) {
		return new File(path).isDirectory();
	}

	/**
	 * Checks whether a directory at the specified <var>path</var> contains any files/subdirectories
	 * or not.
	 *
	 * @param path Path of the desired directory to check.
	 * @return {@code True} if the given path points to directory which at this time exists and
	 * doesn't contains any files/subdirectories, {@code false} if it doesn't exists or if it is
	 * a file.
	 */
	public static boolean isDirectoryEmpty(@NonNull String path) {
		final File file = new File(path);
		return file.isDirectory() && file.list().length == 0;
	}

	/**
	 * Creates a new empty File at the specified <var>path</var> on the file system.
	 * <p>
	 * See {@link File#createNewFile()} for additional info.
	 *
	 * @param path Path of the desired file to be created.
	 * @return {@code True} if the requested file was successfully created or already exists,
	 * {@code false} if at the specified path already exists a directory with the same path
	 * or some IO error occurs.
	 */
	public static boolean createFile(@NonNull String path) {
		final File file = new File(path);
		synchronized (LOCK) {
			if (!file.exists()) {
				// First check if all parent directories exist.
				final Uri fileUri = Uri.parse(path);
				final File parentDirs = new File(fileUri.getPath().replace(fileUri.getLastPathSegment(), ""));
				if ((!parentDirs.exists() || !parentDirs.isDirectory()) && !parentDirs.mkdirs()) {
					Log.e(TAG, "Failed to create parent directories for file('" + path + "').");
					return false;
				}
				try {
					return file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
			return !file.isDirectory();
		}
	}

	/**
	 * Same as {@link #createFiles(String, String...)} with empty <var>basePath</var>.
	 */
	public static int createFiles(@NonNull String... paths) {
		return createFiles("", paths);
	}

	/**
	 * Creates all files at the given <var>paths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #createFile(String)} method, so the given paths will be
	 * looped to perform the requested action for each of them.
	 *
	 * @param basePath A path to be used as base for the given paths, in case when they represents
	 *                 a relative paths.
	 * @param paths    Paths of the desired files to be created.
	 * @return Count of the successfully created files.
	 */
	public static int createFiles(@Nullable String basePath, @NonNull String... paths) {
		if (paths.length > 0) {
			int count = 0;
			for (final String path : paths) {
				if (createFile(basePath + path)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Creates a new empty directory at the specified <var>path</var> on the file system.
	 * <p>
	 * See {@link File#mkdirs()} for additional info.
	 *
	 * @param path Path of the desired directory to be created.
	 * @return {@code True} if the requested directory was successfully created or already exists,
	 * {@code false} if at the specified path already exists a file with the same path or some
	 * IO error occurs.
	 */
	public static boolean createDirectory(@NonNull String path) {
		final File file = new File(path);
		synchronized (LOCK) {
			return file.isDirectory() || file.mkdirs();
		}
	}

	/**
	 * Same as {@link #createDirectories(String, String...)} with empty <var>basePath</var>.
	 */
	public static int createDirectories(@NonNull String... paths) {
		return createDirectories("", paths);
	}

	/**
	 * Creates all directories at the given <var>paths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #createDirectory(String)} method, so the given paths will
	 * be looped to perform the requested action for each of them.
	 *
	 * @param basePath A path to be used as base for the given paths, in case when they represents
	 *                 a relative paths.
	 * @param paths    Paths of the desired directories to be created.
	 * @return Count of the successfully created directories.
	 */
	public static int createDirectories(@Nullable String basePath, @NonNull String... paths) {
		if (paths.length > 0) {
			int count = 0;
			for (final String path : paths) {
				if (createDirectory(basePath + path)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Deletes a file at the specified <var>path</var> on the file system.
	 *
	 * @param path Path to the desired file to be deleted.
	 * @return {@code True} if the requested file was successfully deleted, {@code false}
	 * if it is actually a directory or it doesn't exists.
	 */
	public static boolean deleteFile(@NonNull String path) {
		final File file = new File(path);
		synchronized (LOCK) {
			return file.isFile() && file.delete();
		}
	}

	/**
	 * Same as {@link #deleteFiles(String, String...)} with empty <var>basePath</var>.
	 */
	public static int deleteFiles(@NonNull String... paths) {
		return deleteFiles("", paths);
	}

	/**
	 * Deletes all files at the given <var>paths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #deleteFiles(String...)} method, so the given paths will
	 * be looped to perform the requested action for each of them.
	 *
	 * @param basePath A path to be used as base for the given paths, in case when they represents
	 *                 a relative paths.
	 * @param paths    Paths to the desired files to be deleted.
	 * @return Count of the successfully deleted files.
	 */
	public static int deleteFiles(@Nullable String basePath, @NonNull String... paths) {
		if (paths.length > 0) {
			int count = 0;
			for (final String path : paths) {
				if (deleteFile(basePath + path)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Same as {@link #deleteDirectory(FileFilter, FilenameFilter, String)} with {@code null} filters.
	 */
	public static boolean deleteDirectory(@NonNull String path) {
		return deleteDirectory(null, null, path);
	}

	/**
	 * Wrapped {@link universum.studios.android.device.storage.StorageEditor#deleteDirectory(File, FileFilter, FilenameFilter)}
	 * upon this utils editor instance.
	 *
	 * @param path The path to be used to create <var>directory</var> parameter for editor.
	 */
	public static boolean deleteDirectory(@Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @NonNull String path) {
		return EDITOR.deleteDirectory(new File(path), filter, nameFilter);
	}

	/**
	 * Same as {@link #deleteDirectories(String, String...)} with empty <var>basePath</var>.
	 */
	public static int deleteDirectories(@NonNull String... paths) {
		return deleteDirectories("", paths);
	}

	/**
	 * Same as {@link #deleteDirectories(FileFilter, FilenameFilter, String, String...)}
	 * with {@code null} filters.
	 */
	public static int deleteDirectories(@Nullable String basePath, @NonNull String... paths) {
		return deleteDirectories(null, null, basePath, paths);
	}

	/**
	 * Deletes all directories at the given <var>paths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #deleteDirectory(FileFilter, FilenameFilter, String)}
	 * method, so the given paths will be looped to perform the requested action for each of them.
	 *
	 * @param basePath A path to be used as base for the given paths, in case when they represents
	 *                 a relative paths.
	 * @param paths    Paths to the desired directories to be deleted.
	 * @return Count of the successfully deleted directories.
	 */
	public static int deleteDirectories(@Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String basePath, @NonNull String... paths) {
		if (paths.length > 0) {
			int count = 0;
			for (final String path : paths) {
				if (deleteDirectory(filter, nameFilter, basePath + path)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Wrapped {@link universum.studios.android.device.storage.StorageEditor#copyFileContent(int, File, File)}
	 * upon this utils editor instance.
	 * <p>
	 * <b>Note</b>, that the given <var>toPath</var> will be appended with a last path segment obtained
	 * from the given <var>fromPath</var>.
	 *
	 * @param toPath   The path to be used to create <var>fromFile</var> parameter for editor.
	 * @param fromPath The path to be used to create <var>toFile</var> parameter for editor.
	 */
	public static boolean copyFile(int flags, @Nullable String toPath, @NonNull String fromPath) throws IOException {
		return EDITOR.copyFileContent(flags, new File(fromPath), new File(appendDestinationPathWithFileName(toPath, fromPath)));
	}

	/**
	 * Copies all files at the given <var>fromPaths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #copyFile(int, String, String)} method, so the given paths
	 * will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Paths to the desired files to be copied.
	 * @return Count of the successfully copied files.
	 */
	public static int copyFiles(int flags, @Nullable String toPath, @NonNull String... fromPaths) throws IOException {
		if (fromPaths.length > 0) {
			int count = 0;
			for (final String fromPath : fromPaths) {
				if (copyFile(flags, toPath, fromPath)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Same as {@link #copyDirectory(int, FileFilter, FilenameFilter, String, String)}
	 * with {@code null} filters.
	 */
	public static boolean copyDirectory(int flags, @Nullable String toPath, @NonNull String fromPath) throws IOException {
		return copyDirectory(flags, null, null, toPath, fromPath);
	}

	/**
	 * Wrapped {@link universum.studios.android.device.storage.StorageEditor#copyDirectoryContent(int, File, File, FileFilter, FilenameFilter)}
	 * upon this utils editor instance.
	 * <p>
	 * <b>Note</b>, that the given <var>toPath</var> will be appended with a last path segment obtained
	 * from the given <var>fromPath</var>.
	 *
	 * @param toPath   The path to be used to create <var>fromDirectory</var> parameter for editor.
	 * @param fromPath The path to be used to create <var>toDirectory</var> parameter for editor.
	 */
	public static boolean copyDirectory(int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String fromPath) throws IOException {
		return EDITOR.copyDirectoryContent(flags, new File(fromPath), new File(appendDestinationPathWithFileName(toPath, fromPath)), filter, nameFilter);
	}

	/**
	 * Same as {@link #copyDirectories(int, FileFilter, FilenameFilter, String, String...)}
	 * with {@code null} filters.
	 */
	public static int copyDirectories(int flags, @Nullable String toPath, @NonNull String... fromPaths) throws IOException {
		return copyDirectories(flags, null, null, toPath, fromPaths);
	}

	/**
	 * Copies all directories at the given <var>fromPaths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #copyDirectory(int, FileFilter, FilenameFilter, String, String)}
	 * method, so the given paths will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Paths to the desired directories to be copied.
	 * @return Count of the successfully copied directories.
	 */
	public static int copyDirectories(int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String... fromPaths) throws IOException {
		if (fromPaths.length > 0) {
			int count = 0;
			for (final String fromPath : fromPaths) {
				if (copyDirectory(flags, filter, nameFilter, toPath, fromPath)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Wrapped {@link universum.studios.android.device.storage.StorageEditor#moveFileContent(int, File, File)}
	 * upon this utils editor instance.
	 * <p>
	 * <b>Note</b>, that the given <var>toPath</var> will be appended with a last path segment obtained
	 * from the given <var>fromPath</var>.
	 *
	 * @param toPath   The path to be used to create <var>fromFile</var> parameter for editor.
	 * @param fromPath The path to be used to create <var>toFile</var> parameter for editor.
	 */
	public static boolean moveFile(int flags, @Nullable String toPath, @NonNull String fromPath) throws IOException {
		return EDITOR.moveFileContent(flags, new File(fromPath), new File(appendDestinationPathWithFileName(toPath, fromPath)));
	}

	/**
	 * Moves all files at the given <var>fromPaths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #moveFile(int, String, String)} method, so the given paths
	 * will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Paths to the desired files to be moved.
	 * @return Count of the successfully moved files.
	 */
	public static int moveFiles(int flags, @Nullable String toPath, @NonNull String... fromPaths) throws IOException {
		if (fromPaths.length > 0) {
			int count = 0;
			for (final String fromPath : fromPaths) {
				if (copyFile(flags, toPath, fromPath)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Same as {@link #moveDirectory(int, FileFilter, FilenameFilter, String, String)}
	 * with {@code null} filters.
	 */
	public static boolean moveDirectory(int flags, @Nullable String toPath, @NonNull String fromPath) throws IOException {
		return moveDirectory(flags, null, null, toPath, fromPath);
	}

	/**
	 * Wrapped {@link universum.studios.android.device.storage.StorageEditor#moveDirectoryContent(int, File, File, FileFilter, FilenameFilter)}
	 * upon this utils editor instance.
	 * <p>
	 * <b>Note</b>, that the given <var>toPath</var> will be appended with a last path segment obtained
	 * from the given <var>fromPath</var>.
	 *
	 * @param toPath   The path to be used to create <var>fromDirectory</var> parameter for editor.
	 * @param fromPath The path to be used to create <var>toDirectory</var> parameter for editor.
	 */
	public static boolean moveDirectory(int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String fromPath) throws IOException {
		return EDITOR.moveDirectoryContent(flags, new File(fromPath), new File(appendDestinationPathWithFileName(toPath, fromPath)), filter, nameFilter);
	}

	/**
	 * Same as {@link #moveDirectories(int, FileFilter, FilenameFilter, String, String...)}
	 * with {@code null} filters.
	 */
	public static int moveDirectories(int flags, @Nullable String toPath, @NonNull String... fromPaths) throws IOException {
		return moveDirectories(flags, null, null, toPath, fromPaths);
	}

	/**
	 * Moves all directories at the given <var>fromPaths</var> on the file system.
	 * <p>
	 * This is a bulk operation for {@link #moveDirectory(int, FileFilter, FilenameFilter, String, String)}
	 * method, so the given paths will be looped to perform the requested action for each of them.
	 *
	 * @param fromPaths Paths to the desired directories to be moved.
	 * @return Count of the successfully moved directories.
	 */
	public static int moveDirectories(int flags, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter, @Nullable String toPath, @NonNull String... fromPaths) throws IOException {
		if (fromPaths.length > 0) {
			int count = 0;
			for (final String fromPath : fromPaths) {
				if (moveDirectory(flags, filter, nameFilter, toPath, fromPath)) count++;
			}
			return count;
		}
		return 0;
	}

	/**
	 * Appends the given <var>destinationPath</var> with the last segment of the given <var>filePath</var>.
	 *
	 * @param destinationPath The desired path to append.
	 * @param filePath        The path to be used to extract file name which will be appended to the
	 *                        given <var>destinationPath</var>.
	 * @return Appended path with extracted file name or just same destination path if the given
	 * <var>filePath</var> doesn't contain last segment.
	 */
	private static String appendDestinationPathWithFileName(String destinationPath, String filePath) {
		if (TextUtils.isEmpty(destinationPath)) {
			return "";
		}
		final String lastPathSegment = Uri.parse(filePath).getLastPathSegment();
		return TextUtils.isEmpty(lastPathSegment) ? destinationPath : destinationPath + File.separator + lastPathSegment;
	}
}

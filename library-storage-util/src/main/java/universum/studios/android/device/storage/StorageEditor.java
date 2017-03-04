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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The StorageEditor is a helper class used by {@link StorageUtils} to perform {@link File Files}
 * related operations.
 *
 * @author Martin Albedinsky
 */
public class StorageEditor {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	private static final String TAG = "StorageEditor";

	/**
	 * Flags ---------------------------------------------------------------------------------------
	 */

	/**
	 * Flag used to indicate that default options for storage operation should be used.
	 */
	public static final int DEFAULT = 0x00000000;

	/**
	 * Flag used to indicate that file/directory can be overwritten.
	 */
	public static final int OVERWRITE = 0x000000001;

	/**
	 * Flag used to indicate that copy of file/directory should be created with {@link #COPY_SUFFIX}.
	 */
	public static final int COPY = 0x000000001 << 1;

	/**
	 * ---------------------------------------------------------------------------------------------
	 */

	/**
	 * Small buffer type for {@link #createBuffer(int)} method.
	 * <p>
	 * Size of buffer: <b>512 bytes</b>
	 */
	public static final int SMALL_BUFFER = 0x01;

	/**
	 * Medium buffer type for {@link #createBuffer(int)} method.
	 * <p>
	 * Size of buffer: <b>1024 bytes</b>
	 */
	public static final int MEDIUM_BUFFER = 0x02;

	/**
	 * Large buffer type for {@link #createBuffer(int)} method.
	 * <p>
	 * Size of buffer: <b>2048 bytes</b>
	 */
	public static final int LARGE_BUFFER = 0x04;

	/**
	 * X-Large buffer type for {@link #createBuffer(int)} method.
	 * <p>
	 * Size of buffer: <b>4096 bytes</b>
	 */
	public static final int XLARGE_BUFFER = 0x08;

	/**
	 * XX-Large buffer type for {@link #createBuffer(int)} method.
	 * <p>
	 * Size of buffer: <b>8192 bytes</b>
	 */
	public static final int XXLARGE_BUFFER = 0x10;

	/**
	 * Suffix for name of file/directory when creating a copy of it passing {@link #COPY} flag to some
	 * of this editor's methods accepting <b>flags</b> parameter.
	 */
	public static final String COPY_SUFFIX = "-Copy";

	/**
	 * Base size for bytes buffer.
	 */
	private static final int BASE_BUFFER_SIZE = 512;

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Lock object used for synchronized operations.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Matcher to match file name and extract its type.
	 */
	private final Matcher mFileNameMatcher = Pattern.compile("^(.*)\\.(.+)$").matcher("");

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Constructors ================================================================================
	 */

	/**
	 * Methods =====================================================================================
	 */

	/**
	 * Creates a new <b>bytes</b> buffer for the specified <var>bufferType</var>.
	 *
	 * @param bufferType Type of buffer to create. One of {@link #SMALL_BUFFER}, {@link #MEDIUM_BUFFER},
	 *                   {@link #LARGE_BUFFER}, {@link #XLARGE_BUFFER}, {@link #XXLARGE_BUFFER}.
	 * @return New bytes buffer with size of the requested type or size of <b>1024</b> if there is no
	 * such a buffer type.
	 */
	@NonNull
	public static byte[] createBuffer(int bufferType) {
		switch (bufferType) {
			case SMALL_BUFFER:
			case MEDIUM_BUFFER:
			case LARGE_BUFFER:
			case XLARGE_BUFFER:
			case XXLARGE_BUFFER:
				return new byte[bufferType * BASE_BUFFER_SIZE];
			default:
				return new byte[1024];
		}
	}

	/**
	 * Same as {@link #deleteDirectory(File, FileFilter, FilenameFilter)}
	 * with {@code null} filters.
	 */
	public boolean deleteDirectory(@NonNull File directory) {
		return deleteDirectory(directory, null, null);
	}

	/**
	 * Deletes the given directory and its content (all files + subdirectories) on the file system.
	 *
	 * @param directory  The desired directory to delete.
	 * @param filter     The filter used to delete only desired files form the requested directory.
	 *                   Pass {@code null} to delete all files.
	 * @param nameFilter The filter used to delete only files with desired names form the requested
	 *                   directory. Pass {@code null} to delete all files.
	 * @return {@code True} if directory was successfully deleted, {@code false} otherwise.
	 */
	public boolean deleteDirectory(@NonNull File directory, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter) {
		synchronized (LOCK) {
			return directory.isDirectory() && this.deleteDirectoryContentInner(directory, filter, nameFilter);
		}
	}

	/**
	 * Deletes the given directory and its content on the file system.
	 *
	 * @param directory  The desired directory to delete.
	 * @param filter     The filter used to delete only desired files form the requested directory.
	 * @param nameFilter The filter used to delete only files with desired names form the requested
	 *                   directory.
	 * @return {@code True} if content and the given directory was successfully deleted,
	 * {@code false} if some error occurs during delete process.
	 */
	private boolean deleteDirectoryContentInner(File directory, FileFilter filter, FilenameFilter nameFilter) {
		final File[] files = directory.listFiles();
		boolean isDir;
		boolean failed = false;
		if (files.length > 0) {
			for (final File file : files) {
				// Apply filters.
				if ((filter != null && !filter.accept(file)) || (nameFilter != null && !nameFilter.accept(file, file.getName()))) {
					continue;
				}
				// File passed through filters, resolve how to delete it.
				isDir = file.isDirectory();
				if (isDir && !deleteDirectoryContentInner(file, filter, nameFilter)) {
					logError("Failed to delete directory('" + file.getName() + "').");
					failed = true;
				} else if (!isDir && !file.delete()) {
					logError("Failed to delete file('" + file.getName() + "').");
					failed = true;
				}
			}
		}
		if (directory.listFiles().length == 0) {
			// Apply filters.
			if ((filter == null && nameFilter == null) || (filter != null && filter.accept(directory)) || (nameFilter != null && nameFilter.accept(directory, directory.getName()))) {
				if (!directory.delete()) {
					failed = true;
				}
			}
		}
		return !failed;
	}

	/**
	 * Same as {@link #copyFileContent(int, File, File)} with {@link #COPY} flag and
	 * {@code null} <var>toFile</var> parameter, so there will be created new copy of the given
	 * <var>file</var> at the same path with {@link #COPY_SUFFIX}.
	 */
	public boolean copyFileContent(@NonNull File file) throws IOException {
		return copyFileContent(COPY, file, null);
	}

	/**
	 * Copies a content from the given <var>fromFile</var> into the given <var>toFile</var>.
	 *
	 * @param flags    The flags used manage move process. See {@link #OVERWRITE}, {@link #COPY}.
	 * @param fromFile The desired file of which content to copy.
	 * @param toFile   The desired file to which should be content of the requested file copied.
	 * @return {@code True} if file was successfully copied, {@code false} if some error
	 * occurs during copy process or the given flags were not properly specified.
	 * @throws FileNotFoundException    If the given <var>fromFile</var> doesn't exists or is not a file.
	 * @throws IllegalArgumentException If the given <var>toFile</var> is not a file but a directory.
	 * @throws IOException              If some IO error occurs during copy process of the requested file.
	 */
	public boolean copyFileContent(int flags, @NonNull File fromFile, @Nullable File toFile) throws IOException {
		if (!fromFile.isFile()) {
			throw new FileNotFoundException(
					"Cannot copy content of file(" + fromFile.getPath() + "). Such a file doesn't exist or it is a directory."
			);
		}
		if (toFile != null && toFile.isDirectory()) {
			throw new IllegalArgumentException(
					"Cannot copy content to file(" + toFile.getPath() + "). Destination already exists and it is a directory not a file."
			);
		}
		return this.copyFileContentInner(flags, fromFile, toFile);
	}

	/**
	 * Copies the given file and its content on the file system.
	 *
	 * @param flags    The flags used manage copy process.
	 * @param fromFile The desired file of which content to copy.
	 * @param toFile   The desired file to which should be content of the requested file copied.
	 * @return {@code True} if content of the requested file was successfully copied,
	 * {@code false} if some error occurs during copy process or the given flags were not properly
	 * specified.
	 */
	private boolean copyFileContentInner(int flags, File fromFile, File toFile) throws IOException {
		synchronized (LOCK) {
			String toFilePath = toFile == null ? "" : toFile.getPath();
			if (TextUtils.isEmpty(toFilePath)) {
				if ((flags & COPY) == 0) {
					logError("Failed to copy content of file('" + fromFile.getPath() + "') without StorageEditor.COPY flag. Such a file already exists.");
					return false;
				}
				// Append -Copy suffix to file path.
				toFilePath = appendPath(fromFile, COPY_SUFFIX).getPath();
			} else if (toFile != null && toFile.exists()) {
				final boolean copy = (flags & COPY) != 0;
				final boolean overwrite = (flags & OVERWRITE) != 0;
				if (!copy && !overwrite) {
					logError("Failed to copy content to file('" + toFilePath + "') without StorageEditor.COPY or StorageEditor.OVERWRITE flag. Such a file already exists.");
					return false;
				} else if (copy) {
					// Append -Copy suffix to file path.
					toFilePath = appendPath(toFile, COPY_SUFFIX).getPath();
				}
			}
			if ((toFile = createAndGetFile(toFilePath)) == null) {
				return false;
			}
			final FileInputStream input = new FileInputStream(fromFile);
			final FileOutputStream output = new FileOutputStream(toFile);
			copyFileStreams(input, output);
			input.close();
			output.close();
			return true;
		}
	}

	/**
	 * Creates and returns a new empty file at the specified <var>path</var>.
	 *
	 * @param path The desired path at which should be the requested file created.
	 * @return {@code True} if file was successfully created or already exists, {@code false}
	 * if some error occurs or there is already a directory at the specified path.
	 */
	private static File createAndGetFile(String path) {
		final File file = new File(path);
		synchronized (LOCK) {
			if (file.isFile()) {
				return file;
			}
			if (!file.exists()) {
				// First check if all parent directories exist.
				final Uri fileUri = Uri.parse(path);
				final File parentDirs = new File(fileUri.getPath().replace(fileUri.getLastPathSegment(), ""));
				if ((!parentDirs.exists() || !parentDirs.isDirectory()) && !parentDirs.mkdirs()) {
					logError("Failed to create parent directories for file('" + path + "').");
					return null;
				}
				try {
					return file.createNewFile() ? file : null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	/**
	 * Same as {@link #copyDirectoryContent(int, File, File)} with {@link #COPY} flag
	 * and {@code null} <var>toDirectory</var> parameter, so there will be created new copy of
	 * the given <var>directory</var> at the same path with {@link #COPY_SUFFIX}.
	 */
	public boolean copyDirectoryContent(@NonNull File directory) throws IOException {
		return copyDirectoryContent(COPY, directory, null);
	}

	/**
	 * Same as {@link #copyDirectoryContent(int, File, File, FileFilter, FilenameFilter)}
	 * with {@code null} filters.
	 */
	public boolean copyDirectoryContent(int flags, @NonNull File fromDirectory, @Nullable File toDirectory) throws IOException {
		return copyDirectoryContent(flags, fromDirectory, toDirectory, null, null);
	}

	/**
	 * Copies a content from the given <var>fromDirectory</var> into the given <var>toDirectory</var>.
	 *
	 * @param flags         The flags used manage copy process. See {@link #OVERWRITE}, {@link #COPY}.
	 * @param fromDirectory The desired directory of which content to copy.
	 * @param toDirectory   The desired directory to which should be content of the requested directory
	 *                      copied.
	 * @param filter        The filter used to copy only desired files form the requested directory.
	 *                      Pass {@code null} to copy all files.
	 * @param nameFilter    The filter used to copy only files with desired names form the requested
	 *                      directory. Pass {@code null} to copy all files.
	 * @return {@code True} if directory was successfully copied, {@code false} if some error
	 * occurs during copy process or the given flags were not properly specified.
	 * @throws FileNotFoundException    If the given <var>fromDirectory</var> doesn't exists or is not a directory.
	 * @throws IllegalArgumentException If the given <var>toDirectory</var> is not a directory but a file.
	 * @throws IOException              If some IO error occurs during copy process of the requested file.
	 */
	public boolean copyDirectoryContent(int flags, @NonNull File fromDirectory, @Nullable File toDirectory, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter) throws IOException {
		if (!fromDirectory.isDirectory()) {
			throw new FileNotFoundException(
					"Cannot copy content of directory(" + fromDirectory.getPath() + "). Such a directory doesn't exist or it is a file."
			);
		}
		if (toDirectory != null && toDirectory.isFile()) {
			throw new IllegalArgumentException(
					"Cannot copy content to directory(" + toDirectory.getPath() + "). Destination already exists and it is a file not a directory."
			);
		}
		return this.copyDirectoryContentInner(flags, fromDirectory, toDirectory, filter, nameFilter);
	}

	/**
	 * Copies the given directory and its content on the file system.
	 *
	 * @param flags         The flags used manage copy process.
	 * @param fromDirectory The desired directory of which content to copy.
	 * @param toDirectory   The desired directory to which should be content of the requested directory
	 *                      copied.
	 * @param filter        The filter used to copy only desired files form the requested directory.
	 * @param nameFilter    The filter used to copy only files with desired names form the requested
	 *                      directory.
	 * @return {@code True} if content of the requested directory was successfully copied,
	 * {@code false} if some error occurs during copy process or the given flags were not properly
	 * specified.
	 */
	private boolean copyDirectoryContentInner(int flags, File fromDirectory, File toDirectory, FileFilter filter, FilenameFilter nameFilter) throws IOException {
		final File[] files = fromDirectory.listFiles();
		String toDirectoryPath = toDirectory == null ? "" : toDirectory.getPath();
		boolean failed = false;
		if (files.length > 0) {
			File tempFile;
			synchronized (LOCK) {
				for (final File file : files) {
					// Apply filters.
					if ((filter != null && !filter.accept(file)) || (nameFilter != null && !nameFilter.accept(file, file.getName()))) {
						continue;
					}
					// File passed through filters, resolve how to copy it.
					tempFile = new File(appendPathWithFilename(toDirectoryPath, file.getName()));
					if (file.isDirectory()) {
						if (copyDirectoryContentInner(flags, file, tempFile, filter, nameFilter)) {
							continue;
						}
						logError("Failed to copy content of directory('" + file.getPath() + "').");
						failed = true;
					} else if (file.isFile()) {
						if (copyFileContentInner(flags, file, tempFile)) {
							continue;
						}
						logError("Failed to copy content of file('" + file.getPath() + "').");
						failed = true;
					}
				}
			}
		} else {
			synchronized (LOCK) {
				if (TextUtils.isEmpty(toDirectoryPath)) {
					if ((flags & COPY) == 0) {
						logError("Failed to copy content of directory('" + fromDirectory.getPath() + "') without StorageEditor.COPY flag. Such a directory already exists.");
						failed = true;
					} else {
						// Append -Copy suffix to directory path.
						toDirectoryPath = appendPath(fromDirectory, COPY_SUFFIX).getPath();
					}
				} else if (toDirectory != null && toDirectory.exists()) {
					if ((flags & COPY) == 0) {
						logError("Failed to copy content to directory('" + toDirectoryPath + "') without StorageEditor.COPY flag. Such a directory already exists.");
						failed = true;
					} else {
						// Append -Copy suffix to directory path.
						toDirectoryPath = appendPath(toDirectory, COPY_SUFFIX).getPath();
					}
				}

				if (!failed) {
					final File toDir = new File(toDirectoryPath);
					// Apply filters.
					if ((filter != null && !filter.accept(toDir)) || (nameFilter != null && !nameFilter.accept(toDir, toDir.getName()))) {
						return true;
					}
					// Directory passed through filters.
					if (!toDir.exists() && !createDirectory(toDirectoryPath)) {
						logError("Failed to create directory('" + toDirectoryPath + "').");
						failed = true;
					}
				}
			}
		}
		return !failed;
	}

	/**
	 * Appends the given <var>path</var> with the given <var>filename</var>.
	 *
	 * @param path     The path which to append.
	 * @param filename The name of file which to add at the end of the given path.
	 * @return Appended path or <var>filename</var> if the given path is empty.
	 */
	private String appendPathWithFilename(String path, String filename) {
		return TextUtils.isEmpty(path) ? "" : path + File.separator + filename;
	}

	/**
	 * Appends a path of the given <var>file</var> with the given <var>suffix</var>. If the given file
	 * represents a file, the given suffix will be appended to last path segment of the given file's
	 * path preserving its type at the end.
	 *
	 * @param file   The file of which path to append.
	 * @param suffix The suffix which to add at the end of the given file's path.
	 * @return New file with appended path.
	 */
	private File appendPath(File file, String suffix) {
		String filePath = file.getPath();
		if (TextUtils.isEmpty(filePath)) {
			return file;
		}
		if (file.isDirectory()) {
			return new File(filePath + suffix);
		}
		final String lastPathSegment = Uri.parse(filePath).getLastPathSegment();
		if (mFileNameMatcher.reset(lastPathSegment).matches()) {
			final String fileType = mFileNameMatcher.group(2);
			// Remove the type suffix from path.
			filePath = filePath.substring(0, filePath.length() - fileType.length() - 1);
			return new File(filePath + suffix + "." + fileType);
		}
		return file;
	}

	/**
	 * Creates a new empty directory at the specified <var>path</var>.
	 *
	 * @param path The desired path at which should be the requested directory created.
	 * @return {@code True} if directory was successfully created or already exists, {@code false}
	 * if some error occurs or there is already a file at the specified path.
	 */
	private static boolean createDirectory(String path) {
		final File file = new File(path);
		synchronized (LOCK) {
			return file.isDirectory() || file.mkdirs();
		}
	}

	/**
	 * Moves a content from the given <var>fromFile</var> into the given <var>toFile</var>. When content
	 * is successfully moved, the file, from which was content moved, is deleted.
	 * <p>
	 * This implementation actually moves content between the given files and than deletes the old one.
	 *
	 * @param flags    The flags used manage move process. See {@link #OVERWRITE}, {@link #COPY}.
	 * @param fromFile The desired file of which content to move.
	 * @param toFile   The desired file to which should be content of the requested file moved.
	 * @return {@code True} if file was successfully moved and old one deleted, {@code false}
	 * if some error occurs during move process or the given flags were not properly specified.
	 * @throws FileNotFoundException    If the given <var>fromFile</var> doesn't exists or is not a file.
	 * @throws IllegalArgumentException If the given <var>toFile</var> is not a file but a directory.
	 * @throws IOException              If some IO error occurs during copy process of the requested file.
	 */
	public boolean moveFileContent(int flags, @NonNull File fromFile, @Nullable File toFile) throws IOException {
		if (!fromFile.isFile()) {
			throw new FileNotFoundException(
					"Cannot move content of file(" + fromFile.getPath() + "). Such a file doesn't exist or it is a directory."
			);
		}
		if (toFile != null && toFile.isDirectory()) {
			throw new IllegalArgumentException(
					"Cannot move content to file(" + toFile.getPath() + "). Destination already exists and it is a directory not a file."
			);
		}
		return this.moveFileContentInner(flags, fromFile, toFile);
	}

	/**
	 * Moves the given file and its content on the file system.
	 *
	 * @param flags    The flags used manage move process.
	 * @param fromFile The desired file of which content to move.
	 * @param toFile   The desired file to which should be content of the requested file moved.
	 * @return {@code True} if content of the requested file was successfully moved and old file
	 * was deleted, {@code false} if some error occurs during move process or the given flags
	 * were not properly specified.
	 */
	private boolean moveFileContentInner(int flags, File fromFile, File toFile) throws IOException {
		return this.copyFileContentInner(flags, fromFile, toFile) && fromFile.delete();
	}

	/**
	 * Same as {@link #moveDirectoryContent(int, File, File, FileFilter, FilenameFilter)}
	 * with {@code null} filters.
	 */
	public boolean moveDirectoryContent(int flags, @NonNull File fromDirectory, @Nullable File toDirectory) throws IOException {
		return moveDirectoryContent(flags, fromDirectory, toDirectory, null, null);
	}

	/**
	 * Moves a content (files + subdirectories) from the given <var>fromDirectory</var> into the given
	 * <var>toDirectory</var>. When content is successfully moved, the directory, from which was content
	 * moved, is deleted.
	 * <p>
	 * This implementation actually moves content between the given directories and than deletes the
	 * old one.
	 *
	 * @param flags         The flags used manage move process. See {@link #OVERWRITE}, {@link #COPY}.
	 * @param fromDirectory The desired directory of which content to move.
	 * @param toDirectory   The desired directory to which should be content of the requested directory
	 *                      moved.
	 * @param filter        The filter used to move only desired files form the requested directory.
	 *                      Pass {@code null} to move all files.
	 * @param nameFilter    The filter used to move only files with desired names form the requested
	 *                      directory. Pass {@code null} to move all files.
	 * @return {@code True} if directory was successfully moved and old one deleted, {@code false}
	 * if some error occurs during move process or the given flags were not properly specified.
	 * @throws FileNotFoundException         If the given <var>fromDirectory</var> doesn't exists or is not a directory.
	 * @throws IllegalArgumentException      If the given <var>toDirectory</var> is not a directory but a file.
	 * @throws UnsupportedOperationException
	 * @throws IOException                   If some IO error occurs during copy process of the requested file.
	 */
	public boolean moveDirectoryContent(int flags, @NonNull File fromDirectory, @Nullable File toDirectory, @Nullable FileFilter filter, @Nullable FilenameFilter nameFilter) throws IOException {
		if (!fromDirectory.isDirectory()) {
			throw new FileNotFoundException(
					"Cannot move content of directory(" + fromDirectory.getPath() + "). Such a directory doesn't exist or it is a file."
			);
		}
		if (toDirectory != null) {
			if (toDirectory.isFile()) {
				throw new IllegalArgumentException(
						"Cannot move content to directory(" + toDirectory.getPath() + "). Destination already exists and it is a file not a directory."
				);
			}
			// Check if there is request to move directory to itself, that's not supported.
			final File parent = toDirectory.getParentFile();
			final String directoryPath = fromDirectory.getPath();
			if (parent != null && directoryPath.equals(parent.getPath())) {
				throw new UnsupportedOperationException("Cannot move directory('" + directoryPath + "') to itself.");
			}
		}
		return this.moveDirectoryContentInner(flags, fromDirectory, toDirectory, filter, nameFilter);
	}

	/**
	 * Moves the given directory and its content on the file system.
	 *
	 * @param flags         The flags used manage move process.
	 * @param fromDirectory The desired directory of which content to move.
	 * @param toDirectory   The desired directory to which should be content of the requested directory
	 *                      moved.
	 * @param filter        The filter used to move only desired files form the requested directory.
	 * @param nameFilter    The filter used to move only files with desired names form the requested
	 *                      directory.
	 * @return {@code True} if content of the requested directory was successfully moved and the
	 * old directory was deleted, {@code false} if some error occurs during move process or the
	 * given flags were not properly specified.
	 */
	private boolean moveDirectoryContentInner(int flags, File fromDirectory, File toDirectory, FileFilter filter, FilenameFilter nameFilter) throws IOException {
		return this.copyDirectoryContentInner(flags, fromDirectory, toDirectory, filter, nameFilter) && this.deleteDirectory(fromDirectory, filter, nameFilter);
	}

	/**
	 * Same as {@link #copyFileStreams(FileInputStream, FileOutputStream, int)}
	 * with {@link #MEDIUM_BUFFER} type.
	 */
	public boolean copyFileStreams(@NonNull FileInputStream inputStream, @NonNull FileOutputStream outputStream) throws IOException {
		return copyFileStreams(inputStream, outputStream, MEDIUM_BUFFER);
	}

	/**
	 * Same as {@link #copyFileStreams(FileInputStream, FileOutputStream, byte[])}
	 * with buffer obtained from {@link #createBuffer(int)} for the desired <var>bufferType</var>.
	 */
	public boolean copyFileStreams(@NonNull FileInputStream inputStream, @NonNull FileOutputStream outputStream, int bufferType) throws IOException {
		return copyFileStreams(inputStream, outputStream, createBuffer(bufferType));
	}

	/**
	 * Copies a content of the given <var>inputStream</var> into the given <var>outputStream</var>
	 * using the given <var>buffer</var>.
	 * <p>
	 * <b>Note</b>, that if the IOException occurs, the given streams will be not closed by this implementation,
	 * so it is your duty to close them in <b>finally</b> block of your <b>try catch</b> statement.
	 *
	 * @param inputStream  The input stream of which content to copy.
	 * @param outputStream The output stream where should be content copied.
	 * @param buffer       Buffer used for copy process. Larger buffer will make copy process faster,
	 *                     but also more memory intensive.
	 * @return Always {@code true} if no IOException is thrown.
	 * @throws IOException If some input/output error occurs during copy process.
	 */
	public boolean copyFileStreams(@NonNull FileInputStream inputStream, @NonNull FileOutputStream outputStream, byte[] buffer) throws IOException {
		int bytes;
		while ((bytes = inputStream.read(buffer, 0, buffer.length)) > 0) {
			outputStream.write(buffer, 0, bytes);
		}
		inputStream.close();
		outputStream.close();
		return true;
	}

	/**
	 * Logs the given <var>errorMessage</var> with {@link Log#e(String, String)} with
	 * this class's TAG.
	 *
	 * @param errorMessage The desired error message to log.
	 */
	private static void logError(String errorMessage) {
		Log.e(TAG, errorMessage);
	}

	/**
	 * Inner classes ===============================================================================
	 */
}

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

import android.text.TextUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StorageAction represents base implementation for all file/directory operations requested by {@link StorageImpl}.
 * <ul>
 * <li>{@link StorageAction.Create} implementation for <b>create</b> operations</li>
 * <li>{@link StorageAction.Delete} implementation for <b>delete</b> operations</li>
 * <li>{@link StorageAction.Copy} implementation for <b>copy</b> operations</li>
 * <li>{@link StorageAction.Move} implementation for <b>move</b> operations</li>
 * </ul>
 * Above mentioned implementations use {@link StorageUtils} to perform all requested operations and
 * also handles all thrown exceptions and sets the related error codes to implementation of
 * {@link Storage.BaseResult} when returning result for the preformed action.
 *
 * @author Martin Albedinsky
 * @since 1.0
 */
abstract class StorageAction {

	/*
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "StorageAction";

	/*
	 * Interface ===================================================================================
	 */

	/*
	 * Static members ==============================================================================
	 */

	/*
	 * Members =====================================================================================
	 */

	/**
	 * Storage implementation to access some storage info.
	 */
	private final StorageImpl storage;

	/**
	 * Unique identifier of this storage action.
	 */
	private final int action;

	/**
	 * Action verb.
	 */
	private final String actionVerb, actionVerbPast;

	/*
	 * Constructors ================================================================================
	 */

	/**
	 * Creates a new instance of StorageAction with the given parameters.
	 *
	 * @param storage        An instance of StorageImpl to access some storage info.
	 * @param action         Unique identifier for this action.
	 * @param actionVerb     Verb representing this action.
	 * @param actionVerbPast Past form of the verb representing this action.
	 */
	private StorageAction(final StorageImpl storage, final int action, final String actionVerb, final String actionVerbPast) {
		this.storage = storage;
		this.action = action;
		this.actionVerb = actionVerb;
		this.actionVerbPast = actionVerbPast;
	}

	/*
	 * Methods =====================================================================================
	 */

	/**
	 * Performs storage operation represented by this action for file at the given <var>fromPath</var>.
	 * <p>
	 * See {@link Storage} and its file related operations for additional parameters descriptions
	 * which are passed to this action.
	 *
	 * @return Result data of the performed action.
	 */
	Storage.Result performFileAction(final int storage, final int flags, final String toPath, final String fromPath) {
		int errorCode = Storage.ERROR_API;
		String exMessage = "";
		boolean success = false;
		try {
			success = onPerformFileAction(flags, this.storage.appendBasePath(storage, toPath), this.storage.appendBasePath(storage, fromPath));
		} catch (FileNotFoundException e) {
			errorCode = Storage.ERROR_FILE_NOT_FOUND;
			exMessage = e.getMessage();
		} catch (IOException e) {
			errorCode = Storage.ERROR_IO;
			exMessage = e.getMessage();
		} catch (IllegalArgumentException e) {
			errorCode = Storage.ERROR_FILE_SAME_AS_DIRECTORY;
			exMessage = e.getMessage();
		}
		return createResult(
				action,
				success ?
						buildStorageMessage("Successfully " + actionVerbPast + " file('" + fromPath + "')", storage, null) :
						buildStorageMessage("Failed to " + actionVerb + " file('" + fromPath + "')", storage, exMessage),
				fromPath,
				flags,
				success ? Storage.NO_ERROR : errorCode
		);
	}

	/**
	 * Called from {@link #performFileAction(int, int, String, String)} with same data to perform by
	 * this action represented storage file operation.
	 *
	 * @return {@code True} if this action succeeded, {@code false} otherwise.
	 */
	abstract boolean onPerformFileAction(int flags, String toPath, String fromPath) throws IOException;

	/**
	 * Bulk method for {@link #performFileAction(int, int, String, String)} method.
	 *
	 * @return Results data of the performed action.
	 */
	Storage.Results performFilesAction(final int storage, final int flags, final String toPath, final String... fromPaths) {
		if (fromPaths.length > 0) {
			final List<Storage.Result> results = new ArrayList<>();
			int passed = 0;
			Storage.Result result;
			for (final String fromPath : fromPaths) {
				result = performFileAction(storage, flags, toPath, fromPath);
				if (!result.isError) {
					passed++;
				}
				results.add(result);
			}
			return this.processResults(results, passed, "files", storage, Storage.ERROR_API);
		}
		return createResults(action, 0, "No files to " + actionVerb + ".", null, Storage.NO_FLAGS, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Performs storage operation represented by this action for directory at the given <var>fromPath</var>.
	 * <p>
	 * See {@link Storage} and its directory related operations for additional parameters descriptions
	 * which are passed to this action.
	 *
	 * @return Result data of the performed action.
	 */
	Storage.Result performDirectoryAction(final int storage, final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String fromPath) {
		int errorCode = Storage.ERROR_API;
		String exMessage = "";
		boolean success = false;
		try {
			success = onPerformDirectoryAction(
					flags,
					filter,
					nameFilter,
					this.storage.appendBasePath(storage, toPath),
					this.storage.appendBasePath(storage, fromPath)
			);
		} catch (FileNotFoundException e) {
			errorCode = Storage.ERROR_FILE_NOT_FOUND;
			exMessage = e.getMessage();
		} catch (IOException e) {
			errorCode = Storage.ERROR_IO;
			exMessage = e.getMessage();
		} catch (IllegalArgumentException e) {
			errorCode = Storage.ERROR_FILE_SAME_AS_DIRECTORY;
			exMessage = e.getMessage();
		} catch (UnsupportedOperationException e) {
			errorCode = Storage.ERROR_UNSUPPORTED_OPERATION;
			exMessage = e.getMessage();
		}
		return createResult(
				action,
				success ?
						buildStorageMessage("Successfully " + actionVerbPast + " directory('" + fromPath + "')", storage, null) :
						buildStorageMessage("Failed to " + actionVerb + " directory('" + fromPath + "')", storage, exMessage),
				fromPath,
				flags,
				success ? Storage.NO_ERROR : errorCode
		);
	}

	/**
	 * Called from {@link #performDirectoryAction(int, int, FileFilter, FilenameFilter, String, String)}
	 * with same data to perform by this action represented storage directory operation.
	 *
	 * @return {@code True} if this action succeeded, {@code false} otherwise.
	 */
	abstract boolean onPerformDirectoryAction(int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String fromPath) throws IOException;

	/**
	 * Bulk method for {@link #performDirectoryAction(int, int, FileFilter, FilenameFilter, String, String)}
	 * method.
	 *
	 * @return Results data of the performed action.
	 */
	Storage.Results performDirectoriesAction(final int storage, final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String... fromPaths) {
		if (fromPaths.length > 0) {
			final List<Storage.Result> results = new ArrayList<>();
			int passed = 0;
			Storage.Result result;
			for (final String fromPath : fromPaths) {
				result = performDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
				if (!result.isError) {
					passed++;
				}
				results.add(result);
			}
			return this.processResults(results, passed, "directories", storage, Storage.ERROR_API);
		}
		return createResults(action, 0, "No directories to " + actionVerb + ".", null, flags, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Bulk method for {@link #performFileOrDirectoryAction(int, int, FileFilter, FilenameFilter, String, String)}
	 * method.
	 *
	 * @return Results object for this action.
	 */
	Storage.Results performFilesOrDirectoriesAction(final int storage, final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String... fromPaths) {
		if (fromPaths.length > 0) {
			final List<Storage.Result> results = new ArrayList<>();
			int passed = 0;
			Storage.Result result;
			for (final String fromPath : fromPaths) {
				result = performFileOrDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
				if (!result.isError) {
					passed++;
				}
				results.add(result);
			}
			return this.processResults(results, passed, "files/directories", storage, Storage.ERROR_API);
		}
		return StorageAction.createResults(action, 0, "No files/directories to delete.", null, flags, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Performs storage operation represented by this action for file or directory at the given
	 * <var>fromPath</var>. This will call {@link #performFileAction(int, int, String, String)} or
	 * {@link #performDirectoryAction(int, int, FileFilter, FilenameFilter, String, String)} depends
	 * on whether the given <var>fromPath</var> points to a file or to a directory.
	 * <p>
	 * See {@link Storage} and its file or directory related operations for additional parameters
	 * descriptions which are passed to this action.
	 *
	 * @return Result data of the performed action.
	 */
	Storage.Result performFileOrDirectoryAction(final int storage, final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String fromPath) {
		final File file = new File(this.storage.appendBasePath(storage, fromPath));
		if (file.exists()) {
			return file.isFile() ? performFileAction(storage, flags, toPath, fromPath) : performDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
		}
		return createResult(action, "No file/directory to " + actionVerb + ".", fromPath, flags, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Builds a storage message used to bundle into {@link Storage.BaseResult} implementation as result
	 * message for this action.
	 *
	 * @param baseMessage      Base for the requested message. This should identify storage action.
	 * @param storage          An identifier of storage of which name will be added into message to identify
	 *                         on which storage was this action performed.
	 * @param exceptionMessage The message provided by thrown exception if this storage message is
	 *                         requested as error message.
	 * @return Storage message.
	 */
	private static String buildStorageMessage(final String baseMessage, final int storage, final String exceptionMessage) {
		final String storageName = StorageImpl.getStorageName(storage);
		final String message = TextUtils.isEmpty(storageName) ? baseMessage + " at device storage." : baseMessage + " on" + storageName + " storage.";
		return TextUtils.isEmpty(exceptionMessage) ? message : message + "\n" + exceptionMessage;
	}

	/**
	 * Called to process the given data into Results object.
	 *
	 * @param results   List of results to be bundled into Results instance.
	 * @param passed    The count of results which passes this action.
	 * @param fileType  Type of file (file/directory) for which will be the passed data processed.
	 * @param storage   An identifier of storage where was this action performed.
	 * @param errorCode Error code which will be passed to the instance of Results if this method
	 *                  process all given data as error.
	 * @return New instance of Results object with bundled passed data.
	 */
	private Storage.Results processResults(final List<Storage.Result> results, final int passed, final String fileType, final int storage, final int errorCode) {
		final int size = results.size();
		if (passed != results.size()) {
			return createResults(
					action,
					passed,
					buildStorageMessage("Failed to " + actionVerb + " " + Integer.toString(size - passed) + " " + fileType, storage, null),
					results,
					-1,
					errorCode
			);
		}
		return createResults(
				action,
				size,
				buildStorageMessage("Successfully " + actionVerbPast + " " + Integer.toString(passed) + " " + fileType, storage, null),
				results,
				-1,
				0
		);
	}

	/**
	 * See {@link Storage.Result#Result(int, String, String, int, int)} for additional info.
	 */
	static Storage.Result createResult(final int action, final String message, final String path, final int flags, final int error) {
		return new Storage.Result(action, message, path, flags, error);
	}

	/**
	 * See {@link Storage.Results#Results(int, String, int, List, int, int)} for additional info.
	 */
	private static Storage.Results createResults(final int action, final int size, final String message, final List<Storage.Result> results, final int flags, final int error) {
		return new Storage.Results(action, message, size, results, flags, error);
	}

	/*
	 * Inner classes ===============================================================================
	 */

	/**
	 * StorageAction implementation for file <b>create</b> operation on the file system.
	 */
	static final class Create extends StorageAction {

		/**
		 * Create a new instance of Create action with all necessary data initialized.
		 */
		Create(final StorageImpl storage) {
			super(storage, Storage.ACTION_CREATE, "create", "created");
		}

		/**
		 */
		@Override boolean onPerformFileAction(final int flags, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.createFile(fromPath);
		}

		/**
		 */
		@Override boolean onPerformDirectoryAction(final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.createDirectory(fromPath);
		}
	}

	/**
	 * StorageAction implementation for file <b>delete</b> operation on the file system.
	 */
	static final class Delete extends StorageAction {

		/**
		 * Create a new instance of Delete action with all necessary data initialized.
		 */
		Delete(final StorageImpl storage) {
			super(storage, Storage.ACTION_DELETE, "delete", "deleted");
		}

		/**
		 */
		@Override boolean onPerformFileAction(final int flags, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.deleteFile(fromPath);
		}

		/**
		 */
		@Override
		boolean onPerformDirectoryAction(final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.deleteDirectory(filter, nameFilter, fromPath);
		}
	}

	/**
	 * StorageAction implementation for file <b>copy</b> operation on the file system.
	 */
	static final class Copy extends StorageAction {

		/**
		 * Create a new instance of Copy action with all necessary data initialized.
		 */
		Copy(final StorageImpl storage) {
			super(storage, Storage.ACTION_COPY, "copy", "copied");
		}

		/**
		 */
		@Override boolean onPerformFileAction(final int flags, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.copyFile(flags, toPath, fromPath);
		}

		/**
		 */
		@Override boolean onPerformDirectoryAction(final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.copyDirectory(flags, filter, nameFilter, toPath, fromPath);
		}
	}

	/**
	 * StorageAction implementation for file <b>move</b> operation on the file system.
	 */
	static final class Move extends StorageAction {

		/**
		 * Create a new instance of Move action with all necessary data initialized.
		 */
		Move(final StorageImpl storage) {
			super(storage, Storage.ACTION_MOVE, "move", "moved");
		}

		/**
		 */
		@Override boolean onPerformFileAction(final int flags, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.moveFile(flags, toPath, fromPath);
		}

		/**
		 */
		@Override boolean onPerformDirectoryAction(final int flags, final FileFilter filter, final FilenameFilter nameFilter, final String toPath, final String fromPath) throws IOException {
			return StorageUtils.moveDirectory(flags, filter, nameFilter, toPath, fromPath);
		}
	}
}
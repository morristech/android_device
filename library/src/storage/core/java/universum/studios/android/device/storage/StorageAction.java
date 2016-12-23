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

import android.text.TextUtils;

import universum.studios.android.device.util.StorageUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents base implementation for all file/directory operations requested by {@link StorageImpl}.
 * <ul>
 * <li>{@link StorageAction.Create} implementation for <b>create</b> operations</li>
 * <li>{@link StorageAction.Delete} implementation for <b>delete</b> operations</li>
 * <li>{@link StorageAction.Copy} implementation for <b>copy</b> operations</li>
 * <li>{@link StorageAction.Move} implementation for <b>move</b> operations</li>
 * </ul>
 * Above mentioned implementations use {@link universum.studios.android.device.util.StorageUtils} to
 * perform all requested operations and also handles all thrown exceptions and sets the related error
 * codes to implementation of {@link universum.studios.android.device.Storage.BaseResult} when returning
 * result for the preformed action.
 *
 * @author Martin Albedinsky
 */
abstract class StorageAction {

	/**
	 * Interface ===================================================================================
	 */

	/**
	 * Constants ===================================================================================
	 */

	/**
	 * Log TAG.
	 */
	// private static final String TAG = "StorageAction";

	/**
	 * Static members ==============================================================================
	 */

	/**
	 * Members =====================================================================================
	 */

	/**
	 * Storage implementation to access some storage info.
	 */
	final StorageImpl mStorage;

	/**
	 * Unique identifier of this storage action.
	 */
	final int mAction;

	/**
	 * Action verb.
	 */
	final String mActionVerb, mActionVerbPast;

	/**
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
	StorageAction(StorageImpl storage, int action, String actionVerb, String actionVerbPast) {
		this.mStorage = storage;
		this.mAction = action;
		this.mActionVerb = actionVerb;
		this.mActionVerbPast = actionVerbPast;
	}

	/**
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
	Storage.Result performFileAction(int storage, int flags, String toPath, String fromPath) {
		int errorCode = Storage.ERROR_API;
		String exMessage = "";
		boolean success = false;
		try {
			success = onPerformFileAction(flags, mStorage.appendBasePath(storage, toPath), mStorage.appendBasePath(storage, fromPath));
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
		if (success) {
			return createResult(
					mAction,
					buildStorageMessage("Successfully " + mActionVerbPast + " file('" + fromPath + "')", storage, null),
					fromPath,
					flags,
					Storage.NO_ERROR
			);
		}
		return createResult(
				mAction,
				buildStorageMessage("Failed to " + mActionVerb + " file('" + fromPath + "')", storage, exMessage),
				fromPath,
				flags,
				errorCode
		);
	}

	/**
	 * Called from {@link #performFileAction(int, int, String, String)} with same data to perform
	 * by this action represented storage file operation.
	 *
	 * @return {@code True} if this action succeeded, {@code false} otherwise.
	 */
	abstract boolean onPerformFileAction(int flags, String toPath, String fromPath) throws IOException;

	/**
	 * Bulk method for {@link #performFileAction(int, int, String, String)} method.
	 *
	 * @return Results data of the performed action.
	 */
	Storage.Results performFilesAction(int storage, int flags, String toPath, String... fromPaths) {
		if (fromPaths.length > 0) {
			final List<Storage.Result> results = new ArrayList<>();
			int passed = 0;
			Storage.Result result;
			for (String fromPath : fromPaths) {
				result = performFileAction(storage, flags, toPath, fromPath);
				if (!result.isError) {
					passed++;
				}
				results.add(result);
			}
			return this.processResults(results, passed, "files", storage, Storage.ERROR_API);
		}
		return createResults(mAction, 0, "No files to " + mActionVerb + ".", null, Storage.NO_FLAGS, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Performs storage operation represented by this action for directory at the given <var>fromPath</var>.
	 * <p>
	 * See {@link Storage} and its directory related operations for additional parameters descriptions
	 * which are passed to this action.
	 *
	 * @return Result data of the performed action.
	 */
	Storage.Result performDirectoryAction(int storage, int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String fromPath) {
		int errorCode = Storage.ERROR_API;
		String exMessage = "";
		boolean success = false;
		try {
			success = onPerformDirectoryAction(
					flags,
					filter,
					nameFilter,
					mStorage.appendBasePath(storage, toPath),
					mStorage.appendBasePath(storage, fromPath)
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
		if (success) {
			return createResult(
					mAction,
					buildStorageMessage("Successfully " + mActionVerbPast + " directory('" + fromPath + "')", storage, null),
					fromPath,
					flags,
					Storage.NO_ERROR
			);
		}
		return createResult(
				mAction,
				buildStorageMessage("Failed to " + mActionVerb + " directory('" + fromPath + "')", storage, exMessage),
				fromPath,
				flags,
				errorCode
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
	Storage.Results performDirectoriesAction(int storage, int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String... fromPaths) {
		if (fromPaths.length > 0) {
			final List<Storage.Result> results = new ArrayList<>();
			int passed = 0;
			Storage.Result result;
			for (String fromPath : fromPaths) {
				result = performDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
				if (!result.isError) {
					passed++;
				}
				results.add(result);
			}
			return this.processResults(results, passed, "directories", storage, Storage.ERROR_API);
		}
		return createResults(mAction, 0, "No directories to " + mActionVerb + ".", null, flags, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Bulk method for {@link #performFileOrDirectoryAction(int, int, FileFilter, FilenameFilter, String, String)}
	 * method.
	 *
	 * @return Results object for this action.
	 */
	Storage.Results performFilesOrDirectoriesAction(int storage, int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String... fromPaths) {
		if (fromPaths.length > 0) {
			final List<Storage.Result> results = new ArrayList<>();
			int passed = 0;
			Storage.Result result;
			for (String fromPath : fromPaths) {
				result = performFileOrDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
				if (!result.isError) {
					passed++;
				}
				results.add(result);
			}
			return this.processResults(results, passed, "files/directories", storage, Storage.ERROR_API);
		}
		return StorageAction.createResults(mAction, 0, "No files/directories to delete.", null, flags, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Performs storage operation represented by this action for file or directory at the given
	 * <var>fromPath</var>. This will call {@link #performFileAction(int, int, String, String)} or
	 * {@link #performDirectoryAction(int, int, FileFilter, FilenameFilter, String, String)}
	 * depends on whether the given <var>fromPath</var> points to a file or to a directory.
	 * <p>
	 * See {@link Storage} and its file or directory related operations for additional parameters
	 * descriptions which are passed to this action.
	 *
	 * @return Result data of the performed action.
	 */
	Storage.Result performFileOrDirectoryAction(int storage, int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String fromPath) {
		final File file = new File(mStorage.appendBasePath(storage, fromPath));
		if (file.exists()) {
			return file.isFile() ? performFileAction(storage, flags, toPath, fromPath) : performDirectoryAction(storage, flags, filter, nameFilter, toPath, fromPath);
		}
		return createResult(mAction, "No file/directory to " + mActionVerb + ".", fromPath, flags, Storage.ERROR_EMPTY_REQUEST);
	}

	/**
	 * Builds a storage message used to bundle into {@link universum.studios.android.device.Storage.BaseResult}
	 * implementation as result message for this action.
	 *
	 * @param baseMessage      Base for the requested message. This should identify storage action.
	 * @param storage          An identifier of storage of which name will be added into message to identify
	 *                         on which storage was this action performed.
	 * @param exceptionMessage The message provided by thrown exception if this storage message is
	 *                         requested as error message.
	 * @return Storage message.
	 */
	static String buildStorageMessage(String baseMessage, int storage, String exceptionMessage) {
		final String storageName = StorageImpl.getStorageName(storage);
		final String message = !TextUtils.isEmpty(storageName) ? baseMessage + " on" + storageName + " storage." : baseMessage + " at device storage.";
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
	private Storage.Results processResults(List<Storage.Result> results, int passed, String fileType, int storage, int errorCode) {
		final int size = results.size();
		if (passed != results.size()) {
			return createResults(
					mAction,
					passed,
					buildStorageMessage("Failed to " + mActionVerb + " " + Integer.toString(size - passed) + " " + fileType, storage, null),
					results,
					-1,
					errorCode
			);
		}
		return createResults(
				mAction,
				size,
				buildStorageMessage("Successfully " + mActionVerbPast + " " + Integer.toString(passed) + " " + fileType, storage, null),
				results,
				-1,
				0
		);
	}

	/**
	 * See {@link universum.studios.android.device.Storage.Result#Result(int, String, String, int, int)} for additional info.
	 */
	static Storage.Result createResult(int action, String message, String path, int flags, int error) {
		return new Storage.Result(action, message, path, flags, error);
	}

	/**
	 * See {@link universum.studios.android.device.Storage.Results#Results(int, String, int, List, int, int)} for additional info.
	 */
	static Storage.Results createResults(int action, int size, String message, List<Storage.Result> results, int flags, int error) {
		return new Storage.Results(action, message, size, results, flags, error);
	}

	/**
	 * Inner classes ===============================================================================
	 */

	/**
	 * StorageAction implementation for file <b>create</b> operation on the file system.
	 */
	static final class Create extends StorageAction {

		/**
		 * Create a new instance of Create action with all necessary data initialized.
		 */
		Create(StorageImpl storage) {
			super(storage, Storage.ACTION_CREATE, "create", "created");
		}

		/**
		 */
		@Override
		boolean onPerformFileAction(int flags, String toPath, String fromPath) throws IOException {
			return StorageUtils.createFile(fromPath);
		}

		/**
		 */
		@Override
		boolean onPerformDirectoryAction(int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String fromPath) throws IOException {
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
		Delete(StorageImpl storage) {
			super(storage, Storage.ACTION_DELETE, "delete", "deleted");
		}

		/**
		 */
		@Override
		boolean onPerformFileAction(int flags, String toPath, String fromPath) throws IOException {
			return StorageUtils.deleteFile(fromPath);
		}

		/**
		 */
		@Override
		boolean onPerformDirectoryAction(int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String fromPath) throws IOException {
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
		Copy(StorageImpl storage) {
			super(storage, Storage.ACTION_COPY, "copy", "copied");
		}

		/**
		 */
		@Override
		boolean onPerformFileAction(int flags, String toPath, String fromPath) throws IOException {
			return StorageUtils.copyFile(flags, toPath, fromPath);
		}

		/**
		 */
		@Override
		boolean onPerformDirectoryAction(int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String fromPath) throws IOException {
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
		Move(StorageImpl storage) {
			super(storage, Storage.ACTION_MOVE, "move", "moved");
		}

		/**
		 */
		@Override
		boolean onPerformFileAction(int flags, String toPath, String fromPath) throws IOException {
			return StorageUtils.moveFile(flags, toPath, fromPath);
		}

		/**
		 */
		@Override
		boolean onPerformDirectoryAction(int flags, FileFilter filter, FilenameFilter nameFilter, String toPath, String fromPath) throws IOException {
			return StorageUtils.moveDirectory(flags, filter, nameFilter, toPath, fromPath);
		}
	}
}

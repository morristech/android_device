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
package universum.studios.android.device.screen;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Simple utility class specifying API allowing to manage screen of the current Android device.
 *
 * @author Martin Albedinsky
 */
public final class ScreenUtils {

	/**
	 */
	private ScreenUtils() {
		// Creation of instances of this class is not publicly allowed.
	}

	/**
	 * Hides soft keyboard from the current window.
	 * <p>
	 * <b>Note</b>, that this will not work when you want to hide soft keyboard while a dialog is
	 * visible, use {@link #hideSoftKeyboard(View)} instead.
	 *
	 * @param activity Current activity context to obtain the current focused view.
	 * @return {@code True} if hiding was successful, {@code false} if a window of the given
	 * activity does not have focused view at this time.
	 */
	public static boolean hideSoftKeyboard(@NonNull Activity activity) {
		final View focusedView = activity.getWindow().getCurrentFocus();
		return focusedView != null && hideSoftKeyboard(focusedView);
	}

	/**
	 * Hides soft keyboard from the current window using token of the given focused view.
	 *
	 * @param focusedView The view which has focus at this time.
	 * @return {@code True} if hiding was successful, {@code false} if the view does not
	 * have focus at this time.
	 */
	public static boolean hideSoftKeyboard(@NonNull View focusedView) {
		if (focusedView.getContext() == null || !focusedView.hasFocus()) {
			return false;
		}
		final InputMethodManager imm = (InputMethodManager) focusedView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
	}

	/**
	 * Shows soft keyboard on the current window.
	 * <p>
	 * <b>Note</b>, that this will not work when you want to show soft keyboard while a dialog is
	 * visible, use {@link #showSoftKeyboard(View)} instead.
	 *
	 * @param activity Current activity context to obtain the current focused view.
	 * @return {@code True} if showing was successful, {@code false} otherwise.
	 */
	public static boolean showSoftKeyboard(@NonNull Activity activity) {
		final View view = activity.getWindow().getCurrentFocus();
		return view != null && showSoftKeyboard(view);
	}

	/**
	 * Shows soft keyboard on the current window using token of the given focused view.
	 *
	 * @param focusedView The view which has focus at this time. If the view does not have focus,
	 *                    focus for that view will be requested by {@link View#requestFocus()}.
	 * @return {@code True} if showing was successful, {@code false} otherwise.
	 */
	public static boolean showSoftKeyboard(@NonNull View focusedView) {
		if (focusedView.getContext() == null) {
			return false;
		}
		if (focusedView.hasFocus() || focusedView.requestFocus()) {
			final InputMethodManager imm = (InputMethodManager) focusedView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			return imm.showSoftInput(focusedView, InputMethodManager.RESULT_UNCHANGED_SHOWN);
		}
		return false;
	}
}

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
package universum.studios.android.device;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Interface specifying API that is required for all wrappers implementations from the Device library
 * which are relaying on data received via {@link android.content.BroadcastReceiver}.
 *
 * @author Martin Albedinsky
 */
public interface BroadcastProcessor {

	/**
	 * Called to process the given <var>intent</var> with data by this broadcast processor.
	 *
	 * @param context    Current application context.
	 * @param intent     Broad-casted intent with data.
	 * @param receiverId An id of the broadcast receiver which received the given <var>intent</var>.
	 */
	void processBroadcast(@NonNull Context context, @NonNull Intent intent, int receiverId);
}

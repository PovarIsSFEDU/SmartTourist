package com.wacked.smarttourist.activities;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.wacked.smarttourist.R;

/**
 * Utility class for access to runtime permissions.
 */
public abstract class PermissionUtils {

    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */
    public static void requestPermission(AppCompatActivity activity, int requestId,
                                         String permission, boolean finishActivity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Display a dialog with rationale.
            com.wacked.smarttourist.activities.PermissionUtils.RationaleDialog.newInstance(requestId, finishActivity)
                    .show(activity.getSupportFragmentManager(), "dialog");
        } else {
            // Location permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestId);

        }
    }

    /**
     * Checks if the result contains a {@link PackageManager#PERMISSION_GRANTED} result for a
     * permission from a runtime permissions request.
     *
     * @see ActivityCompat.OnRequestPermissionsResultCallback
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    /**
     * A dialog that displays a permission denied message.
     */
    public static class PermissionDeniedDialog extends DialogFragment {

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private boolean finishActivity = false;

        /**
         * Creates a new instance of this dialog and optionally finishes the calling Activity
         * when the 'Ok' button is clicked.
         */
        public static PermissionDeniedDialog newInstance(boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);

            PermissionDeniedDialog dialog = new PermissionDeniedDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            finishActivity = getArguments().getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.location_permission_denied)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(getActivity(), R.string.permission_required_toast,
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    /**
     * A dialog that explains the use of the location permission and requests the necessary
     * permission.
     * <p>
     * The activity should implement
     * {@link ActivityCompat.OnRequestPermissionsResultCallback}
     * to handle permit or denial of this permission request.
     */
    public static class RationaleDialog extends DialogFragment {

        private static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";

        private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

        private boolean finishActivity = false;

        /**
         * Creates a new instance of a dialog displaying the rationale for the use of the location
         * permission.
         * <p>
         * The permission is requested after clicking 'ok'.
         *
         * @param requestCode    Id of the request that is used to request the permission. It is
         *                       returned to the
         *                       {@link ActivityCompat.OnRequestPermissionsResultCallback}.
         * @param finishActivity Whether the calling Activity should be finished if the dialog is
         *                       cancelled.
         */
        public static RationaleDialog newInstance(int requestCode, boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode);
            arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
            RationaleDialog dialog = new RationaleDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            final int requestCode = arguments.getInt(ARGUMENT_PERMISSION_REQUEST_CODE);
            finishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_rationale_location)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // After click on Ok, request the permission.
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    requestCode);
                            // Do not finish the Activity while requesting permission.
                            finishActivity = false;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(getActivity(),
                        R.string.permission_required_toast,
                        Toast.LENGTH_SHORT)
                        .show();
                getActivity().finish();
            }
        }
    }
}


//The following methods and classes (PermissionUtils.class) were partially taken from open sources owned by Google (https://github.com/googlemaps/android-samples/blob/29ca74b9a3894121f179b9f36b0a51755e7231b0/ApiDemos/java/app/src/v3/java/com/example/mapdemo/MainActivity.java)
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
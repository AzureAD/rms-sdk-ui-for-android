/**
 * Copyright © Microsoft Corporation, All Rights Reserved
 *
 * Licensed under MICROSOFT SOFTWARE LICENSE TERMS, 
 * MICROSOFT RIGHTS MANAGEMENT SERVICE SDK UI LIBRARIES;
 * You may not use this file except in compliance with the License.
 * See the license for specific language governing permissions and limitations.
 * You may obtain a copy of the license (RMS SDK UI libraries - EULA.DOCX) at the 
 * root directory of this project.
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 */

package com.microsoft.rightsmanagement.sampleapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * DialogFragment class used for presenting modal messages to the user.
 */
public class MessageDialogFragment extends DialogFragment
{
    public static final String TAG = "ErrorDialogFragment";
    private static final String ERROR_DIALOG_FRAGMENT_BUNDLE_KEY = "ERROR_DIALOG_FRAGMENT_BUNDLE_KEY";
    private AlertDialog mDialog;
    private String mMessage;

    /**
     * New instance.
     * 
     * @param message the message
     * @return the message dialog fragment
     */
    static MessageDialogFragment newInstance(String message)
    {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ERROR_DIALOG_FRAGMENT_BUNDLE_KEY, message);
        fragment.setArguments(args);
        return fragment;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        restoreInstance(getArguments());
    }

    /**
     * Instantiate the fragment views.
     * 
     * @param savedInstanceState the saved instance state
     * @return the dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreateDialog(savedInstanceState);
        // Build an alert dialog to show the users
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(R.string.app_ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dismissAllowingStateLoss();
            }
        });
        // Restore the dialog in case of orientation change
        if (savedInstanceState != null)
        {
            restoreInstance(savedInstanceState);
        }
        builder.setTitle(getResources().getString(R.string.error));
        mDialog = builder.create();
        mDialog.setMessage(mMessage);
        return mDialog;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(ERROR_DIALOG_FRAGMENT_BUNDLE_KEY, mMessage);
    }

    /**
     * Restore the state of the dialog.
     * 
     * @param savedInstance the Bundle containing the state of the dialog to be restored.
     */
    private void restoreInstance(Bundle savedInstance)
    {
        mMessage = savedInstance.getString(ERROR_DIALOG_FRAGMENT_BUNDLE_KEY);
    }
}

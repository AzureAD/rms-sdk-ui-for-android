//
// Copyright © Microsoft Corporation, All Rights Reserved
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
// OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
// ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
// PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
//
// See the Apache License, Version 2.0 for the specific language
// governing permissions and limitations under the License.

package com.microsoft.rightsmanagement.ipcnotepadsample;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * The DialogFragment used for presenting progress to the user.
 */
public class ProgressDialogFragment extends DialogFragment
{
    /**
     * The listener interface for receiving progressDialogEvent events. The class that is interested in processing a
     * progressDialogEvent event implements this interface, and the object created with that class is registered with a
     * component using the component's <code>addProgressDialogEventListener<code> method. When
     * the progressDialogEvent event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see ProgressDialogEventEvent
     */
    public interface ProgressDialogEventListener
    {
        /**
         * On cancel progress dialog.
         * 
         * @param dialog the dialog
         */
        public void onCancelProgressDialog(DialogInterface dialog);
    }
    public static final String TAG = "ProgressDialogFragment";
    private static final String MESSAGE_BUNDLE_KEY = "MESSAGE_BUNDLE_KEY";
    private ProgressDialog mDialog;
    private String mMessage;
    private ProgressDialogEventListener mProgressDialogEventListener;

    /**
     * New instance.
     * 
     * @param message the message
     * @return the progress dialog fragment
     */
    static ProgressDialogFragment newInstance(String message)
    {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_BUNDLE_KEY, message);
        fragment.setArguments(args);
        return fragment;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mProgressDialogEventListener = (ProgressDialogEventListener)activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement ProgressDialogEventListener");
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
     */
    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        mProgressDialogEventListener.onCancelProgressDialog(dialog);
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
        // Restore the dialog in case of orientation change
        if (savedInstanceState != null)
        {
            restoreInstance(savedInstanceState);
        }
        mDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
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
        outState.putString(MESSAGE_BUNDLE_KEY, mMessage);
    }

    /**
     * Restore the state of the dialog.
     * 
     * @param savedInstance the Bundle containing the state of the dialog to be restored.
     */
    private void restoreInstance(Bundle savedInstance)
    {
        mMessage = savedInstance.getString(MESSAGE_BUNDLE_KEY);
    }
}

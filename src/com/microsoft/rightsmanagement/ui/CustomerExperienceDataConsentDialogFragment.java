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

package com.microsoft.rightsmanagement.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.microsoft.rightsmanagement.ui.utils.CallbackManager;
import com.microsoft.rightsmanagement.ui.utils.Logger;

// TODO: Auto-generated Javadoc
/**
 * The class CustomerExperienceDataConsentDialogFragment provide view implementation for performance & debug logs upload
 * consent UI.
 */
public class CustomerExperienceDataConsentDialogFragment extends DialogFragment
{
    
    /**
     * The Enum DialogState.
     */
    private enum DialogState
    {
        None,
        /* User pressed back or background */
        UserCancelled,
        /* User pressed a button */
        UserResponded
    }
    public static final String TAG = "CustomerExperienceDataConsentDialogFragment";
    private static final String REQUEST_CALLBACK_ID = "REQUEST_CALLBACK_ID";
    private static CallbackManager<Void, Void> sCallbackManager = new CallbackManager<Void, Void>();
    private DialogState mDialogState = DialogState.None;
    private int mRequestCallbackId;
    
    /**
     * Creates an instance of this DialogFragment.
     * 
     * @param consentCompletionCallback the consent completion callback which is called once dialog is dismissed
     * @return the customer experience data consent dialog fragment
     */
    public static CustomerExperienceDataConsentDialogFragment newInstance(CompletionCallback<Void> consentCompletionCallback)
    {
        Logger.ms(TAG, "newInstance");
        CustomerExperienceDataConsentDialogFragment consentDialogFragment = new CustomerExperienceDataConsentDialogFragment();
        consentDialogFragment.mRequestCallbackId = consentCompletionCallback.hashCode();
        sCallbackManager.putWaitingRequest(consentDialogFragment.mRequestCallbackId, consentCompletionCallback);
        
        // Set the arguments for cases when fragment is recreated by OS
        Bundle args = new Bundle();
        args.putInt(REQUEST_CALLBACK_ID, consentDialogFragment.mRequestCallbackId);
        consentDialogFragment.setArguments(args);
        
        Logger.me(TAG, "newInstance");
        return consentDialogFragment;
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
     */
    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        mDialogState = DialogState.UserCancelled;
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
        Logger.ms(TAG, "onCreateDialog");
        super.onCreateDialog(savedInstanceState);
        
        // initialize the field for cases when fragment is recreated by OS
        // During recreation, OS would use default constructor and set the state
        mRequestCallbackId = getArguments().getInt(REQUEST_CALLBACK_ID);
        
        LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.customer_experience_data_consent_dialog_fragment, null);
        Button positiveButton = (Button)view.findViewById(R.id.customer_experience_data_consent_dialog_positive_button);
        positiveButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "onClick listener called on yes button");
                mDialogState = DialogState.UserResponded;
                storeDebugLogPreference(true);
                dismiss();
            }
        });
        Button negativeButton = (Button)view.findViewById(R.id.customer_experience_data_consent_dialog_negative_button);
        negativeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "onClick listener called on no button");
                mDialogState = DialogState.UserResponded;
                storeDebugLogPreference(false);
                dismiss();
            }
        });
        Button neutralButton = (Button)view.findViewById(R.id.customer_experience_data_consent_dialog_neutral_button);
        neutralButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "onClick listener called on learn more");
                Intent internetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.learn_more_uri)));
                startActivity(internetIntent);
            }
        });
        
        OnKeyListener backKeyListener = new OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    Logger.d(TAG, "onback press");
                    mDialogState = DialogState.UserCancelled;
                    // onback press dismiss dialog and assume no thanks
                    storeDebugLogPreference(false);
                    dismiss();
                    return true;
                }
                return false;
            }
        };
        
        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(getActivity());
        dialogbuilder.setView(view);
        dialogbuilder.setOnKeyListener(backKeyListener);
        AlertDialog dialog = dialogbuilder.create();
        Logger.me(TAG, "onCreateDialog");
        return dialog;
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onDismiss(android.content.DialogInterface)
     */
    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        switch (mDialogState)
        {
            case UserResponded:
                invokeCompletionCallback(false);
                break;
            case UserCancelled:
                invokeCompletionCallback(true);
                break;
            default:
                /* OS decides to dismiss the dialog (probably to recreate one) */
                break;
        }
    }
    

    /**
     * Invoke completion callback.
     *
     * @param wasDialogCancelled the was dialog cancelled
     */
    private void invokeCompletionCallback(boolean wasDialogCancelled)
    {
        CompletionCallback<Void> completionCallback = sCallbackManager.getWaitingRequest(mRequestCallbackId);
        if (completionCallback != null)
        {
            sCallbackManager.removeWaitingRequest(mRequestCallbackId);
            if (wasDialogCancelled)
            {
                completionCallback.onCancel();
            }
            else
            {
                completionCallback.onSuccess(null);
            }
        }
    }
    
    /**
     * Store debug log preference.
     * 
     * @param logPreference the log preference
     */
    private void storeDebugLogPreference(boolean logPreference)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IpcCustomerExperienceDataCollectionEnabled", logPreference);
        editor.commit();
    }
}

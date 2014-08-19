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

import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.utils.ConfigurableParameters;

/**
 * The class CustomerExperienceDataConsentDialogFragment provide view implementation for performance & debug logs upload
 * consent UI
 */
public class CustomerExperienceDataConsentDialogFragment extends DialogFragment
{
    public static final String TAG = "CustomerExperienceDataConsentDialogFragment";

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
        LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.customer_experience_data_consent_dialog_fragment, null);
        Button positiveButton = (Button)view
                .findViewById(R.id.customer_experience_data_consent_dialog_positive_button);
        positiveButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "onClick listener called on yes button");
                storeDebugLogPreference(true);
                dismiss();
            }
        });
        Button negativeButton = (Button)view
                .findViewById(R.id.customer_experience_data_consent_dialog_negative_button);
        negativeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "onClick listener called on no button");
                storeDebugLogPreference(false);
                dismiss();
            }
        });
        Button neutralButton = (Button)view
                .findViewById(R.id.customer_experience_data_consent_dialog_neutral_button);
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
                    // onback press dissmiss dialog and assume no thanks
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
     * Stores the debug log preference to be used by SDK
     */
    private void storeDebugLogPreference(boolean logPreference)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ConfigurableParameters.getDiagnosticSharedPreferencePropertyName(), logPreference);
        editor.commit();
    }
}

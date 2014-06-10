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

package com.microsoft.rightsmanagement.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.v4.app.FragmentTransaction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;
import com.microsoft.rightsmanagement.ui.CompletionCallback;
import com.microsoft.rightsmanagement.ui.utils.CallbackManager;
import com.microsoft.rightsmanagement.ui.utils.Helpers;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.ui.widget.EmailFragment;

/**
 * An Activity to control email UI.
 */
public class EmailActivity extends BaseActivity implements EmailFragment.EmailFragmentEventListener
{
    private static final String RESULT_EMAIL = "RESULT_EMAIL";
    private static CallbackManager<String, Void> sCallbackManager = new CallbackManager<String, Void>();
    private static Pattern sEmailPattern = Pattern
            .compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");
    static
    {
        setTAG("EmailActivity");
    }

    /**
     * Processes the result of TemplateDescriptorPickerActivity started via startActivityForResult from the parent
     * activity, and invokes the callback supplied to show(). This method must be called from parent Activity's
     * onActivityResult.
     * 
     * @param resultCode the result code parameter as supplied to parent Activity's onActivityResult
     * @param data the data parameter as supplied to parent Activity's onActivityResult
     */
    public static void onActivityResult(int resultCode, Intent data)
    {
        Logger.ms(TAG, "onActivityResult");
        int requestCallbackId = 0;
        try
        {
            final Bundle extras = data.getExtras();
            requestCallbackId = extras.getInt(REQUEST_CALLBACK_ID);
            final CompletionCallback<String> callback = sCallbackManager.getWaitingRequest(requestCallbackId);
            switch (resultCode)
            {
                case RESULT_OK:
                    Logger.i(TAG, "resultCode=RESULT_OK", "");
                    String emailId = extras.getString(RESULT_EMAIL);
                    callback.onSuccess(emailId);
                    break;
                case RESULT_CANCELED:
                    Logger.i(TAG, "resultCode=RESULT_CANCELED", "");
                    callback.onCancel();
                    break;
            }
        }
        finally
        {
            if (requestCallbackId != 0)
            {
                sCallbackManager.removeWaitingRequest(requestCallbackId);
            }
            Logger.me(TAG, "onActivityResult");
        }
    }

    /**
     * Show UI.
     * 
     * @param requestCode the request code to be returned when activity completes
     * @param activity the parent activity
     * @param emailActivityCompletionCallback the email activity completion callback
     * @throws InvalidParameterException the invalid parameter exception
     */
    public static void show(int requestCode,
                            Activity activity,
                            CompletionCallback<String> emailActivityCompletionCallback)
            throws InvalidParameterException
    {
        Logger.ms(TAG, "show");
        activity = validateActivityInputParameter(activity);
        emailActivityCompletionCallback = validateCompletionCallbackInputParameter(emailActivityCompletionCallback);
        int requestCallbackId = emailActivityCompletionCallback.hashCode();
        sCallbackManager.putWaitingRequest(requestCallbackId, emailActivityCompletionCallback);
        Intent intent = new Intent(activity, EmailActivity.class);
        intent.putExtra(REQUEST_CALLBACK_ID, requestCallbackId);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, requestCode);
        Logger.me(TAG, "show");
    }

    /**
     * Verifies if the email address string matches email address convention.
     * 
     * @param email the email
     * @return email email address input true if email address is validated
     */
    private static boolean isValidEmail(String email)
    {
        Logger.d(TAG, String.format("isValidEmail - email=%s", email));
        Matcher m = sEmailPattern.matcher(email);
        return m.matches();
    }
    EmailFragment mEmailFragment;

    /*
     * (non-Javadoc)
     * @see com.microsoft.protection.CompletionCallback#onSuccess(java.lang.Object)
     */
    @Override
    public void onContinue(final String item)
    {
        Logger.ms(TAG, "onContinue");
        if (!Helpers.IsNullOrEmpty(item) && isValidEmail(item))
        {
            Logger.d(TAG, "item is valid");
            Intent data = new Intent();
            data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
            data.putExtra(RESULT_EMAIL, item);
            returnToCaller(RESULT_OK, data);
        }
        else
        {
            Logger.d(TAG, "item is invalid");
            if (mEmailFragment != null)
            {
                mEmailFragment.setErrorText(getString(R.string.error_invalid_email_address_string));
                mEmailFragment.setEmailText("");
            }
            else
            {
                Logger.ie(TAG, "onContinue() - mEmailFragment shouldn't be null");
            }
        }
        Logger.me(TAG, "onContinue");
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        int layoutId = R.layout.email_activity_layout;
        setContentView(layoutId);
        Intent intent = getIntent();
        mRequestCallbackId = intent.getIntExtra(REQUEST_CALLBACK_ID, 0);
        addEmailFragment();
        addTransparentPartDismissListener(R.id.left_transparent_part);
        addTransparentPartDismissListener(R.id.right_transparent_part);
        // create fader animators
        createBgAnimators(R.id.email_page_base_container, savedInstanceState);
        Logger.me(TAG, "onCreate");
    }

    /**
     * activity sets result to go back to the caller.
     * 
     * @param resultCode the result code
     * @param data the data
     */
    @Override
    protected void returnToCaller(int resultCode, Intent data)
    {
        Logger.d(TAG, String.format("ReturnToCaller - resultCode=%d", resultCode));
        setResult(resultCode, data);
        if (mEmailFragment == null)
        {
            this.finish();
        }
        else
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out_to_down);
            ft.remove(mEmailFragment).commit();
            mEmailFragment = null;
            startActivityEndAnimationAndFinishActivity();
        }
    }

    /**
     * Do fragment transaction to add email fragment.
     */
    private void addEmailFragment()
    {
        int containerId = R.id.email_page_container;
        mEmailFragment = (EmailFragment)getSupportFragmentManager().findFragmentByTag(EmailFragment.TAG);
        if (mEmailFragment == null)
        {
            Logger.d(TAG, "addEmailFragment() - mEmailFragment is null");
            mEmailFragment = new EmailFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in_from_down, 0);
            ft.add(containerId, mEmailFragment, EmailFragment.TAG).commit();
        }
        else
        {
            Logger.d(TAG, "addEmailFragment() - mEmailFragment is not null");
        }
    }
}

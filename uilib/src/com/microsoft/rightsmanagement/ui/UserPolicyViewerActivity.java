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

import java.util.LinkedHashSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.UserPolicy;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;
import com.microsoft.rightsmanagement.ui.model.UserPolicyModel;
import com.microsoft.rightsmanagement.ui.utils.CallbackManager;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.ui.widget.UserPolicyViewerFragment;

/**
 * An Activity to control User Policy Viewer UI.
 */
public class UserPolicyViewerActivity extends BaseActivity implements
        UserPolicyViewerFragment.UserPolicyViewerFragmentEventListener, UserPolicyViewerFragment.UserPolicyDataProvider
{
    /**
     * Represents User Policy Viewer Activity Request Options.
     */
    public class UserPolicyViewerActivityRequestOption
    {
        /**
         * Enable edit user policy button.
         * <p>
         * Note: If a user is owner, he may be allowed to edit the user policy.
         * </p>
         */
        public static final int EDIT_ALLOWED = 0x1;
        public static final int NONE = 0x0;
    }

    /**
     * Represents User Policy Viewer Activity Result.
     */
    public class UserPolicyViewerActivityResult
    {
        /**
         * User requested to Edit policy.
         */
        public static final int EDIT_POLICY = 1;
        public static final int NONE = 0;
    }
    protected static final String RESULT_POLICY_VIEWER = "RESULT_POLICY_VIEWER";
    private static final String REQUEST_RESULT_POLICY_VIEWER_OPTIONS = "REQUEST_RESULT_POLICY_VIEWER_OPTIONS";
    private static final String REQUEST_RESULT_USER_POLICY_MODEL = "REQUEST_RESULT_USER_POLICY_MODEL";
    private static CallbackManager<Integer, Void> sCallbackManager = new CallbackManager<Integer, Void>();
    private UserPolicyModel mUserPolicyModel;
    private int mUserPolicyViewerActivityRequestOption;
    private UserPolicyViewerFragment mUserPolicyViewerFragment;
    static
    {
        setTAG("UserPolicyViewerActivity");
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
        if (data == null)
        {
            Logger.i(TAG, "System closed the activity", "");
            return;
        }
        try
        {
            final Bundle extras = data.getExtras();
            requestCallbackId = extras.getInt(REQUEST_CALLBACK_ID);
            final CompletionCallback<Integer> callback = sCallbackManager.getWaitingRequest(requestCallbackId);
            switch (resultCode)
            {
                case RESULT_OK:
                    Logger.i(TAG, "resultCode=RESULT_OK", "");
                    int result = extras.getInt(RESULT_POLICY_VIEWER);
                    callback.onSuccess(result);
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
     * @param requestCode the request code for startActivityForResult
     * @param parentActivity the parent activity that invokes startActivityForResult
     * @param userPolicy user policy instance that provides data to display on the UI
     * @param supportedRights rights to check access for and display
     * @param policyViewerActivityRequestOption PolicyViewerActivityRequestOptions
     * @param policyViewerActivityCompletionCallback callback that's invoked upon completion of activity.
     * @throws InvalidParameterException the invalid parameter exception
     */
    public static void show(int requestCode,
                            Activity parentActivity,
                            UserPolicy userPolicy,
                            LinkedHashSet<String> supportedRights,
                            int policyViewerActivityRequestOption,
                            CompletionCallback<Integer> policyViewerActivityCompletionCallback)
            throws InvalidParameterException
    {
        Logger.ms(TAG, "show");
        parentActivity = validateActivityInputParameter(parentActivity);
        userPolicy = validateUserPolicyInputParameter(userPolicy);
        policyViewerActivityCompletionCallback = validateCompletionCallbackInputParameter(policyViewerActivityCompletionCallback);
        policyViewerActivityRequestOption = validatePolicyViewerActivityRequestOption(policyViewerActivityRequestOption);
        // put callback
        int requestCallbackId = policyViewerActivityCompletionCallback.hashCode();
        sCallbackManager.putWaitingRequest(requestCallbackId, policyViewerActivityCompletionCallback);
        // set launch intent
        Intent intent = new Intent(parentActivity, UserPolicyViewerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(REQUEST_CALLBACK_ID, requestCallbackId);
        intent.putExtra(REQUEST_RESULT_POLICY_VIEWER_OPTIONS, policyViewerActivityRequestOption);
        intent.putExtra(REQUEST_RESULT_USER_POLICY_MODEL,
                (new UserPolicyModel(userPolicy, supportedRights, parentActivity.getApplicationContext())));
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        parentActivity.startActivityForResult(intent, requestCode);
        Logger.me(TAG, "show");
    }

    /**
     * Validate policy viewer activity request option.
     * 
     * @param policyViewerActivityRequestOption the policy viewer activity request option
     * @return the int
     * @throws InvalidParameterException the invalid parameter exception
     */
    private static int validatePolicyViewerActivityRequestOption(int policyViewerActivityRequestOption)
            throws InvalidParameterException
    {
        if (policyViewerActivityRequestOption < UserPolicyViewerActivityRequestOption.NONE
                || policyViewerActivityRequestOption > UserPolicyViewerActivityRequestOption.EDIT_ALLOWED)
        {
            InvalidParameterException exception = new InvalidParameterException();
            Logger.e(TAG, "invalid parameter policyViewerActivityRequestOption", "", exception);
            throw exception;
        }
        return policyViewerActivityRequestOption;
    }

    /**
     * Validate user policy input parameter.
     * 
     * @param userPolicy the user policy
     * @return the user policy
     * @throws InvalidParameterException the invalid parameter exception
     */
    private static UserPolicy validateUserPolicyInputParameter(UserPolicy userPolicy) throws InvalidParameterException
    {
        if (userPolicy == null)
        {
            InvalidParameterException exception = new InvalidParameterException();
            Logger.e(TAG, "invalid parameter userPolicy", "", exception);
            throw exception;
        }
        return userPolicy;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.microsoft.rightsmanagement.ui.widget.UserPolicyViewerFragment.UserPolicyDataProvider#getUserPolicyModel()
     */
    @Override
    public UserPolicyModel getUserPolicyModel()
    {
        return mUserPolicyModel;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.microsoft.rightsmanagement.ui.widget.UserPolicyViewerFragment.UserPolicyDataProvider#isUserPolicyEditingEnabled
     * ()
     */
    @Override
    public boolean isUserPolicyEditingEnabled()
    {
        // TODO update logic later
        return (mUserPolicyViewerActivityRequestOption & UserPolicyViewerActivityRequestOption.EDIT_ALLOWED) == UserPolicyViewerActivityRequestOption.EDIT_ALLOWED;
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.widget.UserPolicyViewerFragment.UserPolicyViewerFragmentEventListener#
     * onEditButtonClicked()
     */
    @Override
    public void onEditButtonClicked()
    {
        Logger.ms(TAG, "onEditButtonClicked");
        Intent data = new Intent();
        data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
        data.putExtra(RESULT_POLICY_VIEWER, UserPolicyViewerActivityResult.EDIT_POLICY);
        returnToCaller(RESULT_OK, data);
        Logger.me(TAG, "onEditButtonClicked");
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
        int layoutId = R.layout.user_policy_viewer_activity_layout;
        setContentView(layoutId);
        Intent intent = getIntent();
        mRequestCallbackId = intent.getIntExtra(REQUEST_CALLBACK_ID, 0);
        mUserPolicyViewerActivityRequestOption = intent.getIntExtra(REQUEST_RESULT_POLICY_VIEWER_OPTIONS,
                UserPolicyViewerActivityRequestOption.NONE);
        Parcelable possbileUserPolicyModelInstance = intent.getParcelableExtra(REQUEST_RESULT_USER_POLICY_MODEL);
        try
        {
            mUserPolicyModel = (UserPolicyModel)possbileUserPolicyModelInstance;
        }
        catch (ClassCastException ex)
        {
            Logger.ie(TAG, ex.getMessage());
        }
        addUserPolicyViewerFragment();
        addTransparentPartDismissListener(R.id.user_policy_viewer_transparent_part);
        // create fader animators
        createBgAnimators(R.id.user_policy_viewer_base_container, savedInstanceState);
        Logger.me(TAG, "onCreate");
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onDestroy()
     */
    @Override
    protected void onDestroy()
    {
        if ((isFinishing() == true) && (mActivityFinishedWithResult == false))
        {
            sCallbackManager.removeWaitingRequest(mRequestCallbackId);
        }
        super.onDestroy();
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
        super.returnToCaller(resultCode, data);
        Logger.d(TAG, String.format("ReturnToCaller - resultCode=%d", resultCode));
        setResult(resultCode, data);
        if (mUserPolicyViewerFragment == null)
        {
            this.finish();
        }
        else
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out);
            ft.remove(mUserPolicyViewerFragment).commit();
            mUserPolicyViewerFragment = null;
            startActivityEndAnimationAndFinishActivity();
        }
    }

    /**
     * Adds the user policy viewer fragment.
     */
    private void addUserPolicyViewerFragment()
    {
        int containerId = R.id.user_policy_viewer_container;
        mUserPolicyViewerFragment = (UserPolicyViewerFragment)getSupportFragmentManager().findFragmentByTag(
                UserPolicyViewerFragment.TAG);
        if (mUserPolicyViewerFragment == null)
        {
            Logger.d(TAG, "addUserPolicyViewerFragment - mUserPolicyViewerFragment is null");
            mUserPolicyViewerFragment = new UserPolicyViewerFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in, 0);
            ft.add(containerId, mUserPolicyViewerFragment, UserPolicyViewerFragment.TAG).commit();
        }
        else
        {
            Logger.d(TAG, "addUserPolicyViewerFragment - mUserPolicyViewerFragment is not null");
        }
    }
}

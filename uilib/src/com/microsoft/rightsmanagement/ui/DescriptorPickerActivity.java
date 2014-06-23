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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.microsoft.rightsmanagement.TemplateDescriptor;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;
import com.microsoft.rightsmanagement.ui.DescriptorPickerActivityResult.DescriptorPickerActivityResultType;
import com.microsoft.rightsmanagement.ui.model.CustomDescriptorModel;
import com.microsoft.rightsmanagement.ui.model.DescriptorModel;
import com.microsoft.rightsmanagement.ui.utils.CallbackManager;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.ui.widget.ContentExpirationFragment;
import com.microsoft.rightsmanagement.ui.widget.ContentExpirationListFragment;
import com.microsoft.rightsmanagement.ui.widget.CustomDescriptorDetailsFragment;
import com.microsoft.rightsmanagement.ui.widget.DescriptorListFragment;
import com.microsoft.rightsmanagement.ui.widget.DescriptorPickerFragment;

/**
 * An Activity to control Descriptor Picker UI.
 */
public class DescriptorPickerActivity extends BaseActivity implements
        DescriptorPickerFragment.ProtectionButtonEventListener, DescriptorListFragment.DescriptorDataProvider,
        DescriptorListFragment.DescriptorListEventListener,
        CustomDescriptorDetailsFragment.CustomPolicyDurationPickerListner,
        ContentExpirationListFragment.ContentExpirationListEventListner
{
    private static final String CURRENT_SELECTED_DESCRIPTOR_INDEX = "CURRENT_SELECTED_DESCRIPTOR_INDEX";
    private static final String IS_CONTENT_EXPIRES_FRAGMENT_VISIBLE = "IS_CONTENT_EXPIRES_FRAGMENT_VISIBLE";
    private static final String REQUEST_ORIGINAL_DESCRIPTOR_ITEM = "REQUEST_ORIGINAL_DESCRIPTOR_ITEM";
    private static final String REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY = "REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY";
    private static final String RESULT_DESCRIPTOR_ITEM = "RESULT_DESCRIPTOR_ITEM";
    private static final String REQUEST_ALLOW_ORIGINAL_POLICY_REAPPLY = "REQUEST_ALLOW_ORIGINAL_POLICY_REAPPLY";
    private static CallbackManager<DescriptorPickerActivityResult, TemplateDescriptor[]> sCallbackManager = new CallbackManager<DescriptorPickerActivityResult, TemplateDescriptor[]>();
    private ContentExpirationFragment mContentExpirationFragment;
    private int mCurrentSelectedDescriptorItemIndex = -1;
    private DescriptorModel[] mDescriptorItemArray;
    private DescriptorPickerFragment mDescriptorPickerFragment;
    private boolean mIsContentExpiresFragmentVisible = false;
    private boolean mAllowOriginalPolicyReApply = false;
    private DescriptorModel mOriginalDescriptorItem;
    static
    {
        setTAG("DescriptorPickerActivity");
    }

    /**
     * Processes the result of DescriptorPickerActivity started via startActivityForResult from the parent activity, and
     * invokes the callback supplied to show(). This method must be called from parent Activity's onActivityResult.
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
            final CompletionCallback<DescriptorPickerActivityResult> callback = sCallbackManager
                    .getWaitingRequest(requestCallbackId);
            switch (resultCode)
            {
                case RESULT_OK:
                    Logger.i(TAG, "resultCode=RESULT_OK", "");
                    Parcelable result = extras.getParcelable(RESULT_DESCRIPTOR_ITEM);
                    DescriptorPickerActivityResult descriptorPickerActivityResult = new DescriptorPickerActivityResult();
                    DescriptorModel descriptorItem = (DescriptorModel)result;
                    if (descriptorItem instanceof CustomDescriptorModel)
                    {
                        Logger.d(TAG, "custom policy was choosen");
                        descriptorPickerActivityResult.mPolicyDescriptor = ((CustomDescriptorModel)descriptorItem)
                                .createPolicyDescriptorFromModel();
                        descriptorPickerActivityResult.mResultType = DescriptorPickerActivityResultType.Custom;
                    }
                    else
                    {
                        Logger.d(TAG, "Template policy was choosen");
                        TemplateDescriptor[] savedTemplateDescriptors = sCallbackManager.getState(requestCallbackId);
                        descriptorPickerActivityResult.mTemplateDescriptor = descriptorItem
                                .find(savedTemplateDescriptors);
                        descriptorPickerActivityResult.mResultType = DescriptorPickerActivityResultType.Template;
                    }
                    callback.onSuccess(descriptorPickerActivityResult);
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
     * @param requestCode the request code
     * @param activity the activity
     * @param templateDescriptorList the template descriptor list
     * @param originalTemplateDescriptor the original template descriptor
     * @param allowOriginalPolicyReApply enables apply button even if original policy is selected
     * @param pickerCompletionCallback the picker completion callback
     * @throws InvalidParameterException the invalid parameter exception
     */
    public static void show(int requestCode,
                            Activity activity,
                            List<TemplateDescriptor> templateDescriptorList,
                            TemplateDescriptor originalTemplateDescriptor,
                            boolean allowOriginalPolicyReApply,
                            CompletionCallback<DescriptorPickerActivityResult> pickerCompletionCallback)
            throws InvalidParameterException
    {
        Logger.ms(TAG, "show");
        activity = validateActivityInputParameter(activity);
        templateDescriptorList = validateTemplateDescriptorListInputParameter(templateDescriptorList);
        pickerCompletionCallback = validateCompletionCallbackInputParameter(pickerCompletionCallback);
        int requestCallbackId = pickerCompletionCallback.hashCode();
        TemplateDescriptor[] templateDescriptorArray = new TemplateDescriptor[templateDescriptorList.size()];
        templateDescriptorList.toArray(templateDescriptorArray); // fill the array
        sCallbackManager.putWaitingRequest(requestCallbackId, pickerCompletionCallback, templateDescriptorArray);
        Intent intent = new Intent(activity, DescriptorPickerActivity.class);
        // translate MSIPC SDK object model to UI model
        DescriptorModel[] templateDescriptorItemArray = DescriptorModel.create(templateDescriptorArray);
        DescriptorModel originalTemplateDescriptorItem = null;
        if (originalTemplateDescriptor != null)
        {
            originalTemplateDescriptorItem = new DescriptorModel(originalTemplateDescriptor);
        }
        // start activity
        intent.putExtra(REQUEST_CALLBACK_ID, requestCallbackId);
        intent.putExtra(REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY, templateDescriptorItemArray);
        intent.putExtra(REQUEST_ORIGINAL_DESCRIPTOR_ITEM, originalTemplateDescriptorItem);
        intent.putExtra(REQUEST_ALLOW_ORIGINAL_POLICY_REAPPLY, allowOriginalPolicyReApply);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivityForResult(intent, requestCode);
        Logger.me(TAG, "show");
    }

    /**
     * Validate template descriptor list input parameter.
     * 
     * @param templateDescriptorList the template descriptor list
     * @return the list
     * @throws InvalidParameterException the invalid parameter exception
     */
    private static List<TemplateDescriptor> validateTemplateDescriptorListInputParameter(List<TemplateDescriptor> templateDescriptorList)
            throws InvalidParameterException
    {
        if (templateDescriptorList == null)
        {
            InvalidParameterException exception = new InvalidParameterException();
            Logger.e(TAG, "invalid parameter templateDescriptorList", "", exception);
            throw exception;
        }
        return templateDescriptorList;
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.widget.DescriptorListFragment.DescriptorDataProvider#getDescriptorItems()
     */
    @Override
    public DescriptorModel[] getDescriptorItems()
    {
        return mDescriptorItemArray;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.microsoft.rightsmanagement.ui.widget.DescriptorListFragment.DescriptorDataProvider#getSelectedDescriptorItemIndex
     * ()
     */
    @Override
    public int getSelectedDescriptorItemIndex()
    {
        return mCurrentSelectedDescriptorItemIndex;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onBackPressed()
     */
    @Override
    public void onBackPressed()
    {
        Logger.ms(TAG, "onBackPressed");
        if (mIsContentExpiresFragmentVisible)
        {
            toggleFragmentView();
        }
        else if (mDescriptorPickerFragment.isCustomDescriptorDetailsFragmentVisible())
        {
            mDescriptorPickerFragment.showTemplateListFragment();
        }
        else
        {
            Intent data = new Intent();
            data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
            returnToCaller(RESULT_CANCELED, data);
        }
        Logger.me(TAG, "onBackPressed");
    }

    /**
     * When client has chosen a date , update content expires field with the calculated date
     */
    @Override
    public void onContentExpirationListItemSelected(Date contentExpiryDate)
    {
        Logger.d(TAG, "Showing DescriptorFragment");
        FragmentManager childFragmentManager = mDescriptorPickerFragment.getChildFragmentManager();
        CustomDescriptorDetailsFragment customPolicyViewerFragment = (CustomDescriptorDetailsFragment)childFragmentManager
                .findFragmentByTag(CustomDescriptorDetailsFragment.TAG);
        if (customPolicyViewerFragment != null)
        {
            customPolicyViewerFragment.updatePolicyDuration(contentExpiryDate);
            toggleFragmentView();
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.microsoft.rightsmanagement.ui.widget.DescriptorListFragment.DescriptorListEventListener#onDescriptorItemSelected
     * (int)
     */
    @Override
    public void onDescriptorItemSelected(int selectedDescriptorItemIndex)
    {
        Logger.ms(TAG, "onDescriptorItemSelected");
        if (mDescriptorPickerFragment == null)
            return;
        // enable protection button if a descriptor item is selected
        // but don't enable protection button if selected list item is same as original selected list item
        // unless we want to allow user to apply previously selected policy
        DescriptorModel currentSelectedDescriptorItem = mDescriptorItemArray[selectedDescriptorItemIndex];
        mCurrentSelectedDescriptorItemIndex = selectedDescriptorItemIndex;
        if (mOriginalDescriptorItem == null
                ||  mAllowOriginalPolicyReApply  
                || (mOriginalDescriptorItem != null 
                && !currentSelectedDescriptorItem.equals(mOriginalDescriptorItem)))
        {
            mDescriptorPickerFragment.setProtectionButtonEnabled(true);
        }
        else
        {
            mDescriptorPickerFragment.setProtectionButtonEnabled(false);
        }
        Logger.me(TAG, "onDescriptorItemSelected");
    }

    /***
     * When duration is clicked we need to launch Content Expires screen
     */
    @Override
    public void onDurationClicked()
    {
        Logger.d(TAG, "Showing contentExpires screen to change adhoc policy contentExpiry");
        toggleFragmentView();
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.widget.DescriptorPickerFragment.ProtectionButtonEventListener#
     * onProtectionButtonClicked()
     */
    @Override
    public void onProtectionButtonClicked()
    {
        Logger.ms(TAG, "onProtectionButtonClicked");
        if (mDescriptorPickerFragment.isCustomDescriptorDetailsFragmentVisible())
        {
            Intent data = new Intent();
            data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
            data.putExtra(RESULT_DESCRIPTOR_ITEM, mDescriptorItemArray[mCurrentSelectedDescriptorItemIndex]);
            returnToCaller(RESULT_OK, data);
            Logger.me(TAG, "onProtectionButtonClicked. Custom descriptor was selected");
        }
        else
        {
            DescriptorModel choosenDescriptor = mDescriptorItemArray[mCurrentSelectedDescriptorItemIndex];
            if (choosenDescriptor instanceof CustomDescriptorModel)
            {
                mDescriptorPickerFragment.showCustomDescriptorDetailsFragment((CustomDescriptorModel)choosenDescriptor);
                Logger.me(TAG, "onProtectionButtonClicked. Showing custom descriptor details");
            }
            else
            {
                Intent data = new Intent();
                data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
                data.putExtra(RESULT_DESCRIPTOR_ITEM, mDescriptorItemArray[mCurrentSelectedDescriptorItemIndex]);
                returnToCaller(RESULT_OK, data);
                Logger.me(TAG, "onProtectionButtonClicked. Template descriptor was selected");
            }
        }
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
        setContentView(R.layout.descriptor_picker_activity_layout);
        if (savedInstanceState == null)
        {
            Logger.d(TAG, "bundle is null");
            // creation from scratch
            Intent intent = getIntent();
            Bundle argumentsBundle = intent.getExtras();
            parseBundleInput(argumentsBundle);
            updateDescriptorArrayWithCustomTemplates();
            // get value in mCurrentSelectedtemplateDescriptorIndex based on mOriginalTemplateDescriptor
            for (int i = 0; i < mDescriptorItemArray.length; i++)
            {
                if (mDescriptorItemArray[i].equals(mOriginalDescriptorItem))
                {
                    mCurrentSelectedDescriptorItemIndex = i;
                    break;
                }
            }
        }
        else
        {
            Logger.d(TAG, "bundle is not null");
            // creation from saved state
            parseBundleInput(savedInstanceState);
        }
        addDescriptorPickerFragment();
        addContentExpiresFragment();
        addTransparentPartDismissListener(R.id.descriptor_picker_transparent_part);
        // create fader animators
        createBgAnimators(R.id.descriptor_picker_base_container, savedInstanceState);
    }

    /*
     * (non-Javadoc)
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

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Logger.ms(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(REQUEST_CALLBACK_ID, mRequestCallbackId);
        outState.putParcelableArray(REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY, mDescriptorItemArray);
        outState.putParcelable(REQUEST_ORIGINAL_DESCRIPTOR_ITEM, mOriginalDescriptorItem);
        outState.putInt(CURRENT_SELECTED_DESCRIPTOR_INDEX, mCurrentSelectedDescriptorItemIndex);
        outState.putBoolean(IS_CONTENT_EXPIRES_FRAGMENT_VISIBLE, mIsContentExpiresFragmentVisible);
        outState.putBoolean(REQUEST_ALLOW_ORIGINAL_POLICY_REAPPLY, mAllowOriginalPolicyReApply);
        Logger.me(TAG, "onSaveInstanceState");
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
        if (mDescriptorPickerFragment == null)
        {
            this.finish();
        }
        else
        {
            mDescriptorPickerFragment.removeChildFragments();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out);
            ft.remove(mDescriptorPickerFragment).commit();
            mDescriptorPickerFragment = null;
            startActivityEndAnimationAndFinishActivity();
        }
    }

    /**
     * Adds the content Expires fragment
     */
    private void addContentExpiresFragment()
    {
        // show the widget
        int containerId = R.id.descriptor_picker_container;
        mContentExpirationFragment = (ContentExpirationFragment)getSupportFragmentManager().findFragmentByTag(
                ContentExpirationFragment.TAG);
        if (mContentExpirationFragment == null)
        {
            Logger.d(TAG, "Content expires fragment is null");
            mContentExpirationFragment = new ContentExpirationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(containerId, mContentExpirationFragment, ContentExpirationFragment.TAG)
                    .hide(mContentExpirationFragment).commit();
        }
        else
        {
            // if not visible hide it , because it will get rendered on the screen in case of screen rotation if not
            // hidden
            if (!mIsContentExpiresFragmentVisible)
            {
                getSupportFragmentManager().beginTransaction().hide(mContentExpirationFragment).commit();
            }
            Logger.d(TAG, "Content expires fragment is not null");
        }
    }

    /**
     * Adds the descriptor picker fragment.
     */
    private void addDescriptorPickerFragment()
    {
        // show the widget
        int containerId = R.id.descriptor_picker_container;
        mDescriptorPickerFragment = (DescriptorPickerFragment)getSupportFragmentManager().findFragmentByTag(
                DescriptorPickerFragment.TAG);
        if (mDescriptorPickerFragment == null)
        {
            Logger.d(TAG, "addDescriptorPickerFragment - mDescriptorPickerFragment is null");
            mDescriptorPickerFragment = new DescriptorPickerFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in, 0);
            ft.add(containerId, mDescriptorPickerFragment, DescriptorPickerFragment.TAG).commit();
        }
        else
        {
            if (mIsContentExpiresFragmentVisible)
            {
                getSupportFragmentManager().beginTransaction().hide(mDescriptorPickerFragment).commit();
            }
            Logger.d(TAG, "addDescriptorPickerFragment - mDescriptorPickerFragment is not null");
        }
    }

    /**
     * Retrieve data from intent.
     * 
     * @param bundle the bundle
     */
    private void parseBundleInput(Bundle bundle)
    {
        if (bundle.containsKey(REQUEST_ORIGINAL_DESCRIPTOR_ITEM))
        {
            Logger.d(TAG, "parseBundleInput - parsing OriginalTemplateDescriptorItem");
            Parcelable requestOriginalTemplateDescriptor = bundle
                    .getParcelable(DescriptorPickerActivity.REQUEST_ORIGINAL_DESCRIPTOR_ITEM);
            if (requestOriginalTemplateDescriptor != null)
            {
                Logger.ie(TAG, "requestOriginalTemplateDescriptor is null");
            }
            try
            {
                mOriginalDescriptorItem = (DescriptorModel)requestOriginalTemplateDescriptor;
            }
            catch (ClassCastException ex)
            {
                Logger.ie(TAG, ex.getMessage());
                throw ex;
            }
        }
        if (bundle.containsKey(REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY))
        {
            Logger.d(TAG, "parseBundleInput - parsing TemplateDescriptorItemArray");
            Parcelable[] requestArray = bundle
                    .getParcelableArray(DescriptorPickerActivity.REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY);
            try
            {
                Object[] requestObjectArray = requestArray;
                mDescriptorItemArray = Arrays.copyOf(requestObjectArray, requestObjectArray.length,
                        DescriptorModel[].class);
            }
            catch (ClassCastException ex)
            {
                Logger.ie(TAG, ex.getMessage());
                throw ex;
            }
        }
        if (bundle.containsKey(REQUEST_CALLBACK_ID))
        {
            Logger.d(TAG, "parseBundleInput - parsing RequestCallbackId");
            mRequestCallbackId = bundle.getInt(REQUEST_CALLBACK_ID);
        }
        if (bundle.containsKey(CURRENT_SELECTED_DESCRIPTOR_INDEX))
        {
            Logger.d(TAG, "parseBundleInput - parsing CurrentSelectedtemplateDescriptorItemIndex");
            mCurrentSelectedDescriptorItemIndex = bundle.getInt(CURRENT_SELECTED_DESCRIPTOR_INDEX);
        }
        if (bundle.containsKey(IS_CONTENT_EXPIRES_FRAGMENT_VISIBLE))
        {
            Logger.d(TAG, "parseBundleInput - parsing isContentExpiresFragmentVisible");
            mIsContentExpiresFragmentVisible = bundle.getBoolean(IS_CONTENT_EXPIRES_FRAGMENT_VISIBLE);
        }
        if(bundle.containsKey(REQUEST_ALLOW_ORIGINAL_POLICY_REAPPLY))
        {
            Logger.d(TAG, "parseBundleInput - parsing allowOriginalPolicyApply");
            mAllowOriginalPolicyReApply = bundle.getBoolean(REQUEST_ALLOW_ORIGINAL_POLICY_REAPPLY);
        }
    }

    /**
     * Toggles between showing ContentExpiresFragment and DescriptorPickerFragment
     */
    private void toggleFragmentView()
    {
        Logger.ms(TAG, "toggleFragmentView");
        mContentExpirationFragment = (ContentExpirationFragment)getSupportFragmentManager().findFragmentByTag(
                ContentExpirationFragment.TAG);
        mDescriptorPickerFragment = (DescriptorPickerFragment)getSupportFragmentManager().findFragmentByTag(
                DescriptorPickerFragment.TAG);
        if (mContentExpirationFragment != null && mDescriptorPickerFragment != null)
        {
            if (mIsContentExpiresFragmentVisible)
            {
                mIsContentExpiresFragmentVisible = false;
                getSupportFragmentManager().beginTransaction().hide(mContentExpirationFragment)
                        .show(mDescriptorPickerFragment).addToBackStack(null).commit();
            }
            else
            {
                mIsContentExpiresFragmentVisible = true;
                getSupportFragmentManager().beginTransaction().hide(mDescriptorPickerFragment)
                        .show(mContentExpirationFragment).addToBackStack(null).commit();
            }
        }
        else
        {
            Logger.ie(TAG, "ContentExpiresFragment and DescriptorPickerFragment should both be initialized");
        }
        Logger.me(TAG, "toggleFragmentView");
    }

    /**
     * Update descriptor array with custom descriptors.
     */
    private void updateDescriptorArrayWithCustomTemplates()
    {
        List<CustomDescriptorModel> customPermissionPickerList = CustomDescriptorModel.create(getApplicationContext());
        CustomDescriptorModel[] customPermissionPickerItems = customPermissionPickerList
                .toArray(new CustomDescriptorModel[customPermissionPickerList.size()]);
        // resize to put customDescriptors at top
        DescriptorModel[] fullTemplateDescriptorItemArray = new DescriptorModel[mDescriptorItemArray.length
                + customPermissionPickerItems.length];
        System.arraycopy(customPermissionPickerItems, 0, fullTemplateDescriptorItemArray, 0,
                customPermissionPickerItems.length);
        System.arraycopy(mDescriptorItemArray, 0, fullTemplateDescriptorItemArray, customPermissionPickerItems.length,
                mDescriptorItemArray.length);
        mDescriptorItemArray = fullTemplateDescriptorItemArray;
    }
}

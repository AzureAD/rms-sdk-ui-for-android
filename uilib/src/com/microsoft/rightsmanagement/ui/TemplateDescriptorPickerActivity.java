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
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.TemplateDescriptor;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;
import com.microsoft.rightsmanagement.ui.TemplateDescriptorPickerActivityResult.TemplateDescriptorPickerActivityResultType;
import com.microsoft.rightsmanagement.ui.model.TemplateDescriptorModel;
import com.microsoft.rightsmanagement.ui.utils.CallbackManager;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.ui.widget.TemplateDescriptorListFragment;
import com.microsoft.rightsmanagement.ui.widget.TemplateDescriptorPickerFragment;

/**
 * An Activity to control Template Descriptor Picker UI.
 */
public class TemplateDescriptorPickerActivity extends BaseActivity implements
        TemplateDescriptorPickerFragment.ProtectionButtonEventListener,
        TemplateDescriptorListFragment.TemplateDescriptorDataProvider,
        TemplateDescriptorListFragment.TemplateDescriptorListEventListener
{
    private static final String CURRENT_SELECTED_TEMPLATE_DESCRIPTOR_INDEX = "CURRENT_SELECTED_TEMPLATE_DESCRIPTOR_INDEX";
    private static final String REQUEST_ORIGINAL_TEMPLATE_DESCRIPTOR_ITEM = "REQUEST_ORIGINAL_TEMPLATE_DESCRIPTOR_ITEM";
    private static final String REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY = "REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY";
    private static final String RESULT_TEMPLATE_DESCRIPTOR_ITEM = "RESULT_TEMPLATE_DESCRIPTOR_ITEM";
    private static CallbackManager<TemplateDescriptorPickerActivityResult, TemplateDescriptor[]> sCallbackManager = new CallbackManager<TemplateDescriptorPickerActivityResult, TemplateDescriptor[]>();
    static
    {
        setTAG("TemplateDescriptorPickerActivity");
    }

    /**
     * Temporary fix to disable custom permissions item. This will be replaced by navigation link to Custom Permission
     * UI.
     * 
     * @param templateDescriptorItem the template descriptor item
     * @return true, if is template descriptor item enabled
     */
    public static boolean isTemplateDescriptorItemEnabled(TemplateDescriptorModel templateDescriptorItem)
    {
        return !templateDescriptorItem.isCustomPermissionsTemplateDescriptorItem();
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
            final CompletionCallback<TemplateDescriptorPickerActivityResult> callback = sCallbackManager
                    .getWaitingRequest(requestCallbackId);
            switch (resultCode)
            {
                case RESULT_OK:
                    Logger.i(TAG, "resultCode=RESULT_OK", "");
                    Parcelable result = extras.getParcelable(RESULT_TEMPLATE_DESCRIPTOR_ITEM);
                    TemplateDescriptorPickerActivityResult templateDescriptorPickerActivityResult = new TemplateDescriptorPickerActivityResult();
                    TemplateDescriptorModel templateDescriptorItem = (TemplateDescriptorModel)result;
                    if (templateDescriptorItem.isNoProtectionTemplateDescriptorItem())
                    {
                        Logger.d(TAG, "in templateDescriptorItem.isNoProtectionTemplateDescriptorItem()");
                        templateDescriptorPickerActivityResult.mTemplateDescriptor = null;
                        templateDescriptorPickerActivityResult.mResultType = TemplateDescriptorPickerActivityResultType.Default;
                    }
                    else if (templateDescriptorItem.isCustomPermissionsTemplateDescriptorItem())
                    {
                        Logger.d(TAG, "in templateDescriptorItem.isCustomPermissionsTemplateDescriptorItem()");
                        templateDescriptorPickerActivityResult.mTemplateDescriptor = null;
                        templateDescriptorPickerActivityResult.mResultType = TemplateDescriptorPickerActivityResultType.ShowPolicyDescriptorPicker;
                    }
                    else
                    {
                        Logger.d(
                                TAG,
                                "neither templateDescriptorItem.isNoProtectionTemplateDescriptorItem(), nor templateDescriptorItem.isCustomPermissionsTemplateDescriptorItem()");
                        TemplateDescriptor[] savedTemplateDescriptors = sCallbackManager.getState(requestCallbackId);
                        templateDescriptorPickerActivityResult.mTemplateDescriptor = templateDescriptorItem
                                .find(savedTemplateDescriptors);
                        templateDescriptorPickerActivityResult.mResultType = TemplateDescriptorPickerActivityResultType.Default;
                    }
                    callback.onSuccess(templateDescriptorPickerActivityResult);
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
     * @param pickerCompletionCallback the picker completion callback
     * @throws InvalidParameterException the invalid parameter exception
     */
    public static void show(int requestCode,
                            Activity activity,
                            List<TemplateDescriptor> templateDescriptorList,
                            TemplateDescriptor originalTemplateDescriptor,
                            CompletionCallback<TemplateDescriptorPickerActivityResult> pickerCompletionCallback)
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
        Intent intent = new Intent(activity, TemplateDescriptorPickerActivity.class);
        // translate MSIPC SDK object model to UI model
        TemplateDescriptorModel[] templateDescriptorItemArray = TemplateDescriptorModel.create(templateDescriptorArray);
        TemplateDescriptorModel originalTemplateDescriptorItem = null;
        if (originalTemplateDescriptor != null)
        {
            originalTemplateDescriptorItem = new TemplateDescriptorModel(originalTemplateDescriptor);
        }
        // start activity
        intent.putExtra(REQUEST_CALLBACK_ID, requestCallbackId);
        intent.putExtra(REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY, templateDescriptorItemArray);
        intent.putExtra(REQUEST_ORIGINAL_TEMPLATE_DESCRIPTOR_ITEM, originalTemplateDescriptorItem);
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
    private int mCurrentSelectedtemplateDescriptorItemIndex = -1;
    private TemplateDescriptorModel mCustomPermissionDescriptorFakeItem;
    private TemplateDescriptorModel mNoProtectionDescriptorFakeItem;
    private TemplateDescriptorModel mOriginalTemplateDescriptorItem;
    private TemplateDescriptorModel[] mTemplateDescriptorItemArray;
    private TemplateDescriptorPickerFragment mTemplateDescriptorPickerFragment;

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.widget.TemplateDescriptorListFragment.TemplateDescriptorDataProvider#
     * getmSelectedTemplateDescriptorItemIndex()
     */
    @Override
    public int getSelectedTemplateDescriptorItemIndex()
    {
        return mCurrentSelectedtemplateDescriptorItemIndex;
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.widget.TemplateDescriptorListFragment.TemplateDescriptorDataProvider#
     * getTemplateDescritorItems()
     */
    @Override
    public TemplateDescriptorModel[] getTemplateDescriptorItems()
    {
        return mTemplateDescriptorItemArray;
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.widget.TemplateDescriptorPickerFragment.
     * TemplateDescriptorPickerFragmentEventListener#onProtectionButtonClicked()
     */
    @Override
    public void onProtectionButtonClicked()
    {
        Logger.ms(TAG, "onProtectionButtonClicked");
        Intent data = new Intent();
        data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
        data.putExtra(RESULT_TEMPLATE_DESCRIPTOR_ITEM,
                mTemplateDescriptorItemArray[mCurrentSelectedtemplateDescriptorItemIndex]);
        returnToCaller(RESULT_OK, data);
        Logger.me(TAG, "onProtectionButtonClicked");
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.widget.TemplateDescriptorPickerFragment.
     * TemplateDescriptorPickerFragmentEventListener#onTemplateDescriptorItemSelected(int)
     */
    @Override
    public void onTemplateDescriptorItemSelected(int selectedTemplateDescriptorItemIndex)
    {
        Logger.ms(TAG, "onTemplateDescriptorItemSelected");
        if (mTemplateDescriptorPickerFragment == null)
            return;
        // enable protection button if a template is selected
        // but don't enable protection button if selected template is same as original template
        TemplateDescriptorModel currentSelectedTemplateDescriptorItem = mTemplateDescriptorItemArray[selectedTemplateDescriptorItemIndex];
        mCurrentSelectedtemplateDescriptorItemIndex = selectedTemplateDescriptorItemIndex;
        if (mOriginalTemplateDescriptorItem == null
                || (mOriginalTemplateDescriptorItem != null && !currentSelectedTemplateDescriptorItem.getId().equals(
                        mOriginalTemplateDescriptorItem.getId())))
        {
            mTemplateDescriptorPickerFragment.setProtectionButtonEnabled(true);
        }
        else
        {
            mTemplateDescriptorPickerFragment.setProtectionButtonEnabled(false);
        }
        Logger.me(TAG, "onTemplateDescriptorItemSelected");
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
        setContentView(R.layout.tempate_descriptor_picker_activity_layout);
        if (savedInstanceState == null)
        {
            Logger.d(TAG, "bundle is null");
            // creation from scratch
            Intent intent = getIntent();
            Bundle argumentsBundle = intent.getExtras();
            parseBundleInput(argumentsBundle);
            updateTemplateDescriptorArrayWithFakeTemplates();
            // get value in mCurrentSelectedtemplateDescriptorIndex based on mOriginalTemplateDescriptor
            for (int i = 0; i < mTemplateDescriptorItemArray.length; i++)
            {
                if (mTemplateDescriptorItemArray[i].getId().equals(mOriginalTemplateDescriptorItem.getId()))
                {
                    mCurrentSelectedtemplateDescriptorItemIndex = i;
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
        addTempalteDescriptorPickerFragment();
        addTransparentPartDismissListener(R.id.template_descriptor_picker_transparent_part);
        // create fader animators
        createBgAnimators(R.id.template_descriptor_picker_base_container, savedInstanceState);
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
        outState.putParcelableArray(REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY, mTemplateDescriptorItemArray);
        outState.putParcelable(REQUEST_ORIGINAL_TEMPLATE_DESCRIPTOR_ITEM, mOriginalTemplateDescriptorItem);
        outState.putInt(CURRENT_SELECTED_TEMPLATE_DESCRIPTOR_INDEX, mCurrentSelectedtemplateDescriptorItemIndex);
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
        Logger.d(TAG, String.format("ReturnToCaller - resultCode=%d", resultCode));
        setResult(resultCode, data);
        if (mTemplateDescriptorPickerFragment == null)
        {
            this.finish();
        }
        else
        {
            mTemplateDescriptorPickerFragment.removeChildFragments();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out);
            ft.remove(mTemplateDescriptorPickerFragment).commit();
            mTemplateDescriptorPickerFragment = null;
            startActivityEndAnimationAndFinishActivity();
        }
    }

    /**
     * Adds the template descriptor picker fragment.
     */
    private void addTempalteDescriptorPickerFragment()
    {
        // show the widget
        int containerId = R.id.template_descriptor_picker_container;
        mTemplateDescriptorPickerFragment = (TemplateDescriptorPickerFragment)getSupportFragmentManager()
                .findFragmentByTag(TemplateDescriptorPickerFragment.TAG);
        if (mTemplateDescriptorPickerFragment == null)
        {
            Logger.d(TAG, "addTempalteDescriptorPickerFragment - mTemplateDescriptorPickerFragment is null");
            mTemplateDescriptorPickerFragment = new TemplateDescriptorPickerFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in, 0);
            ft.add(containerId, mTemplateDescriptorPickerFragment, TemplateDescriptorPickerFragment.TAG).commit();
        }
        else
        {
            Logger.d(TAG, "addTempalteDescriptorPickerFragment - mTemplateDescriptorPickerFragment is not null");
        }
    }

    /**
     * Retrieve data from intent.
     * 
     * @param bundle the bundle
     * @return the UI policy pick orientation info.
     */
    private void parseBundleInput(Bundle bundle)
    {
        if (bundle.containsKey(REQUEST_ORIGINAL_TEMPLATE_DESCRIPTOR_ITEM))
        {
            Logger.d(TAG, "parseBundleInput - parsing OriginalTemplateDescriptorItem");
            Parcelable requestOriginalTemplateDescriptor = bundle
                    .getParcelable(TemplateDescriptorPickerActivity.REQUEST_ORIGINAL_TEMPLATE_DESCRIPTOR_ITEM);
            if (requestOriginalTemplateDescriptor != null)
            {
                Logger.ie(TAG, "requestOriginalTemplateDescriptor is null");
            }
            try
            {
                mOriginalTemplateDescriptorItem = (TemplateDescriptorModel)requestOriginalTemplateDescriptor;
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
                    .getParcelableArray(TemplateDescriptorPickerActivity.REQUEST_TEMPLATE_DESCRIPTOR_ITEM_ARRAY);
            try
            {
                Object[] requestObjectArray = requestArray;
                mTemplateDescriptorItemArray = Arrays.copyOf(requestObjectArray, requestObjectArray.length,
                        TemplateDescriptorModel[].class);
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
        if (bundle.containsKey(CURRENT_SELECTED_TEMPLATE_DESCRIPTOR_INDEX))
        {
            Logger.d(TAG, "parseBundleInput - parsing CurrentSelectedtemplateDescriptorItemIndex");
            mCurrentSelectedtemplateDescriptorItemIndex = bundle.getInt(CURRENT_SELECTED_TEMPLATE_DESCRIPTOR_INDEX);
        }
    }

    /**
     * Update template descriptor array with fake templates.
     */
    private void updateTemplateDescriptorArrayWithFakeTemplates()
    {
        mCustomPermissionDescriptorFakeItem = TemplateDescriptorModel
                .createCustomPermissionFakeItem(getApplicationContext());
        mNoProtectionDescriptorFakeItem = TemplateDescriptorModel.createNoProtectionFakeItem(getApplicationContext());
        // resize and put custom protection on top and no protection at bottom
        TemplateDescriptorModel[] fullTemplateDescriptorItemArray = new TemplateDescriptorModel[mTemplateDescriptorItemArray.length + 2];
        System.arraycopy(mTemplateDescriptorItemArray, 0, fullTemplateDescriptorItemArray, 1,
                mTemplateDescriptorItemArray.length);
        mTemplateDescriptorItemArray = fullTemplateDescriptorItemArray;
        mTemplateDescriptorItemArray[0] = mCustomPermissionDescriptorFakeItem;
        mTemplateDescriptorItemArray[mTemplateDescriptorItemArray.length - 1] = mNoProtectionDescriptorFakeItem;
        // if there is no original descriptor, assume that content is unprotected
        if (mOriginalTemplateDescriptorItem == null)
        {
            mOriginalTemplateDescriptorItem = mNoProtectionDescriptorFakeItem;
        }
    }
}

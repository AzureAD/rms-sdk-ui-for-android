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

package com.microsoft.rightsmanagement.ui.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.model.DescriptorModel;
import com.microsoft.rightsmanagement.ui.model.CustomDescriptorModel;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * The class CustomDescriptorDetailsFragment provides view implementation for showing details of cusotm policy and
 * letting user choose content expiry date.
 */
public class CustomDescriptorDetailsFragment extends Fragment
{
    /**
     * The interface for notifying activity that user would like to change duration on this customDescriptor
     */
    public interface CustomPolicyDurationPickerListner
    {
        /**
         * Notifies when Duration is clicked on custom descriptor
         */
        public void onDurationClicked();
    }
    /** Tag for this fragment */
    public static final String TAG = "CustomDescriptorDetailsFragment";
    private CustomDescriptorModel mChosenPolicyDescriptor;
    private CustomPolicyDurationPickerListner mCustomPolicyDurationPickerListener;
    private TextView mCustomPolicyDurationTextView;
    private Date mPolicyDuration;

    /**
     * @return the ChoosenCustomPolicy
     */
    public DescriptorModel getChoosenCustomPolicy()
    {
        return mChosenPolicyDescriptor;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity)
    {
        Logger.ms(TAG, "onAttach");
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try
        {
            mCustomPolicyDurationPickerListener = (CustomPolicyDurationPickerListner)activity;
        }
        catch (ClassCastException e)
        {
            Logger.ie(TAG, "Activity must implement CustomPolicyDurationPicker");
            throw e;
        }
        Logger.me(TAG, "onAttach");
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
     * android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.custom_permissions_fragment_layout, container, false);
        if (mChosenPolicyDescriptor != null)
        {
            // Set policy name text
            TextView policyNameText = (TextView)view.findViewById(R.id.custom_policy_name);
            policyNameText.setText(mChosenPolicyDescriptor.getName());
        }
        else
        {
            Logger.d(TAG, "No customPolicy was set to display");
        }
        // retrieve policy duration to set listener
        TextView policyDuration = (TextView)view.findViewById(R.id.custom_policy_duration);
        mCustomPolicyDurationTextView = policyDuration;
        mCustomPolicyDurationTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Logger.d(TAG, "protection button onClick called");
                mCustomPolicyDurationPickerListener.onDurationClicked();
            }
        });
        Logger.me(TAG, "onCreateView");
        return view;
    }

    /**
     * Sets chosen custom policy
     * 
     * @param chosenPolicyDescriptor the ChosenPolicyDescriptor to set
     */
    public void setChoosenCustomPolicy(CustomDescriptorModel chosenPolicyDescriptor)
    {
        mChosenPolicyDescriptor = chosenPolicyDescriptor;
    }

    /**
     * Sets Policy duration for custom policy
     * 
     * @param contentExpiryDate
     */
    public void updatePolicyDuration(Date contentExpiryDate)
    {
        View view = getView();
        TextView durationView = (TextView)view.findViewById(R.id.custom_policy_duration);
        mPolicyDuration = contentExpiryDate;
        if (mChosenPolicyDescriptor != null)
        {
            Logger.d(TAG, "setting custom policy duration");
            mChosenPolicyDescriptor.setContentValidUntil(contentExpiryDate);
        }
        else
        {
            Logger.ie(TAG, "Custom policy should not be null");
        }
        if (mPolicyDuration != null)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy", Locale.US);
            String formatedDate = dateFormat.format(mPolicyDuration);
            durationView.setText(formatedDate);
        }
        else
        {
            String never = getResources().getString(R.string.duration_never);
            durationView.setText(never);
        }
    }
}

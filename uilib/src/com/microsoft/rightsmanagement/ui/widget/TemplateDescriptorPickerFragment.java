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

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * Provides the view implementation of Template Descriptor Picker UI.
 */
public class TemplateDescriptorPickerFragment extends Fragment
{
    /**
     * The listener interface for receiving templateDescriptorSelection events. The class that is interested in
     * processing a templateDescriptorSelection event implements this interface, and the object created with that class
     * is registered with a component using the component's
     * <code>addTemplateDescriptorSelectionListener<code> method. When
     * the templateDescriptorSelection event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see ProtectionButtonEventEvent
     */
    public interface ProtectionButtonEventListener
    {
        /**
         * On protection button clicked.
         */
        public void onProtectionButtonClicked();
    }
    public static final String TAG = "TemplateDescriptorPickerFragment";
    private int mDisabledButtonColor;
    private int mEnabledButtonColor;
    private Button mProtectionButton;
    private ProtectionButtonEventListener mProtectionButtonEventListener;
    private TemplateDescriptorListFragment mTemplatesFragment;

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
            mProtectionButtonEventListener = (ProtectionButtonEventListener)activity;
        }
        catch (ClassCastException e)
        {
            Logger.ie(TAG, "Activity must implement ProtectionButtonEventListener");
            throw e;
        }
        Logger.me(TAG, "onAttach");
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Resources resources = getActivity().getResources();
        mDisabledButtonColor = resources.getColor(R.color.light_gray);
        mEnabledButtonColor = resources.getColor(R.color.dark_black);
        Logger.me(TAG, "onCreate");
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
        addTemplateDescriptorListFragment();
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.template_descriptor_picker_fragment_layout, container, false);
        mProtectionButton = (Button)view.findViewById(R.id.protect_btn_template_picker_layout);
        setProtectionButtonEnabled(false);// button is not enabled until an item is selected.
        mProtectionButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Logger.d(TAG, "protection button onClick called");
                mProtectionButtonEventListener.onProtectionButtonClicked();
            }
        });
        Logger.me(TAG, "onCreateView");
        return view;
    }

    /**
     * removes child fragments
     */
    public void removeChildFragments()
    {
        if (mTemplatesFragment != null)
        {
            FragmentManager childFragmentManager = getChildFragmentManager();
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out);
            ft.remove(mTemplatesFragment).commit();
            mTemplatesFragment = null;
        }
    }

    /**
     * Sets the protection button enabled.
     * 
     * @param enabled the new protection button enabled
     */
    public void setProtectionButtonEnabled(boolean enabled)
    {
        mProtectionButton.setEnabled(enabled);
        mProtectionButton.setTextColor(enabled ? mEnabledButtonColor : mDisabledButtonColor);
    }

    /**
     * Adds the template descriptor list fragment.
     */
    private void addTemplateDescriptorListFragment()
    {
        FragmentManager childFragmentManager = getChildFragmentManager();
        mTemplatesFragment = (TemplateDescriptorListFragment)childFragmentManager
                .findFragmentByTag(TemplateDescriptorListFragment.TAG);
        if (mTemplatesFragment == null)
        {
            Logger.d(TAG, "templatesFragment is null");
            mTemplatesFragment = new TemplateDescriptorListFragment();
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in, 0);
            ft.add(R.id.template_fragment, mTemplatesFragment, TemplateDescriptorListFragment.TAG).commit();
        }
        else
        {
            Logger.d(TAG, "templatesFragment is not null");
        }
    }
}

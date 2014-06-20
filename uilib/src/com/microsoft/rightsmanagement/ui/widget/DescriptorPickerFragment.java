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
import com.microsoft.rightsmanagement.ui.model.CustomDescriptorModel;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * Provides the view implementation of Descriptor Picker UI.
 */
public class DescriptorPickerFragment extends Fragment
{
    /**
     * The listener interface for receiving descriptorSelection events. The class that is interested in processing a
     * descriptorSelection event implements this interface, and the object created with that class is registered with a
     * component using the component's <code>addDescriptorSelectionListener<code> method. When
     * the descriptorSelection event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see ProtectionButtonEventListener
     */
    public interface ProtectionButtonEventListener
    {
        /**
         * On protection button clicked.
         */
        public void onProtectionButtonClicked();
    }
    /** Key for storing Protection button state in event of screen rotation */
    public static final String PROTECTION_BUTTON_STATE = "PROTECTION_BUTTON_STATE";
    /** TAG for this fragment */
    public static final String TAG = "DescriptorPickerFragment";
    private CustomDescriptorDetailsFragment mCustomDescriptorDetailsFragment;
    private DescriptorListFragment mDescriptorFragment;
    private int mDisabledButtonColor;
    private int mEnabledButtonColor;
    private boolean mIsCustomDescriptorDetailsFragmentVisible = false;
    private boolean mIsProtectionButtonEnabled = false;
    private Button mProtectionButton;
    private ProtectionButtonEventListener mProtectionButtonEventListener;

    /**
     * Returns visibility of CustomDescriptorDetailsFragment
     * 
     * @return true if CustomDescriptorDetailsFragment is visible false otherwise
     */
    public boolean isCustomDescriptorDetailsFragmentVisible()
    {
        return mIsCustomDescriptorDetailsFragmentVisible;
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
        addCustomDescriptorDetailsFragment();
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.descriptor_picker_fragment_layout, container, false);
        mProtectionButton = (Button)view.findViewById(R.id.protect_btn_descriptor_picker_layout);
        if (savedInstanceState != null && savedInstanceState.containsKey(PROTECTION_BUTTON_STATE))
        {
            mIsProtectionButtonEnabled = savedInstanceState.getBoolean(PROTECTION_BUTTON_STATE);
        }
        setProtectionButtonEnabled(mIsProtectionButtonEnabled);// button is not enabled until an item is selected.
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Logger.ms(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROTECTION_BUTTON_STATE, mIsProtectionButtonEnabled);
        Logger.me(TAG, "onSaveInstanceState");
    }

    /**
     * removes child fragments
     */
    public void removeChildFragments()
    {
        if (mDescriptorFragment != null)
        {
            FragmentManager childFragmentManager = getChildFragmentManager();
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out);
            ft.remove(mDescriptorFragment).commit();
            mDescriptorFragment = null;
        }
        if (mCustomDescriptorDetailsFragment != null)
        {
            FragmentManager childFragmentManager = getChildFragmentManager();
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out);
            ft.remove(mCustomDescriptorDetailsFragment).commit();
            mCustomDescriptorDetailsFragment = null;
        }
    }

    /**
     * Sets the protection button enabled.
     * 
     * @param enabled the new protection button enabled
     */
    public void setProtectionButtonEnabled(boolean enabled)
    {
        mIsProtectionButtonEnabled = enabled;
        mProtectionButton.setEnabled(enabled);
        mProtectionButton.setTextColor(enabled ? mEnabledButtonColor : mDisabledButtonColor);
    }

    /**
     * Called by parent activity to show CustomPolicyViewerFragment
     * 
     * @param choosenDescriptor
     */
    public void showCustomDescriptorDetailsFragment(CustomDescriptorModel choosenDescriptor)
    {
        Logger.ms(TAG, "showCustomDescriptorDetailsFragment");
        FragmentManager childFragmentManager = getChildFragmentManager();
        mCustomDescriptorDetailsFragment = (CustomDescriptorDetailsFragment)childFragmentManager
                .findFragmentByTag(CustomDescriptorDetailsFragment.TAG);
        mDescriptorFragment = (DescriptorListFragment)childFragmentManager
                .findFragmentByTag(DescriptorListFragment.TAG);
        if (mCustomDescriptorDetailsFragment == null)
        {
            Logger.d(TAG, "CustomDescriptorDetailsFragment is null");
            mCustomDescriptorDetailsFragment = new CustomDescriptorDetailsFragment();
            mCustomDescriptorDetailsFragment.setChoosenCustomPolicy(choosenDescriptor);
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in, 0);
            ft.add(R.id.descriptor_fragment, mCustomDescriptorDetailsFragment, CustomDescriptorDetailsFragment.TAG);
            ft.hide(mDescriptorFragment);
            ft.commit();
            mIsCustomDescriptorDetailsFragmentVisible = true;
        }
        else if (mCustomDescriptorDetailsFragment != null && mDescriptorFragment != null)
        {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            mCustomDescriptorDetailsFragment.setChoosenCustomPolicy(choosenDescriptor);
            ft.show(mCustomDescriptorDetailsFragment);
            ft.hide(mDescriptorFragment);
            ft.addToBackStack(null);
            ft.commit();
            mIsCustomDescriptorDetailsFragmentVisible = true;
        }
        else
        {
            Logger.d(TAG,
                    "both CustomDescriptorDetailsFragment and DescriptorFragment should already be added to switch visibility");
        }
        Logger.me(TAG, "showCustomDescriptorDetailsFragment");
    }

    /**
     * Shows Template List fragment and hides customPolicyViewerFragment
     */
    public void showTemplateListFragment()
    {
        Logger.ms(TAG, "showTemplateListFragment");
        FragmentManager childFragmentManager = getChildFragmentManager();
        mCustomDescriptorDetailsFragment = (CustomDescriptorDetailsFragment)childFragmentManager
                .findFragmentByTag(CustomDescriptorDetailsFragment.TAG);
        mDescriptorFragment = (DescriptorListFragment)childFragmentManager
                .findFragmentByTag(DescriptorListFragment.TAG);
        if (mCustomDescriptorDetailsFragment != null && mDescriptorFragment != null)
        {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.show(mDescriptorFragment);
            ft.hide(mCustomDescriptorDetailsFragment);
            ft.addToBackStack(null);
            ft.commit();
            mIsCustomDescriptorDetailsFragmentVisible = false;
        }
        else
        {
            Logger.d(TAG,
                    "both CustomDescriptorDetailsFragment and DescriptorFragment should already be added to switch visibility");
        }
        Logger.me(TAG, "showTemplateListFragment");
    }

    /**
     * Adds the CustomDescriptorDetailsFragment.
     */
    private void addCustomDescriptorDetailsFragment()
    {
        FragmentManager childFragmentManager = getChildFragmentManager();
        mCustomDescriptorDetailsFragment = (CustomDescriptorDetailsFragment)childFragmentManager
                .findFragmentByTag(CustomDescriptorDetailsFragment.TAG);
    }

    /**
     * Adds the template descriptor list fragment.
     */
    private void addTemplateDescriptorListFragment()
    {
        FragmentManager childFragmentManager = getChildFragmentManager();
        mDescriptorFragment = (DescriptorListFragment)childFragmentManager
                .findFragmentByTag(DescriptorListFragment.TAG);
        if (mDescriptorFragment == null)
        {
            Logger.d(TAG, "descriptorFragment is null");
            mDescriptorFragment = new DescriptorListFragment();
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in, 0);
            ft.add(R.id.descriptor_fragment, mDescriptorFragment, DescriptorListFragment.TAG).commit();
        }
        else
        {
            Logger.d(TAG, "descriptorFragment is not null");
        }
    }
}

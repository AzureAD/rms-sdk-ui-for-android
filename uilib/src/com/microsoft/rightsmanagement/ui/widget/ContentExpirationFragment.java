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

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.utils.Logger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The class ContentExpiresFragment provides view implementation for picking the expiry for custom protected policy
 */
public class ContentExpirationFragment extends Fragment
{
    /** TAG */
    public static final String TAG = "ContentExpirationFragment";

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
        addContentExpiresDurationListFragment();
        View view = inflater.inflate(R.layout.content_expiration_fragment_layout, container, false);
        Logger.me(TAG, "onCreateView");
        return view;
    }

    /**
     * adds Content Expires List Fragment to this container
     */
    private void addContentExpiresDurationListFragment()
    {
        FragmentManager childFragmentManager = getChildFragmentManager();
        ContentExpirationListFragment contentExpiresListFragment = (ContentExpirationListFragment)childFragmentManager
                .findFragmentByTag(ContentExpirationListFragment.TAG);
        if (contentExpiresListFragment == null)
        {
            Logger.d(TAG, "contentExpiresListFragment is null");
            contentExpiresListFragment = new ContentExpirationListFragment();
            childFragmentManager
                    .beginTransaction()
                    .add(R.id.content_expiration_list_fragment, contentExpiresListFragment,
                            ContentExpirationListFragment.TAG).addToBackStack(null).commit();
        }
        else
        {
            Logger.d(TAG, "contentExpiresListFragment is not null");
        }
    }
}

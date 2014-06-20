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

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.ListView;
import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.model.DescriptorModel;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * Provides view implementation for displaying the list of descriptors.
 */
public final class DescriptorListFragment extends ListFragment
{
    /**
     * The Interface for providing descriptor data to UI.
     */
    public interface DescriptorDataProvider
    {
        /**
         * Gets the descriptor items.
         * 
         * @return the descriptor items
         */
        public DescriptorModel[] getDescriptorItems();

        /**
         * Gets the selected descriptor item index.
         * 
         * @return the selected descriptor item index
         */
        public int getSelectedDescriptorItemIndex();
    }

    /**
     * The listener interface for receiving descriptorList events. The class that is interested in processing a
     * descriptorList event implements this interface, and the object created with that class is registered with a
     * component using the component's <code>addDescriptorListEventListener<code> method. When
     * the descriptorListEvent event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see DescriptorListEventListener
     */
    public interface DescriptorListEventListener
    {
        /**
         * On descriptor selected.
         * 
         * @param selectedDescriptorIndex the selected descriptor index
         */
        public void onDescriptorItemSelected(int selectedDescriptorIndex);
    }
    /** Tag for this fragment */
    public static final String TAG = "DescriptorListFragment";
    private DescriptorListAdapter mDescriptorArrayAdapter;
    private DescriptorDataProvider mDescriptorDataProvider;
    private DescriptorListEventListener mDescriptorListEventListener;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (mDescriptorDataProvider.getDescriptorItems() != null)
        {
            mDescriptorArrayAdapter = new DescriptorListAdapter(this.getActivity(), R.layout.descriptor_list_item,
                    Arrays.asList(mDescriptorDataProvider.getDescriptorItems()),
                    new DescriptorListAdapter.DescriptorListAdapterEventListener()
                    {
                        @Override
                        public int getSelectedIndex()
                        {
                            return mDescriptorDataProvider.getSelectedDescriptorItemIndex();
                        }
                    });
        }
        setListAdapter(mDescriptorArrayAdapter);
        Logger.me(TAG, "onActivityCreated");
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
        try
        {
            mDescriptorListEventListener = (DescriptorListEventListener)activity;
            mDescriptorDataProvider = (DescriptorDataProvider)activity;
        }
        catch (ClassCastException e)
        {
            Logger.ie(TAG, "Activity must implement TemplateDescriptorListEventListener");
            throw e;
        }
        Logger.me(TAG, "onAttach");
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView list, View view, int position, long id)
    {
        Logger.d(TAG, String.format("onListItemClick invoked with postion = %d and id = %d", position, id));
        selectListItem(view, position);
        View v = getActivity().findViewById(R.id.descriptor_picker_fragment_container);
        v.invalidate();
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View, android.os.Bundle)
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        // When rotating we need to scroll to the current policy selected.
        getListView().addOnAttachStateChangeListener(new OnAttachStateChangeListener()
        {
            @Override
            public void onViewAttachedToWindow(View v)
            {
                Logger.d(TAG, String.format("onViewAttachedToWindow(%s)", v.getClass().toString()));
                // select original item
                final int position = mDescriptorDataProvider.getSelectedDescriptorItemIndex();
                selectListItem(view, position);
                // when item is clicked make sure the entire view is visible.
                getListView().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getListView().smoothScrollToPosition(position);
                    }
                });
            }

            @Override
            public void onViewDetachedFromWindow(View v)
            {
                // Do nothing.
            }
        });
        Logger.me(TAG, "onViewCreated");
    }

    /**
     * Select list item.
     * 
     * @param view the view
     * @param position the position
     */
    private void selectListItem(View view, final int position)
    {
        Logger.d(TAG, String.format("selectListItem(%s, %d)", view.getClass().toString(), position));
        DescriptorModel[] descriptorItemArray = mDescriptorDataProvider.getDescriptorItems();
        if (descriptorItemArray == null || position < 0 || position >= descriptorItemArray.length)
        {
            Logger.ie(TAG, "selectListItem has received invalid arguments");
            return;
        }
        // Update view only if new position was selected.
        if (position != mDescriptorDataProvider.getSelectedDescriptorItemIndex())
        {
            mDescriptorArrayAdapter.setViewHighlights(view, descriptorItemArray[position].getDescription());
        }
        // Fire the listener.
        mDescriptorListEventListener.onDescriptorItemSelected(position);
    }
}

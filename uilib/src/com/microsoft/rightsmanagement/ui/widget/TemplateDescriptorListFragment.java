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

package com.microsoft.rightsmanagement.ui.widget;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.ListView;
import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.PolicyPickerActivity;
import com.microsoft.rightsmanagement.ui.model.TemplateDescriptorModel;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * Provides view implementation for displaying the list of templates descriptors.
 */
public final class TemplateDescriptorListFragment extends ListFragment
{
    /**
     * The Interface for providing template descriptor data to UI.
     */
    public interface TemplateDescriptorDataProvider
    {
        /**
         * Gets the selected template descriptor item index.
         * 
         * @return the selected template descriptor item index
         */
        public int getSelectedTemplateDescriptorItemIndex();

        /**
         * Gets the template descriptor items.
         * 
         * @return the template descriptor items
         */
        public TemplateDescriptorModel[] getTemplateDescriptorItems();
    }

    /**
     * The listener interface for receiving templateDescriptorListEvent events. The class that is interested in
     * processing a templateDescriptorListEvent event implements this interface, and the object created with that class
     * is registered with a component using the component's
     * <code>addTemplateDescriptorListEventListener<code> method. When
     * the templateDescriptorListEvent event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see TemplateDescriptorListEventEvent
     */
    public interface TemplateDescriptorListEventListener
    {
        /**
         * On template descriptor selected.
         * 
         * @param selectedTemplateDescriptorIndex the selected template descriptor index
         */
        public void onTemplateDescriptorItemSelected(int selectedTemplateDescriptorIndex);
    }
    public static final String TAG = "TemplateDescriptorListFragment";
    private TemplateDescriptorListAdapter mTemplateDescriptorArrayAdapter;
    private TemplateDescriptorDataProvider mTemplateDescriptorDataProvider;
    private TemplateDescriptorListEventListener mTemplateDescriptorListEventListener;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (mTemplateDescriptorDataProvider.getTemplateDescriptorItems() != null)
        {
            mTemplateDescriptorArrayAdapter = new TemplateDescriptorListAdapter(this.getActivity(),
                    R.layout.template_descriptor_list_item, Arrays.asList(mTemplateDescriptorDataProvider
                            .getTemplateDescriptorItems()),
                    new TemplateDescriptorListAdapter.TemplateDescriptorListAdapterEventListener()
                    {
                        @Override
                        public int getSelectedIndex()
                        {
                            return mTemplateDescriptorDataProvider.getSelectedTemplateDescriptorItemIndex();
                        }
                    });
        }
        setListAdapter(mTemplateDescriptorArrayAdapter);
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
            mTemplateDescriptorListEventListener = (TemplateDescriptorListEventListener)activity;
            mTemplateDescriptorDataProvider = (TemplateDescriptorDataProvider)activity;
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
        View v =  getActivity().findViewById(R.id.template_picker_fragment_container);
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
                Logger.d(TAG,String.format("onViewAttachedToWindow(%s)", v.getClass().toString()));
                // select original item
                final int position = mTemplateDescriptorDataProvider.getSelectedTemplateDescriptorItemIndex();
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
        Logger.d(TAG, String.format("selectListItem(%s, %d)",view.getClass().toString(), position));
        TemplateDescriptorModel[] templateDescriptorItemArray = mTemplateDescriptorDataProvider
                .getTemplateDescriptorItems();
        if (templateDescriptorItemArray == null || position < 0 || position >= templateDescriptorItemArray.length)
        {
            Logger.ie(TAG, "selectListItem has received invalid arguments");
            return;
        }
        // temporary solution for absence of custom permission feature
        if (!PolicyPickerActivity.isTemplateDescriptorItemEnabled(templateDescriptorItemArray[position]))
        {
            return;
        }
        // Update view only if new position was selected.
        if (position != mTemplateDescriptorDataProvider.getSelectedTemplateDescriptorItemIndex())
        {
            mTemplateDescriptorArrayAdapter.setViewHighlights(view,
                    templateDescriptorItemArray[position].getDescription());
        }
        // Fire the listener.
        mTemplateDescriptorListEventListener.onTemplateDescriptorItemSelected(position);
    }
}

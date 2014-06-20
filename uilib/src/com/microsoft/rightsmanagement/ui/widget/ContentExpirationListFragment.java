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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.model.ContentExpirationModel;
import com.microsoft.rightsmanagement.ui.model.ContentExpirationModel.ExpiryDateType;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.ui.widget.DescriptorListFragment.DescriptorListEventListener;

/**
 * class ContentExpirationListFragment provides view implementation for various content expiry durations user can select
 * for custom policy
 */
public class ContentExpirationListFragment extends ListFragment implements OnDateSetListener
{
    /**
     * The listener interface for receiving ContentExpirationList events. The class that is interested in processing a
     * ContentExpirationList event implements this interface, and the object created with that class is registered with
     * a component using the component's <code>onContentExpirationListItemSelected<code> method. When
     * the ContentExpirationList event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see DescriptorListEventListener
     */
    public interface ContentExpirationListEventListner
    {
        /**
         * To be Called On ContentExpirationList item selected
         * 
         * @param cotentExpiryDate - corresponding date for the content expiration list item selected
         */
        public void onContentExpirationListItemSelected(Date cotentExpiryDate);
    }
    /** Tag */
    public static final String TAG = "ContentExpirationListFragment";
    private ContentExpirationListAdaptor mContentExpirationListAdaptor;
    private ContentExpirationListEventListner mContentExpirationListEventListner;
    private List<ContentExpirationModel> mContentExpirationModelList;
    private Date mDate;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContentExpirationModelList = ContentExpirationModel.getContextExpiresList(this.getActivity());
        mContentExpirationListAdaptor = new ContentExpirationListAdaptor(this.getActivity(),
                R.layout.content_expiration_list_item, mContentExpirationModelList);
        setListAdapter(mContentExpirationListAdaptor);
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
            mContentExpirationListEventListner = (ContentExpirationListEventListner)activity;
        }
        catch (ClassCastException e)
        {
            Logger.ie(TAG, "Activity must implement ContentExpiresListEventListner");
            throw e;
        }
        Logger.me(TAG, "onAttach");
    }

    /*
     * (non-Javadoc)
     * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mDate = calendar.getTime();
        mContentExpirationListEventListner.onContentExpirationListItemSelected(calendar.getTime());
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

    /**
     * Creates datePickerdialog with cached date if exists ; current date otherwise
     * 
     * @return DatePicker dialog
     */
    private DatePickerDialog getDatePicker()
    {
        Calendar calendar = Calendar.getInstance();
        if (mDate != null)
        {
            calendar.setTime(mDate);
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        return datePickerDialog;
    }

    /**
     * Select List item
     * 
     * @param view
     * @param position
     */
    private void selectListItem(View view, final int position)
    {
        Logger.d(TAG, String.format("selectListItem(%s, %d)", view.getClass().toString(), position));
        ContentExpirationModel contentExpiresModel = mContentExpirationModelList.get(position);
        if (contentExpiresModel == null || position < 0 || position >= mContentExpirationModelList.size())
        {
            Logger.ie(TAG, "selectListItem has received invalid arguments");
            return;
        }
        mContentExpirationListAdaptor.setViewHighlights(view);
        if (contentExpiresModel.getExpiryDateType() == ExpiryDateType.CUSTOM)
        {
            DatePickerDialog datePickerDialog = getDatePicker();
            datePickerDialog.show();
        }
        else
        {
            mContentExpirationListEventListner.onContentExpirationListItemSelected(contentExpiresModel
                    .getContentValidUntilDate());
        }
    }
}

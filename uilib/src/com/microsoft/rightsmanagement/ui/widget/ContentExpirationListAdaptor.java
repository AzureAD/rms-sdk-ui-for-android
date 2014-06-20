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

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.model.ContentExpirationModel;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * Array Adaptor that generates view for ContentExpires list
 */
public class ContentExpirationListAdaptor extends ArrayAdapter<ContentExpirationModel>
{
    private static final String TAG = "ContentExpiresListAdaptor";
    private List<ContentExpirationModel> mContentExpiresModelList;
    private Context mContext;
    private View mPrevTouchedView;
    private int mSelectedBackgroundColor;
    private int mSelectedTextColor;
    private int mUnSelectedBackgroundColor;
    private int mUnSelectedTextColor;

    /**
     * Constructor for ContentExpirationListAdaptor
     * 
     * @param context applicationContext
     * @param layoutId - layout id for list item
     * @param objects - model objects for view ContentExpires
     */
    public ContentExpirationListAdaptor(Context context,
                                        int layoutId,
                                        List<ContentExpirationModel> objects)
    {
        super(context, layoutId, objects);
        mContext = context;
        Resources resources = context.getResources();
        mUnSelectedTextColor = resources.getColor(R.color.dark_grey);
        mUnSelectedBackgroundColor = resources.getColor(R.color.white);
        mSelectedTextColor = resources.getColor(R.color.white);
        mSelectedBackgroundColor = resources.getColor(R.color.dark_grey);
        mContentExpiresModelList = objects;
    }

    /**
     * This methods is called to each list item on its creation. Each item is an individual view and inflated and set
     * separately.
     * 
     * @param position the position in the list.
     * @param view the view of the item.
     * @param parent the parent the parent view of the list.
     * @return the initialized view.
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        Logger.d(TAG, String.format("getView - position = %d", position));
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.content_expiration_list_item, null);
        }
        ContentExpirationModel selectedContentExpiresItem = mContentExpiresModelList.get(position);
        if (selectedContentExpiresItem != null)
        {
            TextView templateNameTxtView = (TextView)view.findViewById(R.id.duration_title);
            if (templateNameTxtView != null)
            {
                templateNameTxtView.setText(selectedContentExpiresItem.getExpiryDateTitle());
            }
        }
        return view;
    }

    /**
     * Sets the view highlights.
     * 
     * @param view the view to be set.
     */
    public void setViewHighlights(View view)
    {
        if (mPrevTouchedView != null)
        {
            setUnHighlighted(mPrevTouchedView);
        }
        setHighlighted(view);
        mPrevTouchedView = view;
    }

    /**
     * Sets an item to be highlighted.
     * 
     * @param defaultSelectedView the view to be set.
     * @param description when highlighted a description text is added.
     */
    protected void setHighlighted(View defaultSelectedView)
    {
        setColorToItem(defaultSelectedView, mSelectedBackgroundColor, mSelectedTextColor);
        defaultSelectedView.setSelected(true);
    }

    /**
     * Sets the un highlighted.
     * 
     * @param defaultSelectedView the new un highlighted
     */
    protected void setUnHighlighted(View defaultSelectedView)
    {
        setColorToItem(defaultSelectedView, mUnSelectedBackgroundColor, mUnSelectedTextColor);
    }

    /**
     * Sets the color to item.
     * 
     * @param defaultSelectedView the default selected view
     * @param colorBackground the color background
     * @param colorText the color text
     */
    private void setColorToItem(View defaultSelectedView, int colorBackground, int colorText)
    {
        if (defaultSelectedView != null)
        {
            LinearLayout layout = (LinearLayout)defaultSelectedView
                    .findViewById(R.id.content_expiration_list_fragment_container);
            if (layout != null)
            {
                layout.setBackgroundColor(colorBackground);
            }
            TextView templateNameTxtView = (TextView)defaultSelectedView.findViewById(R.id.duration_title);
            if (templateNameTxtView != null)
            {
                templateNameTxtView.setTextColor(colorText);
            }
        }
    }
}

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.TemplateDescriptorPickerActivity;
import com.microsoft.rightsmanagement.ui.model.TemplateDescriptorModel;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * An ArrayAdapter that generates view for TemplateDescriptor list.
 */
class TemplateDescriptorListAdapter extends ArrayAdapter<TemplateDescriptorModel>
{
    /**
     * The listener interface for receiving templateDescriptorListAdapterEvent events. The class that is interested in
     * processing a templateDescriptorListAdapterEvent event implements this interface, and the object created with that
     * class is registered with a component using the component's
     * <code>addTemplateDescriptorListAdapterEventListener<code> method. When
     * the templateDescriptorListAdapterEvent event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see TemplateDescriptorListAdapterEventEvent
     */
    public interface TemplateDescriptorListAdapterEventListener
    {
        /**
         * Gets the selected index.
         * 
         * @return the selected index
         */
        public int getSelectedIndex();
    }
    private Context mContext;
    private View mPrevTouchedView;
    private int mSelectedBackgroundColor;
    private int mSelectedTextColor;
    private List<TemplateDescriptorModel> mTemplateDescriptorItemList;
    private TemplateDescriptorListAdapterEventListener mTemplateDescriptorListAdapterEventListener;
    private int mUnSelectatbleTextColor;
    private int mUnSelectedBackgroundColor;
    private int mUnSelectedTextColor;
    private String TAG = "TemplateDescriptorListAdapter";

    /**
     * Instantiates a new template descriptor list adapter.
     * 
     * @param context the context
     * @param layoutId the layout id
     * @param objects the objects
     * @param callback the callback
     */
    public TemplateDescriptorListAdapter(Context context,
                                         int layoutId,
                                         List<TemplateDescriptorModel> objects,
                                         TemplateDescriptorListAdapterEventListener callback)
    {
        super(context, layoutId, objects);
        mTemplateDescriptorItemList = objects;
        Resources resources = context.getResources();
        mUnSelectedTextColor = resources.getColor(R.color.dark_grey);
        mUnSelectedBackgroundColor = resources.getColor(R.color.white);
        mSelectedTextColor = resources.getColor(R.color.white);
        mSelectedBackgroundColor = resources.getColor(R.color.dark_grey);
        mUnSelectatbleTextColor = resources.getColor(R.color.light_gray);
        mContext = context;
        mTemplateDescriptorListAdapterEventListener = callback;
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
            view = inflater.inflate(R.layout.template_descriptor_list_item, null);
        }
        TemplateDescriptorModel selectedTemplateDescriptorItem = mTemplateDescriptorItemList.get(position);
        if (selectedTemplateDescriptorItem != null)
        {
            TextView templateNameTxtView = (TextView)view.findViewById(R.id.template_name);
            if (templateNameTxtView != null)
            {
                templateNameTxtView.setText(selectedTemplateDescriptorItem.getName());
                // temporary solution to disable custom permissions item
                if (!TemplateDescriptorPickerActivity.isTemplateDescriptorItemEnabled(selectedTemplateDescriptorItem))
                {
                    view.setClickable(false);
                    view.setEnabled(false);
                    templateNameTxtView.setEnabled(false);
                    templateNameTxtView.setTextColor(mUnSelectatbleTextColor);
                }
                else
                {
                    if (position == mTemplateDescriptorListAdapterEventListener.getSelectedIndex())
                    {
                        String description = selectedTemplateDescriptorItem.getDescription();
                        setViewHighlights(view, description);
                    }
                    else
                    {
                        setUnHighlighted(view);
                    }
                }
            }
        }
        return view;
    }

    /**
     * Sets the view highlights.
     * 
     * @param view the view to be set.
     * @param description the description for the highlighted view.
     */
    public void setViewHighlights(View view, String description)
    {
        if (mPrevTouchedView != null)
        {
            setUnHighlighted(mPrevTouchedView);
        }
        setHighlighted(view, description);
        mPrevTouchedView = view;
    }

    /**
     * Sets an item to be highlighted.
     * 
     * @param defaultSelectedView the view to be set.
     * @param description when highlighted a description text is added.
     */
    protected void setHighlighted(View defaultSelectedView, String description)
    {
        setColorToItem(defaultSelectedView, mSelectedBackgroundColor, mSelectedTextColor);
        showDescription(defaultSelectedView, description);
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
        hideDescription(defaultSelectedView);
    }

    /**
     * hide description.
     * 
     * @param defaultSelectedView the default selected view
     */
    private void hideDescription(View defaultSelectedView)
    {
        if (defaultSelectedView != null)
        {
            TextView templateDescriptionTxtView = (TextView)defaultSelectedView.findViewById(R.id.template_description);
            if (templateDescriptionTxtView != null)
            {
                templateDescriptionTxtView.setText("");
                templateDescriptionTxtView.setVisibility(TextView.GONE);
            }
        }
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
            LinearLayout layout = (LinearLayout)defaultSelectedView.findViewById(R.id.template_item_container);
            if (layout != null)
            {
                layout.setBackgroundColor(colorBackground);
            }
            TextView templateNameTxtView = (TextView)defaultSelectedView.findViewById(R.id.template_name);
            if (templateNameTxtView != null)
            {
                templateNameTxtView.setTextColor(colorText);
            }
        }
    }

    /**
     * Show description.
     * 
     * @param defaultSelectedView the default selected view
     * @param description the description
     */
    private void showDescription(View defaultSelectedView, String description)
    {
        if ((defaultSelectedView != null) && (description != null) && (description.length() > 0))
        {
            final TextView templateDescriptionTxtView = (TextView)defaultSelectedView
                    .findViewById(R.id.template_description);
            if (templateDescriptionTxtView != null)
            {
                templateDescriptionTxtView.setText(description);
                Animation scaleText = AnimationUtils.loadAnimation(mContext, R.animator.slide_down_animation);
                scaleText.reset();
                templateDescriptionTxtView.setVisibility(TextView.VISIBLE);
                templateDescriptionTxtView.clearAnimation();
                templateDescriptionTxtView.startAnimation(scaleText);
            }
        }
    }
}

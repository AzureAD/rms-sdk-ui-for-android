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
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.model.RightAccessCheckModel;
import com.microsoft.rightsmanagement.ui.model.UserPolicyModel;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * Provides view implementation for User Policy Viewer UI.
 */
public class UserPolicyViewerFragment extends Fragment
{
    /**
     * The Interface for providing user policy data to UI.
     */
    public interface UserPolicyDataProvider
    {
        /**
         * Gets the user policy model.
         * 
         * @return the user policy model
         */
        public UserPolicyModel getUserPolicyModel();

        /**
         * Checks if is user policy editing enabled.
         * 
         * @return true, if is user policy editing enabled
         */
        public boolean isUserPolicyEditingEnabled();
    }

    /**
     * The listener interface for receiving userPolicyViewerFragmentEvent events. The class that is interested in
     * processing a userPolicyViewerFragmentEvent event implements this interface, and the object created with that
     * class is registered with a component using the component's
     * <code>addUserPolicyViewerFragmentEventListener<code> method. When
     * the userPolicyViewerFragmentEvent event occurs, that object's appropriate
     * method is invoked.
     * 
     * @see UserPolicyViewerFragmentEventEvent
     */
    public interface UserPolicyViewerFragmentEventListener
    {
        /**
         * On edit button clicked.
         */
        public void onEditButtonClicked();
    }
    /** The key used to get a PolicyViewerParcelableData object in orientation changes */
    public static final String FRAGMENT_BUNDLE_DATA_KEY = "dataKey";
    /** Tag for this Fragment */
    public static final String TAG = "UserPolicyViewerFragment";
    private static final int ALPHA_VALUE = (int)(0.8 * 255);
    private final static String UNKNOWN_TEXT = "Unknown";
    private Button mEditBtn;
    private ViewGroup mEditBtnContainer;
    private TextView mOwnerNameTextView;
    private TextView mPolicyDescTextView;
    private TextView mPolicyNameTextView;
    private LinearLayout mRightLayout;
    private TextView mUpperTitleTextView;
    private UserPolicyDataProvider mUserPolicyDataProvider;
    private UserPolicyViewerFragmentEventListener mUserPolicyViewerFragmentEventListener;

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
            mUserPolicyViewerFragmentEventListener = (UserPolicyViewerFragmentEventListener)activity;
            mUserPolicyDataProvider = (UserPolicyDataProvider)activity;
        }
        catch (ClassCastException e)
        {
            Logger.ie(TAG, "Activity must implement UserPolicyViewerFragmentEventListener");
            throw e;
        }
        Logger.me(TAG, "onAttach");
    }

    /**
     * Called when the view is created. Here we will fill inflate our view contained by the fragment according to the
     * XML layout.
     * 
     * @param inflater the inflater
     * @param container the container
     * @param savedInstanceState the saved instance state
     * @return the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onCreateView");
        int backGroundColor = R.color.black;
        int color = Color.argb(ALPHA_VALUE, Color.red(backGroundColor), Color.green(backGroundColor),
                Color.blue(backGroundColor));
        View view = inflater.inflate(R.layout.user_policy_viewer_fragment_layout, container, false);
        view.setBackgroundColor(color);
        // lets set all the view members and fragment members in the fragment
        mOwnerNameTextView = (TextView)view.findViewById(R.id.policy_viewer_owner_txt_view);
        mUpperTitleTextView = (TextView)view.findViewById(R.id.policy_viewer_main_title_top);
        mPolicyDescTextView = (TextView)view.findViewById(R.id.policy_viewer_policy_decription_txt_view);
        mPolicyNameTextView = (TextView)view.findViewById(R.id.policy_viewer_policy_name_txt_view);
        mRightLayout = (LinearLayout)view.findViewById(R.id.list_layout);
        mEditBtnContainer = (ViewGroup)view.findViewById(R.id.edit_btn_container);
        mEditBtn = (Button)view.findViewById(R.id.edit_btn_policy_viewer);
        mEditBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "Edit button onClick called");
                mUserPolicyViewerFragmentEventListener.onEditButtonClicked();
            }
        });
        // Use information in storage if it exists and is the latest info.
        if (savedInstanceState != null)
        {
            // TODO restore state
        }
        drawUI();
        Logger.me(TAG, "onCreateView");
        return view;
    }

    /**
     * Draws and appends rights views.
     */
    private void drawRights()
    {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        List<RightAccessCheckModel> effectiveViewableRights = mUserPolicyDataProvider.getUserPolicyModel()
                .getEffectiveViewableRights();
        for (RightAccessCheckModel obj : effectiveViewableRights)
        {
            View rightView = inflater.inflate(R.layout.user_policy_viewer_rights_item, null);
            TextView rightNameTxtView = (TextView)rightView.findViewById(R.id.right_text_view);
            ImageView imageView = (ImageView)rightView.findViewById(R.id.rights_item_image_view);
            if (rightNameTxtView != null)
            {
                rightNameTxtView.setText(obj.getRightName());
                // set the correct icon in case right is supported or not
                if (obj.getHasAccess())
                {
                    rightNameTxtView.setTextColor(getResources().getColor(R.color.light_gray));
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.v));
                }
                else
                {
                    rightNameTxtView.setTextColor(getResources().getColor(R.color.light_black));
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.x));
                }
            }
            // add the view to the container panel
            mRightLayout.addView(rightView);
        }
    }

    /**
     * Updates the fragment with policy details.
     */
    private void drawUI()
    {
        UserPolicyModel userPolicyModel = mUserPolicyDataProvider.getUserPolicyModel();
        if ((mPolicyNameTextView == null) || (mOwnerNameTextView == null) || (userPolicyModel == null)
                || (mUpperTitleTextView == null) || (mPolicyDescTextView == null) || (mEditBtn == null))
        {
            Logger.d(TAG, "Failed updating UI as view is not available");
            return;
        }
        String policyName = ((userPolicyModel != null) && (userPolicyModel.getName() != null)) ? userPolicyModel
                .getName() : UNKNOWN_TEXT + " policy";
        String policyDescription = ((userPolicyModel != null) && (userPolicyModel.getDescription() != null)) ? userPolicyModel
                .getDescription() : UNKNOWN_TEXT + " description";
        mPolicyNameTextView.setText(policyName);
        mPolicyDescTextView.setText(policyDescription);
        updateViewAccordingToOwnership();
    }

    /**
     * Sets the policy editing button view state.
     * 
     * @param isEnabled the new policy editing button view state
     */
    private void setPolicyEditingButtonViewState(boolean isEnabled)
    {
        if (mEditBtn != null)
        {
            if (isEnabled)
            {
                mEditBtn.setVisibility(View.VISIBLE);
                mEditBtnContainer.setVisibility(View.VISIBLE);
            }
            else
            {
                mEditBtnContainer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Updates the policy viewer according to the ownership
     */
    private void updateViewAccordingToOwnership()
    {
        UserPolicyModel userPolicyModel = mUserPolicyDataProvider.getUserPolicyModel();
        // if this is not owner just show the rights with no header
        if (userPolicyModel.isIssuedToOwner())
        {
            Logger.d(TAG, "user is the owner of user policy");
            mUpperTitleTextView.setText(getString(R.string.policy_viewer_owner_content));
            mOwnerNameTextView.setVisibility(View.GONE);
            setPolicyEditingButtonViewState(mUserPolicyDataProvider.isUserPolicyEditingEnabled());
        }
        else
        {
            Logger.d(TAG, "user is not the owner of user policy");
            //ignore input of allowing edit enabled and hide policy edit button 
            setPolicyEditingButtonViewState(false);
            mUpperTitleTextView.setText(R.string.policy_viewer_non_owner_content);
            SpannableStringBuilder sb = new SpannableStringBuilder();
            String grantedBy = getString(R.string.granted_by_string);
            sb.append(grantedBy);
            sb.append(" ");
            sb.append(userPolicyModel.getOwner());
            sb.setSpan(new StyleSpan(Typeface.BOLD), grantedBy.length(), sb.length(), 0);
            mOwnerNameTextView.setText(sb);
            drawRights();
        }
    }
}

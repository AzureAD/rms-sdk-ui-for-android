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

package com.microsoft.rightsmanagement.ui;

import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;
import com.microsoft.rightsmanagement.ui.utils.Helpers;
import com.microsoft.rightsmanagement.ui.utils.Logger;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * The Class BaseAnimatedActivity. Contains common code used across sub-activities.
 */
abstract class BaseActivity extends FragmentActivity
{
    protected static final String REQUEST_CALLBACK_ID = "REQUEST_CALLBACK_ID";
    
    protected static String TAG = "BaseAnimatedActivity";

    protected View mBaseContainerView;

    protected ValueAnimator mBgColorAnimationAtActivityEnd;

    protected ValueAnimator mBgColorAnimationAtActivityStart;
    
    protected int mRequestCallbackId;
    
    protected boolean mActivityFinishedWithResult;
    
    /**
     * Sets the tag.
     * 
     * @param tag the new tag
     */
    protected static void setTAG(String tag)
    {
        TAG = tag;
    }
    
    /**
     * Validate activity input parameter.
     * 
     * @param activity the activity
     * @return the activity
     * @throws InvalidParameterException the invalid parameter exception
     */
    protected static Activity validateActivityInputParameter(Activity activity) throws InvalidParameterException
    {
        if (activity == null)
        {
            InvalidParameterException exception = new InvalidParameterException();
            Logger.e(TAG, "invalid parameter activity", "", exception);
            throw exception;
        }
        return activity;
    }
    /**
     * Validate completion callback input parameter.
     * 
     * @param <T> the generic type
     * @param completionCallback the completion callback
     * @return the completion callback
     * @throws InvalidParameterException the invalid parameter exception
     */
    protected static <T> CompletionCallback<T> validateCompletionCallbackInputParameter(CompletionCallback<T> completionCallback)
            throws InvalidParameterException
    {
        if (completionCallback == null)
        {
            InvalidParameterException exception = new InvalidParameterException();
            Logger.e(TAG, "invalid parameter completionCallback", "", exception);
            throw exception;
        }
        return completionCallback;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onBackPressed()
     */
    @Override
    public void onBackPressed()
    {
        Logger.ms(TAG, "onBackPressed");
        Intent data = new Intent();
        data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
        returnToCaller(RESULT_CANCELED, data);
        Logger.me(TAG, "onBackPressed");
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onWindowFocusChanged(boolean)
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        // start Activity Start Animation
        if (hasFocus && mBaseContainerView != null && mBgColorAnimationAtActivityStart != null)
        {
            int animationDuration = this.getResources().getInteger(R.integer.fragment_slide_duration);
            mBgColorAnimationAtActivityStart.setDuration(animationDuration);
            mBgColorAnimationAtActivityStart.start();
        }
    }

    /**
     * This methods sets up finishing activity when transparent parts are clicked.
     * 
     * @param viewId view id of transparent view
     */
    protected void addTransparentPartDismissListener(int viewId)
    {
        View view = findViewById(viewId);
        if (view != null)
        {
            view.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Logger.ms(TAG, "onClick - for dismissing activity");
                    Intent data = new Intent();
                    data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
                    returnToCaller(RESULT_CANCELED, data);
                    Logger.me(TAG, "onClick - for dismissing activity");
                }
            });
        }
    }

    /**
     * Creates the bg animators.
     * 
     * @param baseContainerId the base container id
     * @param savedInstanceState the saved instance state
     */
    protected void createBgAnimators(int baseContainerId, Bundle savedInstanceState)
    {
        Logger.ms(TAG, "createBgAnimators");
        mBaseContainerView = findViewById(baseContainerId);
        if (mBaseContainerView != null)
        {
            int originalBackgroundColor = Color.TRANSPARENT;
            Drawable background = mBaseContainerView.getBackground();
            if (background instanceof ColorDrawable)
            {
                originalBackgroundColor = ((ColorDrawable)background).getColor();
            }
            int overlayBackgroundColor = getResources().getColor(R.color.overlayed);
            if (savedInstanceState == null)
            {
                mBgColorAnimationAtActivityStart = Helpers.createBackgroundColorFaderAnimation(mBaseContainerView,
                        originalBackgroundColor, overlayBackgroundColor);
            }
            else
            // on configuration change (e.g. rotation) don't animate from original color
            {
                mBaseContainerView.setBackgroundColor(overlayBackgroundColor);
            }
            mBgColorAnimationAtActivityEnd = Helpers.createBackgroundColorFaderAnimation(mBaseContainerView,
                    overlayBackgroundColor, originalBackgroundColor);
        }
        Logger.me(TAG, "createBgAnimators");
    }
    
    /**
     * Return to caller.
     * 
     * @param resultCode the result code
     * @param data the data
     */
    protected void returnToCaller(int resultCode, Intent data)
    {
        mActivityFinishedWithResult = true;
    }
    
    /**
     * Start activity end animation and finish activity.
     */
    protected void startActivityEndAnimationAndFinishActivity()
    {
        int animationDuration = this.getResources().getInteger(R.integer.fragment_slide_duration);
        // start the background color fader animation
        if (mBaseContainerView != null)
        {
            mBgColorAnimationAtActivityEnd.setDuration(animationDuration);
            mBgColorAnimationAtActivityEnd.start();
        }
        
        // delay finish to allow fragment animation to complete
        //TODO: call finish on animation end
        Handler handler = new Handler(this.getMainLooper());
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                finish();
            }
        }, animationDuration);
    }
}

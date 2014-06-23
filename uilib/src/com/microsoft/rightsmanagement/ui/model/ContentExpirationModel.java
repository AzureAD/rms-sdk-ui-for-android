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

package com.microsoft.rightsmanagement.ui.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;

import com.microsoft.rightsmanagement.ui.R;

/**
 * Models Content Expiration duration picker page
 */
public class ContentExpirationModel
{
    /**
     * Enum to indicate content expiry duration type
     */
    public enum ExpiryDateType
    {
        /** custom duration by user **/
        CUSTOM,
        /** Never expires **/
        NEVER,
        /** fixed duration like 1 month **/
        STATIC
    }
    private static List<ContentExpirationModel> sContentExpirationModelList;
    private Integer mCalendarField;// Indicates if we need to add CALENDAR.MONTH or CALENDAR.WEEK
    private Integer mDuration;
    private String mExpiryDateTitle;
    private ExpiryDateType mExpiryDateType;

    private ContentExpirationModel(ExpiryDateType expiryDateType,
                                   String title,
                                   Integer calendarFieldType,
                                   Integer durationInFieldUnits)
    {
        mExpiryDateType = expiryDateType;
        mExpiryDateTitle = title;
        mCalendarField = calendarFieldType;
        mDuration = durationInFieldUnits;
    }

    /**
     * Returns static list to display on content expires page
     * 
     * @param applicationContext
     * @return List<ContentExpirationModel> unmodifiable list to render content expiration list in the view
     */
    public static List<ContentExpirationModel> getContextExpiresList(Context applicationContext)
    {
        Resources resources = applicationContext.getResources();
        if (sContentExpirationModelList == null)
        {
            List<ContentExpirationModel> contentExpiresModelList = new ArrayList<ContentExpirationModel>();
            contentExpiresModelList.add(new ContentExpirationModel(ExpiryDateType.NEVER, resources
                    .getString(R.string.duration_never), null, null));
            contentExpiresModelList.add(new ContentExpirationModel(ExpiryDateType.STATIC, resources
                    .getString(R.string.duration_one_week), Calendar.DATE, 7));
            contentExpiresModelList.add(new ContentExpirationModel(ExpiryDateType.STATIC, resources
                    .getString(R.string.duration_one_month), Calendar.MONTH, 1));
            contentExpiresModelList.add(new ContentExpirationModel(ExpiryDateType.STATIC, resources
                    .getString(R.string.duration_two_months), Calendar.MONTH, 2));
            contentExpiresModelList.add(new ContentExpirationModel(ExpiryDateType.CUSTOM, resources
                    .getString(R.string.duration_custom), null, null));
            sContentExpirationModelList = Collections.unmodifiableList(contentExpiresModelList);
        }
        return sContentExpirationModelList;
    }

    /**
     * Calendar Field only used for static duration types
     * 
     * @return calendar objects duration type CALENDAR.MONTH , CALENDAR.WEEK etc
     */
    public Integer getCalendarField()
    {
        return mCalendarField;
    }

    /**
     * Calculates date till which the content should be valid based on duration
     * 
     * @return Date till which content should be valid
     */
    public Date getContentValidUntilDate()
    {
        if (getExpiryDateType() == ExpiryDateType.NEVER)
        {
            return null;
        }
        else
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(getCalendarField(), getDuration());
            return calendar.getTime();
        }
    }

    /**
     * Duration in CalnderField units , eg. duration 1 and Calendar field CALENDAR.MONTH suggest 1 month duration
     * 
     * @return Duration
     */
    public Integer getDuration()
    {
        return mDuration;
    }

    /**
     * Date Title to display in the view
     * 
     * @return ExpiryDateTitle
     */
    public String getExpiryDateTitle()
    {
        return mExpiryDateTitle;
    }

    /**
     * Gets the expiry date type if custom , static etc
     * 
     * @return Expiry date type enum
     */
    public ExpiryDateType getExpiryDateType()
    {
        return mExpiryDateType;
    }
}

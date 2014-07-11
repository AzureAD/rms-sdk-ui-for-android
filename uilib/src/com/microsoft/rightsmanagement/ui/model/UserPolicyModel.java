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

package com.microsoft.rightsmanagement.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.utils.Helpers;
import com.microsoft.rightsmanagement.ui.utils.Logger;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Parcel;
import android.os.Parcelable;
import com.microsoft.rightsmanagement.UserPolicy;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;

/**
 * Models User Policy.
 */
public class UserPolicyModel implements Parcelable
{
    public static final Parcelable.Creator<UserPolicyModel> CREATOR = new Creator<UserPolicyModel>()
    {
        @Override
        public UserPolicyModel createFromParcel(Parcel in)
        {
            return new UserPolicyModel(in);
        }

        @Override
        public UserPolicyModel[] newArray(int size)
        {
            return new UserPolicyModel[size];
        }
    };
    public static final String TAG = "UserPolicyModel";
    private static HashMap<String, Integer> sRightIDToResourceIDMap = Helpers.createHashMap(String.class, Integer.class,
            "OWNER", R.string.owner_description_string,
            "VIEW", R.string.view_description_string,
            "EDIT", R.string.edit_description_string,
            "EXPORT", R.string.export_description_string,
            "EXTRACT", R.string.extract_description_string,
            "PRINT", R.string.print_description_string,
            "REPLY", R.string.reply_description_string,
            "REPLYALL", R.string.reply_all_description_string,
            "FORWARD", R.string.forward_description_string,
            "COMMENT", R.string.comment_description_string);
    private String mDescription;
    private List<RightAccessCheckModel> mEffectiveViewableRights;
    private boolean mIsIssuedToOwner;
    private String mName;
    private String mOwner;

    /**
     * Instantiates a new user policy model.
     * 
     * @param userPolicy the user policy
     * @param supportedRights rights to check access for and display
     * @param applicationContext the application context
     * @throws InvalidParameterException the invalid parameter exception
     */
    public UserPolicyModel(UserPolicy userPolicy,
                           LinkedHashSet<String> supportedRights,
                           Context applicationContext) throws InvalidParameterException
    {
        mName = userPolicy.getName();
        mDescription = userPolicy.getDescription();
        mOwner = userPolicy.getOwner();
        mIsIssuedToOwner = userPolicy.isIssuedToOwner();
        makeEffectiveRights(userPolicy, supportedRights, applicationContext);
    }

    /**
     * Gets the rights display name.
     * 
     * @param applicationContext the context
     * @param right the right string ID
     * @return the rights display name if found else the right ID
     */
    public static String getRightsDisplayName(Context applicationContext, String right)
    {
        String rightsDisplayName = right;
        if (sRightIDToResourceIDMap.containsKey(right))
        {
            try
            {
                rightsDisplayName = applicationContext.getResources().getString(sRightIDToResourceIDMap.get(right));
            }
            catch (NotFoundException ex)
            {
                Logger.ie(TAG, String.format(
                        "Resource id for Right: %s was not found in resources. Exception Message: %s", right,
                        ex.getMessage()));
            }
        }
        else
        {
            Logger.i(TAG, String.format("Right: %s - resource id was not found", right), "");
        }
        return rightsDisplayName;
    }

    /**
     * Instantiates a new user policy model.
     * 
     * @param in the in
     */
    private UserPolicyModel(Parcel in)
    {
        mName = in.readString();
        mDescription = in.readString();
        mOwner = in.readString();
        mIsIssuedToOwner = in.readByte() != 0;
        mEffectiveViewableRights = new ArrayList<RightAccessCheckModel>();
        in.readTypedList(mEffectiveViewableRights, RightAccessCheckModel.CREATOR);
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * Gets the effective viewable rights.
     * 
     * @return the effective viewable rights
     */
    public List<RightAccessCheckModel> getEffectiveViewableRights()
    {
        return mEffectiveViewableRights;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName()
    {
        return mName;
    }

    /**
     * Gets the owner.
     * 
     * @return the owner
     */
    public String getOwner()
    {
        return mOwner;
    }

    /**
     * Checks if is issued to owner.
     * 
     * @return true, if is issued to owner
     */
    public boolean isIssuedToOwner()
    {
        return mIsIssuedToOwner;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(mName);
        out.writeString(mDescription);
        out.writeString(mOwner);
        out.writeByte((byte)(mIsIssuedToOwner ? 1 : 0));
        out.writeTypedList(mEffectiveViewableRights);
    }

    /**
     * Make effective rights.
     * 
     * @param userPolicy the user policy
     * @param supportedRights rights to check access for and display
     * @param applicationContext the application context
     * @throws InvalidParameterException the invalid parameter exception
     */
    private void makeEffectiveRights(UserPolicy userPolicy, LinkedHashSet<String> supportedRights, Context applicationContext)
            throws InvalidParameterException
    {
        mEffectiveViewableRights = new ArrayList<RightAccessCheckModel>();
        for (String right : supportedRights)
        {
            boolean hasAccess = userPolicy.accessCheck(right);
            String rightDisplayName = getRightsDisplayName(applicationContext, right);
            mEffectiveViewableRights.add(new RightAccessCheckModel(rightDisplayName, hasAccess));
        }
    }
}

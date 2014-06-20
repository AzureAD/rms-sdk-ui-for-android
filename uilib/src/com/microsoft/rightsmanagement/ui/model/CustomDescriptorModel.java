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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.microsoft.rightsmanagement.CommonRights;
import com.microsoft.rightsmanagement.EditableDocumentRights;
import com.microsoft.rightsmanagement.PolicyDescriptor;
import com.microsoft.rightsmanagement.UserRights;
import com.microsoft.rightsmanagement.ui.R;

/**
 * Models Custom Descriptor.
 */
public class CustomDescriptorModel extends DescriptorModel implements Parcelable
{
    /**
     * CREATOR for this Parcelable object
     */
    public static final Parcelable.Creator<CustomDescriptorModel> CREATOR = new Creator<CustomDescriptorModel>()
    {
        @Override
        public CustomDescriptorModel createFromParcel(Parcel in)
        {
            return new CustomDescriptorModel(in);
        }

        @Override
        public CustomDescriptorModel[] newArray(int size)
        {
            return new CustomDescriptorModel[size];
        }
    };
    /** Placeholder Id string for custom descriptor */
    public static final String CUSTOM_DESCRIPTOR_ID = "CUSTOM_DESCRIPTOR_ID";
    private Date mContentValidUntil; // by default null , ie. never expires
    private ArrayList<String> mRightsArrayList = new ArrayList<String>();
    private ArrayList<String> mUsersArrayList = new ArrayList<String>();

    /**
     * Instantiates a new policy descriptor model
     */
    private CustomDescriptorModel(Parcel in)
    {
        super(in);
        mContentValidUntil = (Date)in.readSerializable();
        in.readStringList(mRightsArrayList);
        in.readStringList(mUsersArrayList);
    }

    private CustomDescriptorModel(String name,
                                  String description,
                                  Collection<String> rights)
    {
        super(CUSTOM_DESCRIPTOR_ID, name, description);
        mRightsArrayList.addAll(rights);
        // TODO: should any users get rights by default? like owner getting owner right?
    }

    /**
     * Creates the custom permission items
     * 
     * @param applicationContext the application context
     * @return the template descriptor model
     */
    public static CustomDescriptorModel[] create(Context applicationContext)
    {
        List<CustomDescriptorModel> customDescriptorItems = new ArrayList<CustomDescriptorModel>();
        Resources resources = applicationContext.getResources();
        customDescriptorItems.add(new CustomDescriptorModel(resources.getString(R.string.custom_policy_viewer),
                resources.getString(R.string.custom_policy_viewer_description), Arrays.asList(new String[] {
                    CommonRights.View
                })));
        customDescriptorItems.add(new CustomDescriptorModel(resources.getString(R.string.custom_policy_reviewer),
                resources.getString(R.string.custom_policy_reviewer_description), Arrays.asList(new String[] {
                        CommonRights.View, EditableDocumentRights.Edit
                })));
        customDescriptorItems.add(new CustomDescriptorModel(resources.getString(R.string.custom_policy_co_auther),
                resources.getString(R.string.custom_policy_co_auther_description), Arrays.asList(new String[] {
                        CommonRights.View, EditableDocumentRights.Edit, EditableDocumentRights.Extract,
                        EditableDocumentRights.Print
                })));
        customDescriptorItems.add(new CustomDescriptorModel(resources.getString(R.string.custom_policy_co_owner),
                resources.getString(R.string.custom_policy_co_owner_description), EditableDocumentRights.ALL));
        customDescriptorItems.add(new CustomDescriptorModel(resources.getString(R.string.custom_policy_encrypt_only),
                resources.getString(R.string.custom_policy_encrypt_only_description), EditableDocumentRights.ALL));
        return customDescriptorItems.toArray(new CustomDescriptorModel[customDescriptorItems.size()]);
    }

    /**
     * Creates Policy descriptor object from this model
     * 
     * @return PolicyDescriptor object created from model
     */
    public PolicyDescriptor createPolicyDescriptorFromModel()
    {
        UserRights userRights = new UserRights(mUsersArrayList, mRightsArrayList);
        List<UserRights> usersRightsList = new ArrayList<UserRights>();
        usersRightsList.add(userRights);
        PolicyDescriptor policyDescriptor = new PolicyDescriptor(usersRightsList);
        policyDescriptor.setName(getName());
        policyDescriptor.setDescription(getDescription());
        policyDescriptor.setContentValidUntil(mContentValidUntil);
        return policyDescriptor;
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.model.DescriptorModel#describeContents()
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            if (!(obj instanceof CustomDescriptorModel))
                return false;
            CustomDescriptorModel other = (CustomDescriptorModel)obj;
            return (getName() == other.getName());
        }
        return false;
    }

    /**
     * Gets contentValidUntil date of this custom descriptor
     * 
     * @return contentValidUntil
     */
    public Date getContentValidUntil()
    {
        return mContentValidUntil;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + getName().hashCode();
        return result;
    }

    /**
     * Sets contentValidUntil date of this custom descriptor
     * 
     * @param contentValidUntil
     */
    public void setContentValidUntil(Date contentValidUntil)
    {
        mContentValidUntil = contentValidUntil;
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.ui.model.DescriptorModel#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        super.writeToParcel(out, flags);
        out.writeSerializable(mContentValidUntil);
        out.writeStringList(mRightsArrayList);
        out.writeStringList(mUsersArrayList);
    }
}

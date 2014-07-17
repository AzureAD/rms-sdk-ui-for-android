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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.TemplateDescriptor;

/**
 * Models TemplateDescriptor.
 */
public class TemplateDescriptorModel implements Parcelable
{
    public static final Parcelable.Creator<TemplateDescriptorModel> CREATOR = new Creator<TemplateDescriptorModel>()
    {
        @Override
        public TemplateDescriptorModel createFromParcel(Parcel in)
        {
            return new TemplateDescriptorModel(in);
        }

        @Override
        public TemplateDescriptorModel[] newArray(int size)
        {
            return new TemplateDescriptorModel[size];
        }
    };
    private static final String CUSTOM_PERMISSION_TEMPLATE_DESCRIPTOR_ID = "$CustomPermissions$";
    private static final String NO_PROTECTION_TEMPLATE_DESCRIPTOR_ID = "$NoProtection$";
    protected String mDescription;
    protected String mId;
    protected String mName;

    /**
     * Creates the.
     * 
     * @param templateDescriptors the template descriptors
     * @return the template descriptor model[]
     */
    public static TemplateDescriptorModel[] create(TemplateDescriptor[] templateDescriptors)
    {
        TemplateDescriptorModel[] templateDescriptorItems = new TemplateDescriptorModel[templateDescriptors.length];
        for (int i = 0; i < templateDescriptors.length; i++)
        {
            templateDescriptorItems[i] = new TemplateDescriptorModel(templateDescriptors[i]);
        }
        return templateDescriptorItems;
    }

    /**
     * Creates the custom permission fake item.
     * 
     * @param applicationContext the application context
     * @return the template descriptor model
     */
    public static TemplateDescriptorModel createCustomPermissionFakeItem(Context applicationContext)
    {
        TemplateDescriptorModel customProtectionDescriptor = new TemplateDescriptorModel();
        customProtectionDescriptor.mId = CUSTOM_PERMISSION_TEMPLATE_DESCRIPTOR_ID;
        customProtectionDescriptor.mName = applicationContext.getResources().getString(R.string.custom_permissions);
        customProtectionDescriptor.mDescription = applicationContext.getResources().getString(
                R.string.custom_permissions);
        return customProtectionDescriptor;
    }

    /**
     * Creates the no protection fake item.
     * 
     * @param applicationContext the application context
     * @return the template descriptor model
     */
    public static TemplateDescriptorModel createNoProtectionFakeItem(Context applicationContext)
    {
        TemplateDescriptorModel customNoProtectionDescriptor = new TemplateDescriptorModel();
        customNoProtectionDescriptor.mId = NO_PROTECTION_TEMPLATE_DESCRIPTOR_ID;
        customNoProtectionDescriptor.mName = applicationContext.getResources().getString(
                R.string.no_protection_policy_name_string);
        customNoProtectionDescriptor.mDescription = applicationContext.getResources().getString(
                R.string.no_protection_policy_name_string_description);
        return customNoProtectionDescriptor;
    }

    /**
     * Instantiates a new template descriptor model.
     * 
     * @param templateDescriptor the template descriptor
     */
    public TemplateDescriptorModel(TemplateDescriptor templateDescriptor)
    {
        mDescription = templateDescriptor.getDescription();
        mName = templateDescriptor.getName();
        mId = templateDescriptor.getTemplateId();
    }

    /**
     * Instantiates a new template descriptor model.
     */
    protected TemplateDescriptorModel()
    {
    }

    /**
     * Instantiates a new template descriptor model.
     * 
     * @param in the in
     */
    private TemplateDescriptorModel(Parcel in)
    {
        mDescription = in.readString();
        mId = in.readString();
        mName = in.readString();
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
     * Self discovery.
     * 
     * @param inThisPoolOfTemplateDescriptors source pool of template descriptors
     * @return the template descriptor
     */
    public TemplateDescriptor find(TemplateDescriptor[] inThisPoolOfTemplateDescriptors)
    {
        TemplateDescriptor returnValueOfTemplateDescriptor = null;
        if (inThisPoolOfTemplateDescriptors != null)
        {
            for (TemplateDescriptor templateDescriptor : inThisPoolOfTemplateDescriptors)
            {
                if (templateDescriptor.getTemplateId().equals(getId()))
                {
                    returnValueOfTemplateDescriptor = templateDescriptor;
                    break;
                }
            }
        }
        return returnValueOfTemplateDescriptor;
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
     * Gets the id.
     * 
     * @return the id
     */
    public String getId()
    {
        return mId;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public CharSequence getName()
    {
        return mName;
    }

    /**
     * Checks if is custom permissions template descriptor item.
     * 
     * @return true, if is custom permissions template descriptor item
     */
    public boolean isCustomPermissionsTemplateDescriptorItem()
    {
        if (getId().equals(CUSTOM_PERMISSION_TEMPLATE_DESCRIPTOR_ID))
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if is no protection template descriptor item.
     * 
     * @return true, if is no protection template descriptor item
     */
    public boolean isNoProtectionTemplateDescriptorItem()
    {
        if (getId().equals(NO_PROTECTION_TEMPLATE_DESCRIPTOR_ID))
        {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(mDescription);
        out.writeString(mId);
        out.writeString(mName);
    }
}

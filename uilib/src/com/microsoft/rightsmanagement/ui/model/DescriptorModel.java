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

import android.os.Parcel;
import android.os.Parcelable;

import com.microsoft.rightsmanagement.TemplateDescriptor;

/**
 * Models Descriptor.
 */
public class DescriptorModel implements Parcelable
{
    /**
     * CREATOR for this Parcelable object
     */
    public static final Parcelable.Creator<DescriptorModel> CREATOR = new Creator<DescriptorModel>()
    {
        @Override
        public DescriptorModel createFromParcel(Parcel in)
        {
            return new DescriptorModel(in);
        }

        @Override
        public DescriptorModel[] newArray(int size)
        {
            return new DescriptorModel[size];
        }
    };
    private String mDescription;
    private String mId;
    private String mName;

    /**
     * Instantiates a new template descriptor model from TemplateDescriptor object
     * 
     * @param templateDescriptor the template descriptor
     */
    public DescriptorModel(TemplateDescriptor templateDescriptor)
    {
        mId = templateDescriptor.getTemplateId();
        mName = templateDescriptor.getName();
        mDescription = templateDescriptor.getDescription();
    }

    /**
     * Creates DescriptorModel objects from TemplateDescriptor objects
     * 
     * @param templateDescriptors the template descriptors
     * @return the template descriptor model[]
     */
    public static DescriptorModel[] create(TemplateDescriptor[] templateDescriptors)
    {
        DescriptorModel[] templateDescriptorItems = new DescriptorModel[templateDescriptors.length];
        for (int i = 0; i < templateDescriptors.length; i++)
        {
            templateDescriptorItems[i] = new DescriptorModel(templateDescriptors[i]);
        }
        return templateDescriptorItems;
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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DescriptorModel))
            return false;
        DescriptorModel other = (DescriptorModel)obj;
        return (mId == other.getId());
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
     * Gets the Id
     * 
     * @return the Id
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
    public String getName()
    {
        return mName;
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
        result = prime * result + mId.hashCode();
        return result;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(mId);
        out.writeString(mName);
        out.writeString(mDescription);
    }
    
    
    /**
     * Instantiates a new descriptor model.
     * 
     * @param in the in
     */
    protected DescriptorModel(Parcel in)
    {
        mId = in.readString();
        mName = in.readString();
        mDescription = in.readString();
    }

    protected DescriptorModel(String id,
                              String name,
                              String description)
    {
        mId = id;
        mName = name;
        mDescription = description;
    }
}

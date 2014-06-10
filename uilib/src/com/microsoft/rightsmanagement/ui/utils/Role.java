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

package com.microsoft.rightsmanagement.ui.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.rightsmanagement.CommonRights;
import com.microsoft.rightsmanagement.EditableDocumentRights;

public class Role
{
    public static final Role CoAuthor;
    public static final Role CoOwner;
    public static final Role Reviewer;
    public static final Role Viewer;
    /** The Is visible. */
    private boolean mIsVisible;
    /** The Rights. */
    private Map<String, String> mRights;
    /** The Type. */
    private RoleType mType;
    // Default Co-Owner rights
    static
    {
        CoOwner = new Role(RoleType.CoOwner);
        CoOwner.addRight(CommonRights.Owner);
    }
    // Default Co-Author rights
    static
    {
        CoAuthor = new Role(RoleType.CoAuthor);
        CoAuthor.addRight(EditableDocumentRights.Print);
        CoAuthor.addRight(EditableDocumentRights.Export);
    }
    // Default Reviewer rights
    static
    {
        Reviewer = new Role(RoleType.Reviewer);
        Reviewer.addRight(EditableDocumentRights.Edit);
    }
    // Default Viewer rights
    static
    {
        Viewer = new Role(RoleType.Viewer);
        Viewer.addRight(CommonRights.View);
    }

    /**
     * Instantiates a new role.
     * 
     * @param type the type
     */
    public Role(RoleType type)
    {
        mType = type;
        mIsVisible = true;
        mRights = new HashMap<String, String>();
    }

    /**
     * Adds the rights.
     * 
     * @param right the right
     */
    public void addRight(String right)
    {
        if (mRights.containsKey(right) == false)
        {
            mRights.put(right, right);
        }
    }

    /**
     * Copy. create a copy of the role.
     * 
     * @return the role
     */
    public Role copy()
    {
        Role newRole = new Role(getType());
        for (Map.Entry<String, String> entry : this.mRights.entrySet())
        {
            newRole.addRight(entry.getValue());
        }
        newRole.setIsVisible(this.isVisible());
        return newRole;
    }

    /**
     * Gets the rights.
     * 
     * @return the rights
     */
    public Collection<String> getRights()
    {
        return Collections.unmodifiableCollection(mRights.values());
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public RoleType getType()
    {
        return mType;
    }

    /**
     * Checks if is visible.
     * 
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        return mIsVisible;
    }

    /**
     * Removes the right.
     * 
     * @param right the right
     */
    public void removeRight(String right)
    {
        if (mRights.containsKey(right) == true)
        {
            mRights.remove(right);
        }
    }

    /**
     * Sets the checks if is visible.
     * 
     * @param isVisible the new checks if is visible
     */
    public void setIsVisible(boolean isVisible)
    {
        mIsVisible = isVisible;
    }
}

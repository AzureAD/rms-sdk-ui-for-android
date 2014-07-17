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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the custom policy view settings manager.
 */
public class CustomPolicySettings
{
    private boolean mDoesHaveCustomRights;
    private Map<String, Integer> mRightToRoleMap;
    private List<Role> mRoleList;

    /**
     * Instantiates a new ad hoc policy settings.
     */
    public CustomPolicySettings()
    {
        mRoleList = new ArrayList<Role>();
        mRightToRoleMap = new HashMap<String, Integer>();
        mRoleList.add(Role.Viewer.copy());
        putRights(Role.Viewer, mRightToRoleMap);
        mRoleList.add(Role.Reviewer.copy());
        putRights(Role.Reviewer, mRightToRoleMap);
        mRoleList.add(Role.CoAuthor.copy());
        putRights(Role.CoAuthor, mRightToRoleMap);
        mRoleList.add(Role.CoOwner.copy());
        putRights(Role.CoOwner, mRightToRoleMap);
    }

    /**
     * Adds the custom right.
     * 
     * @param type the type
     * @param customRight the custom right
     */
    public void addCustomRight(RoleType type, String customRight)
    {
        mDoesHaveCustomRights = true;
        // Right already contained. we remove it.
        if (mRightToRoleMap.containsKey(customRight))
        {
            // Get the Role that contained the right.
            Integer roleIndex = mRightToRoleMap.get(customRight);
            if (roleIndex == type.getIndex())
            {
                // Right already exists and is properly mapped to the role.
                return;
            }
            // Remove right from role.
            mRoleList.get(roleIndex).removeRight(customRight);
            // Remove right from right to role mapping.
            mRightToRoleMap.remove(customRight);
        }
        mRightToRoleMap.put(customRight, type.getIndex());
        mRoleList.get(type.getIndex()).addRight(customRight);
    }

    /**
     * Adds the role.
     * 
     * @param type the type
     */
    public void addRole(RoleType type)
    {
        if (type != null)
        {
            mRoleList.get(type.getIndex()).setIsVisible(true);
        }
    }

    /**
     * Gets the current roles.
     * 
     * @return the current roles
     */
    public String[] getCurrentRoles()
    {
        List<String> visibleRoleNames = new ArrayList<String>();
        for (int i = 0; i < RoleType.ROLE_NUMBER; i++)
        {
            Role currentRole = mRoleList.get(i);
            if (currentRole.isVisible())
            {
                visibleRoleNames.add(currentRole.getType().toString());
            }
        }
        return visibleRoleNames.toArray(new String[visibleRoleNames.size()]);
    }

    /**
     * Removes the role.
     * 
     * @param type the type
     */
    public void removeRole(RoleType type)
    {
        if (type != null)
        {
            mRoleList.get(type.getIndex()).setIsVisible(false);
        }
    }

    /**
     * Does have custom rights.
     * 
     * @return true, if successful
     */
    protected boolean doesHaveCustomRights()
    {
        return mDoesHaveCustomRights;
    }

    /**
     * Gets all the rights a role has including does of a lower hierarchy.
     * 
     * @param typeRole the type role
     * @return the rights null if.
     */
    protected Collection<String> getAllRights(RoleType typeRole)
    {
        int roleIndex = typeRole.getIndex();
        if (mRoleList.get(roleIndex).isVisible() == false)
        {
            return null;
        }
        Collection<String> rights = new ArrayList<String>();
        while (roleIndex >= 0)
        {
            rights.addAll(mRoleList.get(roleIndex).getRights());
            roleIndex--;
        }
        return Collections.unmodifiableCollection(rights);
    }

    /**
     * Put rights.
     * 
     * @param role the role
     * @param rightToRoleMap the right to role map
     */
    private void putRights(Role role, Map<String, Integer> rightToRoleMap)
    {
        List<String> rights = new ArrayList<String>(role.getRights());
        for (int i = 0; i < rights.size(); i++)
        {
            rightToRoleMap.put(rights.get(i), role.getType().getIndex());
        }
    }
}

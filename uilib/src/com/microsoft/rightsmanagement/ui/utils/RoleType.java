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

public enum RoleType
{
    Viewer(0), Reviewer(1), CoAuthor(2), CoOwner(3);
    public static final int ROLE_NUMBER = 4;
    private final int mIndex;

    /**
     * Instantiates a new role type.
     * 
     * @param index the index
     */
    RoleType(int index)
    {
        mIndex = index;
    }

    /**
     * Gets the index.
     * 
     * @return the index
     */
    public int getIndex()
    {
        return mIndex;
    }
}

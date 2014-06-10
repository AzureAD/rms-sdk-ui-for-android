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

import com.microsoft.rightsmanagement.ui.PolicyEnforcer.EnforcementAction;

;
/**
 * An object used to store the right and EnforcementAction the developer has set on the view using the IPolicyEnforcer
 * objects.
 */
public class PolicyEnforcerRule
{
    // the EnforcementAction associated to the rule
    private EnforcementAction mAction;
    // the previous state of the view (enabled disabled)
    private boolean mPrevViewState;
    // the right associated to the rule
    private String mRight;

    /**
     * Instantiates a new policy enforcer rule.
     * 
     * @param right the right associated with the enforcement rule
     * @param action the action associated with the enforcement rule
     * @param state enabled disabled state of the view
     */
    public PolicyEnforcerRule(String right,
                              EnforcementAction action,
                              boolean state)
    {
        mRight = right;
        mAction = action;
        mPrevViewState = state;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof PolicyEnforcerRule))
        {
            return false;
        }
        return mRight.equals(((PolicyEnforcerRule)obj).getRight());
    }

    /**
     * Gets the action.
     * 
     * @return the action
     */
    public EnforcementAction getAction()
    {
        return mAction;
    }

    /**
     * Gets the prev view state.
     * 
     * @return the prev view state
     */
    public boolean getPrevViewState()
    {
        return mPrevViewState;
    }

    /**
     * Gets the right.
     * 
     * @return the right
     */
    public String getRight()
    {
        return mRight;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return mRight.hashCode();
    }
}

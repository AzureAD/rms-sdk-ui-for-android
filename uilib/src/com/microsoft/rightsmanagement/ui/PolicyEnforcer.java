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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.view.View;

import com.microsoft.rightsmanagement.UserPolicy;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.ui.utils.PolicyEnforcerRule;

/**
 * A helper class designed to implement policy enforcement on android views according to a user policy. A developer that
 * requires to protect UI TextView so that it will be editable only in case the "Edit" right is included in the user
 * policy will add the following rule: addRuleToView(v, EditableDocumentRights.Edit,
 * EnforcementAction.EnforcementActionDisable); Notes: a. When removing the rules from a control, any properties that
 * were changed by the class will be restored to the state before applying the rules. b. Do not apply two rules on the
 * same control
 */
public class PolicyEnforcer
{
    /**
     * The Enum EnforcementAction.
     */
    public enum EnforcementAction
    {
        /**
         * Disable the control entirely.
         */
        EnforcementActionDisable,
    }
    public static final String TAG = "PolicyEnforcer";
    private Map<View, List<PolicyEnforcerRule>> mRulesMap;
    private UserPolicy mUserPolicy;

    /**
     * Constructor of the PolicyEnforcer object.
     * 
     * @param userPolicy the protection policy containing the Rights, upon which the rights added in addRuleToView
     *            method will be checked if they are contained in the protection policy or not. If the right added in
     *            addRuleToView does not exists in the Protection policy in the constructor, the enforcement action in
     *            addRuleToView will be enforced on the view.
     */
    public PolicyEnforcer(UserPolicy userPolicy)
    {
        Logger.ms(TAG, "PolicyEnforcer");
        mRulesMap = new HashMap<View, List<PolicyEnforcerRule>>();
        mUserPolicy = userPolicy;
        Logger.me(TAG, "PolicyEnforcer");
    }

    /**
     * Adds a policy enforcement rule based on the action to be taken when the user does not have the given right.
     * 
     * @param v The view to which the rule will be added.
     * @param rightToAdd The right associated with the rule.
     * @param action The action to take if the user does not have the specified right.
     */
    public void addRuleToView(View v, String rightToAdd, EnforcementAction action)
    {
        Logger.ms(TAG, "addRuleToView");
        // for future implementation we are saving allowing collection of PolicyEnforcerRule objects on the view
        // mapping
        PolicyEnforcerRule rule = new PolicyEnforcerRule(rightToAdd, action, v.isEnabled());
        if (mRulesMap.get(v) == null)
        {
            List<PolicyEnforcerRule> list = new ArrayList<PolicyEnforcerRule>();
            list.add(rule);
            mRulesMap.put(v, list);
        }
        else
        {
            mRulesMap.get(v).add(rule);
        }
        protect(v, rule);
        Logger.me(TAG, "addRuleToView");
    }

    /**
     * Removes all of the policy enforcement rules associated with the specified control.
     * 
     * @param v The View from which to remove the rules.
     */
    public void removeAllRulesFromView(View v)
    {
        Logger.ms(TAG, "removeAllRulesFromView");
        undoProtectionActionOnView(v);
        Logger.me(TAG, "removeAllRulesFromView");
    }

    /**
     * Removes the all rules from views.
     */
    public void removeAllRulesFromViews()
    {
        Logger.ms(TAG, "removeAllRulesFromViews");
        for (Entry<View, List<PolicyEnforcerRule>> entry : mRulesMap.entrySet())
        {
            entry.getKey().setEnabled(entry.getValue().get(0).getPrevViewState());
        }
        mRulesMap.clear();
        Logger.me(TAG, "removeAllRulesFromViews");
    }

    /**
     * Updates the protection policy associated with the policy enforcer triggering reavaluation of all enforcement
     * rules.
     * 
     * @param policy the new policy
     */
    public void setPolicy(UserPolicy policy)
    {
        Logger.ms(TAG, "setPolicy");
        mUserPolicy = policy;
        Set<Entry<View, List<PolicyEnforcerRule>>> entrySet = mRulesMap.entrySet();
        for (Entry<View, List<PolicyEnforcerRule>> entry : entrySet)
        {
            undoProtectionActionOnView(entry.getKey(), false);
            protect(entry.getKey(), entry.getValue().get(0));
        }
        Logger.me(TAG, "setPolicy");
    }

    /**
     * Invoke protection action on view.
     * 
     * @param action the action
     * @param v the v
     */
    private void invokeProtectionActionOnView(EnforcementAction action, final View v)
    {
        switch (action)
        {
            case EnforcementActionDisable:
                v.setEnabled(false);
                break;
        }
    }

    /**
     * Protect.
     * 
     * @param v the v
     * @param rule the rule
     */
    private void protect(View v, PolicyEnforcerRule rule)
    {
        if (!mUserPolicy.accessCheck(rule.getRight()))
        {
            invokeProtectionActionOnView(rule.getAction(), v);
        }
    }

    /**
     * Undo protection action on view.
     * 
     * @param v the v
     */
    private void undoProtectionActionOnView(View v)
    {
        undoProtectionActionOnView(v, true);
    }

    /**
     * Undo protection action on view.
     * 
     * @param v the v
     * @param removeRule the remove rule
     */
    private void undoProtectionActionOnView(View v, boolean removeRule)
    {
        if ((mRulesMap.get(v) != null) && (mRulesMap.get(v).size() > 0))
        {
            v.setEnabled(mRulesMap.get(v).get(0).getPrevViewState());
            if (removeRule)
            {
                mRulesMap.remove(v);
            }
        }
    }
}

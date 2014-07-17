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

package com.microsoft.rightsmanagement.ui;

import com.microsoft.rightsmanagement.PolicyDescriptor;
import com.microsoft.rightsmanagement.TemplateDescriptor;

/**
 * Represents PolicyPickerActivityResult.
 */ 
public class PolicyPickerActivityResult
{
    /**
     * The Enum PolicyPickerActivityResultType.
     */
    public enum PolicyPickerActivityResultType
    {
        /** Result is an instance of PolicyDescriptor. **/
        Custom,
        /** Result is an instance of TemplateDescriptor. **/
        Template
    }
    /** Chosen PolicyDescriptor if ResultType == Custom. **/
    public PolicyDescriptor mPolicyDescriptor;
    /** Result type to indicate if templateDescriptor object is returned or policyDescriptor object is returned. **/
    public PolicyPickerActivityResultType mResultType;
    /** Chosen TemplateDescriptor if ResultType == Template. **/
    public TemplateDescriptor mTemplateDescriptor;
}

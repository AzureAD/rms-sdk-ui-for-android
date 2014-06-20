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

import com.microsoft.rightsmanagement.PolicyDescriptor;
import com.microsoft.rightsmanagement.TemplateDescriptor;

/**
 * Represents DescriptorPickerActivityResult.
 */ 
public class DescriptorPickerActivityResult
{
    /**
     * The Enum DescriptorPickerActivityResultType.
     */
    public enum DescriptorPickerActivityResultType
    {
        /** Result is an instance of PolicyDescriptor. **/
        Custom,
        /** Result is an instance of TemplateDescriptor. **/
        Template
    }
    /** Chosen PolicyDescriptor if ResultType == Custom. **/
    public PolicyDescriptor mPolicyDescriptor;
    /** Result type to indicate if templateDescriptor object is returned or policyDescriptor object is returned. **/
    public DescriptorPickerActivityResultType mResultType;
    /** Chosen TemplateDescriptor if ResultType == Template. **/
    public TemplateDescriptor mTemplateDescriptor;
}

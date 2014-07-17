UI Library for Microsoft RMS SDK v4 for Android 
======================


The UI Library for Microsoft RMS SDK v4 for Android provides Android Activities that implement the required UI for the SDK functionality.  This library is optional and a developer may choose to build their own UI when using Microsoft RMS SDK v4.

##Features

This library contains the following Android Activities:
* **EmailActivity**: Shows an email address input screen, which is required for operations like protection of files. RMS SDK expects to get the email address of the user who wants to protect data or files to redirect his organization sign-in portal.
* **PolicyPickerActivity**: Shows a policy picker screen, where the user can choose RMS template or specify the permissions to create a protection policy and encrypt files.
* **UserPolicyViewerActivity**: Shows the permissions that the user has for RMS protected data or file.

##Contributing

All code is licensed under MICROSOFT SOFTWARE LICENSE TERMS, MICROSOFT RIGHTS MANAGEMENT SERVICE SDK UI LIBRARIES. And we triage actively on GitHub. We enthusiastically welcome contributions and feedback. You can clone the repo and start contributing now.

##Download

To get the source code of this library via git just type
```
git clone https://github.com/AzureAD/rms-sdk-ui-for-android.git
cd ./rms-sdk-ui-for-android/src
```

## How to use this library

### Prerequisites

You must have downloaded and/or installed following software

* Git
* Android SDK 
* AVD image or device running (API level 15) or higher
* Microsoft Rights Management SDK  4.0 (RMS SDK). 
  You can download this from [here](http://go.microsoft.com/fwlink/?LinkId=404271)
* Windows Azure Active Directory Authentication Library (ADAL) for Android . 
  For visit [here](https://github.com/MSOpenTech/azure-activedirectory-library-for-android) more information on ADAL. However, you may use any authentication library that supports OAUTH2


### Setting up development environment
You may use any IDE, however following steps assume use if Eclipse ADT.

1.	Familiarize yourself with RMS SDK v4 [developer guidance](http://msdn.microsoft.com/en-us/library/dn758265\(v=vs.85\).aspx), [code examples](http://msdn.microsoft.com/en-us/library/dn758246\(v=vs.85\).aspx) and [API reference](http://msdn.microsoft.com/en-us/library/dn758245\(v=vs.85\).aspx)
2.	Download Microsoft RMS SDK v4 for Android from [here](from http://go.microsoft.com/fwlink/?LinkId=404271) and setup up your development environment using [this](http://msdn.microsoft.com/en-us/library/dn758247\(v=vs.85\).aspx) guidance. 
3.	Import _uilib_ project under _./rms-sdk-ui-for-android/_ directory. 
4.	Get ADAL project by following instructions [here](https://github.com/AzureAD/azure-activedirectory-library-for-android/blob/master/README.md)
**Note:** You would require two libraries in ADAL project's _libs/_ folder ([android-support-v4.jar](https://developer.android.com/tools/support-library/setup.html) and gson.jar). If these are not present, follow instructions on ADAL webpage to procure them.
5.	Import ADAL project. 
6.	Add library reference of _RMS SDK v4_ library project to _uilib_ project and your _application_ project.
7.	Add library reference of _uilib_ project to your _application_ project.
8.	Add library reference of _ADAL_ project to your _application_ project.
9.	Add following Activities to your application's AndroidManifest.xml

```XML
<activity android:name="com.microsoft.rightsmanagement.ui.EmailActivity"
            android:exported="false"
            android:theme="@style/Overlay"
            android:windowSoftInputMode="stateHidden" />
        
<activity android:name="com.microsoft.rightsmanagement.ui.PolicyPickerActivity"
            android:exported="false"
            android:theme="@style/Overlay"
            android:windowSoftInputMode="stateHidden" />
        
<activity android:name="com.microsoft.rightsmanagement.ui.UserPolicyViewerActivity"
            android:exported="false"
            android:theme="@style/Overlay"
            android:windowSoftInputMode="stateHidden" />
```

Please refer to sample application under _./rms-sdk-ui-for-android/samples/msipcsampleapp_. This sample application uses submodule of ADAL code. Once you download this library source code using git, you may have to perform step 4. 

### Using Activities

Each activity provides two static methods 

*	**show** - to take input and start the activity
*	**onActivityResult** - to process results returned to application's main activity and invoke CompletionCallback.

Following snippets are from PolicyPickerActivity.java

```Java

public class PolicyPickerActivity extends FragmentActivity
{
    /**
     * Show UI.
     * 
     * @param requestCode the request code
     * @param parentActivity the activity
     * @param templateDescriptorList the template descriptor list
     * @param originalTemplateDescriptor the original template descriptor
     * @param pickerCompletionCallback the picker completion callback
     * @throws InvalidParameterException the invalid parameter exception
     */
    public static void show(int requestCode,
                            Activity parentActivity,
                            List<TemplateDescriptor> templateDescriptorList,
                            TemplateDescriptor originalTemplateDescriptor,
                            CompletionCallback<PolicyPickerActivityResult> pickerCompletionCallback)
            throws InvalidParameterException
    { 
              ...
    }

    /**
     * Processes the result of PolicyPickerActivity started via startActivityForResult from the parent
     * activity, and invokes the callback supplied to show(). This method must be called from parent Activity's
     * onActivityResult.
     * 
     * @param resultCode the result code parameter as supplied to parent Activity's onActivityResult
     * @param data the data parameter as supplied to parent Activity's onActivityResult
     */
    public static void onActivityResult(int resultCode, Intent data)
    {
    } 
}
```

Notice show method uses CompletionCallback as one of its parameters

```Java
/**
 * The Interface CompletionCallback.
 * Provides callback methods for MSIPC UI Activity completion events.

 * @param <T> the generic type
 */

public interface CompletionCallback<T>
{
	/**
	 * This method is called upon completion of async operation
	 * @param item the created item
	 */
    void onSuccess(T item);
    
    /**
     * This method is called on cancellation.
     */
    void onCancel();
}
```

For other Activities, please find the following classes:
/uilib/src/com/microsoft/rightsmanagement/ui/EmailActivity.java
/uilib/src/com/microsoft/rightsmanagement/ui/PolicyPickerActivityResult.java


## Sample Usage
Sample application included in the repository demonstrates the usage of this library. It is located at samples\MsipcSampleApp

### Sample Scenario: Publish a file using a RMS template and show UserPolicy.
**Step 1 : Receive email input from user by using EmailActivity**
```Java
CompletionCallback<String> emailActivityCompletionCallback = new CompletionCallback<String>()
        {
            @Override
            public void onCancel()
            {
             
            }

            @Override
            public void onSuccess(String item)
            {
             
                continueMsipcPolicyCreationWithEmailId(item, originalUserPolicy, 
                showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
            }
        };
try
{
    EmailActivity.show(EMAIL_INPUT_REQUEST, getActivity(), emailActivityCompletionCallback);
}
catch (InvalidParameterException e)
{
}
```

**Step 2 : Use user email and get Templates using MSIPC SDK v4**
```Java
CreationCallback<List<TemplateDescriptor>> getTemplatesCreationCallback = new CreationCallback<List<TemplateDescriptor>>()
        {
            @Override
            public Context getContext()
            {
            }

            @Override
            public void onCancel()
            {
            }

            @Override
            public void onFailure(ProtectionException e)
            {
            }

            @Override
            public void onSuccess(List<TemplateDescriptor> templateDescriptors)
            {
                TemplateDescriptor originalTemplateDescriptor = null;
                if (originalUserPolicy != null && 
                    originalUserPolicy.getType() == UserPolicyType.TemplateBased)
                {
                    originalTemplateDescriptor = originalUserPolicy.getTemplateDescriptor();
                }
                continueMsipcPolicyCreationByPickingAPolicy(templateDescriptors, 
                 originalTemplateDescriptor, showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
            }
        };
        try
        {
             mIAsyncControl = TemplateDescriptor.getTemplates(emailId, mRmsAuthCallback, 
               getTemplatesCreationCallback);
        }
        catch (com.microsoft.rightsmanagement.exceptions.InvalidParameterException e)
        {
         
        } 
```

**Step 3: Use PolicyPickerActivity to show these templates**
Use list of templates obtained above to call PolicyPickerActivity.Show method to display templates. Notice you can also pass in previously chosen template (originalTemplateDescriptor) for highlighting it.

```Java
private void continueMsipcPolicyCreationByPickingAPolicy(List<TemplateDescriptor> templateDescriptors, 
                                                         TemplateDescriptor originalTemplateDescriptor, 
                                                         final boolean showUserPolicyViewerOnPolicyCreation, 
                                                         final Runnable onPolicyCreationCallback)
{
        
CompletionCallback<PolicyPickerActivityResult> policyPickerActivityCompletionCallback = new CompletionCallback<PolicyPickerActivityResult>()
        {
            @Override
            public void onCancel()
            {
                
            }

            @Override
            public void onSuccess(PolicyPickerActivityResult policyPickerResult)
            {
                switch (policyPickerResult.mResultType)
                {
                    case Template:
                        if (policyPickerResult.mTemplateDescriptor == null)
                        {
                            
                        }
                        else
                        {
                            
                        }
                        break;
                   …
                }
            }
        };
        try
        {
            PolicyPickerActivity.show(POLICY_PICK_REQUEST, getActivity(), templateDescriptors,
                    originalTemplateDescriptor, policyPickerActivityCompletionCallback);
        }
        catch (InvalidParameterException e)
        {
        }
}
```
**Step 4: Create UserPolicy from TemplateDescriptor chosen in step 3 using MSIPC SDK v4 API**

```Java
mIAsyncControl = UserPolicy.create((TemplateDescriptor)selectedDescriptor, mEmailId, 
                                    mRmsAuthCallback,UserPolicyCreationFlags.NONE, null, 
                                    userPolicyCreationCallback);
```

**Step 5: Show chosen policy to user. Notice that you can allow editing of chosen user policy (assuming user has rights to do so).**
```Java
CompletionCallback<Integer> userPolicyViewerActivityCompletionCallback = new CompletionCallback<Integer>()
        {
            @Override
            public void onCancel()
            {
            }

            @Override
            public void onSuccess(Integer result)
            {
                switch (result)
                {
                    case UserPolicyViewerActivityResult.EDIT_POLICY:
                        startMsipcPolicyCreation(true);
                        break;
                }
            }
        };
        try
        {
            UserPolicy userPolicy = mUserPolicy;
            if (userPolicy != null)
            {
                UserPolicyViewerActivity.show(POLICY_VIEW_REQUEST, getActivity(), userPolicy, 
                   sSupportedRights, 
                   mUserPolicy.isIssuedToOwner() ? UserPolicyViewerActivityRequestOption.EDIT_ALLOWED
                                : UserPolicyViewerActivityRequestOption.NONE,
                   userPolicyViewerActivityCompletionCallback);
            }
        }
        catch (InvalidParameterException e)
        {
        }
```

**Step 6: Add following code to your application MainActivity to handle results from all UI Lib activities**
```Java
   /**
     * Recieve results from child activity.
     * 
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // handle ADAL results
        if (App.getInstance().getAuthenticationContext() != null)
        {
            App.getInstance().getAuthenticationContext().onActivityResult(requestCode, resultCode, data);
        }
        // handle MSIPC Results
        MsipcTaskFragment.handleMsipcUIActivityResult(requestCode, resultCode, data);
    }
    // Request codes for MSIPC UI Activities
    public static final int EMAIL_INPUT_REQUEST = 0x1;
    public static final int POLICY_VIEW_REQUEST = 0x3;
    public static final int POLICY_PICK_REQUEST = 0x2;
    /**
     * Handle msipc ui activity result.
     * 
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the data
     */
    public static void handleMsipcUIActivityResult(int requestCode, int resultCode, Intent data)
    {
        // handle MSIPC Results
        switch (requestCode)
        {
            case POLICY_PICK_REQUEST:
                PolicyPickerActivity.onActivityResult(resultCode, data);
                break;
            case POLICY_VIEW_REQUEST:
                UserPolicyViewerActivity.onActivityResult(resultCode, data);
                break;
            case EMAIL_INPUT_REQUEST:
                EmailActivity.onActivityResult(resultCode, data);
                break;
            default:
                // handle invalid request error
        }
    }
```    

## License

Copyright © Microsoft Corporation, All Rights Reserved

Licensed under MICROSOFT SOFTWARE LICENSE TERMS, 
MICROSOFT RIGHTS MANAGEMENT SERVICE SDK UI LIBRARIES;
You may not use this file except in compliance with the License.
See the license for specific language governing permissions and limitations.
You may obtain a copy of the license (RMS SDK UI libraries - EULA.DOCX) at the 
root directory of this project.

THIS CODE IS PROVIDED AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS
OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.



UI Library for AD RMS SDK v4 for Android 
======================

The UI Library for AD RMS SDK v4 for Android provides Android Activities that implement the required UI for the SDK functionality.  This library is optional and a developer may choose to build his own UI when using AD RMS SDK v4.
This library contains the following Android Activities:
* **EmailActivity**: Shows an email address input screen, which is required for operations like protection of files. RMS SDK expects to get the email address of the user who wants to protect data or files to redirect his organization sign-in portal.
* **PolicyPickerActivity**: Shows a policy picker screen, where the user can choose RMS template or specify the permissions to create a protection policy and encrypt files.
* **UserPolicyViewerActivity**: Shows the permissions that the user has for RMS protected data or file.


## Prerequisites

You must have downloaded and/or installed following software

* Git
* Android SDK 
* AVD image or device running (API level 15) or higher
* Microsoft Rights Management SDK  4.0 (RMS SDK). 
  You can download this from  http://go.microsoft.com/fwlink/?LinkId=404271
* Windows Azure Active Directory Authentication Library (ADAL) for Android . 
  For more information on ADAL please refer to the ADAL documentation here. https://github.com/MSOpenTech/azure-activedirectory-library-for-android
You may create a submodule of ADAL in your project.
Sample app as under samples\ folder uses 
samples\azure-activedirectory-library-for-android submodule.


## Usage

### Setting up development environment

1.	Setup uilib as a library project in eclipse.
2.	Go through usage guide of RMS SDK located here to familiarize yourself with basic usage of MSIPC SDK v4
3.	Setup your application project with MSIPC SDK v4. 
4.	Add a library reference of MSIPC SDK v4 project to uilib project. For help check here : http://developer.android.com/tools/projects/projects-cmdline.html#ReferencingLibraryProject
Note: Please do steps 3 & 4 for samples\MSIPCSampleApp (if trying to use MSIPCSampleApp).
5.	Add library reference of uilib project to your application project. Please check here : http://developer.android.com/tools/projects/projects-eclipse.html
6.	Add following Activities to your application's AndroidManifest.xml

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

7.	Set up your application project with ADAL by following these steps.
Note: Rename _libs\compatibility-v4.jar_ to _libs\android-support-v4.jar_ in ADAL project

### UI Activities

The following Android Activities are provided in this UI library for AD RMS SDK V4 for Android.
* **EmailActivity** – Shows a screen with email input which is required application. The returned email address can be used as emailId that is required for protection and consumption of RMS protected data or files.
* **PolicyPickerActivity** - Takes the list of RMS template descriptors to display them on the screen. Once user selects a template descriptor the app can create a protection policy to protect and encrypt data or files. 
* **UserPolicyViewerActivity** - Takes the user policy object and display it in a user friendly screen. The activity also provides an optional edit user policy button. The click action of edit button is returned as output to application.

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
              ……..
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

Notice: show method uses CompletionCallback as one of its parameters

```Java

public interface CreationCallback<T>
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

You can find a sample Android application in the repository, which demonstrates the usage of this library. It is located at samples\MsipcSampleApp. Simply compile and run it on an Android emulator or device.

###Sample Scenario

1) Get Templates using AD RMS SDK v4 and show them using Policy Picker Activity. 
2) Receive the selected Template object and create UserPolicy object.

Step 1 : Get Templates using AD RMS SDK v4

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
                if (originalUserPolicy != null && originalUserPolicy.getType() == UserPolicyType.TemplateBased)
                {
                    originalTemplateDescriptor = originalUserPolicy.getTemplateDescriptor();
                }
                continueMsipcPolicyCreationByPickingAPolicy(templateDescriptors, originalTemplateDescriptor,
                        showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
            }
        };
        try
        {
                     mIAsyncControl = TemplateDescriptor.getTemplates(emailId, mRmsAuthCallback, getTemplatesCreationCallback);
        }
        catch (com.microsoft.rightsmanagement.exceptions.InvalidParameterException e)
        {
         
        } 
```

Step 2 : Use PolicyPickerActivity to show these templates

Once the EmailActivity is complete onActivityResult is called to process the result of the activity.


```Java
private void continueMsipcPolicyCreationByPickingAPolicy(List<TemplateDescriptor> templateDescriptors, TemplateDescriptor originalTemplateDescriptor, final boolean showUserPolicyViewerOnPolicyCreation, final Runnable onPolicyCreationCallback)
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

Step 3 : Add following code to your application MainActivity to handle results from UI Lib activities

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

Follow similar usage pattern for EmailActivity and UserPolicyViewerActivity. See samples/MsipcSampleApp for detailed usage.

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



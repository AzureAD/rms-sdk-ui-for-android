RMS SDK UI Components for Android (RMS SDK UI)
======================

The RMS SDK UI Components for Android provides you with companion library of classes that are used to present UI elements for displaying protection policies associated with content to the user and for allowing the user to select protection policies to apply to content.
This library aims to get any RMS SDK based app up and running with minimum effort.

## Prerequisites

You must have downloaded and/or installed following software

	*	Git
	*	Maven 3.1.1+
	*	Android SDK (API level 15 or higher)
	*	Microsoft Rights Management SDK  4.0 (RMS SDK) 
			* You can download RMS SDK from http:\\url_to_obtain_sdk
	*	Windows Azure Active Directory Authentication Library (ADAL) for Android
			* This is submodule of this project. 
			  You can find this in samples\azure-activedirectory-library-for-android
			* for more information on ADAL please refer to the ADAL documentation 
			  [here | https://github.com/MSOpenTech/azure-activedirectory-library-for-android]

## Usage

### Setting up development environment

1. Follow Prerequisites.
2. Go through usage guide of RMS SDK located here (TODO: add link) to familiarize yourself with basic usage of SDK
3. Set up RMS SDK as a library project in eclipse.
4. To setup ADAL 
   1. Get required libraries using maven
   
		```
		cd samples\azure-activedirectory-library-for-android
		mvn clean install
		```
		This will get required libraries (jars) for ADAL in folder under samples\azure-activedirectory-library-for-android\adal\lib
   
   2. rename compatibility-v4.jar to android-support-v4
   
5. Set up ADAL as a library project in eclipse
6. Set up RMS SDK UI as a library project in eclipse. Add / Update refence to RMS SDK project setup in previous step. 
    For more help check here : http://developer.android.com/tools/projects/projects-cmdline.html#ReferencingLibraryProject
7. Add RMS SDK UI project as Android library to your project. Please check here: http://developer.android.com/tools/projects/projects-eclipse.html
8. Add project dependency for debugging in your project settings.

### UI Activities

UI Activities provided in this version of RMS SDK UI are. 

* *EmailActivity* -  To take email ID input from user and supply the same to dev code. (User email Id is required for Service Discovery)
* *PolicyPickerActivity* - Takes the list of template descriptors and original value of template descriptor to render them on the screen. 
						 Once user selects a template descriptor, the same is supplied back to dev code.
* *UserPolicyViewerActivity* - Takes User Policy instance and renders the data on the screen. 
							 Also provides an edit button to capture user’s intent to edit the policy.
							 The state of edit button (visible/invisible) is supplied by dev code. 
							 The click action of edit button is returned as output to dev code.
						
### API Surface

Each activity provides two static methods 

* *show* - to take input and start the activity
* *onActivityResult* - to process results returned to application's main activity by child activities.

EmailActivity's show and onActivityResult methods look like

```Java

public class EmailActivity extends FragmentActivity
{
    /**
     * Starts the Email activity. Internally, this method prepares the
     * intent to launch EmailActivity using startActivityForResult.
     * 
     * @param requestCode the request code for startActivityForResult
     * @param activity the parent activity that invokes startActivityForResult
     * @param emailActivityCompletionCallback callback that's invoked upon
     *            completion of activity.
     */
    public static void show(int requestCode,
            Activity activity,
            CreationCallback<String> emailActivityCompletionCallback)
    {
	
    }
    
    /**
     * Processes the result of EmailActivity started via
     * startActivityForResult by the parent activity, and invokes the callback
     * as supplied to EmailActivity.show.
     * This method must be called from parent Activity's onActivityResult.
     * 
     * @param resultCode the result code parameter as supplied to parent
     *            Activity's onActivityResult
     * @param data the data parameter as supplied to parent Activity's
     *            onActivityResult
     */
    public static void onActivityResult(int resultCode, Intent data)
    {
        
    }
}
```

Notice that in example above show method has CreationCallback as one of its parameters
This is similar concept as android RMS SDK and is expected to provide implementation of following methods

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

For other activities , please refer to javadoc here : (http:// url-to-javadoc)

## Sample Usage

Sample application included in the repository demonstrates the usage of this library.
It is located at samples\sampleapp

###Scenario : Create protected file from ptext

Step 1 : Email input from user
Call to Email Activity show method to display UI to take users email as input. 

```Java
try
{
	EmailActivity.show(EMAIL_INPUT_REQUEST, getActivity(), emailActivityCompletionCallback);
}
catch (InvalidParameterException e)
{
}
```

Step 2 : Process result from EmailActivity

Once the EmailActivity is complete onActivityResult is called to process the result of the activity.


```Java
public static void handleMsipcUIActivityResult(int requestCode, int resultCode, Intent data)
{
	....
	
	case EMAIL_INPUT_REQUEST:
		EmailActivity.onActivityResult(resultCode, data);
		break;
	....
}
```

Step 3 : Use RMS SDK to receive list of templates
```Java
	mIAsyncControl = TemplateDescriptor.getTemplates(emailId, mRmsAuthCallback, getTemplatesCreationCallback);
```

Step 4 : Display Policy picker UI to user 

Use list of templates obtained above to call PolicyPickerActivity.Show method to display templates.
Notice you can also pass in previously choosen template (originalTemplateDescriptor) for highlighting it.

```Java
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
       try
        {
            PolicyPickerActivity.show(POLICY_PICK_REQUEST, 
									  getActivity(), 
									  templateDescriptors,
									  originalTemplateDescriptor, 
									  policyPickerActivityCompletionCallback);
        }

```

PolicyPickerActivity returns PolicyPickerActivityResult ; which can help you decide if a template or custom policy was choosen

```Java

	switch (policyPickerResult.mResultType)
	{
		case Template:
			if (policyPickerResult.mTemplateDescriptor == null)
			{
				// no protection was choosen
			}
			else
			{
				// create UserPolicy with TemplateDescriptor
			}
			break;
		case Custom:
			if (policyPickerResult.mPolicyDescriptor == null)
			{
				// no protection was choosen
			}
			else
			{
			   // create UserPolicy with PolicyDescriptor
			}
			break;
	}

```

Step 5 : Create UserPolicy from TemplateDescriptor or PolicyDescriptor chosen in step 4

Below is sample code to create user policy from TemplateDescriptor

```Java

 mIAsyncControl = UserPolicy.create((TemplateDescriptor)selectedDescriptor, mEmailId, mRmsAuthCallback,
                        UserPolicyCreationFlags.NONE, null, userPolicyCreationCallback);

```

Step 6 : Show chosen policy to user.
Notice that you can allow edition of chosen user policy.

```Java

    /**
     * Show UI.
     * 
     * @param requestCode the request code for startActivityForResult
     * @param parentActivity the parent activity that invokes startActivityForResult
     * @param userPolicy user policy instance that provides data to display on the UI
     * @param policyViewerActivityRequestOption PolicyViewerActivityRequestOptions
     * @param policyViewerActivityCompletionCallback callback that's invoked upon completion of activity.
     * @throws InvalidParameterException the invalid parameter exception
     */
	UserPolicyViewerActivity.show(POLICY_VIEW_REQUEST, 
								  getActivity(), 
								  userPolicy, 
								  mUserPolicy.isIssuedToOwner() ? UserPolicyViewerActivityRequestOption.EDIT_ALLOWED: UserPolicyViewerActivityRequestOption.NONE, 
								  userPolicyViewerActivityCompletionCallback);

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


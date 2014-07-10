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

package com.microsoft.rightsmanagement.sampleapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;

import com.microsoft.rightsmanagement.AuthenticationRequestCallback;
import com.microsoft.rightsmanagement.CommonRights;
import com.microsoft.rightsmanagement.CreationCallback;
import com.microsoft.rightsmanagement.CustomProtectedInputStream;
import com.microsoft.rightsmanagement.CustomProtectedOutputStream;
import com.microsoft.rightsmanagement.EditableDocumentRights;
import com.microsoft.rightsmanagement.IAsyncControl;
import com.microsoft.rightsmanagement.PolicyAcquisitionFlags;
import com.microsoft.rightsmanagement.PolicyDescriptor;
import com.microsoft.rightsmanagement.ProtectedFileInputStream;
import com.microsoft.rightsmanagement.ProtectedFileOutputStream;
import com.microsoft.rightsmanagement.TemplateDescriptor;
import com.microsoft.rightsmanagement.UserPolicy;
import com.microsoft.rightsmanagement.UserPolicy.UserPolicyCreationFlags;
import com.microsoft.rightsmanagement.UserPolicyType;
import com.microsoft.rightsmanagement.UserRights;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;
import com.microsoft.rightsmanagement.exceptions.ProtectionException;
import com.microsoft.rightsmanagement.ui.CompletionCallback;
import com.microsoft.rightsmanagement.ui.EmailActivity;
import com.microsoft.rightsmanagement.ui.PolicyPickerActivity;
import com.microsoft.rightsmanagement.ui.PolicyPickerActivityResult;
import com.microsoft.rightsmanagement.ui.UserPolicyViewerActivity;
import com.microsoft.rightsmanagement.ui.UserPolicyViewerActivity.UserPolicyViewerActivityRequestOption;
import com.microsoft.rightsmanagement.ui.UserPolicyViewerActivity.UserPolicyViewerActivityResult;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * A persisting fragment holding async controls of MSIPC SDK.
 */
public class MsipcTaskFragment extends Fragment
{
    /**
     * The Enum Signal.
     */
    public enum Signal
    {
        ContentConsumed, ContentProtected, None;
    }

    /**
     * The Interface TaskEventCallback.
     */
    public interface TaskEventCallback
    {
        /**
         * On msipc task update.
         * 
         * @param taskStatus the task status
         */
        public void onMsipcTaskUpdate(TaskStatus taskStatus);
    }

    /**
     * Represents state of a task inside this fragment. An operation can contain sequence of multiple tasks.
     */
    public enum TaskState
    {
        NotStarted, Starting, Running, Completed, Cancelled, Faulted;
    }

    /**
     * The Class TaskStatus.
     */
    public class TaskStatus
    {
        private String mMessage;
        private boolean mPostToCaller;
        private Signal mSignal;
        private TaskState mTaskState;

        /**
         * Instantiates a new task status.
         * 
         * @param state the state
         * @param message the message
         * @param postToCaller the post to caller
         */
        public TaskStatus(TaskState state,
                          String message,
                          boolean postToCaller)
        {
            this(state, message, postToCaller, Signal.None);
        }

        /**
         * Instantiates a new task status.
         * 
         * @param state the state
         * @param message the message
         * @param postToCaller the post to caller
         * @param signal the signal
         */
        public TaskStatus(TaskState state,
                          String message,
                          boolean postToCaller,
                          Signal signal)
        {
            mTaskState = state;
            mMessage = message;
            mPostToCaller = postToCaller;
            mSignal = signal;
        }

        /**
         * Gets the message.
         * 
         * @return the message
         */
        public String getMessage()
        {
            return mMessage;
        }

        /**
         * Gets the signal.
         * 
         * @return the signal
         */
        public Signal getSignal()
        {
            return mSignal;
        }

        /**
         * Gets the task state.
         * 
         * @return the task state
         */
        public TaskState getTaskState()
        {
            return mTaskState;
        }
    }
    // Request codes for MSIPC UI Activities
    public static final int EMAIL_INPUT_REQUEST = 0x1;
    public static final int POLICY_VIEW_REQUEST = 0x3;
    public static final int POLICY_PICK_REQUEST = 0x2;
    public static final String TAG = "MsipcTaskFragment";
    protected TaskStatus mLatestUnpostedTaskStatus;
    protected Object mLockOnLatestUnpostedTaskStatus = new Object();
    private Context mApplicationContext;
    private String mDecryptedContent;
    private String mEmailId;
    private IAsyncControl mIAsyncControl;
    private String mProtectedContentFilePath;
    private AuthenticationRequestCallback mRmsAuthCallback;
    private TaskEventCallback mTaskEventCallback;
    private UserPolicy mUserPolicy;

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

    /**
     * cancels the current in-progress async task.
     */
    public void cancelTask()
    {
        if (mIAsyncControl != null)
        {
            mIAsyncControl.cancel();
        }
    }

    /**
     * Gets the decrypted content.
     * 
     * @return the decrypted content
     */
    public String getDecryptedContent()
    {
        return mDecryptedContent;
    }

    /**
     * Gets the latest unposted task status.
     * 
     * @return the latest unposted task status
     */
    public TaskStatus getLatestUnpostedTaskStatus()
    {
        TaskStatus latestUnpostedTaskStatus = null;
        synchronized (mLockOnLatestUnpostedTaskStatus)
        {
            latestUnpostedTaskStatus = mLatestUnpostedTaskStatus;
            mLatestUnpostedTaskStatus = null;
        }
        return latestUnpostedTaskStatus;
    }

    /**
     * Gets the protected content file path.
     * 
     * @return the protected content file path
     */
    public String getProtectedContentFilePath()
    {
        return mProtectedContentFilePath;
    }
    

    /**
     * Gets the current User Policy.
     * 
     * @return current User Policy
     */
    public UserPolicy getUserPolicy()
    {
        return mUserPolicy;
    }

    /**
     * Invalidate user policy.
     */
    public void invalidateUserPolicy()
    {
        mUserPolicy = null;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mTaskEventCallback = (TaskEventCallback)activity;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mApplicationContext = getActivity().getApplicationContext();
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        updateTaskStatus(new TaskStatus(TaskState.NotStarted, null, false));
        try
        {
            mRmsAuthCallback = App.getInstance().getRmsAuthenticationCallback(getActivity());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            Logger.ie(TAG, e.getMessage());
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
        catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
            Logger.ie(TAG, e.getMessage());
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            Logger.ie(TAG, e.getMessage());
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onDetach()
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
        mTaskEventCallback = null;
    }

    /**
     * Shows MSIPC user policy on the screen using MSIPC UI library.
     */
    public void showUserPolicy()
    {
        CompletionCallback<Integer> userPolicyViewerActivityCompletionCallback = new CompletionCallback<Integer>()
        {
            @Override
            public void onCancel()
            {
                // Do nothing
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
                UserPolicyViewerActivity.show(POLICY_VIEW_REQUEST, getActivity(), userPolicy, mUserPolicy
                        .isIssuedToOwner() ? UserPolicyViewerActivityRequestOption.EDIT_ALLOWED
                        : UserPolicyViewerActivityRequestOption.NONE, userPolicyViewerActivityCompletionCallback);
            }
        }
        catch (InvalidParameterException e)
        {
            Logger.e(TAG, "Invalid Parameter", "", e);
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////MSIPC OPERATIONS////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Start content consumption and read protected content from ptxt file format
     * 
     * @param inputStream the input stream
     */
    public void startContentConsumptionFromPtxtFileFormat(InputStream inputStream)
    {
        CreationCallback<ProtectedFileInputStream> protectedFileInputStreamCreationCallback = new CreationCallback<ProtectedFileInputStream>()
        {
            @Override
            public Context getContext()
            {
                return mApplicationContext;
            }

            @Override
            public void onCancel()
            {
                updateTaskStatus(new TaskStatus(TaskState.Cancelled, "ProtectedFileInputStream creation was cancelled",
                        true));
            }

            @Override
            public void onFailure(ProtectionException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }

            @Override
            public void onSuccess(ProtectedFileInputStream protectedFileInputStream)
            {
                mUserPolicy = protectedFileInputStream.getUserPolicy();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] dataChunk = new byte[16384];
                try
                {
                    while ((nRead = protectedFileInputStream.read(dataChunk, 0, dataChunk.length)) != -1)
                    {
                        buffer.write(dataChunk, 0, nRead);
                    }
                    buffer.flush();
                    mDecryptedContent = new String(buffer.toByteArray(), Charset.forName("UTF-8"));
                    buffer.close();
                    protectedFileInputStream.close();
                }
                catch (IOException e)
                {
                    updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                    return;
                }
                updateTaskStatus(new TaskStatus(TaskState.Completed, "Content was consumed", true,
                        Signal.ContentConsumed));
            }
        };
        try
        {
            updateTaskStatus(new TaskStatus(TaskState.Starting, "Consuming content", true));
            ProtectedFileInputStream.create(inputStream, null, mRmsAuthCallback, PolicyAcquisitionFlags.NONE,
                    protectedFileInputStreamCreationCallback);
        }
        catch (com.microsoft.rightsmanagement.exceptions.InvalidParameterException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /**
     * Start content consumption and read protected content from my own text file format 
     * 
     * My own File format:
     * serializedContentPolicy Length | serializedContentPolicy | Encrypted Content length | Encrypted Content
     * 
     * @param inputStream the input stream
     */
    public void startContentConsumptionFromMyOwnProtectedTextFileFormat(final InputStream inputStream)
    {
        // This is a 2 step process
        // 1. Create a UserPolicy from serializedContentPolicy
        // 2. Create a CustomProtectedInputStream using UserPolicy and read content from it.
        CreationCallback<UserPolicy> userPolicyCreationCallbackFromSerializedContentPolicy = new CreationCallback<UserPolicy>()
        {
            @Override
            public void onSuccess(UserPolicy userPolicy)
            {
                CreationCallback<CustomProtectedInputStream> customProtectedInputStreamCreationCallback = new CreationCallback<CustomProtectedInputStream>()
                {
                    @Override
                    public Context getContext()
                    {
                        return mApplicationContext;
                    }

                    @Override
                    public void onCancel()
                    {
                        updateTaskStatus(new TaskStatus(TaskState.Cancelled,
                                "CustomProtectedInputStream creation was cancelled", true));
                    }

                    @Override
                    public void onFailure(ProtectionException e)
                    {
                        updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                    }

                    @Override
                    public void onSuccess(CustomProtectedInputStream customProtectedInputStream)
                    {
                        mUserPolicy = customProtectedInputStream.getUserPolicy();
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        int nRead;
                        byte[] dataChunk = new byte[16384];
                        try
                        {
                            while ((nRead = customProtectedInputStream.read(dataChunk, 0, dataChunk.length)) != -1)
                            {
                                buffer.write(dataChunk, 0, nRead);
                            }
                            buffer.flush();
                            mDecryptedContent = new String(buffer.toByteArray(), Charset.forName("UTF-8"));
                            buffer.close();
                            customProtectedInputStream.close();
                        }
                        catch (IOException e)
                        {
                            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                            return;
                        }
                        updateTaskStatus(new TaskStatus(TaskState.Completed, "Content was consumed", true,
                                Signal.ContentConsumed));
                    }
                };
                try
                {
                    // Step 2: Create a CustomProtectedInputStream using UserPolicy and read content from it.
                    // Retrieve the encrypted content size.
                    long encryptedContentLength = readUnsignedInt(inputStream);
                    updateTaskStatus(new TaskStatus(TaskState.Starting, "Consuming content", true));
                    CustomProtectedInputStream.create(userPolicy, inputStream, encryptedContentLength,
                            customProtectedInputStreamCreationCallback);
                }
                catch (com.microsoft.rightsmanagement.exceptions.InvalidParameterException e)
                {
                    updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                }
                catch (IOException e)
                {
                    updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                }
            }

            @Override
            public void onFailure(ProtectionException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }

            @Override
            public void onCancel()
            {
                updateTaskStatus(new TaskStatus(TaskState.Cancelled, "User policy aquisition was cancelled", true));
            }

            @Override
            public Context getContext()
            {
                return mApplicationContext;
            }
        };
        try
        {
            // Step 1: Create a UserPolicy from serializedContentPolicy
            // Read the serializedContentPolicyLength from the inputStream.
            long serializedContentPolicyLength = readUnsignedInt(inputStream);
            // Read the PL bytes from the input stream using the PL size.
            byte[] serializedContentPolicy = new byte[(int)serializedContentPolicyLength];
            inputStream.read(serializedContentPolicy);
            updateTaskStatus(new TaskStatus(TaskState.Starting,
                    "Acquring user policy to consume content from my own file format", true));
            UserPolicy.acquire(serializedContentPolicy, null, mRmsAuthCallback, PolicyAcquisitionFlags.NONE,
                    userPolicyCreationCallbackFromSerializedContentPolicy);
        }
        catch (com.microsoft.rightsmanagement.exceptions.InvalidParameterException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
        catch (IOException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /**
     * Start content protection and serialize protected content to ptxt file format
     * 
     * @param contentToProtect the content to protect
     */
    public void startContentProtectionToPtxtFileFormat(final byte[] contentToProtect)
    {
        if (mUserPolicy == null)
        {
            Runnable onPolicyCreationCallback = new Runnable()
            {
                @Override
                public void run()
                {
                    createPTxt(contentToProtect);
                }
            };
            startMsipcPolicyCreationByTakingEmailId(false, onPolicyCreationCallback);
        }
        else
        {
            createPTxt(contentToProtect);
        }
    }

    /**
     * Start content protection and serialize protected content to my own file format.
     * 
     * @param contentToProtect the content to protect
     */
    public void startContentProtectionToMyOwnProtectedTextFileFormat(final byte[] contentToProtect)
    {
        if (mUserPolicy == null)
        {
            Runnable onPolicyCreationCallback = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        createMyOwnFormatFileForProtectedText(contentToProtect);
                    }
                    catch (FileNotFoundException e)
                    {
                        updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                    }
                }
            };
            startMsipcPolicyCreationByTakingEmailId(false, onPolicyCreationCallback);
        }
        else
        {
            try
            {
                createMyOwnFormatFileForProtectedText(contentToProtect);
            }
            catch (FileNotFoundException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }
        }
    }

    /**
     * MSIPC Create Policy as part of protection Operation.
     * 
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     */
    public void startMsipcPolicyCreation(final boolean showUserPolicyViewerOnPolicyCreation)
    {
        startMsipcPolicyCreationByTakingEmailId(showUserPolicyViewerOnPolicyCreation, null);
    }

    /**
     * # 1 Take user email
     * 
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
    private void startMsipcPolicyCreationByTakingEmailId(final boolean showUserPolicyViewerOnPolicyCreation,
                                                       final Runnable onPolicyCreationCallback)
    {
        final UserPolicy originalUserPolicy = mUserPolicy;
        CompletionCallback<String> emailActivityCompletionCallback = new CompletionCallback<String>()
        {
            @Override
            public void onCancel()
            {
                updateTaskStatus(new TaskStatus(TaskState.Cancelled, "Email Activity was cancelled", false));
            }

            @Override
            public void onSuccess(String item)
            {
                updateTaskStatus(new TaskStatus(TaskState.Completed, "Email id was recieved", false));
                continueMsipcPolicyCreationWithEmailId(item, originalUserPolicy, showUserPolicyViewerOnPolicyCreation,
                        onPolicyCreationCallback);
            }
        };
        if (mEmailId == null || mEmailId.isEmpty())
        {
            updateTaskStatus(new TaskStatus(TaskState.Starting, "Getting user's email id", false));
            try
            {
                EmailActivity.show(EMAIL_INPUT_REQUEST, getActivity(), emailActivityCompletionCallback);
            }
            catch (InvalidParameterException e)
            {
                Logger.e(TAG, "Invalid Parameter", "", e);
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }
        }
        else
        {
            continueMsipcPolicyCreationWithEmailId(mEmailId, originalUserPolicy, showUserPolicyViewerOnPolicyCreation,
                    onPolicyCreationCallback);
        }
    }

    /**
     * # 2.
     * 
     * @param emailId the email id
     * @param originalUserPolicy the original user policy
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
    private void continueMsipcPolicyCreationWithEmailId(final String emailId,
                                                        final UserPolicy originalUserPolicy,
                                                        boolean showUserPolicyViewerOnPolicyCreation,
                                                        final Runnable onPolicyCreationCallback)
    {
        if (originalUserPolicy == null
                || (originalUserPolicy != null && originalUserPolicy.getType() == UserPolicyType.TemplateBased))
        {
            // continue Template based Msipc Policy Creation
            continueMsipcPolicyCreationWithGettingTemplates(emailId, originalUserPolicy,
                    showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
        }
        else
        {
            // continue Customized Msipc Policy Creation
            
            // Note: Below code is a sample for Custom Policy Creation.
            // It's not invoked by this application.
            // A UserPolicy can be created either using templates or in a Customized way.
            // This code path creates a UserPolicy in a customized way.
            // Ideally following objects - UserRights, PolicyDescriptor would be created by using user inputs from a UI
            
            // create userRights list
            UserRights userRights = new UserRights(Arrays.asList("consumer@domain.com"), Arrays.asList(
                    CommonRights.View, EditableDocumentRights.Print));
            ArrayList<UserRights> usersRigthsList = new ArrayList<UserRights>();
            usersRigthsList.add(userRights);
            // Create PolicyDescriptor using userRights list
            PolicyDescriptor policyDescriptor = PolicyDescriptor.createPolicyDescriptorFromUserRights(usersRigthsList);
            policyDescriptor.setOfflineCacheLifetimeInDays(10);
            policyDescriptor.setContentValidUntil(new Date());
            // Jump directly to #6 now.
            continueMsipcPolicyCreationByCreatingUserPolicy(policyDescriptor, showUserPolicyViewerOnPolicyCreation,
                    onPolicyCreationCallback);
        }
    }

    /**
     * # 3
     * 
     * @param emailId the email id
     * @param originalUserPolicy original User policy if re-publishing, else null
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
    private void continueMsipcPolicyCreationWithGettingTemplates(final String emailId,
                                                                 final UserPolicy originalUserPolicy,
                                                                 final boolean showUserPolicyViewerOnPolicyCreation,
                                                                 final Runnable onPolicyCreationCallback)
    {
        CreationCallback<List<TemplateDescriptor>> getTemplatesCreationCallback = new CreationCallback<List<TemplateDescriptor>>()
        {
            @Override
            public Context getContext()
            {
                return mApplicationContext;
            }

            @Override
            public void onCancel()
            {
                updateTaskStatus(new TaskStatus(TaskState.Cancelled, "Get Templates was cancelled", true));
            }

            @Override
            public void onFailure(ProtectionException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }

            @Override
            public void onSuccess(List<TemplateDescriptor> templateDescriptors)
            {
                mEmailId = emailId;// store email id after a successful msipc operation
                updateTaskStatus(new TaskStatus(TaskState.Completed, "Templates were recieved", true));
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
            updateTaskStatus(new TaskStatus(TaskState.Starting, "Getting Templates", true));
            mIAsyncControl = TemplateDescriptor.getTemplates(emailId, mRmsAuthCallback, getTemplatesCreationCallback);
        }
        catch (com.microsoft.rightsmanagement.exceptions.InvalidParameterException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /**
     * # 4.
     * 
     * @param templateDescriptors the template descriptors
     * @param originalTemplateDescriptor the original template descriptor
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
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
                updateTaskStatus(new TaskStatus(TaskState.Cancelled,
                        "Template Descriptor Picker Activity was cancelled", false));
            }

            @Override
            public void onSuccess(PolicyPickerActivityResult policyPickerResult)
            {
                switch (policyPickerResult.mResultType)
                {
                    case Template:
                        if (policyPickerResult.mTemplateDescriptor == null)
                        {
                            updateTaskStatus(new TaskStatus(TaskState.Completed, "No protection was chosen", false));
                            mUserPolicy = null;
                        }
                        else
                        {
                            updateTaskStatus(new TaskStatus(TaskState.Completed, "A template was chosen", false));
                            continueMsipcPolicyCreationByCreatingUserPolicy(policyPickerResult.mTemplateDescriptor,
                                    showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
                        }
                        break;
                    case Custom:
                        if (policyPickerResult.mPolicyDescriptor == null)
                        {
                            updateTaskStatus(new TaskStatus(TaskState.Completed, "No protection was chosen", false));
                            mUserPolicy = null;
                        }
                        else
                        {
                            updateTaskStatus(new TaskStatus(TaskState.Completed, "Custom Permission was chosen", false));
                            continueMsipcPolicyCreationByCreatingUserPolicy(policyPickerResult.mPolicyDescriptor,
                                    showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
                        }
                        break;
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
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /**
     * # 5.
     * 
     * @param selectedDescriptor the selected descriptor
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
    private void continueMsipcPolicyCreationByCreatingUserPolicy(Object selectedDescriptor,
                                                                 final boolean showUserPolicyViewerOnPolicyCreation,
                                                                 final Runnable onPolicyCreationCallback)
    {
        CreationCallback<UserPolicy> userPolicyCreationCallback = new CreationCallback<UserPolicy>()
        {
            @Override
            public Context getContext()
            {
                return mApplicationContext;
            }

            @Override
            public void onCancel()
            {
                updateTaskStatus(new TaskStatus(TaskState.Cancelled, "User Policy creation was cancelled", true));
            }

            @Override
            public void onFailure(ProtectionException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }

            @Override
            public void onSuccess(final UserPolicy item)
            {
                updateTaskStatus(new TaskStatus(TaskState.Completed, "User Policy Created", true));
                // # 6
                mUserPolicy = item;
                // # 7
                if (showUserPolicyViewerOnPolicyCreation)
                {
                    showUserPolicy();
                }
                if (onPolicyCreationCallback != null)
                {
                    onPolicyCreationCallback.run();
                }
            }
        };
        try
        {
            if (selectedDescriptor.getClass().equals(TemplateDescriptor.class))
            {
                updateTaskStatus(new TaskStatus(TaskState.Starting, "Creating template based user policy", true));
                mIAsyncControl = UserPolicy.create((TemplateDescriptor)selectedDescriptor, mEmailId, mRmsAuthCallback,
                        UserPolicyCreationFlags.NONE, null, userPolicyCreationCallback);
            }
            else if (selectedDescriptor.getClass().equals(PolicyDescriptor.class))
            {
                updateTaskStatus(new TaskStatus(TaskState.Starting, "Creating custom user policy", true));
                mIAsyncControl = UserPolicy.create((PolicyDescriptor)selectedDescriptor, mEmailId, mRmsAuthCallback,
                        UserPolicyCreationFlags.NONE, userPolicyCreationCallback);
            }
            else
            {
                Logger.ie(TAG, "invalid selectedDescriptor");
            }
        }
        catch (InvalidParameterException e)
        {
            Logger.e(TAG, "Invalid Parameter", "", e);
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////END OF MSIPC OPERATIONS/////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Creates the p txt.
     * 
     * @param contentToProtect the content to protect
     */
    private void createPTxt(final byte[] contentToProtect)
    {
        OutputStream outputStream = null;
        String originalFileExtension = App.PLAIN_TEXT_FILE_SUFFIX;
        String originalFileName = "sample" + originalFileExtension;
        final String filePath = App.getInstance().getStorageDirectory() + "/"
                + originalFileName.substring(0, originalFileName.lastIndexOf('.')) + ".ptxt";
        CreationCallback<ProtectedFileOutputStream> protectedFileOutputStreamCreationCallback = new CreationCallback<ProtectedFileOutputStream>()
        {
            @Override
            public Context getContext()
            {
                return mApplicationContext;
            }

            @Override
            public void onCancel()
            {
                invalidateUserPolicy();
                updateTaskStatus(new TaskStatus(TaskState.Cancelled,
                        "ProtectedFileOutputStream creation was cancelled", true));
            }

            @Override
            public void onFailure(ProtectionException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }

            @Override
            public void onSuccess(ProtectedFileOutputStream item)
            {
                try
                {
                    // write to this stream
                    item.write(contentToProtect);
                    item.flush();
                    item.close();
                    mProtectedContentFilePath = filePath;
                    updateTaskStatus(new TaskStatus(TaskState.Completed, "Content protected", true,
                            Signal.ContentProtected));
                }
                catch (IOException e)
                {
                    updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                }
            }
        };
        try
        {
            File file = new File(filePath);
            outputStream = new FileOutputStream(file);
            mIAsyncControl = ProtectedFileOutputStream.create(outputStream, mUserPolicy, originalFileExtension,
                    protectedFileOutputStreamCreationCallback);
        }
        catch (FileNotFoundException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
        catch (InvalidParameterException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /**
     * Serialized protected content to a txt2 file format. My own File format: serializedContentPolicy Length |
     * serializedContentPolicy | Encrypted Content length | Encrypted Content
     * 
     * @param contentToProtect the content to protect
     * @throws FileNotFoundException
     */
    private void createMyOwnFormatFileForProtectedText(final byte[] contentToProtect) throws FileNotFoundException
    {
        final String filePath = App.getInstance().getStorageDirectory() + "/" + "sample.txt2";
        File file = new File(filePath);
        final OutputStream outputStream = new FileOutputStream(file);
        CreationCallback<CustomProtectedOutputStream> customProtectedOutputStreamCreationCallback = new CreationCallback<CustomProtectedOutputStream>()
        {
            @Override
            public Context getContext()
            {
                return mApplicationContext;
            }

            @Override
            public void onCancel()
            {
                invalidateUserPolicy();
                updateTaskStatus(new TaskStatus(TaskState.Cancelled,
                        "CustomProtectedOutputStream creation was cancelled", true));
            }

            @Override
            public void onFailure(ProtectionException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }

            @Override
            public void onSuccess(CustomProtectedOutputStream protectedOutputStream)
            {
                try
                {
                    // write serializedContentPolicy
                    byte[] serializedContentPolicy = mUserPolicy.getSerializedContentPolicy();
                    writeLongAsUnsignedIntToStream(outputStream, serializedContentPolicy.length);
                    outputStream.write(serializedContentPolicy);
                    // write encrypted content
                    if (contentToProtect != null)
                    {
                        writeLongAsUnsignedIntToStream(outputStream,
                                CustomProtectedOutputStream.getEncryptedContentLength(contentToProtect.length,
                                        protectedOutputStream.getUserPolicy()));
                        protectedOutputStream.write(contentToProtect);
                        protectedOutputStream.flush();
                        protectedOutputStream.close();
                    }
                    else
                    {
                        outputStream.flush();
                        outputStream.close();
                    }
                    mProtectedContentFilePath = filePath;
                    updateTaskStatus(new TaskStatus(TaskState.Completed, "Content protected into my own file format",
                            true, Signal.ContentProtected));
                }
                catch (IOException e)
                {
                    updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                }
            }
        };
        try
        {
            mIAsyncControl = CustomProtectedOutputStream.create(outputStream, mUserPolicy,
                    customProtectedOutputStreamCreationCallback);
        }
        catch (InvalidParameterException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /**
     * Update task status.
     * 
     * @param taskStatus the task status
     */
    private void updateTaskStatus(final TaskStatus taskStatus)
    {
        if (taskStatus.mPostToCaller)
        {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mTaskEventCallback != null)
                    {
                        mLatestUnpostedTaskStatus = null;
                        mTaskEventCallback.onMsipcTaskUpdate(taskStatus);
                    }
                    else
                    {
                        mLatestUnpostedTaskStatus = taskStatus;
                    }
                }
            });
        }
    }

    /**
     * Read an unsigned Integer from an input stream. This method is used for the IRM file structure.
     * 
     * @param input the Input stream.
     * @return the unsigned integer as a signed long.
     * @throws IOException signals that a general IOException has occurred.
     */
    private static long readUnsignedInt(InputStream input) throws IOException
    {
        byte[] fourBytes = new byte[4];
        if (input.read(fourBytes) != fourBytes.length)
        {
            throw new IOException("Failed to read " + fourBytes.length + " Bytes of data");
        }
        return toLongChangeOrder(fourBytes);
    }

    /**
     * Write long as unsigned Integer to output stream.
     * 
     * @param outputStream The output stream to write the value.
     * @param value The value being written.
     * @throws IOException signals that an I/O exception has occurred.
     */
    private static void writeLongAsUnsignedIntToStream(OutputStream outputStream, long value) throws IOException
    {
        outputStream.write((byte)(value & 0x00000000000000FF));
        outputStream.write((byte)((value & 0x000000000000FF00) >>> 8));
        outputStream.write((byte)((value & 0x0000000000FF0000) >>> 16));
        outputStream.write((byte)((value & 0x00000000FF000000) >>> 24));
    }

    /**
     * Change byte order and convert to long.
     * 
     * @param uintArray the UINT array
     * @return the Integer.
     * @throws IOException signals that a general IOException has occurred.
     */
    private static long toLongChangeOrder(byte[] uintArray) throws IOException
    {
        if (uintArray.length != 4)
        {
            throw new IOException("Byte array cannot be converted to UINT");
        }
        // The ONE_BYTE_MASK is used to prevent errors that are due to byte being singed and << being a signed operator.
        long res = (0x00000000000000FFL & uintArray[0]);
        res |= ((0x00000000000000FFL & uintArray[1]) << 8);
        res |= ((0x00000000000000FFL & uintArray[2]) << 16);
        res |= ((0x00000000000000FFL & uintArray[3]) << 24);
        return res;
    }
}

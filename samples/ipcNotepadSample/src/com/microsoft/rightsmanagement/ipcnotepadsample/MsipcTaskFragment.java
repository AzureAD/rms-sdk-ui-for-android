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

package com.microsoft.rightsmanagement.ipcnotepadsample;

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
import java.util.List;

import com.microsoft.rightsmanagement.CreationCallback;
import com.microsoft.rightsmanagement.IAsyncControl;
import com.microsoft.rightsmanagement.AuthenticationRequestCallback;
import com.microsoft.rightsmanagement.PolicyDescriptor;
import com.microsoft.rightsmanagement.ProtectedFileInputStream;
import com.microsoft.rightsmanagement.ProtectedFileOutputStream;
import com.microsoft.rightsmanagement.ProtectionOperationFlags;
import com.microsoft.rightsmanagement.TemplateDescriptor;
import com.microsoft.rightsmanagement.UserPolicy;
import com.microsoft.rightsmanagement.UserPolicyType;
import com.microsoft.rightsmanagement.UserPolicy.UserPolicyCreationFlags;
import com.microsoft.rightsmanagement.exceptions.ProtectionException;
import com.microsoft.rightsmanagement.ui.CompletionCallback;
import com.microsoft.rightsmanagement.ui.EmailActivity;
import com.microsoft.rightsmanagement.ui.TemplateDescriptorPickerActivity;
import com.microsoft.rightsmanagement.ui.TemplateDescriptorPickerActivityResult;
import com.microsoft.rightsmanagement.ui.UserPolicyViewerActivity;
import com.microsoft.rightsmanagement.ui.UserPolicyViewerActivity.UserPolicyViewerActivityRequestOption;
import com.microsoft.rightsmanagement.ui.UserPolicyViewerActivity.UserPolicyViewerActivityResult;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;

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
    public static final int TEMPLATE_DESCRIPTOR_PICK_REQUEST = 0x2;
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
            case TEMPLATE_DESCRIPTOR_PICK_REQUEST:
                TemplateDescriptorPickerActivity.onActivityResult(resultCode, data);
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
     * Invalidate user policy.
     */
    public void invalidateUserPolicy()
    {
        mUserPolicy = null;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mTaskEventCallback = (TaskEventCallback)activity;
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
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
                        startMsipcCreatePolicy(true);
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
            Logger.e(TAG, "Invalid Parameter","", e);
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////MSIPC OPERATIONS////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Start content consumption.
     * 
     * @param inputStream the input stream
     */
    public void startContentConsumption(InputStream inputStream)
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
            public void onSuccess(ProtectedFileInputStream item)
            {
                mUserPolicy = item.getPolicy();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                try
                {
                    while ((nRead = item.read(data, 0, data.length)) != -1)
                    {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    mDecryptedContent = new String(buffer.toByteArray(), Charset.forName("UTF-8"));
                    buffer.close();
                    item.close();
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
            ProtectedFileInputStream.create(inputStream, null, mRmsAuthCallback, ProtectionOperationFlags.NONE,
                    protectedFileInputStreamCreationCallback);
        }
        catch (com.microsoft.rightsmanagement.exceptions.InvalidParameterException e)
        {
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
        }
    }

    /**
     * Start content protection.
     * 
     * @param contentToProtect the content to protect
     */
    public void startContentProtection(final byte[] contentToProtect)
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
            startMsipcCreatePolicy(false, onPolicyCreationCallback);
        }
        else
        {
            createPTxt(contentToProtect);
        }
    }

    /**
     * # 1 MSIPC Protection Operation.
     * 
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     */
    public void startMsipcCreatePolicy(final boolean showUserPolicyViewerOnPolicyCreation)
    {
        startMsipcCreatePolicy(showUserPolicyViewerOnPolicyCreation, null);
    }

    /**
     * # 5.
     * 
     * @param selectedDescriptor the selected descriptor
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
    private void continueMsipcProtectionByCreatingUserPolicy(Object selectedDescriptor,
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
                // dev handles this
            }

            @Override
            public void onFailure(ProtectionException e)
            {
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
                // dev handles this
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
                        UserPolicyCreationFlags.NONE, userPolicyCreationCallback);
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
            Logger.e(TAG, "Invalid Parameter","", e);
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
    private void continueMsipcProtectionBySelectingATemplating(List<TemplateDescriptor> templateDescriptors,
                                                               TemplateDescriptor originalTemplateDescriptor,
                                                               final boolean showUserPolicyViewerOnPolicyCreation,
                                                               final Runnable onPolicyCreationCallback)
    {
        CompletionCallback<TemplateDescriptorPickerActivityResult> templateDescriptorPickerActivityCompletionCallback = new CompletionCallback<TemplateDescriptorPickerActivityResult>()
        {
            @Override
            public void onCancel()
            {
                updateTaskStatus(new TaskStatus(TaskState.Cancelled,
                        "Template Descriptor Picker Activity was cancelled", false));
            }

            @Override
            public void onSuccess(TemplateDescriptorPickerActivityResult templateResult)
            {
                switch (templateResult.mResultType)
                {
                    case Default:
                        if (templateResult.mTemplateDescriptor == null)
                        {
                            updateTaskStatus(new TaskStatus(TaskState.Completed, "No protection was chosen", false));
                            mUserPolicy = null;
                        }
                        else
                        {
                            updateTaskStatus(new TaskStatus(TaskState.Completed, "A template was chosen", false));
                            continueMsipcProtectionByCreatingUserPolicy(templateResult.mTemplateDescriptor,
                                    showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
                        }
                        break;
                    case ShowPolicyDescriptorPicker:
                        updateTaskStatus(new TaskStatus(TaskState.Completed, "Custom Permissions was chosen", false));
                        // TODO show policy Descriptor picker
                        break;
                }
            }
        };
        try
        {
            TemplateDescriptorPickerActivity.show(TEMPLATE_DESCRIPTOR_PICK_REQUEST, getActivity(), templateDescriptors,
                    originalTemplateDescriptor, templateDescriptorPickerActivityCompletionCallback);
        }
        catch (InvalidParameterException e)
        {
            //e.printStackTrace();
            updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
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
    private void continueMsipcProtectionWithEmailId(final String emailId,
                                                    final UserPolicy originalUserPolicy,
                                                    boolean showUserPolicyViewerOnPolicyCreation,
                                                    final Runnable onPolicyCreationCallback)
    {
        if (originalUserPolicy == null
                || (originalUserPolicy != null && originalUserPolicy.getType() == UserPolicyType.TemplateBased))
        {
            continueMsipcProtectionWithGettingTemplates(emailId, originalUserPolicy,
                    showUserPolicyViewerOnPolicyCreation, onPolicyCreationCallback);
        }
        else
        {
            // TODO publish using custom policy
        }
    }

    /**
     * # 3 Uses MSIPC APIs to publish a content using templates.
     * 
     * @param emailId the email id
     * @param originalUserPolicy original User policy if re-publishing, else null
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
    private void continueMsipcProtectionWithGettingTemplates(final String emailId,
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
                continueMsipcProtectionBySelectingATemplating(templateDescriptors, originalTemplateDescriptor,
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
     * Creates the p txt.
     * 
     * @param contentToProtect the content to protect
     */
    private void createPTxt(final byte[] contentToProtect)
    {
        OutputStream outputStream = null;
        String originalFileExtension = App.PLAIN_TEXT_FILE_SUFFIX_PFILE;
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
     * Start msipc create policy.
     * 
     * @param showUserPolicyViewerOnPolicyCreation the show user policy viewer on policy creation
     * @param onPolicyCreationCallback the on policy creation callback
     */
    private void startMsipcCreatePolicy(final boolean showUserPolicyViewerOnPolicyCreation,
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
                continueMsipcProtectionWithEmailId(item, originalUserPolicy, showUserPolicyViewerOnPolicyCreation,
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
                Logger.e(TAG, "Invalid Parameter","", e);
                updateTaskStatus(new TaskStatus(TaskState.Faulted, e.getLocalizedMessage(), true));
            }
        }
        else
        {
            continueMsipcProtectionWithEmailId(mEmailId, originalUserPolicy, showUserPolicyViewerOnPolicyCreation,
                    onPolicyCreationCallback);
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
}

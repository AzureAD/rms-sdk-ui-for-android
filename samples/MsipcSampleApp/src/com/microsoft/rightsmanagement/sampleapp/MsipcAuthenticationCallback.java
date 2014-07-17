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

package com.microsoft.rightsmanagement.sampleapp;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.os.Build;

import com.microsoft.adal.AuthenticationCallback;
import com.microsoft.adal.AuthenticationCancelError;
import com.microsoft.adal.AuthenticationContext;
import com.microsoft.adal.AuthenticationResult;
import com.microsoft.adal.AuthenticationSettings;
import com.microsoft.adal.PromptBehavior;
import com.microsoft.rightsmanagement.AuthenticationCompletionCallback;
import com.microsoft.rightsmanagement.AuthenticationRequestCallback;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * The Class implements MSIPC AuthenticationRequestCallback using ADAL.
 */
class MsipcAuthenticationCallback implements AuthenticationRequestCallback
{
    public static final String TAG = "RmsAuthenticationCallback";
    private String mClientId;
    private Activity mParentActivity;;
    private PromptBehavior mPromptBehavior = PromptBehavior.Auto;
    private String mRedirectURI;

    /**
     * Instantiates a new rms authentication callback.
     * 
     * @param parentActivity the parent activity
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeySpecException the invalid key spec exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public MsipcAuthenticationCallback(Activity parentActivity) throws NoSuchAlgorithmException, InvalidKeySpecException,
            UnsupportedEncodingException
    {
        mParentActivity = parentActivity;
        setADALKeyStore();
        
        //Note: Following values of are client_id and redirect_uri are for demo purpose only.
        mClientId = "com.microsoft.rightsmanagement.sampleapp";
        mRedirectURI = mClientId + "://authorize";
    }

    /*
     * (non-Javadoc)
     * @see com.microsoft.rightsmanagement.AuthenticationRequestCallback#getToken(java.util.Map,
     * com.microsoft.rightsmanagement.AuthenticationCompletionCallback)
     */
    @Override
    public void getToken(Map<String, String> authenticationParametersMap,
                         final AuthenticationCompletionCallback authenticationCompletionCallbackToMsipc)
    {
        String authority = authenticationParametersMap.get("oauth2.authority");
        String resource = authenticationParametersMap.get("oauth2.resource");
        String userId = authenticationParametersMap.get("userId");
        final String userHint = (userId == null)? "" : userId; 
        AuthenticationContext authenticationContext = App.getInstance().getAuthenticationContext();
        if (authenticationContext == null || !authenticationContext.getAuthority().equalsIgnoreCase(authority))
        {
            try
            {
                authenticationContext = new AuthenticationContext(App.getInstance().getApplicationContext(), authority,
                        false);
                App.getInstance().setAuthenticationContext(authenticationContext);
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
                Logger.ie(TAG, String.format("Error in getting token. %s", e.getMessage()));
                authenticationCompletionCallbackToMsipc.onFailure();
            }
            catch (NoSuchPaddingException e)
            {
                e.printStackTrace();
                Logger.ie(TAG, String.format("Error in getting token. %s", e.getMessage()));
                authenticationCompletionCallbackToMsipc.onFailure();
            }
        }
        App.getInstance()
                .getAuthenticationContext()
                .acquireToken(mParentActivity, resource, mClientId, mRedirectURI, userId, mPromptBehavior,
                        "&USERNAME=" + userHint, new AuthenticationCallback<AuthenticationResult>()
                        {
                            @Override
                            public void onError(Exception exc)
                            {
                                Logger.d(TAG, "ADAL Callback returned error");
                                if (exc instanceof AuthenticationCancelError)
                                {
                                    Logger.d(TAG, "Cancelled");
                                    authenticationCompletionCallbackToMsipc.onCancel();
                                }
                                else
                                {
                                    Logger.d(TAG, "Authentication error:" + exc.getMessage());
                                    authenticationCompletionCallbackToMsipc.onFailure();
                                }
                            }

                            @Override
                            public void onSuccess(AuthenticationResult result)
                            {
                                Logger.d(TAG, "ADAL Callback has result");
                                if (result == null || result.getAccessToken() == null
                                        || result.getAccessToken().isEmpty())
                                {
                                    Logger.d(TAG, "Token is empty");
                                    if (result != null)
                                    {
                                        Logger.d(TAG, String.format("Error: %s", result.getErrorDescription()));
                                    }
                                }
                                else
                                {
                                    // request is successful
                                    Logger.i(TAG, String.format("status:%s, expiresOn:%s", result.getStatus(), result
                                            .getExpiresOn().toString()), "");
                                    authenticationCompletionCallbackToMsipc.onSuccess(result.getAccessToken());
                                }
                            }
                        });
    }

    /**
     * Sets the ADAL key store for API < 18.
     * 
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeySpecException the invalid key spec exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void setADALKeyStore() throws NoSuchAlgorithmException, InvalidKeySpecException,
            UnsupportedEncodingException
    {
        // Allow ADAL to cache token
        if (Build.VERSION.SDK_INT < 18 && AuthenticationSettings.INSTANCE.getSecretKeyData() == null)
        {
            // use same key for tests
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA256And256BitAES-CBC-BC");
            //Note: An application must fine tune below code to their security needs
            SecretKey tempkey = keyFactory.generateSecret(new PBEKeySpec("test".toCharArray(), "abcdedfdfd"
                    .getBytes("UTF-8"), 100, 256));
            SecretKey secretKey = new SecretKeySpec(tempkey.getEncoded(), "AES");
            AuthenticationSettings.INSTANCE.setSecretKey(secretKey.getEncoded());
        }
    }
}

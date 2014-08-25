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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;

import com.microsoft.rightsmanagement.Consent;
import com.microsoft.rightsmanagement.ConsentResult;
import com.microsoft.rightsmanagement.ConsentType;
import com.microsoft.rightsmanagement.exceptions.InvalidParameterException;
import com.microsoft.rightsmanagement.ui.model.ConsentModel;
import com.microsoft.rightsmanagement.ui.utils.CallbackManager;
import com.microsoft.rightsmanagement.ui.utils.Logger;
import com.microsoft.rightsmanagement.ui.widget.ConsentFragment;
import com.microsoft.rightsmanagement.ui.widget.ConsentFragment.ConsentFragmentEventListner;

/**
 * An Activity to control consent UI for ServerURLConsent and DocumentTrackingConsent
 * If both are present this activity presents combined UI for consents
 */
public class ConsentActivity extends BaseActivity implements ConsentFragmentEventListner
{
    private static final String REQUEST_CONSENT_MODEL = "REQUEST_CONSENT_MODEL";
    private static final String RESULT_CONSENT_MODEL = "RESULT_CONSENT_MODEL";
    private static CallbackManager<Collection<Consent>,Collection<Consent>> sCallbackManager = new CallbackManager<Collection<Consent>, Collection<Consent>>();
    private ConsentFragment mConsentFragment;
    private ConsentModel mConsentModel;
    static
    {
        setTAG("ConsentActivity");
    }

    /**
     * Processes the result of ConsentActivity started via startActivityForResult from the parent activity, and invokes
     * the callback supplied to show(). This method must be called from parent Activity's onActivityResult.
     * 
     * @param resultCode the result code parameter as supplied to parent Activity's onActivityResult
     * @param data the data parameter as supplied to parent Activity's onActivityResult
     */
    public static void onActivityResult(int resultCode, Intent data)
    {
        Logger.ms(TAG, "onActivityResult");
        int requestCallbackId = 0;
        if (data == null)
        {
            Logger.i(TAG, "System closed the activity", "");
            return;
        }
        try
        {
            final Bundle extras = data.getExtras();
            requestCallbackId = extras.getInt(REQUEST_CALLBACK_ID);
            final CompletionCallback<Collection<Consent>> callback = sCallbackManager
                    .getWaitingRequest(requestCallbackId);
            switch (resultCode)
            {
                case RESULT_OK:
                    Logger.i(TAG, "resultCode=RESULT_OK", "");
                    ConsentModel consentModel = (ConsentModel)extras.get(RESULT_CONSENT_MODEL);
                    Collection<Consent> consents = sCallbackManager.getState(requestCallbackId);
                    for (Consent consent : consents)
                    {
                        if (consent.getConsentType() == ConsentType.SERVICE_URL_CONSENT
                                || consent.getConsentType() == ConsentType.DOCUMENT_TRACKING_CONSENT)
                        {
                            ConsentResult consentResult = new ConsentResult(consentModel.isAccepted(),
                                    consentModel.isShowAgain(), null);
                            consent.setConsentResult(consentResult);
                        }
                    }
                    callback.onSuccess(consents);
                    break;
                case RESULT_CANCELED:
                    Logger.i(TAG, "resultCode=RESULT_CANCELED", "");
                    callback.onCancel();
                    break;
            }
        }
        finally
        {
            if (requestCallbackId != 0)
            {
                sCallbackManager.removeWaitingRequest(requestCallbackId);
            }
            Logger.me(TAG, "onActivityResult");
        }
    }

    /**
     * Show UI
     * 
     * @param requestCode
     * @param parentActivity
     * @param consents - consents collection containing serverurlconsent or documentTrackingConsent
     * @param emailActivityCompletionCallback
     * @throws InvalidParameterException
     */
    public static void show(int requestCode,
                            Activity parentActivity,
                            Collection<Consent> consents,
                            CompletionCallback<Collection<Consent>> consentActivityCompletionCallback)
            throws InvalidParameterException
    {
        Logger.ms(TAG, "show");
        parentActivity = validateActivityInputParameter(parentActivity);
        consentActivityCompletionCallback = validateCompletionCallbackInputParameter(consentActivityCompletionCallback);
        consents = validateConsentInputParamter(consents);
        ConsentModel consentModel = new ConsentModel(consents);
        int requestCallbackId = consentActivityCompletionCallback.hashCode();
        sCallbackManager.putWaitingRequest(requestCallbackId, consentActivityCompletionCallback, consents);
        Intent intent = new Intent(parentActivity, ConsentActivity.class);
        intent.putExtra(REQUEST_CALLBACK_ID, requestCallbackId);
        intent.putExtra(REQUEST_CONSENT_MODEL, consentModel);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        parentActivity.startActivityForResult(intent, requestCode);
        Logger.me(TAG, "show");
    }

    /**
     * Validate consent collection input parameter
     * 
     * @param consents consent collection
     * @return validated consent collection
     * @throws InvalidParameterException the invalid parameter exception
     */
    private static Collection<Consent> validateConsentInputParamter(Collection<Consent> consents)
            throws InvalidParameterException
    {
        if (consents == null)
        {
            InvalidParameterException exception = new InvalidParameterException();
            Logger.e(TAG, "Invalid parameter consents", "", exception);
            throw exception;
        }
        
        List<Consent> consentsToHandle = new ArrayList<Consent>();
        for(Consent consent : consents)
        {
            if (ConsentType.SERVICE_URL_CONSENT == consent.getConsentType()
                    || ConsentType.DOCUMENT_TRACKING_CONSENT == consent.getConsentType())
            {
                consentsToHandle.add(consent);
            }
        }
        
        if(consentsToHandle.isEmpty())
        {
            InvalidParameterException exception = new InvalidParameterException();
            Logger.e(TAG, "Invalid parameter consents does not contain any consents this activity can process", "",
                    exception);
            throw exception;            
        }
        
        return consentsToHandle;
    }

    /**
     * Do Fragment transaction to add Consent Fragment
     */
    private void addConsentFragment()
    {
        int containerId = R.id.consent_container;
        mConsentFragment = (ConsentFragment)getSupportFragmentManager().findFragmentByTag(ConsentFragment.TAG);
        if (mConsentFragment == null)
        {
            Logger.d(TAG, "addConsentFragment() - mConsentFragment is null");
            mConsentFragment = ConsentFragment.create(mConsentModel);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_animation_in_from_down, 0);
            ft.add(containerId, mConsentFragment, ConsentFragment.TAG).commit();
        }
        else
        {
            Logger.d(TAG, "addConsentFragment() - mConsentFragment is not null");
        }
    }

    /**
     * activity sets result to go back to the caller.
     * 
     * @param resultCode the result code
     * @param data the data
     */
    @Override
    protected void returnToCaller(int resultCode, Intent data)
    {
        super.returnToCaller(resultCode, data);
        Logger.d(TAG, String.format("ReturnToCaller - resultCode=%d", resultCode));
        setResult(resultCode, data);
        if (mConsentFragment == null)
        {
            this.finish();
        }
        else
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(0, R.animator.slide_animation_out_to_down);
            ft.remove(mConsentFragment).commit();
            mConsentFragment = null;
            startActivityEndAnimationAndFinishActivity();
        }
    }
    
    @Override
    public void onAcceptButtonClicked(boolean showAgain)
    {
        Logger.ms(TAG, "onAcceptButtonClicked");
        Intent data = new Intent();
        data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
        mConsentModel.setAccepted(true);
        mConsentModel.setShowAgain(showAgain);
        data.putExtra(RESULT_CONSENT_MODEL, mConsentModel);
        returnToCaller(RESULT_OK, data);
        Logger.me(TAG, "onAcceptButtonClicked");
    }

    @Override
    public void onCancelButtonClicked(boolean showAgain)
    {
        Logger.ms(TAG, "onAcceptButtonClicked");
        Intent data = new Intent();
        data.putExtra(REQUEST_CALLBACK_ID, mRequestCallbackId);
        mConsentModel.setAccepted(false);
        mConsentModel.setShowAgain(showAgain);
        data.putExtra(RESULT_CONSENT_MODEL, mConsentModel);
        returnToCaller(RESULT_OK, data);
        Logger.me(TAG, "onAcceptButtonClicked");
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consent_activity_layout);
        if (savedInstanceState == null)
        {
            Logger.d(TAG, "bundle is null");
            // creating from scratch
            Intent intent = getIntent();
            Bundle argumentsBundle = intent.getExtras();
            parseBundleInput(argumentsBundle);
        }
        else
        {
            Logger.d(TAG, "bundle is not null");
            // creation from saved state
            parseBundleInput(savedInstanceState);
        }
        addConsentFragment();
        addTransparentPartDismissListener(R.id.left_transparent_part);
        addTransparentPartDismissListener(R.id.right_transparent_part);
        // create fader animators
        createBgAnimators(R.id.consent_base_container, savedInstanceState);
        Logger.me(TAG, "onCreate");
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Logger.ms(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(REQUEST_CALLBACK_ID, mRequestCallbackId);
        outState.putParcelable(REQUEST_CONSENT_MODEL, mConsentModel);
        Logger.me(TAG, "onSaveInstanceState");
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onDestroy()
     */
    @Override
    protected void onDestroy()
    {
        if ((isFinishing() == true) && (mActivityFinishedWithResult == false))
        {
            sCallbackManager.removeWaitingRequest(mRequestCallbackId);
        }
        super.onDestroy();
    }

    /**
     * Retrieve data from intent
     * 
     * @param bundle the bundle
     */
    private void parseBundleInput(Bundle bundle)
    {
        if (bundle.containsKey(REQUEST_CONSENT_MODEL))
        {
            Logger.d(TAG, "parseBundleInput - parsing consentModel");
            Parcelable requestModel = bundle.getParcelable(REQUEST_CONSENT_MODEL);
            mConsentModel = (ConsentModel)requestModel;
        }
        if (bundle.containsKey(REQUEST_CALLBACK_ID))
        {
            Logger.d(TAG, "parseBundleInput - parsing RequestCallbackId");
            mRequestCallbackId = bundle.getInt(REQUEST_CALLBACK_ID);
        }
    }
}

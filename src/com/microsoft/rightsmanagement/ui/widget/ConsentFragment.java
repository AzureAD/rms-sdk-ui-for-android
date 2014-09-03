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

package com.microsoft.rightsmanagement.ui.widget;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.model.ConsentModel;
import com.microsoft.rightsmanagement.ui.utils.Helpers;
import com.microsoft.rightsmanagement.ui.utils.Logger;

/**
 * The Class ConsentFragment provides view implementation for Consent UI
 */
public class ConsentFragment extends Fragment
{
    public interface ConsentFragmentEventListner
    {
        /**
         * Called when accept link button is clicked
         * 
         * @param showAgain false if checkbox is shown and clicked , true otherwise
         */
        public void onAcceptButtonClicked(boolean showAgain);

        /**
         * Called when cancel link button is clicked
         * 
         * @param showAgain false if checkbox is shown and clicked , true otherwise
         */
        public void onCancelButtonClicked(boolean showAgain);
    }
    public static final String TAG = "ConsentFragment";
    private static final String CONSENT_MODEL = "CONSENT_MODEL";
    private ConsentFragmentEventListner mConsentFragmentEventListner;
    private ConsentModel mConsentModel;
    private CheckBox mDontShowAgainCheckbox;

    /**
     * Gets new instance of consentFragment
     * 
     * @param consentModel consentModel which holds information for view rendering
     * @return ConsentFragment instance
     */
    public static final ConsentFragment create(ConsentModel consentModel)
    {
        ConsentFragment consentFragment = new ConsentFragment();
        Bundle bundleArgs = new Bundle();
        bundleArgs.putParcelable(CONSENT_MODEL, consentModel);
        consentFragment.setArguments(bundleArgs);
        return consentFragment;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity)
    {
        Logger.ms(TAG, "onAttach");
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, throw an exception
        try
        {
            mConsentFragmentEventListner = (ConsentFragmentEventListner)activity;
        }
        catch (ClassCastException e)
        {
            Logger.ie(TAG, "Activity must implement ConsentFragmentEventListner");
            throw e;
        }
        Logger.me(TAG, "onAttach");
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
     * android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState)
    {
        Logger.ms(TAG, "onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(CONSENT_MODEL))
        {
            mConsentModel = args.getParcelable(CONSENT_MODEL);
        }
        int fragmentId = R.layout.consent_fragment_layout;
        View view = inflater.inflate(fragmentId, container, false);
        
        // set UI elements visibility
        addServiceURLConsent(view);
        addDocumentTrackingConsent(view);
        setButtonListners(view);
        setShowCheckBox(view);
        
        // make hyperlink textviews clickable
        TextView helpHyperLinkTextView = (TextView)view.findViewById(R.id.helpHyperLinkTextView);
        TextView privacyHyperLinkTextView = (TextView)view.findViewById(R.id.privacyHyperLinkTextView);
        Helpers.makeTextViewAHTMLLink(getActivity(), helpHyperLinkTextView,
                getResources().getString(R.string.help_hyperlink));
        Helpers.makeTextViewAHTMLLink(getActivity(), privacyHyperLinkTextView,
                getResources().getString(R.string.privacy_hyperlink));
        Logger.me(TAG, "onCreateView");
        return view;
    }

    /**
     * Helper method to add service URL Consent if applicable
     */
    private void addServiceURLConsent(View view)
    {
        if (mConsentModel.showServiceURLConsent())
        {
            TextView serviceURLMessageTextView = (TextView)view
                    .findViewById(R.id.service_url_consent_messsage_text_view);
            String serviceURLMessageText = getResources().getString(R.string.service_url_consent_messsage);
            serviceURLMessageTextView.setText(Html.fromHtml(serviceURLMessageText + "<br/><br/>" + "<b>"
                    + mConsentModel.getUrlsForURLConsent() + "</b>"));
            serviceURLMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Helper method to add document tracking consent if applicable
     */
    private void addDocumentTrackingConsent(View view)
    {
        if (mConsentModel.showDocumentTrackingConsent())
        {
            TextView documentTrackingConsentTextView = (TextView)view
                    .findViewById(R.id.document_tracking_consent_message_text_view);
            documentTrackingConsentTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Helper method to show check box if applicable
     */
    private void setShowCheckBox(View view)
    {
        if(mConsentModel.showCheckBox())
        {
            CheckBox checkbox = (CheckBox) view.findViewById(R.id.dont_show_again_checkbox);
            checkbox.setVisibility(View.VISIBLE);
            mDontShowAgainCheckbox = checkbox;
        }
    }
    
    /**
     * Helper method to add listners to accept and cancel buttons
     */
    private void setButtonListners(View view)
    {
        Button acceptButton = (Button)view.findViewById(R.id.acceptConsentButton);
        acceptButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "Accept button onClick called");
                if (mDontShowAgainCheckbox != null && mDontShowAgainCheckbox.isShown())
                {
                    mConsentFragmentEventListner.onAcceptButtonClicked(!mDontShowAgainCheckbox.isChecked());
                }
                else
                {
                    mConsentFragmentEventListner.onAcceptButtonClicked(false);
                }
            }
        });
        Button cancelButton = (Button)view.findViewById(R.id.cancelConsentButton);
        cancelButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Logger.d(TAG, "Cancel button onClick called");
                if (mDontShowAgainCheckbox != null && mDontShowAgainCheckbox.isShown())
                {
                    mConsentFragmentEventListner.onCancelButtonClicked(!mDontShowAgainCheckbox.isChecked());
                }
                else
                {
                    mConsentFragmentEventListner.onCancelButtonClicked(true);
                }
            }
        });
    }
}

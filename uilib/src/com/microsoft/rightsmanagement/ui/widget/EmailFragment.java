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

package com.microsoft.rightsmanagement.ui.widget;

import com.microsoft.rightsmanagement.ui.R;
import com.microsoft.rightsmanagement.ui.utils.Helpers;
import com.microsoft.rightsmanagement.ui.utils.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The Class EmailFragment provide view implementation for Email UI.
 */
public class EmailFragment extends Fragment
{
    /**
     * The Interface IEmailFragmentCallback for communicating UI events.
     */
    public interface EmailFragmentEventListener
    {
        /**
         * Called when continue button is pressed.
         * 
         * @param item email address
         */
        public void onContinue(String item);
    }
    public static final String TAG = "EmailFragment";
    private EmailFragmentEventListener mEmailFragmentEventListener;

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
            mEmailFragmentEventListener = (EmailFragmentEventListener)activity;
        }
        catch (ClassCastException e)
        {
            Logger.ie(TAG, "Activity must implement EmailFragmentEventListener");
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
        int fragmentId = R.layout.email_fragment_layout;
        View view = inflater.inflate(fragmentId, container, false);
        int continueButtonId = R.id.emailContinueButton;
        int userMailEditTextId = R.id.userMailEditText;
        Button continueButton = (Button)view.findViewById(continueButtonId);
        final EditText emailEditText = (EditText)view.findViewById(userMailEditTextId);
        continueButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Logger.d(TAG, "onClick listener called on continueButton");
                mEmailFragmentEventListener.onContinue(emailEditText.getText().toString());
            }
        });
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
     * Sets the email text.
     * 
     * @param text the new email text
     */
    public void setEmailText(String text)
    {
        Logger.ms(TAG, "setEmailText");
        Logger.d(TAG, String.format("text= %s", text));
        int userMailEditTextId = R.id.userMailEditText;
        EditText emailEditText = (EditText)getView().findViewById(userMailEditTextId);
        emailEditText.setText(text);
        Logger.me(TAG, "setEmailText");
    }

    /**
     * Sets the error text.
     * 
     * @param text the new error text
     */
    public void setErrorText(String text)
    {
        Logger.ms(TAG, "setErrorText");
        Logger.d(TAG, String.format("text= %s", text));
        int errorTextViewId = R.id.errorTextView;
        TextView t = (TextView)getView().findViewById(errorTextViewId);
        t.setText(text);
        t.setVisibility(View.VISIBLE);
        Logger.me(TAG, "setErrorText");
    }
}

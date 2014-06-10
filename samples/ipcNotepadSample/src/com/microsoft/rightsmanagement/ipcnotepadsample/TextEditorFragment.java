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

import com.microsoft.rightsmanagement.ipcnotepadsample.R;
import com.microsoft.rightsmanagement.ui.PolicyEnforcer;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * A fragment that provides view implementation for text editor UI.
 */
public class TextEditorFragment extends Fragment
{
    /**
     * The Interface for communicating TextEditorFragment Events.
     */
    public interface TextEditorFragmentEventListener
    {
        /**
         * On blank area click.
         */
        public void onBlankAreaClick();

        /**
         * On protection button click.
         */
        public void onProtectionButtonClick();

        /**
         * On send mail button click.
         */
        public void onSendMailButtonClick();
    }

    /**
     * The Enum representing the text editor user policy enforcement mode.
     */
    public enum TextEditorMode
    {
        Enforced, NotEnforced,
    }
    public static final String TAG = "PlaceholderFragment";
    private static final String KEY_EDITOR_MODE = "KEY_EDITOR_MODE";
    private PolicyEnforcer mPolicyEnforcer;
    private EditText mTextEditor;
    private TextEditorFragmentEventListener mTextEditorFragmentEventCallback;
    private TextEditorMode mTextEditorMode;

    /**
     * New instance.
     * 
     * @param textEditorMode the text editor mode
     * @return the text editor fragment
     */
    public static TextEditorFragment newInstance(TextEditorMode textEditorMode)
    {
        TextEditorFragment fragment = new TextEditorFragment();
        Bundle args = new Bundle();
        if (textEditorMode != null)
        {
            args.putInt(KEY_EDITOR_MODE, textEditorMode.ordinal());
        }
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Gets the text view text.
     * 
     * @return the text view text
     */
    public String getTextViewText()
    {
        return (String)mTextEditor.getEditableText().toString();
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, throw an exception
        try
        {
            mTextEditorFragmentEventCallback = (TextEditorFragmentEventListener)activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement ITextEditorFragmentEventCallback");
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_EDITOR_MODE))
        {
            mTextEditorMode = TextEditorMode.values()[args.getInt(KEY_EDITOR_MODE)];
        }
        setHasOptionsMenu(true);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
     * android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mTextEditor = (EditText)rootView.findViewById(R.id.edit_text_view);
        rootView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mTextEditorFragmentEventCallback.onBlankAreaClick();
            }
        });
        honorTextEditorMode(rootView);
        return rootView;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.protect_menu_button:
                mTextEditorFragmentEventCallback.onProtectionButtonClick();
                return true;
            case R.id.send_menu_button:
                mTextEditorFragmentEventCallback.onSendMailButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sets the policy enforcer.
     * 
     * @param policyEnforcer the new policy enforcer
     */
    public void setPolicyEnforcer(PolicyEnforcer policyEnforcer)
    {
        mPolicyEnforcer = policyEnforcer;
    }

    /**
     * Sets the text view text.
     * 
     * @param decryptedContent the new text view text
     */
    public void setTextViewText(final String decryptedContent)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                mTextEditor.setText(decryptedContent);
            }
        });
    }

    /**
     * Honor text editor mode.
     * 
     * @param rootView the root view
     */
    private void honorTextEditorMode(View rootView)
    {
        if (mTextEditorMode == TextEditorMode.NotEnforced)
        {
            mTextEditor.setFocusableInTouchMode(true);
            mTextEditor.setFocusable(true);
        }
        else
        {
            if (mPolicyEnforcer != null)
            {
                // TODO honor enforcer
            }
            else
            {
                mTextEditor.setFocusableInTouchMode(false);
                mTextEditor.setFocusable(false);
            }
        }
    }
}

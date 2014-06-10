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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.microsoft.adal.AuthenticationContext;
import com.microsoft.rightsmanagement.AuthenticationRequestCallback;

/**
 * The Class App.
 */
public class App extends Application
{
    // A DocumentManager instance used for opening protected content.
    public static Class<?> DECLARED_CLASS;
    public final static String PLAIN_TEXT_FILE_SUFFIX_PFILE = ".txt";
    public static final String PROTECTED_FILE_SUFFIX_PFILE = ".ptxt";
    // TAG used from logging.
    public static String TAG = "MsipcSampleApplication";
    /*
     * Save the progressDialogFragment as sometime findFragmentByTag(ProgressDialogFragment.TAG) fails during
     * dismissProgressDialog
     */
    private static DialogFragment sProgressDialogFragment = null;
    // The instance of the application.
    private static App sInstance;
    private AuthenticationContext mAuthenticationContext;
    private File mStorageDir;

    /**
     * Dismiss progress dialog.
     */
    public static void dismissProgressDialog(FragmentManager supportedFragmentManager)
    {
        DialogFragment previous = (ProgressDialogFragment)supportedFragmentManager
                .findFragmentByTag(ProgressDialogFragment.TAG);
        if (previous != null)
        {
            previous.dismissAllowingStateLoss();
        }
        else if(sProgressDialogFragment != null)
        {
            // sometimes findFragmentByTag(ProgressDialogFragment.TAG) fails when fragment is still to be shown
            sProgressDialogFragment.dismissAllowingStateLoss();
        }
        sProgressDialogFragment = null;
    }

    /**
     * Display message dialog.
     * 
     * @param message the message
     */
    public static void displayMessageDialog(FragmentManager supportedFragmentManager, String message)
    {
        FragmentTransaction fragmentTransaction = supportedFragmentManager.beginTransaction();
        Fragment previous = supportedFragmentManager.findFragmentByTag(MessageDialogFragment.TAG);
        if (previous != null)
        {
            fragmentTransaction.remove(previous);
        }
        DialogFragment newFragment = MessageDialogFragment.newInstance(message);
        fragmentTransaction.add(newFragment, MessageDialogFragment.TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    /**
     * Display progress dialog.
     * 
     * @param message the message
     */
    public static void displayProgressDialog(FragmentManager supportedFragmentManager, String message)
    {
        FragmentTransaction fragmentTransaction = supportedFragmentManager.beginTransaction();
        Fragment previous = supportedFragmentManager.findFragmentByTag(ProgressDialogFragment.TAG);
        if (previous != null)
        {
            fragmentTransaction.remove(previous);
        }
        DialogFragment newFragment = ProgressDialogFragment.newInstance(message);
        fragmentTransaction.add(newFragment, ProgressDialogFragment.TAG);
        fragmentTransaction.commitAllowingStateLoss();
        sProgressDialogFragment = newFragment;// save
    }

    /**
     * Gets the file name from a content URI.
     * 
     * @param activity the activity
     * @param uri the uri
     * @return the file name from the content uri.
     */
    public static String getFileNameFromContent(Activity activity, Uri uri)
    {
        String fileName = null;
        if (uri != null)
        {
            Cursor c = activity.getContentResolver().query(uri, null, null, null, null);
            c.moveToFirst();
            final int fileNameColumnId = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            if (fileNameColumnId >= 0)
            {
                fileName = c.getString(fileNameColumnId);
            }
        }
        // Emulator attachment files are stored in another place.
        if (fileName == null)
        {
            String[] proj = {
                MediaColumns.DISPLAY_NAME
            };
            Cursor cursor = activity.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null && cursor.getCount() > 0)
            {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                fileName = cursor.getString(columnIndex);
            }
        }
        return fileName;
    }

    /**
     * Gets the single instance of App.
     * 
     * @return single instance of App
     */
    public static App getInstance()
    {
        return sInstance;
    }

    /**
     * Checks if file is a ptxt file.
     * 
     * @param fileName the file name
     * @return true, if is ptxt file
     */
    public static boolean isPTxtFile(String fileName)
    {
        if (fileName == null || fileName.lastIndexOf('.') < 0)
        {
            // TODO throw invalid argument exception
        }
        String extension = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
        boolean isProtected = false;
        if (extension.compareToIgnoreCase(PROTECTED_FILE_SUFFIX_PFILE) == 0)
        {
            isProtected = true;
        }
        return isProtected;
    }

    /**
     * Send file.
     * 
     * @param activity the activity
     * @param filePath the file path
     */
    static void sendFile(Activity activity, String filePath)
    {
        // New intent to open mail client.
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_SEND);
        if (isPTxtFile(filePath))
        {
            intent.setType("application/octet-stream");
        }
        else
        {
            intent.setType("plain/text");
        }
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
        activity.startActivity(Intent.createChooser(intent, "Sending File..."));
    }

    /**
     * Gets the authentication context.
     * 
     * @return the authentication context
     */
    public AuthenticationContext getAuthenticationContext()
    {
        return mAuthenticationContext;
    }

    /**
     * Gets the rms authentication callback.
     * 
     * @param activity the activity
     * @return the rms authentication callback
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeySpecException the invalid key spec exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public AuthenticationRequestCallback getRmsAuthenticationCallback(Activity activity)
            throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException
    {
        return new MsipcAuthenticationCallback(activity);
    }

    /**
     * Gets the storage directory.
     * 
     * @return the storage directory
     */
    public File getStorageDirectory()
    {
        return mStorageDir;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate()
    {
        sInstance = this;
        DECLARED_CLASS = this.getClass();
        mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    }

    /**
     * Sets the authentication context.
     * 
     * @param authenticationContext the authentication context
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        mAuthenticationContext = authenticationContext;
    }
}

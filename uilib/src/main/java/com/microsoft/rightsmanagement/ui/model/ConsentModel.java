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

package com.microsoft.rightsmanagement.ui.model;

import java.net.URL;
import java.util.Collection;

import android.os.Parcel;
import android.os.Parcelable;

import com.microsoft.rightsmanagement.Consent;
import com.microsoft.rightsmanagement.ConsentType;
import com.microsoft.rightsmanagement.ServiceURLConsent;

/**
 * Models Consent
 */
public class ConsentModel implements Parcelable
{
    public static final Parcelable.Creator<ConsentModel> CREATOR = new Creator<ConsentModel>()
    {
        @Override
        public ConsentModel createFromParcel(Parcel in)
        {
            return new ConsentModel(in);
        }

        @Override
        public ConsentModel[] newArray(int size)
        {
            return new ConsentModel[size];
        }
    };
    private boolean mShowConsentForServiceURL;
    private boolean mShowConsentForDocumentTracking;
    private String mUrlsForURLConsent;
    private boolean mAccepted;
    private boolean mShowAgain;
    private boolean mShowCheckBox;

    /**
     * Creates consent Model object based on consents
     * 
     * @param consents
     * @return consentModel view model to display consent page to user
     */
    public ConsentModel(Collection<Consent> consents)
    {
        setConsentsToShow(consents);
        mUrlsForURLConsent = getUrlsToDisplay(consents);
        mShowCheckBox = true;
    }

    /**
     * Instantiates a new consent model.
     * 
     * @param in the input parcel
     */
    private ConsentModel(Parcel in)
    {
        mShowConsentForServiceURL = in.readByte() != 0;
        mShowConsentForDocumentTracking = in.readByte() != 0;
        mUrlsForURLConsent = in.readString();
        mAccepted = in.readByte() != 0;
        mShowAgain = in.readByte() != 0;
        mShowCheckBox = in.readByte() != 0;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * If we should show Service URL Consent
     * 
     * @return True if we should show Service URL Consent , False otherwise
     */
    public boolean showServiceURLConsent()
    {
        return mShowConsentForServiceURL;
    }

    /**
     * If we should show Document Tracking Consent
     * 
     * @return True if we should show DocumentTrackingConsent , False otherwise
     */
    public boolean showDocumentTrackingConsent()
    {
        return mShowConsentForDocumentTracking;
    }

    /**
     * Get urls Text to display for this model
     * 
     * @return urls to display , if no urls to display returns empty string
     */
    public String getUrlsForURLConsent()
    {
        return mUrlsForURLConsent;
    }

    /**
     * Gets URLs to display from service URL consent if one exists
     * 
     * @param consents
     * @return String of URLs to display seeprated by newline
     */
    private String getUrlsToDisplay(Collection<Consent> consents)
    {
        StringBuffer urlStringBuffer = new StringBuffer();
        for (Consent consent : consents)
        {
            if (consent.getConsentType() == ConsentType.SERVICE_URL_CONSENT)
            {
                ServiceURLConsent serviceURLConsent = (ServiceURLConsent)consent;
                URL[] urls = serviceURLConsent.getUrls();
                if (urls != null)
                {
                    for (int i = 0; i < urls.length; i++)
                    {
                        urlStringBuffer.append(urls[i].toString()).append("\n");
                    }
                    // remove extra newline
                    urlStringBuffer.setLength(urlStringBuffer.length() - 1);
                }
            }
        }
        return urlStringBuffer.toString();
    }

    /**
     * If user accepted the consent
     * 
     * @return true if user accepted consent ; false otherwise
     */
    public boolean isAccepted()
    {
        return mAccepted;
    }

    /**
     * If user chooses to see the notification again
     * 
     * @return True if we should show the notification again , false otherwise
     */
    public boolean isShowAgain()
    {
        return mShowAgain;
    }

    /**
     * Tells if the consent screen should have a check box for don't show again
     * 
     * @return True if we should show checkbox on the screen , flase otherwise
     */
    public boolean showCheckBox()
    {
        return mShowCheckBox;
    }

    /**
     * Sets accepted flag
     * 
     * @param mAccepted
     */
    public void setAccepted(boolean accepted)
    {
        mAccepted = accepted;
    }

    /**
     * Creates bit mask of consents to show
     * 
     * @param consents
     * @return bit mask of consents to show
     */
    private void setConsentsToShow(Collection<Consent> consents)
    {
        for (Consent consent : consents)
        {
            if (consent.getConsentType() == ConsentType.SERVICE_URL_CONSENT)
            {
                mShowConsentForServiceURL = true;
            }
            else if (consent.getConsentType() == ConsentType.DOCUMENT_TRACKING_CONSENT)
            {
                mShowConsentForDocumentTracking = true;
            }
        }
    }

    /**
     * Sets the flag show again - if the consent needs to be shown again
     * 
     * @param showAgain
     */
    public void setShowAgain(boolean showAgain)
    {
        mShowAgain = showAgain;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeByte((byte)(mShowConsentForServiceURL ? 1 : 0));
        out.writeByte((byte)(mShowConsentForDocumentTracking ? 1 : 0));
        out.writeString(mUrlsForURLConsent);
        out.writeByte((byte)(mAccepted ? 1 : 0));
        out.writeByte((byte)(mShowAgain ? 1 : 0));
        out.writeByte((byte)(mShowCheckBox ? 1 : 0));
    }
}

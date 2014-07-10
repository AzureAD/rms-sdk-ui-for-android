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

package com.microsoft.rightsmanagement.ui.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import android.util.SparseArray;

import com.microsoft.rightsmanagement.ui.CompletionCallback;

/**
 * Manages callbacks used in onActivityResult.
 * 
 * @param <T> callback result type
 * @param <S> extra info (state) holder type
 */
public class CallbackManager<T, S>
{
    /**
     * Represents payload that is mapped to callback id.
     */
    private class Item
    {
        public CompletionCallback<T> mCompletetionCallback;
        public S mState;
    }
    private final ReentrantReadWriteLock mRwl = new ReentrantReadWriteLock();
    private final Lock mReadLock = mRwl.readLock();
    private final Lock mWriteLock = mRwl.writeLock();
    private SparseArray<Item> sCallbackMap = new SparseArray<Item>();
    private String TAG = "CallbackManager";

    /**
     * Gets the state.
     * 
     * @param requestCallbackId the request callback id
     * @return the state
     */
    public S getState(int requestCallbackId)
    {
        Logger.ms(TAG, "getState");
        S state = null;
        mReadLock.lock();
        try
        {
            Item item = sCallbackMap.get(requestCallbackId);
            state = item.mState;
        }
        finally
        {
            mReadLock.unlock();
            Logger.me(TAG, "getState");
        }
        return state;
    }

    /**
     * Gets the waiting request.
     * 
     * @param requestCallbackId the request callback id
     * @return the waiting request
     */
    public CompletionCallback<T> getWaitingRequest(int requestCallbackId)
    {
        Logger.ms(TAG, "getWaitingRequest");
        CompletionCallback<T> request = null;
        mReadLock.lock();
        try
        {
            Item item = sCallbackMap.get(requestCallbackId);
            request = item.mCompletetionCallback;
        }
        finally
        {
            mReadLock.unlock();
            Logger.me(TAG, "getWaitingRequest");
        }
        return request;
    }

    /**
     * Put waiting request.
     * 
     * @param requestCallbackId the request callback id
     * @param requestCallback the request callback
     */
    public void putWaitingRequest(int requestCallbackId, CompletionCallback<T> requestCallback)
    {
        putWaitingRequest(requestCallbackId, requestCallback, null);
    }

    /**
     * Put waiting request.
     * 
     * @param requestCallbackId the request callback id
     * @param requestCallback the request callback
     * @param state the state
     */
    public void putWaitingRequest(int requestCallbackId, CompletionCallback<T> requestCallback, S state)
    {
        Logger.ms(TAG, "putWaitingRequest");
        if (requestCallback != null)
        {
            mWriteLock.lock();
            try
            {
                Item item = new Item();
                item.mCompletetionCallback = requestCallback;
                item.mState = state;
                sCallbackMap.put(requestCallbackId, item);
            }
            finally
            {
                mWriteLock.unlock();
                Logger.me(TAG, "putWaitingRequest");
            }
        }
    }

    /**
     * Removes the waiting request.
     * 
     * @param requestId the request id
     */
    public void removeWaitingRequest(int requestId)
    {
        Logger.ms(TAG, "removeWaitingRequest");
        mWriteLock.lock();
        try
        {
            sCallbackMap.remove(requestId);
        }
        finally
        {
            mWriteLock.unlock();
            Logger.me(TAG, "removeWaitingRequest");
        }
    }
}

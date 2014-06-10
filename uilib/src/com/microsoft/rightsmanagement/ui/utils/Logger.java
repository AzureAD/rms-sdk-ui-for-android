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

package com.microsoft.rightsmanagement.ui.utils;

import com.microsoft.rightsmanagement.exceptions.ProtectionException;

import android.util.Log;

/**
 * A Wrapper class for Logger.
 */
public class Logger
{
    public enum LogLevel
    {
        Error, Warn, Info, Verbose, Debug
    }

    private static class LAZYHOLER
    {
        public static Logger sInstance = new Logger();
    };
    // log tot logcat by default
    protected boolean mAndroidLogEnabled = true;
    /**
     * Child logger per 'chain of responsibility' design pattern
     */
    private Logger mChildLogger = null;
    protected LogLevel mLogLevel;

    /**
     * Debug
     * 
     * @param tag the tag
     * @param message the message
     */
    public static void d(String tag, String message)
    {
        Logger.getInstance().debug(tag, message);
    }

    /**
     * External error.
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     * @param protectionException the protection exception
     */
    public static void e(String tag, String message, String additionalMessage, ProtectionException protectionException)
    {
        Logger.getInstance().error(tag, message, additionalMessage, protectionException);
    }

    /**
     * Gets the single instance of Logger.
     * 
     * @return single instance of Logger
     */
    public static Logger getInstance()
    {
        return LAZYHOLER.sInstance;
    }

    /**
     * Inform
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     */
    public static void i(String tag, String message, String additionalMessage)
    {
        Logger.getInstance().inform(tag, message, additionalMessage, null);
    }

    /**
     * Internal error.
     * 
     * @param tag the tag
     * @param additionalMessage the additional message
     */
    public static void ie(String tag, String additionalMessage)
    {
        Logger.getInstance().error(tag, "Internal Error", additionalMessage, null);
    }

    /**
     * Method End.
     * 
     * @param tag the tag
     * @param methodname the methodname
     */
    public static void me(String tag, String methodname)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("END ");
        sb.append(methodname);
        Logger.getInstance().verbose(tag, sb.toString(), "", null);
    }

    /**
     * Method Start.
     * 
     * @param tag the tag
     * @param methodname the methodname
     */
    public static void ms(String tag, String methodname)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("START ");
        sb.append(methodname);
        Logger.getInstance().verbose(tag, sb.toString(), "", null);
    }

    /**
     * Vebose
     * 
     * @param tag the tag
     * @param message the message
     */
    public static void v(String tag, String message)
    {
        Logger.getInstance().verbose(tag, message, null, null);
    }

    /**
     * Verbose
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     * @param protectionException the protection exception
     */
    public static void v(String tag, String message, String additionalMessage, ProtectionException protectionException)
    {
        Logger.getInstance().verbose(tag, message, additionalMessage, protectionException);
    }

    /**
     * Warning
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     * @param protectionException the protection exception
     */
    public static void w(String tag, String message, String additionalMessage, ProtectionException protectionException)
    {
        Logger.getInstance().warn(tag, message, additionalMessage, protectionException);
    }

    /**
     * Instantiates a new logger.
     */
    protected Logger()
    {
        mLogLevel = LogLevel.Debug;
    }

    /**
     * Debug.
     * 
     * @param tag the tag
     * @param message the message
     */
    public void debug(String tag, String message)
    {
        if (mLogLevel.ordinal() < LogLevel.Debug.ordinal() || Helpers.IsNullOrEmpty(message))
            return;
        if (mAndroidLogEnabled)
        {
            Log.d(tag, message);
        }
        if (mChildLogger != null)
        {
            try
            {
                mChildLogger.debug(tag, message);
            }
            catch (Exception e)
            {
                logChildLoggerError(tag, e, message);
            }
        }
    }

    /**
     * Error.
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     * @param protectionException the protection exception
     */
    public void error(String tag, String message, String additionalMessage, ProtectionException protectionException)
    {
        if (mAndroidLogEnabled)
        {
            if (protectionException != null)
            {
                Log.e(tag,
                        String.format("%s: %s. %s", protectionException.getType().name(), message, additionalMessage));
            }
            else
            {
                Log.e(tag, String.format("%s. %s", message, additionalMessage));
            }
        }
        if (mChildLogger != null)
        {
            try
            {
                mChildLogger.error(tag, message, additionalMessage, protectionException);
            }
            catch (Exception e)
            {
                logChildLoggerError(tag, e, message);
            }
        }
    }

    /**
     * Gets the log level.
     * 
     * @return the log level
     */
    public LogLevel getLogLevel()
    {
        return mLogLevel;
    }

    /**
     * Inform.
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     * @param protectionException the protection exception
     */
    public void inform(String tag, String message, String additionalMessage, ProtectionException protectionException)
    {
        if (mLogLevel.ordinal() < LogLevel.Info.ordinal())
            return;
        if (mAndroidLogEnabled)
        {
            if (protectionException != null)
            {
                Log.i(tag,
                        String.format("%s: %s. %s", protectionException.getType().name(), message, additionalMessage));
            }
            else
            {
                Log.i(tag, String.format("%s. %s", message, additionalMessage));
            }
        }
        if (mChildLogger != null)
        {
            try
            {
                mChildLogger.inform(tag, message, additionalMessage, protectionException);
            }
            catch (Exception e)
            {
                logChildLoggerError(tag, e, message);
            }
        }
    }

    /**
     * Checks if is android log enabled.
     * 
     * @return true, if is android log enabled
     */
    public boolean isAndroidLogEnabled()
    {
        return mAndroidLogEnabled;
    }

    /**
     * Sets the android log enabled.
     * 
     * @param androidLogEnable the new android log enabled
     */
    public void setAndroidLogEnabled(boolean androidLogEnable)
    {
        this.mAndroidLogEnabled = androidLogEnable;
    }

    /**
     * Sets the external logger.
     * 
     * @param externalLogger the new external logger
     */
    public void setExternalLogger(Logger externalLogger)
    {
        this.mChildLogger = externalLogger;
    }

    /**
     * Sets the log level.
     * 
     * @param level the new log level
     */
    public void setLogLevel(LogLevel level)
    {
        this.mLogLevel = level;
    }

    /**
     * Verbose.
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     * @param protectionException the protection exception
     */
    public void verbose(String tag, String message, String additionalMessage, ProtectionException protectionException)
    {
        if (mLogLevel.ordinal() < LogLevel.Verbose.ordinal())
            return;
        if (mAndroidLogEnabled)
        {
            if (protectionException != null)
            {
                Log.v(tag,
                        String.format("%s: %s. %s", protectionException.getType().name(), message, additionalMessage));
            }
            else
            {
                Log.v(tag, String.format("%s. %s", message, additionalMessage));
            }
        }
        if (mChildLogger != null)
        {
            try
            {
                mChildLogger.verbose(tag, message, additionalMessage, protectionException);
            }
            catch (Exception e)
            {
                logChildLoggerError(tag, e, message);
            }
        }
    }

    /**
     * Warn.
     * 
     * @param tag the tag
     * @param message the message
     * @param additionalMessage the additional message
     * @param protectionException the protection exception
     */
    public void warn(String tag, String message, String additionalMessage, ProtectionException protectionException)
    {
        if (mLogLevel.ordinal() < LogLevel.Warn.ordinal())
            return;
        if (mAndroidLogEnabled)
        {
            if (protectionException != null)
            {
                Log.w(tag,
                        String.format("%s: %s. %s", protectionException.getType().name(), message, additionalMessage));
            }
            else
            {
                Log.w(tag, String.format("%s. %s", message, additionalMessage));
            }
        }
        if (mChildLogger != null)
        {
            try
            {
                mChildLogger.warn(tag, message, additionalMessage, protectionException);
            }
            catch (Exception e)
            {
                logChildLoggerError(tag, e, message);
            }
        }
    }

    private void logChildLoggerError(String tag, Exception e, String message)
    {
        Log.w(tag, String.format("Child logger failed to log message: \"%s\". Error: %s", message, e.getMessage()));
    }
}

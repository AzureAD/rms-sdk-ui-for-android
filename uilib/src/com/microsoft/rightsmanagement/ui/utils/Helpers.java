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

import java.util.HashMap;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

public class Helpers
{
    public static final String TAG = "Helpers";

    /**
     * Creates a ValueAnimator for a specified view on the background color property
     * 
     * @param view view on which to apply animation
     * @param startColor color to start animating from
     * @param endColor color to end animation at
     * @return
     */
    public static ValueAnimator createBackgroundColorFaderAnimation(final View view,
                                                                   int startColor,
                                                                   int endColor)
    {
        ValueAnimator colorAnimationAtStart = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimationAtStart.addUpdateListener(new AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animator)
            {
                if (view != null)
                {
                    view.setBackgroundColor((Integer)animator.getAnimatedValue());
                }
            }
        });
        return colorAnimationAtStart;
    }

    /**
     * Creates a hash map.
     * 
     * @param keyType
     * @param valueType
     * @param alternateKeyValueArray
     * @return
     */
    public static <T, U> HashMap<T, U> createHashMap(Class<T> keyType,
                                                     Class<U> valueType,
                                                     Object... alternateKeyValueArray)
    {
        if (alternateKeyValueArray.length % 2 != 0)
        {
            throw new RuntimeException(
                    "createHashMap(,,alternateKeyValueArray) - alternateKeyValueArray length was not even");
        }
        HashMap<T, U> map = new HashMap<T, U>();
        for (int i = 0; i < (alternateKeyValueArray.length - 1); i += 2)
        {
            try
            {
                map.put(keyType.cast(alternateKeyValueArray[i]), valueType.cast(alternateKeyValueArray[i + 1]));
            }
            catch (ClassCastException e)
            {
                Logger.ie(TAG, String.format("incorrect key or value type class. Error: %s", e.getMessage()));
                return null;
            }
        }
        return map;
    }

    /**
     * Checks if String is null or empty.
     * 
     * @param string the string
     * @return true, if successful
     */
    public static boolean IsNullOrEmpty(String string)
    {
        if (string == null || string.trim().length() == 0)
        {
            return true;
        }
        return false;
    }

    /**
     * Make UI TextView a html link.
     * 
     * @param context the context
     * @param textView the text view
     * @param html the html containing link info
     */
    public static void makeTextViewAHTMLLink(final Context context, TextView textView, String html)
    {
        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = spannableStringBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (final URLSpan urlSpan : urls)
        {
            int start = spannableStringBuilder.getSpanStart(urlSpan);
            int end = spannableStringBuilder.getSpanEnd(urlSpan);
            int flags = spannableStringBuilder.getSpanFlags(urlSpan);
            ClickableSpan clickable = new ClickableSpan()
            {
                public void onClick(View view)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlSpan.getURL()));
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint textPaint)
                {
                    super.updateDrawState(textPaint);
                    textPaint.setUnderlineText(false);
                }
            };
            spannableStringBuilder.removeSpan(urlSpan);
            spannableStringBuilder.setSpan(clickable, start, end, flags);
        }
        textView.setText(spannableStringBuilder);
    }
}

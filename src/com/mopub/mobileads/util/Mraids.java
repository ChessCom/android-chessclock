/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import com.mopub.mobileads.MraidVideoPlayerActivity;
import com.mopub.mobileads.Utils;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.MEDIA_MOUNTED;

public class Mraids {
    public static final String ANDROID_CALENDAR_CONTENT_TYPE = "vnd.android.cursor.item/event";

    public static boolean isTelAvailable(Context context) {
        Intent telIntent = new Intent(Intent.ACTION_DIAL);
        telIntent.setData(Uri.parse("tel:"));

        return Utils.deviceCanHandleIntent(context, telIntent);
    }

    public static boolean isSmsAvailable(Context context) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:"));

        return Utils.deviceCanHandleIntent(context, smsIntent);
    }

    public static boolean isStorePictureSupported(Context context) {
        return MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && context.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCalendarAvailable(Context context) {
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT).setType(ANDROID_CALENDAR_CONTENT_TYPE);

        return VersionCode.currentApiLevel().isAtLeast(VersionCode.ICE_CREAM_SANDWICH)
                && Utils.deviceCanHandleIntent(context, calendarIntent);
    }

    public static boolean isInlineVideoAvailable(Context context) {
        Intent mraidVideoIntent = new Intent(context, MraidVideoPlayerActivity.class);

        return Utils.deviceCanHandleIntent(context, mraidVideoIntent);
    }
}

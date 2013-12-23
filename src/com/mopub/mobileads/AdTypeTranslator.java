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

package com.mopub.mobileads;

import java.util.*;

public class AdTypeTranslator {
    public static final String ADMOB_BANNER = "com.mopub.mobileads.GoogleAdMobBanner";
    public static final String ADMOB_INTERSTITIAL = "com.mopub.mobileads.GoogleAdMobInterstitial";
    public static final String MILLENNIAL_BANNER = "com.mopub.mobileads.MillennialBanner";
    public static final String MILLENNIAL_INTERSTITIAL = "com.mopub.mobileads.MillennialInterstitial";
    public static final String MRAID_BANNER = "com.mopub.mobileads.MraidBanner";
    public static final String MRAID_INTERSTITIAL = "com.mopub.mobileads.MraidInterstitial";
    public static final String HTML_BANNER = "com.mopub.mobileads.HtmlBanner";
    public static final String HTML_INTERSTITIAL = "com.mopub.mobileads.HtmlInterstitial";
    public static final String VAST_VIDEO_INTERSTITIAL = "com.mopub.mobileads.VastVideoInterstitial";
    private static Map<String, String> customEventNameForAdType = new HashMap<String, String>();

    static {
        customEventNameForAdType.put("admob_native_banner", ADMOB_BANNER);
        customEventNameForAdType.put("admob_full_interstitial", ADMOB_INTERSTITIAL);
        customEventNameForAdType.put("millennial_native_banner", MILLENNIAL_BANNER);
        customEventNameForAdType.put("millennial_full_interstitial", MILLENNIAL_INTERSTITIAL);
        customEventNameForAdType.put("mraid_banner", MRAID_BANNER);
        customEventNameForAdType.put("mraid_interstitial", MRAID_INTERSTITIAL);
        customEventNameForAdType.put("html_banner", HTML_BANNER);
        customEventNameForAdType.put("html_interstitial", HTML_INTERSTITIAL);
        customEventNameForAdType.put("vast_interstitial", VAST_VIDEO_INTERSTITIAL);
    }

    static String getAdNetworkType(String adType, String fullAdType) {
        String adNetworkType = "interstitial".equals(adType) ? fullAdType : adType;
        return adNetworkType != null ? adNetworkType : "unknown";
    }

    static String getCustomEventNameForAdType(MoPubView moPubView, String adType, String fullAdType) {
        if ("html".equals(adType) || "mraid".equals(adType)) {
            return isInterstitial(moPubView)
                   ? customEventNameForAdType.get(adType + "_interstitial")
                   : customEventNameForAdType.get(adType + "_banner");
        } else {
            return "interstitial".equals(adType)
                    ? customEventNameForAdType.get(fullAdType + "_interstitial")
                    : customEventNameForAdType.get(adType + "_banner");
        }
    }

    private static boolean isInterstitial(MoPubView moPubView) {
        return moPubView instanceof MoPubInterstitial.MoPubInterstitialView;
    }
}

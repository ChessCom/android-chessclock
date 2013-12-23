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

import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;

abstract class MraidProperty {
    private String sanitize(String str) {
        return (str != null) ? str.replaceAll("[^a-zA-Z0-9_,:\\s\\{\\}\\\'\\\"]", "") : "";
    }

    @Override
    public String toString() {
        return sanitize(toJsonPair());
    }

    public abstract String toJsonPair();
}

class MraidPlacementTypeProperty extends MraidProperty {
    private final PlacementType mPlacementType;

    MraidPlacementTypeProperty(PlacementType placementType) {
        mPlacementType = placementType;
    }

    public static MraidPlacementTypeProperty createWithType(
            PlacementType placementType) {
        return new MraidPlacementTypeProperty(placementType);
    }

    @Override
    public String toJsonPair() {
        return "placementType: '" + mPlacementType.toString().toLowerCase() + "'";
    }
}

class MraidScreenSizeProperty extends MraidProperty {
    private final int mScreenWidth;
    private final int mScreenHeight;

    MraidScreenSizeProperty(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    public static MraidScreenSizeProperty createWithSize(int width, int height) {
        return new MraidScreenSizeProperty(width, height);
    }

    @Override
    public String toJsonPair() {
        return "screenSize: { width: " + mScreenWidth + ", height: " + mScreenHeight + " }";
    }
}

class MraidStateProperty extends MraidProperty {
    private final ViewState mViewState;

    MraidStateProperty(ViewState viewState) {
        mViewState = viewState;
    }

    public static MraidStateProperty createWithViewState(ViewState viewState) {
        return new MraidStateProperty(viewState);
    }

    @Override
    public String toJsonPair() {
        return "state: '" + mViewState.toString().toLowerCase() + "'";
    }
}

class MraidViewableProperty extends MraidProperty {
    private final boolean mViewable;

    MraidViewableProperty(boolean viewable) {
        mViewable = viewable;
    }

    public static MraidViewableProperty createWithViewable(boolean viewable) {
        return new MraidViewableProperty(viewable);
    }

    @Override
    public String toJsonPair() {
        return "viewable: " + (mViewable ? "true" : "false");
    }
}

class MraidSupportsProperty extends MraidProperty{
    private boolean sms;
    private boolean tel;
    private boolean calendar;
    private boolean storePicture;
    private boolean inlineVideo;

    @Override
    public String toJsonPair() {
        return "supports: {" +
                "sms: " + String.valueOf(sms) + ", " +
                "tel: " + String.valueOf(tel) + ", " +
                "calendar: " + String.valueOf(calendar) + ", " +
                "storePicture: " + String.valueOf(storePicture) + ", " +
                "inlineVideo: " + String.valueOf(inlineVideo) + "}";
    }

    public MraidSupportsProperty withSms(boolean value) {
        sms = value;
        return this;
    }


    public MraidSupportsProperty withTel(boolean value) {
        tel = value;
        return this;
    }

    public MraidSupportsProperty withCalendar(boolean value) {
        calendar = value;
        return this;
    }

    public MraidSupportsProperty withStorePicture(boolean value) {
        storePicture = value;
        return this;
    }

    public MraidSupportsProperty withInlineVideo(boolean value) {
        inlineVideo = value;
        return this;
    }
}

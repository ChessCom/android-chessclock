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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

public class Json {
    public static Map<String, String> jsonStringToMap(String jsonParams) throws Exception {
        Map<String, String> jsonMap = new HashMap<String, String>();

        if (jsonParams == null || jsonParams.equals("")) return jsonMap;

        JSONObject jsonObject = (JSONObject) new JSONTokener(jsonParams).nextValue();
        Iterator<?> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            jsonMap.put(key, jsonObject.getString(key));
        }

        return jsonMap;
    }

    public static String mapToJsonString(Map<String, String> map) {
        if (map == null) {
            return "{}";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                builder.append(",");
            }
            builder.append("\"");
            builder.append(entry.getKey());
            builder.append("\":\"");
            builder.append(entry.getValue());
            builder.append("\"");
            first = false;
        }

        builder.append("}");
        return builder.toString();
    }

    public static String[] jsonArrayToStringArray(String jsonString) {
        jsonString = "{key:" + jsonString + "}";

        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONArray jsonArray = jsonObject.getJSONArray("key");

            String[] result = new String[jsonArray.length()];
            for (int i = 0; i < result.length; i++) {
                result[i] = jsonArray.getString(i);
            }

            return result;
        } catch (JSONException exception) {
            return new String[0];
        }
    }

}

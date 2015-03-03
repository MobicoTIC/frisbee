/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.api;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * An HttpStack that performs request over an {@link HttpClient}.
 */
public class GdgStack implements HttpStack {

    private static final String BASE_URL = "https://developers.google.com";
    private static final String DIRECTORY_URL = BASE_URL + "/groups/directory/";
    private static final String USER_AGENT = "GDG-Frisbee/0.1 (Android)";
    private static String mCsrfToken = null;

    private HttpStack mInnerStack;

    public GdgStack() {
        mInnerStack = new OkStack(); // SPDY+HTTP
    }

    private void acquireCsrfToken() {
        StringRequest csrfRequest = new StringRequest(DIRECTORY_URL, null, null);
        HashMap<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put(HTTP.USER_AGENT, USER_AGENT);

        try {
            HttpResponse response = performRequest(csrfRequest, additionalHeaders);

            if (response.getStatusLine().getStatusCode() == 200) {
                Header csrfCookie = response.getFirstHeader("Set-Cookie");
                if (csrfCookie != null && csrfCookie.getValue().contains("csrftoken")) {
                    Pattern pattern = Pattern.compile("csrftoken=([a-z0-9]{32})");
                    Matcher matcher = pattern.matcher(csrfCookie.getValue());
                    if (matcher.find()) {
                        mCsrfToken = matcher.group(1);
                        Timber.d("Got csrf token: " + mCsrfToken);
                    }
                }
            }
        } catch (IOException | AuthFailureError e) {
            e.printStackTrace();
        }
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {

        if (request.getMethod() == Request.Method.POST) {
            if (mCsrfToken == null) {
                acquireCsrfToken();
            }

            additionalHeaders.put("Cookie", "csrftoken=" + mCsrfToken);
            additionalHeaders.put("X-CSRFToken", mCsrfToken);
        }

        return mInnerStack.performRequest(request, additionalHeaders);
    }
}

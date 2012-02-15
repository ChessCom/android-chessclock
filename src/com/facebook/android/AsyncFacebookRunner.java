/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import android.content.Context;
import android.os.Bundle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * A sample implementation of asynchronous API requests.  This class provides
 * the ability to execute API methods and have the call return immediately,
 * without blocking the calling thread.  This is necessary when accessing the
 * API in the UI thread, for instance.  The request response is returned to
 * the caller via a callback interface, which the developer must implement.
 * <p/>
 * This sample implementation simply spawns a new thread for each request,
 * and makes the API call immediately.  This may work in many applications,
 * but more sophisticated users may re-implement this behavior using a thread
 * pool, a network thread, a request queue, or other mechanism.  Advanced
 * functionality could be built, such as rate-limiting of requests, as per
 * a specific application's needs.
 *
 * @author ssoneff@facebook.com
 * @see RequestListener
 *      The callback interface.
 */
public class AsyncFacebookRunner {

	Facebook fb;

	public AsyncFacebookRunner(Facebook fb) {
		this.fb = fb;
	}

	/**
	 * Invalidate the current user session by removing the access token in
	 * memory, clearing the browser cookies, and calling auth.expireSession
	 * through the API. The application will be notified when logout is
	 * complete via the callback interface.
	 * <p/>
	 * Note that this method is asynchronous and the callback will be invoked
	 * in a background thread; operations that affect the UI will need to be
	 * posted to the UI thread or an appropriate handler.
	 *
	 * @param context  The Android coreContext in which the logout should be called: it
	 *                 should be the same coreContext in which the login occurred in
	 *                 order to clear any stored cookies
	 * @param listener Callback interface to notify the application when the request
	 *                 has completed.
	 */
	public void logout(final Context context, final RequestListener listener) {
		new Thread() {
			@Override
			public void run() {
				try {
					String response = fb.logout(context);
					if (response.length() == 0 || response.equals("false")) {
						listener.onFacebookError(new FacebookError(
								"auth.expireSession failed"));
						return;
					}
					listener.onComplete(response);
				} catch (FileNotFoundException e) {
					listener.onFileNotFoundException(e);
				} catch (MalformedURLException e) {
					listener.onMalformedURLException(e);
				} catch (IOException e) {
					listener.onIOException(e);
				}
			}
		}.start();
	}

	/**
	 * Make a request to Facebook's old (pre-graph) API with the given
	 * parameters. One of the parameter keys must be "method" and its value
	 * should be a valid REST server API method.
	 * <p/>
	 * <p/>
	 * See http://developers.facebook.com/docs/reference/rest/
	 * <p/>
	 * Note that this method is asynchronous and the callback will be invoked
	 * in a background thread; operations that affect the UI will need to be
	 * posted to the UI thread or an appropriate handler.
	 * <p/>
	 * Example:
	 * <code>
	 * Bundle parameters = new Bundle();
	 * parameters.putString("method", "auth.expireSession", new Listener());
	 * String response = request(parameters);
	 * </code>
	 *
	 * @param parameters Key-value pairs of parameters to the request. Refer to the
	 *                   documentation: one of the parameters must be "method".
	 * @param listener   Callback interface to notify the application when the request
	 *                   has completed.
	 */
	public void request(Bundle parameters,
						RequestListener listener) {
		request(null, parameters, "GET", listener);
	}

	/**
	 * Make a request to the Facebook Graph API without any parameters.
	 * <p/>
	 * See http://developers.facebook.com/docs/api
	 * <p/>
	 * Note that this method is asynchronous and the callback will be invoked
	 * in a background thread; operations that affect the UI will need to be
	 * posted to the UI thread or an appropriate handler.
	 *
	 * @param graphPath Path to resource in the Facebook graph, e.g., to fetch data
	 *                  about the currently logged authenticated user, provide "me",
	 *                  which will fetch http://graph.facebook.com/me
	 * @param listener  Callback interface to notify the application when the request
	 *                  has completed.
	 */
	public void request(String graphPath,
						RequestListener listener) {
		request(graphPath, new Bundle(), "GET", listener);
	}

	/**
	 * Make a request to the Facebook Graph API with the given string parameters
	 * using an HTTP GET (default method).
	 * <p/>
	 * See http://developers.facebook.com/docs/api
	 * <p/>
	 * Note that this method is asynchronous and the callback will be invoked
	 * in a background thread; operations that affect the UI will need to be
	 * posted to the UI thread or an appropriate handler.
	 *
	 * @param graphPath  Path to resource in the Facebook graph, e.g., to fetch data
	 *                   about the currently logged authenticated user, provide "me",
	 *                   which will fetch http://graph.facebook.com/me
	 * @param parameters key-value string parameters, e.g. the path "search" with
	 *                   parameters "q" : "facebook" would produce a query for the
	 *                   following graph resource:
	 *                   https://graph.facebook.com/search?q=facebook
	 * @param listener   Callback interface to notify the application when the request
	 *                   has completed.
	 */
	public void request(String graphPath,
						Bundle parameters,
						RequestListener listener) {
		request(graphPath, parameters, "GET", listener);
	}

	/**
	 * Make a request to the Facebook Graph API with the given HTTP method and
	 * string parameters. Note that binary data parameters (e.g. pictures) are
	 * not yet supported by this helper function.
	 * <p/>
	 * See http://developers.facebook.com/docs/api
	 * <p/>
	 * Note that this method is asynchronous and the callback will be invoked
	 * in a background thread; operations that affect the UI will need to be
	 * posted to the UI thread or an appropriate handler.
	 *
	 * @param graphPath  Path to resource in the Facebook graph, e.g., to fetch data
	 *                   about the currently logged authenticated user, provide "me",
	 *                   which will fetch http://graph.facebook.com/me
	 * @param parameters key-value string parameters, e.g. the path "search" with
	 *                   parameters {"q" : "facebook"} would produce a query for the
	 *                   following graph resource:
	 *                   https://graph.facebook.com/search?q=facebook
	 * @param httpMethod http verb, e.g. "POST", "DELETE"
	 * @param listener   Callback interface to notify the application when the request
	 *                   has completed.
	 */
	public void request(final String graphPath,
						final Bundle parameters,
						final String httpMethod,
						final RequestListener listener) {
		new Thread() {
			@Override
			public void run() {
				try {
					String resp = fb.request(graphPath, parameters, httpMethod);
					listener.onComplete(resp);
				} catch (FileNotFoundException e) {
					listener.onFileNotFoundException(e);
				} catch (MalformedURLException e) {
					listener.onMalformedURLException(e);
				} catch (IOException e) {
					listener.onIOException(e);
				}
			}
		}.start();
	}


	/**
	 * Callback interface for API requests.
	 */
	public static interface RequestListener {

		/**
		 * Called when a request completes with the given response.
		 * <p/>
		 * Executed by a background thread: do not update the UI in this method.
		 */
		public void onComplete(String response);

		/**
		 * Called when a request has a network or request error.
		 * <p/>
		 * Executed by a background thread: do not update the UI in this method.
		 */
		public void onIOException(IOException e);

		/**
		 * Called when a request fails because the requested resource is
		 * invalid or does not exist.
		 * <p/>
		 * Executed by a background thread: do not update the UI in this method.
		 */
		public void onFileNotFoundException(FileNotFoundException e);

		/**
		 * Called if an invalid graph path is provided (which may result in a
		 * malformed URL).
		 * <p/>
		 * Executed by a background thread: do not update the UI in this method.
		 */
		public void onMalformedURLException(MalformedURLException e);

		/**
		 * Called when the server-side Facebook method fails.
		 * <p/>
		 * Executed by a background thread: do not update the UI in this method.
		 */
		public void onFacebookError(FacebookError e);

	}

}

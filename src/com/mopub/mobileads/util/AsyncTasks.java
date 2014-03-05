package com.mopub.mobileads.util;


import com.chess.backend.image_load.bitmapfun.AsyncTask;

public class AsyncTasks {
	public static <P> void safeExecuteOnExecutor(AsyncTask<P, ?, ?> asyncTask, P... params) throws Exception {
		if (asyncTask == null) {
			throw new IllegalArgumentException("Unable to execute null AsyncTask.");
		}

//		if (currentApiLevel().isAtLeast(ICE_CREAM_SANDWICH)) { // we already use smart asyncTask that do this check
//			Executor threadPoolExecutor = (Executor) AsyncTask.class.getField("THREAD_POOL_EXECUTOR").get(AsyncTask.class);
//
//			new MethodBuilder(asyncTask, "executeOnExecutor")
//					.addParam(Executor.class, threadPoolExecutor)
//					.addParam(Object[].class, params)
//					.execute();
//		} else {
		asyncTask.execute(params);
//		}
	}
}

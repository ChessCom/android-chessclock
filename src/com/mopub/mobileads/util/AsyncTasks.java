package com.mopub.mobileads.util;

import android.os.AsyncTask;

import java.util.concurrent.*;

import static com.mopub.mobileads.util.Reflection.MethodBuilder;
import static com.mopub.mobileads.util.VersionCode.ICE_CREAM_SANDWICH;
import static com.mopub.mobileads.util.VersionCode.currentApiLevel;

public class AsyncTasks {
    public static <P> void safeExecuteOnExecutor(AsyncTask<P, ?, ?> asyncTask, P... params) throws Exception {
        if (asyncTask == null) {
            throw new IllegalArgumentException("Unable to execute null AsyncTask.");
        }

        if (currentApiLevel().isAtLeast(ICE_CREAM_SANDWICH)) {
            Executor threadPoolExecutor = (Executor) AsyncTask.class.getField("THREAD_POOL_EXECUTOR").get(AsyncTask.class);

            new MethodBuilder(asyncTask, "executeOnExecutor")
                    .addParam(Executor.class, threadPoolExecutor)
                    .addParam(Object[].class, params)
                    .execute();
        } else {
            asyncTask.execute(params);
        }
    }
}

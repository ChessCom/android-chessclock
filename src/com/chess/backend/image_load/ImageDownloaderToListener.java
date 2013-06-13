package com.chess.backend.image_load;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.utilities.AppUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ImageDownloaderToListener {
    private static final String LOG_TAG = "EnhancedImageDownloader";
	private final Context context;
	private int imgSize;

	public enum Mode {
        NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT
    }

    private File cacheDir;

    private static Resources resources;

    public ImageDownloaderToListener(Context context) {
		this.context = context;
        resources = context.getResources();
        // Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), AppUtils.getApplicationCacheDir(context.getPackageName()));
        else
            cacheDir = context.getCacheDir();

        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    /**
     * Download the specified image from the Internet and binds it to the
     * provided ImageView. The binding is immediate if the image is found in the
     * cache and will be done asynchronously otherwise. A null bitmap will be
     * associated to the ImageView if an error occurs.
     *
	 * @param url    The URL of the image to download.
	 * @param holder The ImageView to bind the downloaded image to.
	 * @param imgSize size of image to be scaled
	 */
    public void download(String url, ImageReadyListener holder, int imgSize) {
		this.imgSize = imgSize;
		if (TextUtils.isEmpty(url)) {
			Log.e(LOG_TAG, " passed url is null. Don't start loading");
			return;
		}
		Bitmap bitmap = getBitmapFromCache(url, holder);
		Log.d(LOG_TAG, " download url = " + url);

        if (bitmap == null) {
            forceDownload(url, holder);
        }
    }
    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url, ImageReadyListener readyListener) {
        // I identify images by hashcode. Not a perfect solution, good for the
        // demo.
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);

        // from SD cache
        // if file is stored so simply read it, do not resize
		Bitmap bmp = readFile(f);
		if(bmp != null){
			readyListener.onImageReady(bmp);
			addBitmapToCache(url, bmp);
		}

        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap holder = sHardBitmapCache.get(url);
            if (holder != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, holder);
                return holder;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap holder = bitmapReference.get();
            if (holder != null) {
                // Bitmap found in soft cache
                return holder;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }

        return null;
    }

    /*
      * Same as download but the image is always downloaded and the cache is not
      * used. Kept private at the moment as its interest is not clear. private
      * void forceDownload(String url, ImageView view) { forceDownload(url, view,
      * null); }
      */

    /**
     * Same as download but the image is always downloaded and the cache is not
     * used. Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageReadyListener holder) {
        // State sanity: url is guaranteed to never be null in
        // DownloadedDrawable and cache keys.
        if (url == null) {
            return;
        }

		new BitmapDownloaderTask(holder).executeTask(url);
    }

    /*
      * An InputStream that skips the exact number of bytes provided, unless it
      * reaches EOF.
      */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

	/**
	 * Read file from stored hashlink on SD
	 *
	 * @param f file from which we read
	 * @return read Bitmap
	 */
	private Bitmap readFile(File f) {
		try {
			return BitmapFactory.decodeStream(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final SoftReference<ImageReadyListener> holderReference;

        public BitmapDownloaderTask(ImageReadyListener holder) {
            holderReference = new SoftReference<ImageReadyListener>(holder);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];

			return downloadBitmap(url);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
			if (holderReference == null || holderReference.get() == null) {
                return;
            }

			if (isCancelled() || context == null) { // if activity dead, escape
				return;
			}

            addBitmapToCache(url, bitmap);

			holderReference.get().onImageReady(bitmap);
        }

        public AsyncTask<String, Void, Bitmap> executeTask(String... input){
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
				executeOnExecutor(THREAD_POOL_EXECUTOR, input);
			} else {
				execute(input);
			}
            return this;
        }
    }

    private Bitmap downloadBitmap(String url) {
        // AndroidHttpClient is not allowed to be used from the main thread
        final HttpClient client = AndroidHttpClient.newInstance("Android");
        url = url.replace(" ", "%20");
		if (!url.startsWith(EnhancedImageDownloader.HTTP)) {
			url = EnhancedImageDownloader.HTTP_PREFIX + url;
		}
        final HttpGet getRequest = new HttpGet(url);

        try {
			if (RestHelper.IS_TEST_SERVER_MODE) {
//				getRequest.addHeader(RestHelper.AUTHORIZATION_HEADER, RestHelper.AUTHORIZATION_HEADER_VALUE);
			}

            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.e("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.

                    // create descriptor
                    String filename = String.valueOf(url.hashCode());
                    File f = new File(cacheDir, filename);

                    InputStream is = new URL(url).openStream();
                    // copy stream to file
                    OutputStream os = new FileOutputStream(f); // save stream to
                    // SD
                    AppUtils.copyStream(is, os);
                    os.close();

                    // decode image size
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(new FileInputStream(f), null, o);

                    // Find the correct scale value. It should be the power of 2.
                    int width_tmp = o.outWidth, height_tmp = o.outHeight;
                    int scale = 1;
                    while (true) {
                        if (width_tmp / 2 < imgSize || height_tmp / 2 < imgSize)
                            break;
                        width_tmp /= 2;
                        height_tmp /= 2;
                        scale *= 2;
                    }

                    // decode with inSampleSize
                    BitmapFactory.Options o2 = new BitmapFactory.Options();
                    o2.inSampleSize = scale;
                    return BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, o2);
//					return BitmapFactory.decodeStream(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        } catch (IOException e) {
            getRequest.abort();
            Log.e(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.e(LOG_TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            getRequest.abort();
            Log.e(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } finally {
            if ((client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
        }
        return null;
    }


    public static class EnhDownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public EnhDownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, ProgressImageView holder) {
            super(resources, holder.placeholder);

            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }

        @Override
        public int getOpacity() {
            return 0;
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }
    }

    /*
      * Cache-related fields and methods.
      *
      * We use a hard and a soft cache. A soft reference cache is too
      * aggressively cleared by the Garbage Collector.
      */

    private static final int HARD_CACHE_CAPACITY = 30;
    private static final int DELAY_BEFORE_PURGE = 120 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {


        @Override
        protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to
                // soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    /**
     * Adds this bitmap to the cache.
     *
     * @param holder The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap holder) {
        if (holder != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, holder);
            }
        }
    }

}

package com.chess.backend.image_load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.chess.utilities.AppUtils;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ImageDownloaderToListener {
    private static final String LOG_TAG = "EnhancedImageDownloader";
	private final Context context;
	private int imgSize;
	private int imgWidth;
	private int imgHeight;
	private boolean useScale;

	public enum Mode {
        NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT
    }

    private File cacheDir;

    public ImageDownloaderToListener(Context context) {
		this.context = context;
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
		useScale = true;
		if (TextUtils.isEmpty(url)) {
			Log.e(LOG_TAG, " passed url is null. Don't start loading");
			return;
		}
		Bitmap bitmap = getBitmapFromCache(url, holder);
		Log.d(LOG_TAG, "^ _________________________________ ^");
		Log.d(LOG_TAG, " download url = " + url);

        if (bitmap == null) {
            forceDownload(url, holder);
        }
    }

	/**
	 * Download the specified image from the Internet and binds it to the
	 * provided ImageView. The binding is immediate if the image is found in the
	 * cache and will be done asynchronously otherwise. A null bitmap will be
	 * associated to the ImageView if an error occurs.
	 *
	 * @param url    The URL of the image to download.
	 * @param holder The ImageView to bind the downloaded image to.
	 * @param imgWidth width of scaled image
	 * @param imgHeight height of scaled image
	 */
	public void download(String url, ImageReadyListener holder, int imgWidth, int imgHeight) {
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		useScale = false;
		if (TextUtils.isEmpty(url)) {
			Log.e(LOG_TAG, " passed url is null. Don't start loading");
			return;
		}
		Bitmap bitmap = getBitmapFromCache(url, holder);
		Log.d(LOG_TAG, "^ _________________________________ ^");
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
        ImageReadyListener holderReference;

        public BitmapDownloaderTask(ImageReadyListener holder) {
            holderReference = holder;
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
			Log.d(LOG_TAG, "onPostExecute bitmap " + bitmap +  " for url = " + url);

			if (holderReference == null /*|| holderReference.get() == null*/) {
				Log.d(LOG_TAG, "holderReference == null || holderReference.get() == null bitmap " + bitmap +  " for url = " + url);
                return;
            }

			if (isCancelled() || context == null) { // if activity dead, escape
				Log.d(LOG_TAG, "isCancelled() || context == null bitmap " + bitmap +  " for url = " + url);
				return;
			}

            addBitmapToCache(url, bitmap);

			holderReference.onImageReady(bitmap);
			Log.d(LOG_TAG, "onImageReady bitmap " + bitmap +  " for url = " + url);
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
		Log.d(LOG_TAG, "downloadBitmap start url = " + url);

		String filename = String.valueOf(url.hashCode());

		url = url.replace(" ", "%20");
		if (!url.startsWith(EnhancedImageDownloader.HTTP)) {
			url = EnhancedImageDownloader.HTTP_PREFIX + url;
		}
		try {
			// create descriptor
			File imgFile = new File(cacheDir, filename);

			InputStream is = new URL(url).openStream();
			// copy stream to imgFile
			OutputStream os = new FileOutputStream(imgFile); // save stream to
			// SD
			AppUtils.copyStream(is, os);
			os.close();

			if (useScale) {
				// Get the dimensions of the View
				int targetW = imgSize;
				int targetH = imgSize;

				// Get the dimensions of the bitmap
				BitmapFactory.Options bmOptions = new BitmapFactory.Options();
				bmOptions.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);
				int photoW = bmOptions.outWidth;
				int photoH = bmOptions.outHeight;

				// Determine how much to scale down the image
				int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

				// Decode the image imgFile into a Bitmap sized to fill the View
				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize = scaleFactor;
				bmOptions.inPurgeable = true;

				return BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);
			}
			// TODO adjust usage for width and height

			return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
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

package com.chess.backend.image_load;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.chess.utilities.AppUtils;

import java.io.*;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class EnhancedImageDownloader {
	private static final String LOG_TAG = "EnhancedImageDownloader";
	public static final String HTTP = "http";
	public static final String HTTP_PREFIX = "http:";
	private final Context context;
	private final Resources resources;
	private HashMap<String, Integer> imgSizeMap;
	private boolean useScale;

	public enum Mode {
        NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT
    }

    private File cacheDir;

    public EnhancedImageDownloader(Context context) {
		this.context = context;
		imgSizeMap = new HashMap<String, Integer>();
        resources = context.getResources();
		cacheDir = AppUtils.getCacheDir(context);
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
    public void download(String url, ProgressImageView holder, int imgSize) {
		imgSizeMap.put(url, imgSize);
		useScale = true;
		if (TextUtils.isEmpty(url)) {
			Log.e(LOG_TAG, " passed url is null. Don't start loading");
			return;
		}
		Bitmap bitmap = getBitmapFromCache(url, holder);
//		Log.d(LOG_TAG, "^ _________________________________ ^");
//		Log.d(LOG_TAG, " download url = " + url);

        if (bitmap == null) {
            forceDownload(url, holder);
        } else {
            cancelPotentialDownload(url, holder.getImageView());
            holder.setImageBitmap(bitmap);
            holder.progress.setVisibility(View.INVISIBLE);
        }
    }

    public void download(String url, ProgressImageView holder, int imgSize, int imgHeight) {
		imgSizeMap.put(url, imgSize);
//		this.imgSize = imgSize;

		useScale = false;
		if (TextUtils.isEmpty(url)) {
			Log.e(LOG_TAG, " passed url is null. Don't start loading");
			return;
		}
		Bitmap bitmap = getBitmapFromCache(url, holder);
//		Log.d(LOG_TAG, "^ _________________________________ ^");
//		Log.d(LOG_TAG, " download url = " + url);

        if (bitmap == null) {
            forceDownload(url, holder);
        } else {
            cancelPotentialDownload(url, holder.getImageView());
            holder.setImageBitmap(bitmap);
            holder.progress.setVisibility(View.INVISIBLE);
        }
    }
    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url, ProgressImageView pHolder) {
        // I identify images by hashcode. Not a perfect solution, good for the
        // demo.
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);

        // from SD cache
        // if file is stored so simply read it, do not resize
		Bitmap bmp = readFile(f);
		if(bmp != null){
			pHolder.setBitmap(bmp);
			addBitmapToCache(url, pHolder);
		}

        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final ProgressImageView holder = sHardBitmapCache.get(url);
            if (holder != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, holder);
                return holder.getBitmap();
            }
        }

        // Then try the soft reference cache
        SoftReference<ProgressImageView> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final ProgressImageView holder = bitmapReference.get();
            if (holder != null) {
                // Bitmap found in soft cache
                return holder.getBitmap();
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
    private void forceDownload(String url, ProgressImageView holder) {
        // State sanity: url is guaranteed to never be null in
        // DownloadedDrawable and cache keys.
        if (url == null) {
            holder.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, holder.getImageView())) {
            BitmapDownloaderTask task;

			task = new BitmapDownloaderTask(holder);
			EnhDownloadedDrawable enhDownloadedDrawable = new EnhDownloadedDrawable(task, holder, resources);
			holder.getImageView().setImageDrawable(enhDownloadedDrawable);
			holder.getImageView().setMinimumHeight(50);
			task.executeTask(url);
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no
     * download in progress on this image view. Returns false if the download in
     * progress deals with the same url. The download is not stopped in that
     * case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated
     *         with this imageView. null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof EnhDownloadedDrawable) {
                EnhDownloadedDrawable downloadedDrawable = (EnhDownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

	/**
	 * Read file from stored hashlink on SD
	 *
	 * @param f file from which we read
	 * @return read Bitmap or null, if file wasn't cached on storage
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
        private final WeakReference<ProgressImageView> holderReference;

        public BitmapDownloaderTask(ProgressImageView holder) {
            holderReference = new WeakReference<ProgressImageView>(holder);
        }

        @Override
        protected void onPreExecute() {
            holderReference.get().progress.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            Bitmap bmp = downloadBitmap(url);

            if (bmp == null) { // in case http link was wrong
                if (holderReference != null && holderReference.get() != null && holderReference.get().noImage != null)
                    bmp = holderReference.get().noImage; // set no image if we didn't load
            }

            return bmp;
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
			Log.d(LOG_TAG, "onPostExecute bitmap " + bitmap +  " for url = " + url);
            if (holderReference == null || holderReference.get() == null) {
//				Log.d(LOG_TAG, "holderReference == null || holderReference.get() == null bitmap " + bitmap +  " for url = " + url);
                return;
            } else {
                holderReference.get().progress.setVisibility(View.GONE);
            }

            if (isCancelled()) {
//				Log.d(LOG_TAG, "isCancelled bitmap " + bitmap +  " for url = " + url);
                bitmap = null;
            }

			if (context == null) { // if activity dead, escape
//				Log.d(LOG_TAG, "context == null bitmap " + bitmap +  " for url = " + url);
				return;
			}
            ProgressImageView holder = holderReference.get();

            holder.setBitmap(bitmap);

            addBitmapToCache(url, holder);

            holder.setImageView(holderReference.get().getImageView());

            BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(holder.getImageView());

            // Change bitmap only if this process is still associated with it
            if (this == bitmapDownloaderTask) {
                holder.updateImageBitmap();
            }
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
		String originalUrl = url;

		String filename = String.valueOf(url.hashCode());

        url = url.replace(" ", "%20");
		if (!url.startsWith(HTTP)) {
			url = EnhancedImageDownloader.HTTP_PREFIX + url;
		}

		try {
			// create descriptor
			File imgFile = new File(cacheDir, filename);

			InputStream is = new URL(url).openStream();
			// copy stream to file
			OutputStream os = new FileOutputStream(imgFile); // save stream to
			// SD
			AppUtils.copyStream(is, os);
			os.close();

			if (useScale) {
				// Get the dimensions of the View
				int targetW = imgSizeMap.get(originalUrl);
				int targetH = imgSizeMap.get(originalUrl);

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


    public static class EnhDownloadedDrawable extends BitmapDrawable {

        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public EnhDownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, ProgressImageView holder, Resources resources) {
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
    private final HashMap<String, ProgressImageView> sHardBitmapCache = new LinkedHashMap<String, ProgressImageView>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        /**
         *
         */
        private static final long serialVersionUID = 7891177567092447801L;

        @Override
        protected boolean removeEldestEntry(Entry<String, ProgressImageView> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to
                // soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<ProgressImageView>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<ProgressImageView>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<ProgressImageView>>(HARD_CACHE_CAPACITY / 2);

    /**
     * Adds this bitmap to the cache.
     *
     * @param holder The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, ProgressImageView holder) {
        if (holder != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, holder);
            }
        }
    }

}

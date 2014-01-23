package com.chess.backend.image_load.bitmapfun;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import com.chess.BuildConfig;
import com.chess.backend.RestHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.10.13
 * Time: 10:27
 */
public class SmartImageFetcher extends ImageFetcher {

	private static final String TAG = "ImageFetcher";
	private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
	private static final String HTTP_CACHE_DIR = "http";
	private static final int IO_BUFFER_SIZE = 8 * 1024;
	public static final String HTTP = "http";
	public static final String HTTP_PREFIX = "http:";

	private DiskLruCache mHttpDiskCache;
	private File mHttpCacheDir;
	private boolean mHttpDiskCacheStarting = true;
	private final Object mHttpDiskCacheLock = new Object();
	private static final int DISK_CACHE_INDEX = 0;

	/**
	 * Initialize providing a target image width and height for the processing images.
	 *
	 * @param context
	 */
	public SmartImageFetcher(Context context) {
		super(context, 0);
		init(context);
	}


	private void init(Context context) {
		checkConnection(context);
		mHttpCacheDir = ImageCache.getDiskCacheDir(context, HTTP_CACHE_DIR);
	}

	@Override
	protected void initDiskCacheInternal() {
		super.initDiskCacheInternal();
		initHttpDiskCache();
	}

	private void initHttpDiskCache() {
		if (!mHttpCacheDir.exists()) {
			mHttpCacheDir.mkdirs();
		}
		synchronized (mHttpDiskCacheLock) {
			if (ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE) {
				try {
					mHttpDiskCache = DiskLruCache.open(mHttpCacheDir, 1, 1, HTTP_CACHE_SIZE);
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "HTTP cache initialized");
					}
				} catch (IOException e) {
					mHttpDiskCache = null;
				}
			}
			mHttpDiskCacheStarting = false;
			mHttpDiskCacheLock.notifyAll();
		}
	}

	@Override
	protected void clearCacheInternal() {
		super.clearCacheInternal();
		synchronized (mHttpDiskCacheLock) {
			if (mHttpDiskCache != null && !mHttpDiskCache.isClosed()) {
				try {
					mHttpDiskCache.delete();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "HTTP cache cleared");
					}
				} catch (IOException e) {
					Log.e(TAG, "clearCacheInternal - " + e);
				}
				mHttpDiskCache = null;
				mHttpDiskCacheStarting = true;
				initHttpDiskCache();
			}
		}
	}

	@Override
	protected void flushCacheInternal() {
		super.flushCacheInternal();
		synchronized (mHttpDiskCacheLock) {
			if (mHttpDiskCache != null) {
				try {
					mHttpDiskCache.flush();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "HTTP cache flushed");
					}
				} catch (IOException e) {
					Log.e(TAG, "flush - " + e);
				}
			}
		}
	}

	@Override
	protected void closeCacheInternal() {
		super.closeCacheInternal();
		synchronized (mHttpDiskCacheLock) {
			if (mHttpDiskCache != null) {
				try {
					if (!mHttpDiskCache.isClosed()) {
						mHttpDiskCache.close();
						mHttpDiskCache = null;
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "HTTP cache closed");
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "closeCacheInternal - " + e);
				}
			}
		}
	}

	/**
	 * Simple network connection check.
	 *
	 * @param context
	 */
	private void checkConnection(Context context) {
		final ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
			Log.e(TAG, "checkConnection - no connection found");
		}
	}

	/**
	 * The main process method, which will be called by the ImageWorker in the AsyncTask background
	 * thread.
	 *
	 * @param data The data to load the bitmap, in this case, a regular http URL. And perform resizing
	 * @return The downloaded and resized bitmap
	 */
	private Bitmap processBitmap(Data data) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "processBitmap - " + data);
		}

		final String key = ImageCache.hashKeyForDisk(data.getUrl());
		FileDescriptor fileDescriptor = null;
		FileInputStream fileInputStream = null;
		DiskLruCache.Snapshot snapshot;
		synchronized (mHttpDiskCacheLock) {
			// Wait for disk cache to initialize
			while (mHttpDiskCacheStarting) {
				try {
					mHttpDiskCacheLock.wait();
				} catch (InterruptedException ignored) {}
			}

			if (mHttpDiskCache != null) {
				try {
					snapshot = mHttpDiskCache.get(key);
					if (snapshot == null) {
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "processBitmap, not found in http cache, downloading...");
						}
						DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
						if (editor != null) {
							if (downloadUrlToStream(data.getUrl(), editor.newOutputStream(DISK_CACHE_INDEX))) {
								editor.commit();
							} else {
								editor.abort();
							}
						}
						snapshot = mHttpDiskCache.get(key);
					}
					if (snapshot != null) {
						fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
						fileDescriptor = fileInputStream.getFD();
					}
				} catch (IOException e) {
					Log.e(TAG, "processBitmap - " + e);
				} catch (IllegalStateException e) {
					Log.e(TAG, "processBitmap - " + e);
				} finally {
					if (fileDescriptor == null && fileInputStream != null) {
						try {
							fileInputStream.close();
						} catch (IOException ignored) {}
					}
				}
			}
		}

		Bitmap bitmap = null;
		if (fileDescriptor != null) {
			bitmap = decodeSampledBitmapFromDescriptor(fileDescriptor, data.getImageWidth(), data.getImageHeight(),
					getImageCache());
		}
		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (IOException ignored) {}
		}
		return bitmap;
	}

	@Override
	protected Bitmap processBitmap(Object data) {
		return processBitmap((Data)data);
	}

	/**
	 * Download a bitmap from a URL and write the content to an output stream.
	 *
	 * @param urlString The URL to fetch
	 * @return true if successful, false otherwise
	 */
	@Override
	public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
		disableConnectionReuseIfNecessary();
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		urlString = urlString.replace(" ", "%20");
		if (!urlString.startsWith(HTTP)) {
			urlString = HTTP_PREFIX + urlString;
		}
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(RestHelper.TIME_OUT);
			urlConnection.setReadTimeout(RestHelper.TIME_OUT);

			in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final IOException e) {
			Log.e(TAG, "Error in downloadBitmap - " + e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException ignored) {}
		}
		return false;
	}

	/**
	 * Workaround for bug pre-Froyo, see here for more info:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 */
	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public static class Data{
		String url;
		int imageWidth;
		int imageHeight;

		public Data(String url, int imageSize) {
			this.url = url;
			this.imageWidth = imageSize;
			this.imageHeight = imageSize;
		}

		public Data(String url, int imageWidth, int imageHeight) {
			this.url = url;
			this.imageWidth = imageWidth;
			this.imageHeight = imageHeight;
		}

		public String getUrl() {
			return url != null ? url : "http://d1lalstwiwz2br.cloudfront.net/images/noavatar_l.gif";
		}

		public int getImageWidth() {
			return imageWidth;
		}

		public int getImageHeight() {
			return imageHeight;
		}

		@Override
		public String toString() {
			return getUrl();
		}
	}
}

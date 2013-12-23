package com.mopub.mobileads;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.util.Log;
import com.mopub.mobileads.util.Files;
import com.mopub.mobileads.util.Streams;

import java.io.*;

/*
 * Please use putStream, getUri, and removeStream (instead of put, get, and remove).
 * The original methods do not perform necessary hashing of fileNames
 */
public class DiskLruCache extends LruCache<String, File> {
    private final Context mContext;
    private final String mCacheDirectoryName;
    private final File mCacheDirectory;

    public DiskLruCache(Context context, String cacheDirectoryName, int maxSizeBytes) throws IllegalArgumentException, IOException {
        super(maxSizeBytes);

        if (context == null) {
            throw new IllegalArgumentException("context may not be null.");
        } else if (cacheDirectoryName == null) {
            throw new IllegalArgumentException("cacheDirectoryName may not be null.");
        } else if (maxSizeBytes < 0) {
            throw new IllegalArgumentException("maxSizeBytes must be positive.");
        }

        mContext = context;
        mCacheDirectoryName = cacheDirectoryName;
        mCacheDirectory = Files.createDirectory(context.getFilesDir() + File.separator + mCacheDirectoryName);

        if (mCacheDirectory == null) {
            throw new IOException("Unable to obtain access to directory " + mCacheDirectoryName);
        }

        loadFilesFromDisk();
    }

    File getCacheDirectory() {
        return mCacheDirectory;
    }

    Uri getUri(final String key) {
        File value = get(Utils.sha1(key));

        if (value == null) {
            return null;
        }

        return Uri.parse(value.getAbsolutePath());
    }

    synchronized boolean putStream(final String fileName, final InputStream content) {
        if (fileName == null || content == null) {
            return false;
        }

        String hashedFileName = Utils.sha1(fileName);

        if (getUri(hashedFileName) != null) {
            return false;
        }

        File file = createFile(hashedFileName, content);

        if (file == null || !file.exists()) {
            return false;
        }

        put(hashedFileName, file);
        return true;
    }

    synchronized File removeStream(final String fileName) {
        if (fileName == null) {
            return null;
        }

        return remove(Utils.sha1(fileName));
    }

    private File createFile(String fileName, InputStream content) {
        File file = new File(mContext.getFilesDir() + File.separator + mCacheDirectoryName + File.separator + fileName);

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }

        try {
            Streams.copyContent(content, fileOutputStream);
        } catch (IOException e) {
            file.delete();
            return null;
        } finally {
            Streams.closeStream(fileOutputStream);
        }

        return file;
    }

    private void loadFilesFromDisk() {
        File[] allFiles = mCacheDirectory.listFiles();

        if (allFiles != null) {
            for (final File file : allFiles) {
                put(file.getName(), file);
            }
        }
    }

    /*
     * From android.support.v4.util.LruCache
     */

    @Override
    protected void entryRemoved(final boolean evicted, final String key, final File oldValue, final File newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        if (oldValue != null) {
            if (!oldValue.delete()) {
                Log.d("MoPub", "Unable to delete file from cache: " + oldValue.getName());
            }
        }
    }

    @Override
    protected int sizeOf(String key, File value) {
        if (value != null && value.exists() && value.length() > 0) {
            return Files.intLength(value);
        }

        return super.sizeOf(key, value);
    }
}


package com.tribe.app.data.network.util;

public interface DownloadProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
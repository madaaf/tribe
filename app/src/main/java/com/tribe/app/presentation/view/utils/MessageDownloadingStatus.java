package com.tribe.app.presentation.view.utils;

import android.support.annotation.StringDef;

/**
 * Created by tiago on 10/07/2016.
 */
public class MessageDownloadingStatus {

    public static final String STATUS_TO_DOWNLOAD = "to_download";
    public static final String STATUS_DOWNLOADING = "downloading";
    public static final String STATUS_DOWNLOADED = "downloaded";
    public static final String STATUS_DOWNLOAD_ERROR = "download_error";

    @StringDef({STATUS_TO_DOWNLOAD, STATUS_DOWNLOADING, STATUS_DOWNLOADED, STATUS_DOWNLOAD_ERROR})
    public @interface Status{}
}

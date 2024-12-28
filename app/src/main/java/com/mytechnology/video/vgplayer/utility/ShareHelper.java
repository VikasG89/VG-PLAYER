package com.mytechnology.video.vgplayer.utility;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

public class ShareHelper {
    private final Context context;

    public ShareHelper(Context context) {
        this.context = context;
    }

    public void shareVideo(String filePath) {
        File file = new File(filePath);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        ClipData clipData = ClipData.newUri(context.getContentResolver(), "Video", uri);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.setClipData(clipData);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ((Activity) context).startActivityForResult(intent, 222333);
    }
}

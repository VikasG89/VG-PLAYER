package com.mytechnology.video.vgplayer.utility;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FileUpdater {

    public static boolean deleteVideoFile(Context context, String videoFilePath) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{videoFilePath};

        try (Cursor cursor = contentResolver.query(uri, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                Uri deleteUri = ContentUris.withAppendedId(uri, id);

                int rowsDeleted = contentResolver.delete(deleteUri, null, null);
                if (rowsDeleted > 0) {
                    System.out.println("Video file deleted successfully.");
                    return true;
                } else {
                    System.out.println("Failed to delete video file.");
                }
            } else {
                System.out.println("Video file not found.");
            }
        } catch (Exception e) {
            Log.e("Error Delete", " " + e.getLocalizedMessage());
            System.out.println("An error occurred.");
        }
        return false;
    }


    public static boolean renameFile(Context context, String oldFilePath, String newFileName) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.MediaColumns.DATA + "=?";
        String[] selectionArgs = new String[]{oldFilePath};

        try (Cursor cursor = contentResolver.query(uri, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                Uri updateUri = ContentUris.withAppendedId(uri, id);

                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName);

                int rowsUpdated = contentResolver.update(updateUri, contentValues, null, null);
                if (rowsUpdated > 0) {
                    System.out.println("File renamed successfully.");
                    return true;
                } else {
                    System.out.println("Failed to rename file.");
                }
            } else {
                System.out.println("File not found.");
            }
        } catch (Exception e) {
            Log.e("Error ReName", " " + e.getLocalizedMessage());
            System.out.println("An error occurred.");
        }
        return false;
    }


}


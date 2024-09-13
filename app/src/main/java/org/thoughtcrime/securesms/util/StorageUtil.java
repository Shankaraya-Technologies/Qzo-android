package org.thoughtcrime.securesms.util;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.NoExternalStorageException;
import org.thoughtcrime.securesms.dependencies.AppDependencies;
import org.thoughtcrime.securesms.permissions.PermissionCompat;
import org.thoughtcrime.securesms.permissions.Permissions;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class StorageUtil {

  public static File getOrCreateBackupDirectory() throws NoExternalStorageException {
    File storage = Environment.getExternalStorageDirectory();

    if (!storage.canWrite()) {
      throw new NoExternalStorageException();
    }

    File backups = getBackupDirectory();

    if (!backups.exists()) {
      if (!backups.mkdirs()) {
        throw new NoExternalStorageException("Unable to create backup directory...");
      }
    }

    return backups;
  }

  public static File getOrCreateBackupV2Directory() throws NoExternalStorageException {
    File storage = Environment.getExternalStorageDirectory();

    if (!storage.canWrite()) {
      throw new NoExternalStorageException();
    }

    File backups = getBackupV2Directory();

    if (!backups.exists()) {
      if (!backups.mkdirs()) {
        throw new NoExternalStorageException("Unable to create backup directory...");
      }
    }

    return backups;
  }

  public static File getBackupDirectory() throws NoExternalStorageException {
    File storage = Environment.getExternalStorageDirectory();
    File signal  = new File(storage, getBackupDirectoryName());
    return new File(signal, "Backups");
  }

  public static File getBackupV2Directory() throws NoExternalStorageException {
    File storage = Environment.getExternalStorageDirectory();
    return new File(storage, getBackupDirectoryName());
  }

  private static String getBackupDirectoryName() {
    return AppDependencies.getApplication()
                          .getString(R.string.app_name)
                          .replace(" Staging", ".staging");
  }

  public static File getTileCacheDirectory(@NonNull Context context) {
    return new File(context.getCacheDir(), "tiles");
  }

  @RequiresApi(24)
  public static @NonNull String getDisplayPath(@NonNull Context context, @NonNull Uri uri) {
    String lastPathSegment = Objects.requireNonNull(uri.getLastPathSegment());
    String backupVolume    = lastPathSegment.replaceFirst(":.*", "");
    String backupName      = lastPathSegment.replaceFirst(".*:", "");

    StorageManager      storageManager = ServiceUtil.getStorageManager(context);
    List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
    StorageVolume       storageVolume  = null;

    for (StorageVolume volume : storageVolumes) {
      if (Objects.equals(volume.getUuid(), backupVolume)) {
        storageVolume = volume;
        break;
      }
    }

    if (storageVolume == null) {
      return backupName;
    } else {
      return context.getString(R.string.StorageUtil__s_s, storageVolume.getDescription(context), backupName);
    }
  }

  private static File getSignalStorageDir() throws NoExternalStorageException {
    final File storage = Environment.getExternalStorageDirectory();

    if (!storage.canWrite()) {
      throw new NoExternalStorageException();
    }

    return storage;
  }

  public static boolean canWriteInSignalStorageDir() {
    File storage;

    try {
      storage = getSignalStorageDir();
    } catch (NoExternalStorageException e) {
      return false;
    }

    return storage.canWrite();
  }

  public static boolean canWriteToMediaStore() {
    return Build.VERSION.SDK_INT > 28 ||
           Permissions.hasAll(AppDependencies.getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
  }

  public static boolean canReadAnyFromMediaStore() {
    return Permissions.hasAny(AppDependencies.getApplication(), PermissionCompat.forImagesAndVideos());
  }

  public static boolean canOnlyReadSelectedMediaStore() {
    return Build.VERSION.SDK_INT >= 34 &&
           Permissions.hasAll(AppDependencies.getApplication(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) &&
           !Permissions.hasAny(AppDependencies.getApplication(), Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO);
  }

  public static boolean canReadAllFromMediaStore() {
    return Permissions.hasAll(AppDependencies.getApplication(), PermissionCompat.forImagesAndVideos());
  }

  public static @NonNull Uri getVideoUri() {
    return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
  }

  public static @NonNull Uri getAudioUri() {
    return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
  }

  public static @NonNull Uri getImageUri() {
    return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  }

  public static @NonNull Uri getDownloadUri() {
    if (Build.VERSION.SDK_INT < 29) {
      return getLegacyUri(Environment.DIRECTORY_DOWNLOADS);
    } else {
      return MediaStore.Downloads.EXTERNAL_CONTENT_URI;
    }
  }

  public static @NonNull Uri getLegacyUri(@NonNull String directory) {
    return Uri.fromFile(Environment.getExternalStoragePublicDirectory(directory));
  }

  public static @Nullable String getCleanFileName(@Nullable String fileName) {
    if (fileName == null) return null;

    fileName = fileName.replace('\u202D', '\uFFFD');
    fileName = fileName.replace('\u202E', '\uFFFD');

    return fileName;
  }
}

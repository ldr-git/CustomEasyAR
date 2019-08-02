package com.easytargetar;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Objects;

public class FileHelper {

    private static final String TAG = FileHelper.class.getSimpleName();

    public static enum Path {
        TRAILS,
        HOME,
        JSON,
        RESOURCE
    }

    public static boolean isExist(File file) {
        return file != null ? file.exists() : false;
    }

    public static boolean isExist(Path path, String... parameters) {
        return isExist(createFile(path, parameters));
    }

    public static boolean isFileExist(@NonNull String filePath) {
        return new File(filePath).exists();
    }

    public static boolean delete(File file) {
        return file.delete();
    }

    public static boolean delete(String filePath) {
        return deleteRecursive(new File(filePath));
    }

    public static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        if (fileOrDirectory.delete()) {
            Log.d(TAG, fileOrDirectory.getName() + " deleting");
            return true;
        } else {
            return false;
        }
    }

    public static File getFile(Path path, String... parameters) {
        return createFile(path, parameters);
    }

    public static File createFile(Path path, String... parameters) {
        Context context = SampleApp.getContext();
        return new File(getPath(context, path, parameters));
    }

    public static File createFileDirectory(Path path, String... parameters) {
        Context context = SampleApp.getContext();
        File fileDirectory = new File(getPath(context, path, parameters));
        fileDirectory.mkdirs();
        return fileDirectory;
    }

    public static File createFile(Path path) {
        Context context = SampleApp.getContext();
        return new File(getPath(context, path));
    }

    public static String getPath(Context activity, Path path) {
        return getApplicationDirectory(activity, path.name().toLowerCase());
    }

    public static String getPath(Context activity, Path path, String... parameters) {
        StringBuilder builder = new StringBuilder();
        builder.append(getApplicationDirectory(activity, path.name().toLowerCase()));
        for (String param : parameters) {
            builder.append(File.separator);
            builder.append(param);
        }
        return builder.toString();
    }

    public static String getPath(Path path, String... parameters) {
        Context context = SampleApp.getContext();
        StringBuilder builder = new StringBuilder();
        builder.append(getApplicationDirectory(context, path.name().toLowerCase()));
        for (String param : parameters) {
            builder.append(File.separator);
            builder.append(param);
        }
        return builder.toString();
    }

    public static String getApplicationDirectory(Context activity, String path) {
        if (BuildConfig.DEBUG) {
            return Objects.requireNonNull(activity.getExternalFilesDir(path)).getAbsolutePath();
        } else {
            File applicationFolder = new File(activity.getFilesDir() + File.separator + path);
            if (!applicationFolder.exists()) {
                applicationFolder.mkdir();
            }
            return activity.getFilesDir() + File.separator + path;
        }
    }

    public static String readJson(String path) {
        Log.d(TAG, "readJson: " + path);
        File file = FileHelper.getFile(Path.HOME, path);
        return readJsonFromFile(file);
    }

    //Json
    public static String readJsonFromFile(File file) {
        Log.d(TAG, "readJsonFromFile : " + file.getAbsolutePath());
        String json = "";
        if (file.exists()) {
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            try {
                FileInputStream in = new FileInputStream(file);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }
                json = new String(bytes);
                return json;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public static String readJsonFromFilePath(String filePath) {
        Log.d(TAG, "readJsonFromFilePath : " + filePath);
        File file = new File(filePath);
        String json = "";
        if (file.exists()) {
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            try {
                FileInputStream in = new FileInputStream(file);
                try {
                    in.read(bytes);
                } finally {
                    in.close();
                }
                json = new String(bytes);
                return json;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public static void saveJson(File file, String data) {
        try {
            if (file.exists() && file.delete()) {
                Log.d(TAG, "File exists & deleted");
            }
            FileUtils.write(file, data, Charset.forName("UTF-8"));
            Log.d(TAG, "File saved : " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveJson(File file, String data, @NonNull OnHelperListener listener) {
        try {
            if (file.exists() && file.delete()) {
                Log.d(TAG, "File exists & deleted");
            }
            FileUtils.write(file, data, Charset.forName("UTF-8"));
            Log.d(TAG, "File saved : " + file.getAbsolutePath());
            listener.onFinish();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError();
        }
    }

    //Listener

    public interface BaseOnHelperListener {
        void onFinish();

        void onError();
    }

    public static class OnHelperListener implements BaseOnHelperListener {

        @Override
        public void onFinish() {

        }

        @Override
        public void onError() {

        }
    }

}

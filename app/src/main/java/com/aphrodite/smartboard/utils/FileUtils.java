package com.aphrodite.smartboard.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * 文件工具类
 * Created by frank on 2018/5/9.
 */

public class FileUtils {

    private final static String TYPE_MP3 = "mp3";
    private final static String TYPE_AAC = "aac";
    private final static String TYPE_AMR = "amr";
    private final static String TYPE_FLAC = "flac";
    private final static String TYPE_M4A = "m4a";
    private final static String TYPE_WMA = "wma";
    private final static String TYPE_WAV = "wav";
    private final static String TYPE_OGG = "ogg";
    private final static String TYPE_AC3 = "ac3";

    public final static String TYPE_MP4 = "mp4";
    private final static String TYPE_MKV = "mkv";
    private final static String TYPE_WEBM = "webm";
    private final static String TYPE_AVI = "avi";
    private final static String TYPE_WMV = "wmv";
    private final static String TYPE_FLV = "flv";
    private final static String TYPE_TS = "ts";
    private final static String TYPE_M3U8 = "m3u8";
    private final static String TYPE_3GP = "3gp";
    private final static String TYPE_MOV = "mov";
    private final static String TYPE_MPG = "mpg";

    public static boolean concatFile(String srcFilePath, String appendFilePath, String concatFilePath) {
        if (TextUtils.isEmpty(srcFilePath)
                || TextUtils.isEmpty(appendFilePath)
                || TextUtils.isEmpty(concatFilePath)) {
            return false;
        }
        File srcFile = new File(srcFilePath);
        if (!srcFile.exists()) {
            return false;
        }
        File appendFile = new File(appendFilePath);
        if (!appendFile.exists()) {
            return false;
        }
        FileOutputStream outputStream = null;
        FileInputStream inputStream1 = null, inputStream2 = null;
        try {
            inputStream1 = new FileInputStream(srcFile);
            inputStream2 = new FileInputStream(appendFile);
            outputStream = new FileOutputStream(new File(concatFilePath));
            byte[] data = new byte[1024];
            int len;
            while ((len = inputStream1.read(data)) > 0) {
                outputStream.write(data, 0, len);
            }
            outputStream.flush();
            while ((len = inputStream2.read(data)) > 0) {
                outputStream.write(data, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream1 != null) {
                    inputStream1.close();
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return 文件是否存在
     */
    public static boolean checkFileExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            Log.e("FileUtil", path + " is not exist!");
            return false;
        }
        return true;
    }

    public static boolean isAudio(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(TYPE_MP3)
                || path.endsWith(TYPE_AAC)
                || path.endsWith(TYPE_AMR)
                || path.endsWith(TYPE_FLAC)
                || path.endsWith(TYPE_M4A)
                || path.endsWith(TYPE_WMA)
                || path.endsWith(TYPE_WAV)
                || path.endsWith(TYPE_OGG)
                || path.endsWith(TYPE_AC3);
    }

    public static boolean isVideo(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(TYPE_MP4)
                || path.endsWith(TYPE_MKV)
                || path.endsWith(TYPE_WEBM)
                || path.endsWith(TYPE_WMV)
                || path.endsWith(TYPE_AVI)
                || path.endsWith(TYPE_FLV)
                || path.endsWith(TYPE_3GP)
                || path.endsWith(TYPE_TS)
                || path.endsWith(TYPE_M3U8)
                || path.endsWith(TYPE_MOV)
                || path.endsWith(TYPE_MPG);
    }

    public static String getFileSuffix(String fileName) {
        if (TextUtils.isEmpty(fileName) || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static String getFilePath(String filePath) {
        if (TextUtils.isEmpty(filePath) || !filePath.contains("/")) {
            return null;
        }
        return filePath.substring(0, filePath.lastIndexOf("/"));
    }

    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath) || !filePath.contains("/")) {
            return null;
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public static String createListFile(String listPath, String[] fileArray) {
        if ((TextUtils.isEmpty(listPath) || fileArray == null || fileArray.length == 0)) {
            return null;
        }
        FileOutputStream outputStream = null;
        try {
            File listFile = new File(listPath);
            if (!listFile.getParentFile().exists()) {
                if (!listFile.mkdirs()) {
                    return null;
                }
            }
            if (!listFile.exists()) {
                if (!listFile.createNewFile()) {
                    return null;
                }
            }
            outputStream = new FileOutputStream(listFile);
            StringBuilder fileBuilder = new StringBuilder();
            for (String file : fileArray) {
                fileBuilder
                        .append("file")
                        .append(" ")
                        .append("'")
                        .append(file)
                        .append("'")
                        .append("\n");
            }
            byte[] fileData = fileBuilder.toString().getBytes();
            outputStream.write(fileData, 0, fileData.length);
            outputStream.flush();
            return listFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean ensureDir(String fileDir) {
        if (TextUtils.isEmpty(fileDir)) {
            return false;
        }
        File listFile = new File(fileDir);
        if (!listFile.exists()) {
            return listFile.mkdirs();
        }
        return true;
    }

    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @return
     */
    public static boolean deleteSDFile(String path) {
        return deleteSDFile(path, false);
    }

    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @param deleteParent true为删除父目录
     * @return
     */
    public static boolean deleteSDFile(String path, boolean deleteParent) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        File file = new File(path);
        if (!file.exists()) {
            //不存在
            return true;
        }
        return deleteFile(file, deleteParent);
    }

    /**
     * 删除文件夹
     *
     * @param file
     * @return
     */
    public static boolean deleteFolder(File file) {
        if (!file.exists()) {
            return true;
        }
        return file.delete();
    }

    /**
     * 删除文件夹下所有文件
     *
     * @param file
     * @return
     */
    public static boolean deleteDir(File file, boolean deleteParent) {
        if (null == file) {
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files || files.length <= 0) {
                return true;
            }
            int fileNum = files.length;

            while (fileNum > 0) {
                files[fileNum - 1].delete();
                fileNum--;
            }
        }

        if (deleteParent) {
            file.delete();
        }

        return true;
    }

    /**
     * @param file
     * @param deleteParent true为删除父目录
     * @return
     */
    public static boolean deleteFile(File file, boolean deleteParent) {
        if (null == file) {
            return true;
        }

        boolean result = false;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files || files.length <= 0) {
                result = true;
            }

            for (int i = 0; i < files.length; i++) {
                result = deleteFile(files[i], deleteParent);
            }

            if (deleteParent) {
                result = file.delete();
            }
        } else if (file.isFile()) {
            result = file.delete();
        }

        return result;
    }

    /**
     * 添加到媒体数据库
     *
     * @param context 上下文
     */
    public static Uri fileScanVideo(Context context, String videoPath, int videoWidth, int videoHeight,
                                    int videoTime) {

        File file = new File(videoPath);
        if (file.exists()) {

            Uri uri = null;

            long size = file.length();
            String fileName = file.getName();
            long dateTaken = System.currentTimeMillis();

            ContentValues values = new ContentValues(11);
            values.put(MediaStore.Video.Media.DATA, videoPath); // 路径;
            values.put(MediaStore.Video.Media.TITLE, fileName); // 标题;
            values.put(MediaStore.Video.Media.DURATION, videoTime * 1000); // 时长
            values.put(MediaStore.Video.Media.WIDTH, videoWidth); // 视频宽
            values.put(MediaStore.Video.Media.HEIGHT, videoHeight); // 视频高
            values.put(MediaStore.Video.Media.SIZE, size); // 视频大小;
            values.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken); // 插入时间;
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);// 文件名;
            values.put(MediaStore.Video.Media.DATE_MODIFIED, dateTaken / 1000);// 修改时间;
            values.put(MediaStore.Video.Media.DATE_ADDED, dateTaken / 1000); // 添加时间;
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

            ContentResolver resolver = context.getContentResolver();

            if (resolver != null) {
                try {
                    uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                } catch (Exception e) {
                    e.printStackTrace();
                    uri = null;
                }
            }

            if (uri == null) {
                MediaScannerConnection.scanFile(context, new String[]{videoPath}, new String[]{"video/*"}, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
            }

            return uri;
        }

        return null;
    }

    /**
     * SD卡存在并可以使用
     */
    public static boolean isSDExists() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡的剩余容量，单位是Byte
     *
     * @return
     */
    public static long getSDFreeMemory() {
        try {
            if (isSDExists()) {
                File pathFile = Environment.getExternalStorageDirectory();
                // Retrieve overall information about the space on a filesystem.
                // This is a Wrapper for Unix statfs().
                StatFs statfs = new StatFs(pathFile.getPath());
                // 获取SDCard上每一个block的SIZE
                long nBlockSize = statfs.getBlockSize();
                // 获取可供程序使用的Block的数量
                // long nAvailBlock = statfs.getAvailableBlocksLong();
                long nAvailBlock = statfs.getAvailableBlocks();
                // 计算SDCard剩余大小Byte
                long nSDFreeSize = nAvailBlock * nBlockSize;
                return nSDFreeSize;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 合成amr_nb编码的音频
     *
     * @param partsPaths
     * @param unitedFilePath
     */
    public static void uniteAMRFile(List<String> partsPaths, String unitedFilePath) {
        try {
            File unitedFile = new File(unitedFilePath);
            FileOutputStream fos = new FileOutputStream(unitedFile);
            RandomAccessFile ra = null;
            for (int i = 0; i < partsPaths.size(); i++) {
                ra = new RandomAccessFile(partsPaths.get(i), "rw");
                if (i != 0) {
                    ra.seek(6);
                }
                byte[] buffer = new byte[1024 * 8];
                int len = 0;
                while ((len = ra.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                File file = new File(partsPaths.get(i));
                if (file.exists()) {
                    file.delete();
                }
            }
            if (ra != null) {
                ra.close();
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        oleFile.renameTo(newFile);
    }

    //文件夹是否存在
    public static boolean isExist(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

}

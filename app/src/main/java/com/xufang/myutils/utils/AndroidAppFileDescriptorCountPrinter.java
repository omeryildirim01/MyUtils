package com.xufang.myutils.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by xufang on 2018/1/2.
 */

public class AndroidAppFileDescriptorCountPrinter {

    public static void print(final Context context) {
        new Thread(new Runnable() {
            int x = 0;

            @Override
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(5000, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    x++;

                    try {
                        File sdCardDir = context.getApplicationContext().getExternalFilesDir("111fd111");
                        if (!sdCardDir.exists()) {
                            sdCardDir.mkdirs();
                        }

                        File saveFile = new File(sdCardDir, "fd" + x + ".txt");
                        if (saveFile.exists()) {
                            saveFile.delete();
                        }
                        if (!saveFile.exists()) {
                            saveFile.createNewFile();
                        }
                        FileOutputStream outStream = new FileOutputStream(saveFile);

                        File fddir = new File("/proc/self/fd");
                        int count = 0;
                        for (File ff : fddir.listFiles()) {
                            //     if (ff.getCanonicalPath().contains("pipe")) {
                            Date lastModified = new Date(ff.lastModified());
                            String line = String.format("%s -> %s -> %s\n", ff.getName(), lastModified.toString(), ff.getCanonicalPath());
                            outStream.write(line.getBytes());
                            count = count + 1;
                            //   }
                        }
                        outStream.close();
                        Log.w("CountPrinter", "FD COUNT = " + count + " file=" + saveFile.getName());
                        int threadCount = Thread.getAllStackTraces().size();
                        Log.w("CountPrinter", "THREAD COUNT = " + threadCount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}

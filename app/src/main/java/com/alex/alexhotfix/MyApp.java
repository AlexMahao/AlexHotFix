package com.alex.alexhotfix;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.alex.hotpatch.HotPatch;

import java.io.File;
import java.io.IOException;

/**
 * Created by alex_mahao on 2016/8/26.
 */
public class MyApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        HotPatch.init(this);
        // 获取补丁，如果存在就执行注入操作
        String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat(File.separator + "patch_dex.jar");

        // 系统的私有目录
        String targetFile = this.getDir("odex", Context.MODE_PRIVATE).getAbsolutePath() + File.separator
                + "patch_dex.jar";
        if (!new File(dexPath).exists()) {
            return;
        }
        try {
            HotPatch.copyFile(dexPath, targetFile);
            HotPatch.inject(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

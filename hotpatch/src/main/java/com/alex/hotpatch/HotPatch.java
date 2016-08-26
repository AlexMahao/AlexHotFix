package com.alex.hotpatch;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

import dalvik.system.DexClassLoader;


/**
 * 补丁加载的主要工具类
 */
public class HotPatch {


    private static Context mContext;

    /**
     * 初始化文件
     */
    public static void init(Context context) {
        mContext = context;
        File hackDir = context.getDir("hackDir", 0);
        File hackJar = new File(hackDir, "hack.jar");
        try {
            AssetsUtil.copyAssets(context, "hack.jar", hackJar.getAbsolutePath());
            inject(hackJar.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导入补丁包的位置
     * @param path
     */
    public static void inject(String path) {
        File file = new File(path);
        if (file.exists()) {
            Log.e("info", "补丁文件存在");
            try {
                // 获取classes的dexElements
                Class<?> cl = Class.forName("dalvik.system.BaseDexClassLoader");
                Object pathList = ReflectUtil.getField(cl, "pathList", mContext.getClassLoader());
                Object baseElements = ReflectUtil.getField(pathList.getClass(), "dexElements", pathList);

                // 获取patch_dex的dexElements（需要先加载dex）
                String dexopt = mContext.getDir("dexopt", 0).getAbsolutePath();
                DexClassLoader dexClassLoader = new DexClassLoader(path, dexopt, dexopt, mContext.getClassLoader());
                Object obj = ReflectUtil.getField(cl, "pathList", dexClassLoader);
                Object dexElements = ReflectUtil.getField(obj.getClass(), "dexElements", obj);
                Log.e("info", Array.get(dexElements, 0).toString() + "");
                // 合并两个Elements
                Object combineElements = ReflectUtil.combineArray(dexElements, baseElements);

                // 将合并后的Element数组重新赋值给app的classLoader
                ReflectUtil.setField(pathList.getClass(), "dexElements", pathList, combineElements);
                for (int i = 0; i < Array.getLength(combineElements); i++) {
                    Log.e("info", Array.get(combineElements, i).toString() + "合并后的");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("HotPatch", file.getAbsolutePath() + "does not exists");
        }
    }


    /**
     * 文件拷贝
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void copyFile(String sourceFile, String targetFile) throws IOException {

        InputStream is = new FileInputStream(sourceFile);

        File outFile = new File(targetFile);

        if (outFile.exists()) {
            outFile.delete();
        }

        OutputStream os = new FileOutputStream(targetFile);

        int len = 0;

        byte[] buffer = new byte[1024];

        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }

        os.close();
        is.close();

    }
}

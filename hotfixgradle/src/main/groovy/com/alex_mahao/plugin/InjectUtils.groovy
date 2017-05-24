package com.alex_mahao.plugin

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import org.gradle.api.Project

/**
 * 代码注入的工具类
 */
public class InjectUtils {

    // 代码注入的工具类
    static ClassPool classPool = ClassPool.default


    static List noProcessJar = ["com${File.separator}android${File.separator}support", 'com.android.support'];

    static List noProcessClsPath = ["android${File.separator}support${File.separator}", '$', 'R.class', 'BuildConfig.class']

    // 动态加载jar 包的工具类不能被加载
    static List noProcessClsName = ["com${File.separator}alex${File.separator}hotpatch", 'MyApp']

    /**
     * hash值得文件
     */
    static File hashFile

    static String hashFilePath;
    /**
     * sdk 的目录
     */
    static String sdkDir


    static void init(Project project) {

        hashFilePath = project.rootDir.absolutePath + "${File.separator}hash.txt"

        initJavassist(project)
    }

    /**
     * 判断某些jar包是否需要注入
     *      jar 包分为两种，一种是系统提供的，第二种是我们创建的Library Module 并依赖
     *
     * @param filePath jar 包的绝对路径
     * @return
     */
    static boolean shouldInjectJar(String filePath) {
        for (String value : noProcessJar) {
            if (filePath.contains(value)) {
                println(filePath + "不需要注入代码")
                return false
            }
        }
        return true;
    }

    /**
     * 判断哪些类能够被注入
     *      R.java ，android 原生库，动态加载jar包的工具不需要被注入
     * @param filePath
     * @return
     */
    static boolean shouldInjectClass(String filePath) {
        for (String value : noProcessClsPath) {
            if (filePath.contains(value)) {
                return false
            }
        }

        for (String value : noProcessClsName) {
            if (filePath.contains(value)) {
                return false
            }
        }
        return true
    }

    /**
     * 对jar 包进行注入.
     *      - 解压到当前目录
     *      - 代码注入
     *      - 重新压缩成jar包
     * @param file
     */
    static void injectJar(File file) {
        // jar包解压的目录
        File jarDir = new File(file.parent, file.name.replace('.jar', ''))
        // 解压jar 包
        FileUtils.unZipJar(file, jarDir.absolutePath)

        classPool.appendClassPath(jarDir.absolutePath)

        jarDir.eachFileRecurse { f ->

            if (f.getName().endsWith(".class") && shouldInjectClass(f.absolutePath)) {
                println(f.absolutePath + ":" + FileUtils.getClassName(jarDir, f))
                injectClass(jarDir.absolutePath, FileUtils.getClassName(jarDir, f))
                println(FileUtils.getClassName(jarDir, f) + "注入完成")
            }
        }

        // 重新压缩
        // 删除原文件，重新压缩

        file.delete()
        println(file.absolutePath + "删除原文件")
        FileUtils.zipJar(jarDir, file.absolutePath)
        println(jarDir.absolutePath + "重新打包成jar")
    }

    /**
     * 将指定的代码注入到每一个类的构造函数中 ,注入代码之后，保存相应的md5值到文件
     * @param classPath
     * @param className
     */
    static void injectClass(String classPath, String className) {
        CtClass c = classPool.getCtClass(className)

        if (c.isFrozen()) {
            c.defrost()
        }

        CtConstructor[] cts = c.getDeclaredConstructors()
        if (cts == null || cts.length == 0) {
            CtConstructor constructor = new CtConstructor(new CtClass[0], c)
            constructor.setBody("{\nSystem.out.println(com.alex_mahao.hook.AntilazyLoad.class);\n}")
            c.addConstructor(constructor)
        } else {
            cts[0].insertBeforeBody('System.out.println(com.alex_mahao.hook.AntilazyLoad.class);')
        }
        c.writeFile(classPath)
        c.detach()
        // 处理md5值
        processMd5(classPath, className);
    }

    /**
     *  处理md5 ，分为两种情况，release 和 dohot 两种情况
     *      relase: 删除之前保存的hash文件，重现创建文件，并保存每一类的hash值
     *      doHot： 将文件保存到指定位置
     * @param classPath
     * @param className
     */
    static void processMd5(String classPath, String className) {
        switch (FixPlugin.FLAG) {
            case FixPlugin.FLAG_DO_HOT:
                if (hashFile == null) {
                    hashFile = new File(hashFilePath)
                    if (!hashFile.exists()) {
                        throw new Exception("请先运行release 生成对比文件")
                    }
                }
                FixUtils.processDoHotMD5(hashFile, classPath, className)
                break;
            case FixPlugin.FLAG_RELEASE:
                println("生成release")
                //如果mapFile 还未存在，则创建文件
                if (hashFile == null) {
                    hashFile = FileUtils.getHashFile(hashFilePath)
                }
                // 处理生成md5值
                FixUtils.processReleaseMD5(hashFile, classPath, className);
                break;
        }

    }
    /**
     * 初始化代码注入工具 javassist
     * @param project
     */
    static void initJavassist(Project project) {
        Properties prop = new Properties()
        File local = new File(project.rootDir, 'local.properties')
        prop.load(local.newInputStream())
        sdkDir = prop.getProperty('sdk.dir')

        String version = project.android.compileSdkVersion
        String androidJar = sdkDir + File.separator + "platforms" + File.separator + version + File.separator + "android.jar"
        String apacheJar = sdkDir + File.separator + "platforms" + File.separator + version + File.separator + "optional${File.separator}org.apache.http.legacy.jar"

        classPool.appendClassPath(androidJar)

        if (new File(apacheJar).exists()) {
            classPool.appendClassPath(apacheJar)
        }

        def libPath = project.rootDir.absolutePath.concat("${File.separator}antilazyLoad.jar")
        classPool.appendClassPath(libPath)
    }

    /**
     * 对目录进行注入
     * @param fileDir
     */
    static void injectDir(File fileDir) {
        classPool.appendClassPath(fileDir.absolutePath)
        // 循环遍历，注入代码
        fileDir.eachFileRecurse { File file ->
            if (file.name.endsWith(".class") && shouldInjectClass(file.absolutePath)) {
                println(file.absolutePath + "注入")
                String classname = FileUtils.getClassName(fileDir, file)
                injectClass(fileDir.absolutePath, classname)
            }
        }
    }
}
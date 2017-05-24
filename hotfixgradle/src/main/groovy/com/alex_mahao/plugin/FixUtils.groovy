package com.alex_mahao.plugin

import org.gradle.api.Project

import java.security.MessageDigest

/**
 * 补丁包的操作
 */
public class FixUtils {

    static Writer writer;

    static Map<String, String> md5Map;

    /**
     * 保存补丁的文件夹
     */
    static File hotFile

    /**
     *  生成每一个文件的md5值，保存到hashFile 中
     * @param hashFile
     * @param classPath
     * @param className
     */
    static void processReleaseMD5(File hashFile, String classPath, String className) {
        if (writer == null) {
            writer = hashFile.newPrintWriter()
        }
        String filePath = classPath + File.separator + className.replace(".", File.separator) + ".class"

        // 生成md5 值
        String md5 = md5(new File(filePath))
        //保存md5值
        writer.println(className + "-" + md5)

        println(className + "：md5生成并写入")

    }

    /**
     * 初始化热补丁对应文件夹
     */
    static void initHotDir() {
        if (hotFile == null) {
            hotFile = new File(new File(InjectUtils.hashFilePath).getParent() + File.separator + "hot" + File.separator)
        }
        if (hotFile.exists()) {
            println("******** 清空hot文件夹 ××××××××××××")
            FileUtils.cleanDirectory(hotFile)
        } else {
            println("******** hot文件夹不存在，创建${hotFile.absolutePath} ××××××××××××")
            hotFile.mkdirs();
        }

    }

    /**
     *  生成每一个文件的md5值，对比hashFile，抽出补丁
     * @param hashFile
     * @param classPath
     * @param className
     */
    static void processDoHotMD5(File hashFile, String classPath, String className) {
        if (md5Map == null) {
            md5Map = resolveHashFile(hashFile)
        }

        String filePath = classPath + File.separator + className.replace(".", File.separator) + ".class"
        // 生成md5 值
        String md5 = md5(new File(filePath))

        String oldMd5 = md5Map.get(className, "false");
        // 如果不相等，说明有补丁文件

        if (!md5.equals(oldMd5)) {
            println("${filePath} 文件修改，复制到hot目录")
            // 赋值当前文件到到指定目录
            FileUtils.copyFile(new File(filePath), new File(hotFile, className.replace(".", File.separator) + ".class"))
        }
    }

    /**
     * 将文件转成md5
     * @param file
     * @return
     */
    static String md5(File file) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        def inputStream = file.newInputStream()
        byte[] buf = new byte[16384]
        int len
        while ((len = inputStream.read(buf)) != -1) {
            digest.update(buf, 0, len)
        }
        inputStream.close()

        char[] chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f']
        byte[] bytes = digest.digest()
        int l = bytes.length;
        char[] str = new char[l << 1];
        int i = 0;
        for (int j = 0; i < l; ++i) {
            str[j++] = chars[(240 & bytes[i]) >>> 4];
            str[j++] = chars[15 & bytes[i]];
        }
        return new String(str)
    }

    /**
     * 将hash.txt解析成map
     * @param hashFile
     * @return
     */
    static Map<String, String> resolveHashFile(File hashFile) {
        Map<String, String> map = new HashMap<>()
        def reader = hashFile.newReader()
        reader.eachLine { line ->
            String[] strs = line.split('-')
            map.put(strs[0], strs[1])
        }
        reader.close()
        return map
    }

    /**
     * 将补丁的目录打包成补丁文件
     * @param project 工程
     * @param patchDir 补丁的目录
     * @param patchName 打成补丁的名字
     */
    static void dx(Project project, String patchDir, String patchName) {
        File file = new File(patchDir)
        if (file.isDirectory() && file.exists()) {
            File[] files = file.listFiles()
            if (files != null && files.size() > 0) {
                String buildToos = project.android.buildToolsVersion
                def stdout = new ByteArrayOutputStream()

                def command = "${InjectUtils.sdkDir}${File.separator}build-tools${File.separator}$buildToos${File.separator}dx --dex --output  $patchDir${File.separator}$patchName ${patchDir}"
                println(command)
                Process p = command.execute()
                p.waitFor();
                println(new PrintWriter(p.outputStream))

                /*project.exec {
                    workingDir "${InjectUtils.sdkDir}${File.separator}build-tools${File.separator}$buildToos"
                    commandLine 'dx', '--dex', '--output', "$patchDir${File.separator}$patchName", patchDir
                    standardOutput = stdout
                }
                def error = stdout.toString().trim()
                if (error) {
                    println "dex error:" + error
                }*/
            }
        }
    }
}
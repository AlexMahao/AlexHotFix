package com.alex_mahao.plugin

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * 文件操作的工具类
 *  - 解压jar包
 *  - 压缩jar包
 */
public class FileUtils{




    /**
     * 将jar 包解压缩到指定目录
     * @param jar   需要解压的jar 路径
     * @param dest  解压到指定的目录
     */
        static void unZipJar(File jar ,String dest){
        JarFile jarFile = new JarFile(jar)
        Enumeration<JarEntry> jarEntrys = jarFile.entries()
        while(jarEntrys.hasMoreElements()){
            JarEntry entry = jarEntrys.nextElement()
            if(jar.isDirectory()){
                continue
            }
            String entryName = entry.getName()
            String outFileName = dest+"/"+entryName
            File outFile =new File(outFileName)
            outFile.getParentFile().mkdirs()
            InputStream is = jarFile.getInputStream(entry)
            FileOutputStream fos = new FileOutputStream(outFile)
            fos<<is
            fos.close()
            is.close()
        }
        jarFile.close()
    }

    /**
     * 获取类的全路径名   包名+类名
     * @param parent
     * @param file
     * @return
     */
    static String getClassName(File parent,File file){
        def cPath = file.absolutePath
        def pPath = parent.absolutePath
        //  获取带有包名的类声明     .class   -6
        return cPath.substring(pPath.length() + 1, cPath.length() - 6).replace('\\', '.').replace('/', '.')

    }

    /**
     * 将目录重新打包成jar包
     * @param jarDir
     * @param file
     */
    static void zipJar(File jarDir,String dest){
        JarOutputStream os = new JarOutputStream(new FileOutputStream(dest))
        jarDir.eachFileRecurse { File f ->
            if(!f.isDirectory()) {
                String entryName = f.absolutePath.substring(jarDir.absolutePath.length() + 1)
                os.putNextEntry(new ZipEntry(entryName))
                InputStream is = new FileInputStream(f)
                os<<is
                is.close()
            }
        }
        os.close()
    }

    /**
     * 获取hash的保存文件
     * @return
     */
    static File getHashFile(String mapFilePath){
        File file = new File(mapFilePath)
        if(file.exists()){
            file.delete()
        }
        file.createNewFile();
        return file
    }


    /**
     * 复制文件
     * @param srcFile
     * @param destFile
     */
    static void copyFile(File srcFile, File destFile) {
        destFile.getParentFile().mkdirs()
        destFile.newOutputStream() << srcFile.newInputStream()
    }


    /**
     * 删除此文件下的所有内容，但是这个文件夹不会删除
     * @param directory
     */
    static void cleanDirectory(File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException("$directory does not exists")
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("$directory is not a directory")
        } else {
            File[] files = directory.listFiles()
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanDirectory(file)
                    file.delete()
                } else {
                    file.delete()
                }
            }
        }
    }
}
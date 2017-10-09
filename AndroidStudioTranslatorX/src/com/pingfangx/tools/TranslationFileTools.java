package com.pingfangx.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.pingfangx.tools.base.ILogger;

import javafx.concurrent.Task;

public class TranslationFileTools {
    private static TranslationFileTools sInstance;
    private static final String jarListFilePath = "jar_list.txt";

    private List<String> mJarFileList;

    private TranslationFileTools() {
        mJarFileList = readLines(jarListFilePath);
        if (mJarFileList == null || mJarFileList.isEmpty()) {
            mJarFileList = new ArrayList<>();
            mJarFileList.add("lib/idea.jar");
            mJarFileList.add("lib/resources.jar");
            mJarFileList.add("lib/resources_en.jar");
            mJarFileList.add("plugins/android/lib/android.jar");
            mJarFileList.add("plugins/android/lib/resources_en.jar");
        }
        // 如果以 ; 开头，将其忽略
        for (Iterator<String> iterator = mJarFileList.iterator(); iterator.hasNext();) {
            if (iterator.next().startsWith(";")) {
                iterator.remove();
            }
        }
        for (int i = 0; i < mJarFileList.size(); i++) {
            mJarFileList.set(i, mJarFileList.get(i).replace("/", File.separator).replace("\\", File.separator));
        }
    }

    public static TranslationFileTools getInstance() {
        if (sInstance == null) {
            sInstance = new TranslationFileTools();
        }
        return sInstance;
    }

    /**
     * 
     * 校验是否是正确的as路径
     * 
     * @return 如果正确返回null，如果不正确返回错误信息
     */
    public String validateAsPath(String path) {
        if (path == null) {
            return "未指定 AndroidStudio 目录";
        }
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        for (String jarFile : mJarFileList) {
            File file = new File(path + jarFile);
            if (!file.exists()) {
                return "找不到文件" + jarFile;
            }
        }
        return null;
    }

    public String copyJars(String sourceRoot, String destinationRoot, ILogger logger) {
        Thread t = new Thread(new CopyTask(sourceRoot, destinationRoot, null, logger));
        t.setDaemon(true);
        t.start();
        return null;
    }

    /**
     * 将 sourceRoot 中的 jar 文件，根据 destinationRoot 中的翻译文件列表，解压到 translationRoot
     * 中对应的文件夹中
     * 
     * @param sourceRoot 源文件夹，拼接上 jar文件得到其地址
     * @param destinationRoot 目标文件，拼接上 jar 文件（去掉 .jar），再拼上 翻译文件的目录及文件，得到要解压的文件名
     * @param translationRoot 翻译文件夹，
     * @return
     */
    public String unzipFileList(String sourceRoot, String destinationRoot, String translationRoot, ILogger logger) {
        Thread t = new Thread(new UnzipFileListTask(sourceRoot, destinationRoot, translationRoot, logger));
        t.setDaemon(true);
        t.start();
        return null;
    }

    /**
     * 将 sourceRoot 中的文件，压缩到 destinationRoot 中对应的 jar中
     * 如果translationRoot不为null，会删除 translationRoot中的文件
     * 
     * @param sourceRoot
     * @param destinationRoot
     * @return
     */
    public String zipFileList(String sourceRoot, String destinationRoot, String translationRoot, ILogger logger) {
        Thread t = new Thread(new ZipFileListTask(sourceRoot, destinationRoot, translationRoot, logger));
        t.setDaemon(true);
        t.start();
        return null;
    }

    /**
     * 复制文件
     */
    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        File parent = destinationFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileInputStream ins = new FileInputStream(sourceFile);
        FileOutputStream out = new FileOutputStream(destinationFile);
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = ins.read(buf)) != -1) {
            out.write(buf, 0, len);
        }

        ins.close();
        out.close();
    }

    /**
     * https://stackoverflow.com/questions/ 添加文件到已存在的 压缩包中
     */
    public static void addFilesToExistingZip(File zipFile, List<String> files, List<String> ignoreFiles,
            String fileParent) throws IOException {
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);
        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();

        boolean renameOk = zipFile.renameTo(tempFile);
        if (!renameOk) {
            throw new RuntimeException(
                    "could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            if (!files.contains(name) && !ignoreFiles.contains(name)) {
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        // Close the streams
        zin.close();
        // Compress the files
        for (String file : files) {
            InputStream in = new FileInputStream(fileParent + file);
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(file));
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Complete the entry
            out.closeEntry();
            in.close();
        }
        // Complete the ZIP file
        out.close();
        tempFile.delete();
    }

    /**
     * 在 jarFile 中 ，如果有 translationFileList 中的文件，则将其解压至 destinationPath
     * 从压缩包中解压指定的文件到指定的目录，如果不存在则不解压
     */
    public static void unzipFiles(File sourceFile, String destinationDir, List<String> fileList)
            throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(sourceFile);
        for (String file : fileList) {
            ZipEntry entry = zipFile.getEntry(file);
            if (entry != null) {
                File destFile = new File(destinationDir + file);
                File parent = destFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(destFile);
                int count;
                byte[] buf = new byte[1024];
                while ((count = is.read(buf)) != -1) {
                    fos.write(buf, 0, count);
                }
                is.close();
                fos.close();
            }
        }
        zipFile.close();
    }

    /**
     * 
     * 列出 jar 的所有翻译文件，结果相对于 translationDir，同时将"\\"替换为"/"，因为即使是 windows中 zip
     * 中的entry也是用的/
     */
    private static List<String> listTranslationFileOfJar(String translationDir) {
        List<File> fileList = listFile(translationDir);
        List<String> jarContentList = new ArrayList<>();
        for (File file : fileList) {
            // 去除前面的路径
            String jarContent = file.getAbsolutePath().replace(translationDir, "");
            // 因为即使是 windows中 zip 中的entry也是用的/
            jarContent = jarContent.replace("\\", "/");
            jarContentList.add(jarContent);
        }
        return jarContentList;
    }

    /**
     * 列出文件
     */
    private static List<File> listFile(String dir) {
        if (dir == null || dir.isEmpty()) {
            return new ArrayList<>();
        } else {
            File file = new File(dir);
            if (file.exists()) {
                return listFile(file);
            } else {
                return new ArrayList<>();
            }
        }
    }

    /**
     * 列出文件
     */
    private static List<File> listFile(File dir) {
        List<File> fileList = new ArrayList<>();
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    fileList.addAll(listFile(file));
                } else {
                    fileList.add(file);
                }
            }
        } else {
            fileList.add(dir);
        }
        return fileList;
    }

    /**
     * 读取所有行
     */
    private static List<String> readLines(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

    static abstract class TranslationFileTask extends Task<Void> {

        protected String sourceRoot;
        protected String destinationRoot;
        protected String translationRoot;
        protected ILogger logger;

        public TranslationFileTask(String sourceRoot, String destinationRoot, String translationRoot, ILogger logger) {
            if (sourceRoot != null && !sourceRoot.endsWith(File.separator)) {
                sourceRoot += File.separator;
            }
            if (destinationRoot != null && !destinationRoot.endsWith(File.separator)) {
                destinationRoot += File.separator;
            }
            if (translationRoot != null && !translationRoot.endsWith(File.separator)) {
                translationRoot += File.separator;
            }
            this.sourceRoot = sourceRoot;
            this.destinationRoot = destinationRoot;
            this.translationRoot = translationRoot;
            this.logger = logger;
        }

        @Override
        protected void updateMessage(String message) {
            super.updateMessage(message);
            logger.i(message);
        }

    }

    class CopyTask extends TranslationFileTask {
        public CopyTask(String sourceRoot, String destinationRoot, String translationRoot, ILogger logger) {
            super(sourceRoot, destinationRoot, translationRoot, logger);
        }

        @Override
        protected Void call() throws Exception {
            for (String jarFile : mJarFileList) {
                File sourceFile = new File(sourceRoot + jarFile);
                if (!sourceFile.exists()) {
                    updateMessage("找不到文件" + sourceFile.getAbsolutePath());
                    continue;
                }
                String destFile = destinationRoot + jarFile;
                updateMessage(String.format("将 %s 复制到 %s", sourceFile, destFile));
                try {
                    copyFile(sourceFile, new File(destFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    updateMessage(e.getMessage());
                }
            }
            updateMessage("操作完成");
            return null;
        }

    }

    class ZipFileListTask extends TranslationFileTask {

        public ZipFileListTask(String sourceRoot, String destinationRoot, String translationRoot, ILogger logger) {
            super(sourceRoot, destinationRoot, translationRoot, logger);
        }

        @Override
        protected Void call() throws Exception {

            for (String jarFile : mJarFileList) {
                File destFile = new File(destinationRoot + jarFile);
                if (!destFile.exists()) {
                    updateMessage("找不到文件" + destFile.getAbsolutePath());
                    continue;
                }
                String sourceDir = sourceRoot + jarFile.replace(".jar", "") + File.separator;
                if (!new File(sourceDir).exists()) {
                    updateMessage("找不到文件夹" + sourceDir);
                    continue;
                }
                String translationDir = translationRoot + jarFile.replace(".jar", "") + File.separator;
                try {
                    updateMessage(String.format("将 %s 压缩到 %s", sourceDir, destFile));
                    // 20M
                    if (destFile.length() > 1024 * 1024 * 20) {
                        updateMessage("文件较大请稍等...");
                    }
                    addFilesToExistingZip(destFile, listTranslationFileOfJar(sourceDir),
                            listTranslationFileOfJar(translationDir), sourceDir);
                } catch (IOException e) {
                    e.printStackTrace();
                    updateMessage(e.getMessage());
                }
            }
            updateMessage("操作完成");
            return null;
        }
    }

    class UnzipFileListTask extends TranslationFileTask {

        public UnzipFileListTask(String sourceRoot, String destinationRoot, String translationRoot, ILogger logger) {
            super(sourceRoot, destinationRoot, translationRoot, logger);
        }

        @Override
        protected Void call() throws Exception {
            for (String jarFile : mJarFileList) {
                File sourceFile = new File(sourceRoot + jarFile);
                if (!sourceFile.exists()) {
                    updateMessage("找不到文件" + sourceFile.getAbsolutePath());
                    continue;
                }
                String destDir = destinationRoot + jarFile.replace(".jar", "") + File.separator;
                String translationDir = translationRoot + jarFile.replace(".jar", "") + File.separator;
                if (!new File(translationDir).exists()) {
                    updateMessage("找不到汉化文件夹" + translationDir);
                    continue;
                }
                try {
                    updateMessage(String.format("将 %s 解压到 %s", sourceFile, destDir));
                    unzipFiles(sourceFile, destDir, listTranslationFileOfJar(translationDir));
                } catch (ZipException e) {
                    e.printStackTrace();
                    updateMessage(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    updateMessage(e.getMessage());
                }
            }
            updateMessage("操作完成");
            return null;
        }
    }

}

package com.pingfangx.tools;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
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

import org.gjt.jclasslib.io.ClassFileWriter;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.constants.ConstantUtf8Info;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * class 文件修改
 * 
 * @author 平方X
 *
 */
public class ClassFileEditor {
    @Option(name = "-h", aliases = { "-help", "--help" }, help = true, usage = "显示帮助")
    private boolean help;

    @Option(name = "-s", aliases = "-source", required = true, usage = "要处理的class文件")
    private String source;

    @Option(name = "-a", aliases = "-action", usage = "操作 s(show)|t(translate)")
    private String action;

    @Option(name = "-d", aliases = "-destination", usage = "处理后的保存的class文件")
    private String destination;

    @Option(name = "-t", aliases = "-translation", usage = "翻译文件，每行一个翻译，用=分隔中英文")
    private String translation;

    public static void main(String[] args) throws IOException {
        new ClassFileEditor().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // 解析
            parser.parseArgument(args);
            // 显示帮助
            if (help) {
                parser.printUsage(System.err);
                return;
            }
            // 解析操作
            if (action == null) {
                action = "t";
            } else {
                action = action.toLowerCase();
            }
            // 执行操作
            switch (action) {
            case "s":
            case "show":
                // 显示
                readClassFile(source, destination);
                break;
            case "t":
            case "translate":
                // 翻译
                if (destination == null) {
                    throw new IllegalArgumentException("require -d");
                } else if (translation == null) {
                    throw new IllegalArgumentException("require -t");
                }
                translateClassFile(source, destination, translation);
                break;
            case "unzip":
                unzipClassFile(source, destination, translation);
                break;
            default:
                throw new IllegalArgumentException("unknown action:" + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

    }

    private static void translateClassFile(String source, String destination, String translation) throws Exception {
        List<File> fileList = listFile(source, new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().endsWith(".class");
            }
        });
        List<Translation> allTranslation = getTranslations(translation);
        int size = fileList.size();
        for (int i = 0; i < size; i++) {
            File file = fileList.get(i);
            List<Translation> translationForFile = getTranslationForFile(file.getName().split("\\.")[0],
                    allTranslation);
            if (translationForFile.isEmpty()) {
                System.out.println("汉有对应的翻译 " + file.getName());
                continue;
            } else {
                // 如果是文件，也会直接替换文件名
                String resultFile = file.getAbsolutePath().replace(source, destination);
                System.out.println(String.format("翻译 %d/%d :%s", i + 1, size, file.getName()));
                updateClassFile(file.getAbsolutePath(), resultFile, translationForFile);
            }
        }
    }

    /**
     * 获取某一文件对应的翻译，为了防止错误的翻译，对其进行区分
     * 如果要不区分类，可添加标记，如用 null 表示
     * 
     * @param fileName
     * @param allTranslation
     * @return
     */
    private static List<Translation> getTranslationForFile(String fileName, List<Translation> allTranslation) {
        List<Translation> result = new ArrayList<>();
        for (Translation translation : allTranslation) {
            if (fileName.equals(translation.className)) {
                result.add(translation);
            }
        }
        return result;
    }

    /**
     * 返回 key 对应的翻译
     */
    private static String getTranslationForKey(String key, List<Translation> allTranslation) {
        for (Translation translation : allTranslation) {
            if (key.equals(translation.en)) {
                return translation.cn;
            }
        }
        return null;
    }

    /**
     * 更新class 文件
     * 
     * @param filePath 原文件
     * @param resultFilePath 修改后的文件
     * @param replaceMap 替换字典
     * @param printLine 是否打印行，用来显示所有信息
     * @throws Exception
     */
    private static void updateClassFile(String filePath, String resultFilePath, List<Translation> translations)
            throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        DataInput dataInput = new DataInputStream(fileInputStream);
        ClassFile classFile = new ClassFile();
        classFile.read(dataInput);
        fileInputStream.close();

        CPInfo[] infos = classFile.getConstantPool();

        int count = infos.length;
        for (int i = 0; i < count; i++) {
            CPInfo info = infos[i];
            if (info != null) {
                String name = info.getVerbose();
                // 这里会有2个，ConstantUtf8Info和CONSTANT_String_info
                if (info instanceof ConstantUtf8Info) {
                    String value = getTranslationForKey(name, translations);
                    if (value == null) {
                        continue;
                    }
                    String lineInfo = String.format("%d,%s=%s", i, name, info.getTagVerbose());
                    System.out.println(lineInfo);
                    System.out.println(String.format("包含【%s】,替换为【%s】\n", name, value));
                    ((ConstantUtf8Info) info).setString(value);
                    infos[i] = info;
                }
            }
        }
        classFile.setConstantPool(infos);
        if (resultFilePath != null) {
            // 创建文件夹
            File resultFile = new File(resultFilePath);
            if (!resultFile.getParentFile().exists()) {
                resultFile.getParentFile().mkdirs();
            }
            ClassFileWriter.writeToFile(resultFile, classFile);
            System.out.println("已输出文件" + resultFilePath + "\n");
        }
    }

    /**
     * 读取class文件
     * 
     * @param filePath
     * @param resultFilePath
     */
    private static void readClassFile(String filePath, String resultFilePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        DataInput dataInput = new DataInputStream(fileInputStream);
        ClassFile classFile = new ClassFile();
        classFile.read(dataInput);
        fileInputStream.close();

        CPInfo[] infos = classFile.getConstantPool();

        int count = infos.length;
        for (int i = 0; i < count; i++) {
            CPInfo info = infos[i];
            if (info != null) {
                if (info instanceof ConstantUtf8Info) {
                    String name = info.getVerbose();
                    String lineInfo = String.format("%d,%s", i, name);
                    System.out.println(lineInfo);
                }
            }
        }
    }

    /**
     * 根据翻译文件中指明的文件，从 jar 中解压出相应的 class 文件
     * 
     * @param source jar 包文件
     * @param destination 解压目录，可以为空
     * @param translationFile 翻译文件
     */
    private static void unzipClassFile(String source, String destination, String translationFile) throws Exception {
        List<Translation> translations = getTranslations(translationFile);
        List<String> fileNameList = new ArrayList<>();
        for (Translation translation : translations) {
            String fileName = translation.packageName.replace(".", "/") + "/" + translation.className + ".class";
            if (!fileNameList.contains(fileName)) {
                fileNameList.add(fileName);
            }
        }

        if (destination == null) {
            File file = new File(source);
            destination = file.getParent() + File.separator + file.getName().split("\\.")[0] + File.separator;
        }

        unzipFiles(new File(source), destination, fileNameList);
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
                System.out.println("解压缩" + file);
            } else {
                System.out.println("找不到" + file);
            }
        }
        zipFile.close();
    }

    /**
     * 读取翻译
     */
    private static List<Translation> getTranslations(String filePath) {
        List<Translation> result = new ArrayList<>();
        List<String> lines = readLines(filePath);
        // 空行没用，删除
        Iterator<String> iterator = lines.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next == null || next.isEmpty()) {
                iterator.remove();
            }
        }
        int size = lines.size();
        String packageName = "";
        String className = "";
        for (int i = 0; i < size; i++) {
            String line = lines.get(i);
            if (line.startsWith(";;")) {
                packageName = line.substring(2);
                continue;
            } else if (line.startsWith(";")) {
                className = line.substring(1);
                continue;
            } else {
                if (line.contains("=")) {
                    String[] enAndCn = line.split("=");
                    if (enAndCn.length > 1) {
                        result.add(new Translation(packageName, className, enAndCn[0], enAndCn[1]));
                    } else {
                        result.add(new Translation(packageName, className, enAndCn[0], ""));
                    }
                } else {
                    System.out.println("第" + i + "行格式不正确" + line);
                }
            }
        }
        return result;
    }

    /**
     * 列出文件
     * 
     * @param fileFilter
     */
    private static List<File> listFile(String dir, FileFilter fileFilter) {
        if (dir == null || dir.isEmpty()) {
            return new ArrayList<>();
        } else {
            File file = new File(dir);
            if (file.exists()) {
                return listFile(file, fileFilter);
            } else {
                return new ArrayList<>();
            }
        }
    }

    /**
     * 列出文件
     */
    private static List<File> listFile(File dir, FileFilter fileFilter) {
        List<File> fileList = new ArrayList<>();
        if (dir.isDirectory()) {
            for (File file : dir.listFiles(fileFilter)) {
                if (file.isDirectory()) {
                    fileList.addAll(listFile(file, fileFilter));
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
        List<String> lines = new ArrayList<>();
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = new FileInputStream(filePath);
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

    /**
     * 翻译
     *
     */
    private static class Translation {
        private String packageName;
        private String className;
        private String en;
        private String cn;

        public Translation(String packageName, String className, String en, String cn) {
            this.packageName = packageName;
            this.className = className;
            this.en = en;
            this.cn = cn;
        }

    }

}

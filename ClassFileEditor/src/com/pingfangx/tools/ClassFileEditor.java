package com.pingfangx.tools;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

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

    @Option(name = "-a", aliases = "-action", usage = "操作 s(show)|t(translation)")
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
            parser.parseArgument(args);
            if (help) {
                parser.printUsage(System.err);
                return;
            }
            if (action == null) {
                action = "t";
            } else {
                action = action.toLowerCase();
            }
            if (action.equals("s")) {
                // 显示
                readClassFile(source, destination);
            } else if (action.equals("t")) {
                // 翻译
                if (destination == null) {
                    throw new IllegalArgumentException("require -d");
                } else if (translation == null) {
                    throw new IllegalArgumentException("require -t");
                }
                try {
                    updateClassFile(source, destination, readTranslation(translation), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

    }

    /**
     * 读取翻译文件，每行一个翻译，按=分隔
     * 
     * @param filePath 文件
     * @return
     */
    private static Map<String, String> readTranslation(String filePath) {
        Map<String, String> result = new HashMap<>();
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = new FileInputStream(filePath);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("=")) {
                    String[] enAndCn = line.split("=");
                    if (enAndCn.length > 1) {
                        result.put(enAndCn[0], enAndCn[1]);
                    }
                }
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
        return result;
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
    private static void updateClassFile(String filePath, String resultFilePath, Map<String, String> replaceMap,
            boolean printLine) throws Exception {
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
                String lineInfo = String.format("%d,%s=%s", i, name, info.getTagVerbose());
                if (printLine) {
                    System.out.println(lineInfo);
                }
                if (replaceMap.containsKey(name)) {
                    // 这里会有2个，ConstantUtf8Info和CONSTANT_String_info
                    if (info instanceof ConstantUtf8Info) {
                        String value = replaceMap.get(name);
                        System.out.println(lineInfo);
                        System.out.println(String.format("包含%s,替换为%s\n", name, value));
                        ((ConstantUtf8Info) info).setString(value);
                        infos[i] = info;
                    }
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
            System.out.println("已输出文件" + resultFilePath);
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
}

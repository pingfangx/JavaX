package com.pingfangx.tools;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.pingfangx.tools.base.IBaseConfig;
import com.pingfangx.tools.base.ILogger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainSceneController implements Initializable {
    @FXML
    private TextField textFieldASPath;
    @FXML
    private ChoiceBox<String> choiceBoxBackupPath;
    @FXML
    private TextField textFieldBackupPath;
    @FXML
    private ChoiceBox<String> choiceBoxBackupType;
    @FXML
    TextField textFieldTranslationPath;
    @FXML
    private TextArea textAreaOutput;
    @FXML
    private ChoiceBox<String> choiceBoxTranslationType;

    private Stage mStage;

    private IBaseConfig mConfig;

    private ILogger mLogger;

    private TranslationFileTools mTranslationFileTools;

    /**
     * 备份目录选项
     */
    private String[] mBackupPathChoiceList = { "备份到 AndroidStudio 安装目录", "备份到当前目录", "自定义" };
    public static final int BACKUP_PATH_AS = 0;
    public static final int BACKUP_PATH_CURRENT = 1;
    /**
     * 翻译选项
     */
    private String[] mBackupTypeChoiceList = { "jar 包中被翻译的文件", "整个 jar 包" };
    public static final int BACKUP_TYPE_FILES_IN_JARS = 0;
    public static final int BACKUP_TYPE_JARS = 1;

    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mLogger = new TextAreaLogger(textAreaOutput);
        mConfig = new SoftwareConfig();
        mTranslationFileTools = TranslationFileTools.getInstance();
        // 设置选项
        choiceBoxBackupPath.setItems(FXCollections.observableArrayList(mBackupPathChoiceList));
        choiceBoxBackupPath.getSelectionModel().selectedIndexProperty().addListener((ov, oldv, newv) -> {
            setAndSaveBackupPathIndex(newv.intValue());
        });

        choiceBoxBackupType.setItems(FXCollections.observableArrayList(mBackupTypeChoiceList));
        choiceBoxBackupType.getSelectionModel().selectedIndexProperty().addListener((ov, oldv, newv) -> {
            mConfig.set(SoftwareConfig.BACKUP_TYPE_INDEX, newv.toString());
        });

        choiceBoxTranslationType.setItems(FXCollections.observableArrayList(mBackupTypeChoiceList));
        choiceBoxTranslationType.getSelectionModel().selectedIndexProperty().addListener((ov, oldv, newv) -> {
            mConfig.set(SoftwareConfig.TRANSLATION_TYPE_INDEX, newv.toString());
        });

        // 初始化配置
        initTextField(textFieldASPath, SoftwareConfig.ANDROID_STUDIO_PATH, null);
        initTextField(textFieldBackupPath, SoftwareConfig.BACKUP_PATH, null);
        initTextField(textFieldTranslationPath, SoftwareConfig.TRANSLATION_PATH, getCurrentDir());

        initChoiceBox(choiceBoxBackupPath, SoftwareConfig.BACKUP_PATH_INDEX);
        initChoiceBox(choiceBoxBackupType, SoftwareConfig.BACKUP_TYPE_INDEX);
        initChoiceBox(choiceBoxTranslationType, SoftwareConfig.TRANSLATION_TYPE_INDEX);

        String asPath = textFieldASPath.getText();
        if (asPath == null || asPath.isEmpty()) {
            scanAsPath();
        }
    }

    /**
     * 扫描 as安装目录
     */
    private void scanAsPath() {
        String userHomePath = System.getProperty("user.home");
        if (userHomePath == null || userHomePath.isEmpty()) {
            return;
        }

        File userHomeDir = new File(userHomePath);
        if (!userHomeDir.exists()) {
            return;
        }

        File[] asConfigDirs = userHomeDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(".AndroidStudio");
            }
        });

        if (asConfigDirs.length == 0) {
            return;
        }
        // 取最新一个，有可能会取到pre版本的
        File asConfigDir = asConfigDirs[asConfigDirs.length - 1];
        File asConfigHomeFile = new File(asConfigDir, "/system/.home");
        if (!asConfigHomeFile.exists() || !asConfigHomeFile.isFile()) {
            return;
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(asConfigHomeFile);
            bufferedReader = new BufferedReader(fileReader);
            String asHomePath = bufferedReader.readLine();
            File asHomeDir = new File(asHomePath);
            if (asHomeDir.exists()) {
                if (mTranslationFileTools.validateAsPath(asHomePath) == null) {
                    setAndSaveAsPath(asHomePath);
                    mLogger.i("自动扫描出 AndroidStudio 安装路径" + asHomePath);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
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
    }

    private void initTextField(TextField textField, String name, String defaultValue) {
        String value = mConfig.get(name);
        if (value != null && !value.isEmpty()) {
            textField.setText(value);
        } else {
            textField.setText(defaultValue);
        }
    }

    private void initChoiceBox(ChoiceBox<String> choiceBox, String name) {
        String indexString = mConfig.get(name);
        int index = -1;
        if (indexString != null && !indexString.isEmpty()) {
            try {
                index = Integer.parseInt(indexString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (index < 0) {
            index = 0;
        }
        choiceBox.getSelectionModel().select(index);
    }

    /**
     * 获取当前目录
     */
    private static String getCurrentDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 更新备份目录
     */
    private void updateBackupPath() {
        String backupParentPath = "";
        int selectedIndex = choiceBoxBackupPath.getSelectionModel().getSelectedIndex();
        if (selectedIndex == BACKUP_PATH_AS) {
            if (textFieldASPath.getText() != null && !textFieldASPath.getText().equals("")) {
                backupParentPath = textFieldASPath.getText();
            } else {
                setAndSaveBackupPath("");
            }
        } else if (selectedIndex == BACKUP_PATH_CURRENT) {
            backupParentPath = System.getProperty("user.dir");
        }
        if (backupParentPath != null && !backupParentPath.equals("")) {
            if (!backupParentPath.endsWith(File.separator)) {
                backupParentPath += File.separator;
            }
            setAndSaveBackupPath(backupParentPath + "backup");
        }
    }

    private void setAndSaveAsPath(String path) {
        String message = mTranslationFileTools.validateAsPath(path);
        if (message != null) {
            checkAsPathMessage(message);
            return;
        }
        textFieldASPath.setText(path);
        mConfig.set(SoftwareConfig.ANDROID_STUDIO_PATH, path);
        updateBackupPath();
    }

    private void setAndSaveBackupPath(String path) {
        textFieldBackupPath.setText(path);
        mConfig.set(SoftwareConfig.BACKUP_PATH, path);
    }

    private void setAndSaveTranslationPath(String path) {
        textFieldTranslationPath.setText(path);
        mConfig.set(SoftwareConfig.TRANSLATION_PATH, path);
    }

    private void setAndSaveBackupPathIndex(int index) {
        mConfig.set(SoftwareConfig.BACKUP_PATH_INDEX, String.valueOf(index));
        updateBackupPath();
    }

    /**
     * 选择 AndroidStudio 安装目录
     */
    @FXML
    public void onClickChooseASPath(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("请选择 AndroidStudio 安装目录");
        if (textFieldASPath.getText() != null) {
            File currentDir = new File(textFieldASPath.getText());
            if (currentDir.exists()) {
                directoryChooser.setInitialDirectory(currentDir);
            }
        }
        File selectedFile = directoryChooser.showDialog(mStage);
        if (selectedFile != null) {
            setAndSaveAsPath(selectedFile.getAbsolutePath());
        }
    }

    /**
     * 选择备份存放目录
     */
    @FXML
    public void onClickChooseBackupPath(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("请选择备份文件存放目录");
        if (textFieldBackupPath.getText() != null) {
            File currentDir = new File(textFieldBackupPath.getText());
            if (currentDir.exists()) {
                directoryChooser.setInitialDirectory(currentDir);
            }
        }
        File selectedFile = directoryChooser.showDialog(mStage);
        if (selectedFile != null) {
            setAndSaveBackupPath(selectedFile.getAbsolutePath());
            choiceBoxBackupPath.getSelectionModel().select(mBackupPathChoiceList.length - 1);
        }
    }

    /**
     * 选择翻译文件目录
     */
    @FXML
    public void onClickChooseTranslationPath(MouseEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("请选择汉化文件存放目录");
        if (textFieldTranslationPath.getText() != null) {
            File currentDir = new File(textFieldTranslationPath.getText());
            if (currentDir.exists()) {
                directoryChooser.setInitialDirectory(currentDir);
            }
        }
        File selectedFile = directoryChooser.showDialog(mStage);
        if (selectedFile != null) {
            setAndSaveTranslationPath(selectedFile.getAbsolutePath());
        }
    }

    /**
     * 检查选择as的路径消息
     * 
     * @param message
     */
    private void checkAsPathMessage(String message) {
        if (message != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION, message, new ButtonType("重选", ButtonData.YES),
                    new ButtonType("取消", ButtonData.NO));
            alert.setHeaderText("所选路径不是正确的 AndroidStudio 安装路径");
            Optional<ButtonType> result = alert.showAndWait();
            if (result != null) {
                if (result.get().getButtonData() == ButtonData.YES) {
                    onClickChooseASPath(null);
                }
            }
        }
    }

    /**
     * 检查操作消息
     * 
     * @param message
     */
    private void checkActionResult(String message) {
        if (message != null) {
            Alert alert = new Alert(AlertType.ERROR, message);
            alert.setHeaderText(null);
            alert.show();
        }
    }

    // Event Listener on Button[#btnBackup].onMouseClicked
    @FXML
    public void onClickBackup(MouseEvent event) {
        String message = mTranslationFileTools.validateAsPath(textFieldASPath.getText());
        if (message != null) {
            checkAsPathMessage(message);
            return;
        }
        message = null;
        int selectedIndex = choiceBoxBackupType.getSelectionModel().getSelectedIndex();
        if (selectedIndex == BACKUP_TYPE_FILES_IN_JARS) {
            message = mTranslationFileTools.unzipFileList(textFieldASPath.getText(), textFieldBackupPath.getText(),
                    textFieldTranslationPath.getText(), mLogger);
        } else {
            message = mTranslationFileTools.copyJars(textFieldASPath.getText(), textFieldBackupPath.getText(), mLogger);
        }
        checkActionResult(message);
    }

    // Event Listener on Button[#btnRestore].onMouseClicked
    @FXML
    public void onClickRestore(MouseEvent event) {
        String message = mTranslationFileTools.validateAsPath(textFieldASPath.getText());
        if (message != null) {
            checkAsPathMessage(message);
            return;
        }

        message = null;
        int selectedIndex = choiceBoxBackupType.getSelectionModel().getSelectedIndex();
        if (selectedIndex == BACKUP_TYPE_FILES_IN_JARS) {
            message = mTranslationFileTools.zipFileList(textFieldBackupPath.getText(), textFieldASPath.getText(),
                    textFieldTranslationPath.getText(), mLogger);
        } else {
            message = mTranslationFileTools.copyJars(textFieldBackupPath.getText(), textFieldASPath.getText(), mLogger);
        }
        checkActionResult(message);
    }

    // Event Listener on Button[#btnTranslate].onMouseClicked
    @FXML
    public void onClickTranslate(MouseEvent event) {
        String message = mTranslationFileTools.validateAsPath(textFieldASPath.getText());
        if (message != null) {
            checkAsPathMessage(message);
            return;
        }

        message = null;
        int selectedIndex = choiceBoxTranslationType.getSelectionModel().getSelectedIndex();
        if (selectedIndex == BACKUP_TYPE_FILES_IN_JARS) {
            message = mTranslationFileTools.zipFileList(textFieldTranslationPath.getText(), textFieldASPath.getText(),
                    null, mLogger);
        } else {
            message = mTranslationFileTools.copyJars(textFieldTranslationPath.getText(), textFieldASPath.getText(),
                    mLogger);
        }

        checkActionResult(message);
    }

    /**
     * 设置支持拖放
     */
    @FXML
    public void onDragOver(DragEvent event) {
        event.acceptTransferModes(TransferMode.ANY);
    }

    /**
     * 拖放 as 目录
     */
    @FXML
    public void onDragDroppedAsPath(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            try {
                File file = dragboard.getFiles().get(0);
                if (file != null) {
                    setAndSaveAsPath(file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拖放备份目录
     */
    @FXML
    public void onDragDroppedBackupPath(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            try {
                File file = dragboard.getFiles().get(0);
                if (file != null) {
                    setAndSaveBackupPath(file.getAbsolutePath());
                    choiceBoxBackupPath.getSelectionModel().select(mBackupPathChoiceList.length - 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拖放翻译目录
     */
    @FXML
    public void onDragDroppedTranslationPath(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            try {
                File file = dragboard.getFiles().get(0);
                if (file != null) {
                    setAndSaveTranslationPath(file.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void menuPathAs(ActionEvent event) {
        openPath(textFieldASPath.getText());
    }

    @FXML
    public void menuPathBackup(ActionEvent event) {
        openPath(textFieldBackupPath.getText());
    }

    @FXML
    public void menuPathTranslation(ActionEvent event) {
        openPath(textFieldTranslationPath.getText());
    }

    @FXML
    public void menuHelp(ActionEvent event) {
        openUrl("https://github.com/pingfangx/TranslatorX");
    }

    @FXML
    public void menuFeedback(ActionEvent event) {
        openUrl("https://github.com/pingfangx/TranslatorX");
    }

    /**
     * 打开目录
     */
    private void openPath(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开网址
     */
    private void openUrl(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (uri == null) {
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Action.BROWSE)) {
            return;
        }
        try {
            desktop.browse(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

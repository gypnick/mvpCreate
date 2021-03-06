package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MvpCreateAction extends AnAction {

    private Project project;
    //包名
    private String packageName = "";
    private String mAuthor;//作者
    private String mModuleName;//模块名称
    private String mClassName;//class名称
    private int mType = 0;//0为class 1为fragment
    private boolean mIsRecycle;//是否为recycle

    private enum CodeType {
        Activity, Fragment, Contract, Presenter, CommonActivity, CommonFragment, CommonPresenter, Adapter, Bean
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        packageName = getPackageName();
        init();
        refreshProject(e);
    }

    /**
     * 刷新项目
     *
     * @param e
     */
    private void refreshProject(AnActionEvent e) {
        e.getProject().getBaseDir().refresh(false, true);
    }

    /**
     * 初始化Dialog
     */
    private void init() {
        MvpDialog myDialog = new MvpDialog(new MvpDialog.DialogCallBack() {
            @Override
            public void ok(String author, String moduleName, String className, int type, boolean isRecycle) {
                mAuthor = author;
                mModuleName = moduleName;
                mClassName = className;
                mType = type;
                mIsRecycle = isRecycle;
                if (mIsRecycle) {
                    createRecycleClassFiles();
                } else {
                    createClassFiles();
                }
                Messages.showInfoMessage(project, "create mvp code success", "title");
            }
        });
        myDialog.setVisible(true);

    }

    /**
     * 生成类文件
     */
    private void createClassFiles() {
        if (mType == 0) {
            createClassFile(CodeType.Activity);
        } else {
            createClassFile(CodeType.Fragment);
        }
        createClassFile(CodeType.Contract);
        createClassFile(CodeType.Presenter);
        createClassFile(CodeType.Bean);
        createXmlFile();
    }

    /**
     * 生成类文件
     */
    private void createRecycleClassFiles() {
        if (mType == 0) {
            createClassFile(CodeType.CommonActivity);
        } else {
            createClassFile(CodeType.CommonFragment);
        }
        createClassFile(CodeType.CommonPresenter);
        createClassFile(CodeType.Adapter);
        createClassFile(CodeType.Bean);
        createRecycleItemXmlFile();
    }

    /**
     * 创建xml
     */
    private void createXmlFile() {
        String fileName = "";
        String content = "";
        String appPath = getXmlPath();
        if (mType == 0) {
            fileName = "TemplateActivityXml.txt";
            content = ReadTemplateFile(fileName);
            content = dealTemplateContent(content, "");
            writeToFile(content, appPath, "activity" +camelToUnderline(mClassName).toLowerCase() + ".xml");
        } else {
            fileName = "TemplateFragmentXml.txt";
            content = ReadTemplateFile(fileName);
            content = dealTemplateContent(content, "");
            writeToFile(content, appPath, "fragment" + camelToUnderline(mClassName).toLowerCase() + ".xml");
        }

    }

    private void createRecycleItemXmlFile() {
        String fileName = "";
        String content = "";
        String appPath = getXmlPath();

        fileName = "TemplateItemXml.txt";
        content = ReadTemplateFile(fileName);
        content = dealTemplateContent(content, "");
        writeToFile(content, appPath, "item" + camelToUnderline(mClassName).toLowerCase() + ".xml");

    }


    /**
     * 生成mvp框架代码
     *
     * @param codeType
     */
    private void createClassFile(CodeType codeType) {
        String fileName = "";
        String content = "";
        String appPath = getAppPath();
        switch (codeType) {
            case Activity:
                fileName = "TemplateActivity.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.v");
                writeToFile(content, appPath + "v/" + mModuleName.toLowerCase(), mClassName + "Activity.java");
                break;
            case Fragment:
                fileName = "TemplateFragment.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.v");
                writeToFile(content, appPath + "v/" + mModuleName.toLowerCase(), mClassName + "Fragment.java");
                break;
            case Contract:
                fileName = "TemplateContract.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.c");
                writeToFile(content, appPath + "c/" + mModuleName.toLowerCase(), mClassName + (mType == 0 ? "Activity" : "Fragment") + "Contract.java");
                break;
            case Presenter:
                fileName = "TemplatePresenter.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.p");
                writeToFile(content, appPath + "p/" + mModuleName.toLowerCase(), mClassName + (mType == 0 ? "Activity" : "Fragment") + "Presenter.java");
                break;
            case CommonActivity:
                fileName = "TemplateCommonActivity.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.v");
                writeToFile(content, appPath + "v/" + mModuleName.toLowerCase(), mClassName + "Activity.java");
                break;
            case CommonFragment:
                fileName = "TemplateCommonFragment.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.v");
                writeToFile(content, appPath + "v/" + mModuleName.toLowerCase(), mClassName + "Fragment.java");
                break;
            case CommonPresenter:
                fileName = "TemplateCommonPresenter.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.p");
                writeToFile(content, appPath + "p/" + mModuleName.toLowerCase(), mClassName + (mType == 0 ? "Activity" : "Fragment") + "Presenter.java");
                break;
            case Adapter:
                fileName = "TemplateAdapter.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.adapter");
                writeToFile(content, appPath + "adapter/", mClassName + "Adapter.java");
                break;
            case Bean:
                fileName = "TemplateBean.txt";
                content = ReadTemplateFile(fileName);
                content = dealTemplateContent(content, ".mvp.m");
                writeToFile(content, appPath + "m/" + mModuleName.toLowerCase(), mClassName + "Bean.java");
                break;
        }
    }

    /**
     * 获取包名文件路径
     *
     * @return
     */
    private String getAppPath() {
        String packagePath = packageName.replace(".", "/");
        String appPath = project.getBasePath() + "/App/src/main/java/" + packagePath + "/mvp/";
        return appPath;
    }

    /**
     * 获取包名文件路径
     *
     * @return
     */
    private String getXmlPath() {
        String appPath = project.getBasePath() + "/App/src/main/res/layout";
        return appPath;
    }

    /**
     * 替换模板中字符
     *
     * @param content
     * @return
     */
    private String dealTemplateContent(String content, String module) {
        content = content.replace("$name", mClassName + (mType == 0 ? "Activity" : "Fragment"));
        content = content.replace("$className", mClassName);
        content = content.replace("$moduleName", mModuleName.toLowerCase());
        content = content.replace("$layoutname", (mType == 0 ? "activity" : "fragment") + camelToUnderline(mClassName).toLowerCase());
        if (content.contains("$packagename")) {
            content = content.replace("$packagename", packageName + module + "." + mModuleName.toLowerCase());
        }
        if (content.contains("$itemLayout")) {
            content = content.replace("$itemLayout", "item" + camelToUnderline(mClassName).toLowerCase());
        }
        if (content.contains("$basepackagename")) {
            content = content.replace("$basepackagename", packageName);
        }
        content = content.replace("$author", mAuthor);
        content = content.replace("$date", getDate());
        return content;
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public String getDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 将驼峰转换成"_"(userId:user_id)
     *
     * @param param
     * @return
     */
    public static String camelToUnderline(String param) {
        char UNDERLINE = '_';
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 读取模板文件中的字符内容
     *
     * @param fileName 模板文件名
     * @return
     */
    private String ReadTemplateFile(String fileName) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("/Template/" + fileName);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }


    private byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputStream.close();
            inputStream.close();
        }

        return outputStream.toByteArray();
    }


    /**
     * 生成
     *
     * @param content   类中的内容
     * @param classPath 类文件路径
     * @param className 类文件名称
     */
    private void writeToFile(String content, String classPath, String className) {
        try {
            File floder = new File(classPath);
            if (!floder.exists()) {
                floder.mkdirs();
            }

            File file = new File(classPath + "/" + className);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 从AndroidManifest.xml文件中获取当前app的包名
     *
     * @return
     */
    private String getPackageName() {
        String package_name = "";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(project.getBasePath() + "/App/src/main/AndroidManifest.xml");

            NodeList nodeList = doc.getElementsByTagName("manifest");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element element = (Element) node;
                package_name = element.getAttribute("package");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return package_name;
    }

}

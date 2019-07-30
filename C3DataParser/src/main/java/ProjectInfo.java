import java.util.ArrayList;

/**
 * Created by kvirus on 2019/7/11 16:54
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ProjectInfo {

    public final static String filePath = "D:\\data\\";

    public final static ArrayList<Project> projects = new ArrayList();

    static {
        // small
        projects.add(new Project("cobertura",   filePath + "original_cobertura.json",  filePath + "cobertura"));
        projects.add(new Project("fitlibrary",  filePath + "original_fitlibrary.json", filePath + "fitlibrary-fitlibrary"));
        projects.add(new Project("jgrapht",     filePath + "original_jgrapht.json",    filePath + "jgrapht"));
        projects.add(new Project("junit",       filePath + "original_junit.json",      filePath + "junit4"));

        // medium
        projects.add(new Project("ant",         filePath + "original_ant.json",        filePath + "ant"));
        projects.add(new Project("checkstyle",  filePath + "original_checkstyle.json", filePath + "checkstyle"));
        projects.add(new Project("drjava",      filePath + "original_drjava.json",     filePath + "drjava"));

        //large
        projects.add(new Project("jdt",         filePath + "original_eclipsejdt.json", filePath + "eclipse.jdt.core"));
        projects.add(new Project("swt",         filePath + "original_eclipseswt.json", filePath + "eclipse.platform.swt"));
    }

    public static class Project {
        String name         = "";
        String originalFile = "";
        String gitPath      = "";
        String storePath    = "";
        public Project(String name, String originalFile, String gitPath) {
            this.name           = name;
            this.originalFile   = originalFile;
            this.gitPath        = gitPath;
            this.storePath      = originalFile.replaceAll("original_", "extract_");
        }
    }
}

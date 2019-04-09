package analyzer.commit;

import analyzer.histories.variation.MethodMutantType;

public class ChangedMethod {
    public MethodMutantType type;
    public String filePath;
    public String className;
    public String methodName;
    public String[] fileContent;

    public boolean newlyAdded;
    public boolean beCalled;
    public boolean externalCalled;
    public int dependent = 0;


    public boolean contains(String keyWord) {
        for (String token: fileContent) {
            if (token.equals(keyWord))
                return true;
        }
        return false;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent.split("[^a-zA-Z0-9_]");
    }
}

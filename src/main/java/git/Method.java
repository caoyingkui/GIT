package git;

public class Method {
    public String fullName ;
    public String name;
    public int startLine;
    public int endLine;
    public String methodContent;

    public Method(String fullName, String name, int startLine, int endLine, String methodContent){
        this.fullName = fullName;
        this.name = name;
        this.startLine = startLine;
        this.endLine = endLine;
        this.methodContent = methodContent;
    }

}

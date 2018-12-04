package graph;

import jdk.nashorn.internal.objects.annotations.Setter;

import java.util.ArrayList;
import java.util.List;

public class SCSFile {
    private String path;

    List<String> library = new ArrayList<>();
    List<SCSUnit> units = new ArrayList<>();

    public void setPath(String path){
        this.path = path;
    }

    public void addLibrary(String library){
        this.library.add(library);
    }

    public void addUnit(SCSUnit unit){
        units.add(unit);
    }


}

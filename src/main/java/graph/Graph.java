package graph;

import java.util.*;

public class Graph {
    Map<String, Integer> methods = new HashMap<>();
    Map<Integer, Set<Integer>> parents = new HashMap<>();
    Map<Integer, Set<Integer>> children = new HashMap<>();
    public int size = 0;


    public void addMethod(String methodName){
        methods.put(methodName, size ++);
    }


    /**
     * 添加图中的一条边
     * @param parentName
     * @param childName
     */
    public void addRelation(String parentName, String childName){
        int parentId = methods.getOrDefault(parentName, -1);
        int childId = methods.getOrDefault(childName, -1);
        if(parentId > -1 && childId > -1 ){
            if(!parents.containsKey(childId)){
                parents.put(childId, new HashSet<>());
            }
            parents.get(childId).add(parentId);

            if(!children.containsKey(parentId)){
                children.put(parentId, new HashSet<>());
            }
            children.get(parentId).add(childId);
        }
    }


    /**
     * 获取该图中没有父亲节点的节点，就是在图中最顶层的节点
     * @return
     */
    public List<String> getTop(){
        List<String> result = new ArrayList<>();
        for(String name: methods.keySet()){
            int id = methods.get(name);
            if(!parents.containsKey(id)){
                result.add(name);
            }
        }
        return result;
    }
}

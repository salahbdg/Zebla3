package SEMANTIC;

import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;

public class FunctionTable {
    private Map<String, CommonTree> functions;
    
    public FunctionTable() {
        functions = new HashMap<>();
    }
    
    public void addFunction(String name, CommonTree node) {
        functions.put(name, node);
    }
    
    public boolean containsFunction(String name) {
        return functions.containsKey(name);
    }
    
    public CommonTree getFunction(String name) {
        return functions.get(name);
    }
}

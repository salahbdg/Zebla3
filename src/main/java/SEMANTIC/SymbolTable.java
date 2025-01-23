package SEMANTIC;

import java.util.HashSet;
import java.util.Stack;

public class SymbolTable {
    private Stack<HashSet<String>> scopes;
    
    public SymbolTable() {
        scopes = new Stack<>();
        enterScope(); // Global scope
    }
    
    public void enterScope() {
        scopes.push(new HashSet<>());
    }
    
    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }
    
    public void addVariable(String name) {
        if (!scopes.isEmpty()) {
            scopes.peek().add(name);
        }
    }
    
    public boolean isVariableDefined(String name) {
        for (HashSet<String> scope : scopes) {
            if (scope.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
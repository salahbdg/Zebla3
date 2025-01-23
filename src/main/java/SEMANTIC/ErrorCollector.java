package SEMANTIC;

import java.util.ArrayList;
import java.util.List;

public class ErrorCollector {
    private List<SemanticError> errors;
    
    public ErrorCollector() {
        errors = new ArrayList<>();
    }
    
    public void addError(String message, int line) {
        errors.add(new SemanticError(message, line));
    }
    
    public List<SemanticError> getErrors() {
        return errors;
    }
}

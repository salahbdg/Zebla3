package SEMANTIC;


public class SemanticError {
    private String message;
    private int line;
    
    public SemanticError(String message, int line) {
        this.message = message;
        this.line = line;
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getLine() {
        return line;
    }
    
    @Override
    public String toString() {
        return "Line " + line + ": " + message;
    }
}
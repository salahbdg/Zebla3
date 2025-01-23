package SEMANTIC;


import org.antlr.runtime.tree.*;
import java.util.*;

public class SemanticChecker {
    private SymbolTable symbolTable;
    private ErrorCollector errorCollector;
    private FunctionTable functionTable;

    public SemanticChecker() {
        this.symbolTable = new SymbolTable();
        this.errorCollector = new ErrorCollector();
        this.functionTable = new FunctionTable();
    }

    /**
     * Main entry point for semantic analysis
     * @param tree The AST root node
     * @return true if no semantic errors were found
     */
    public boolean check(CommonTree tree) {
        if (tree.getToken().getText().equals("ROOT")) {
            checkProgram(tree);
        }
        return errorCollector.getErrors().isEmpty();
    }

    /**
     * Print all errors found during semantic analysis 
     */

    public void printErrors() {
        for (SemanticError error : errorCollector.getErrors()) {
            System.err.println("Error: " + error.getMessage() + " at line " + error.getLine());
        }
    }

    /**
     * Checks the entire program structure
     */
    private void checkProgram(CommonTree tree) {
        // First pass: collect all function declarations
        for (int i = 0; i < tree.getChildCount(); i++) {
            CommonTree functionNode = (CommonTree) tree.getChild(i);
            if (functionNode.getText().equals("FUNCTION")) {
                String functionName = functionNode.getChild(0).getText();

                if (functionTable.containsFunction(functionName)) {

                    errorCollector.addError("Function " + functionName + " is already defined", 
                        functionNode.getLine());
                } else {
                    functionTable.addFunction(functionName, functionNode);
                }
            }
        }

        // Check if main function exists
        if (!functionTable.containsFunction("main")) {
            errorCollector.addError("No main function defined", 0);
        }

        // Second pass: check each function's body
        for (int i = 0; i < tree.getChildCount(); i++) {
            checkFunction((CommonTree) tree.getChild(i));
        }
    }

    /**
     * Checks a function definition
     */
    private void checkFunction(CommonTree functionNode) {
        symbolTable.enterScope();
        
        String functionName = functionNode.getChild(0).getText();
        CommonTree definitionNode = (CommonTree) functionNode.getChild(1);
        
        // Check input parameters
        CommonTree inputNode = findNode(definitionNode, "INPUT");
        if (inputNode != null) {
            checkInputParameters(inputNode);
        }
        
        // Check function body
        CommonTree commandsNode = findNode(definitionNode, "COMMANDS");
        if (commandsNode != null) {
            checkCommands(commandsNode);
        }
        
        // Check output parameters
        CommonTree outputNode = findNode(definitionNode, "OUTPUT");
        if (outputNode != null) {
            checkOutputParameters(outputNode);
        }
        
        symbolTable.exitScope();
    }

    /**
     * Checks a block of commands
     */
    private void checkCommands(CommonTree commandsNode) {
        for (int i = 0; i < commandsNode.getChildCount(); i++) {
            CommonTree commandNode = (CommonTree) commandsNode.getChild(i); // assign assi
            checkCommand(commandNode);
        }
    }

    /**
     * Checks a single command
     */
    private void checkCommand(CommonTree commandNode) {
        switch (commandNode.getText()) {
            case "ASSIGN":
                checkAssignment(commandNode);
                break;
            case "IF":
                checkIfStatement(commandNode);
                break;
            case "WHILE":
                checkWhileStatement(commandNode);
                break;
            case "FOR":
                checkForStatement(commandNode);
                break;
            case "FOREACH":
                checkForEachStatement(commandNode);
                break;
            case "NOP":
                // No semantic checking needed for NOP
                break;
        }
    }

    /**
     * Checks an assignment command
     */
    private void checkAssignment(CommonTree assignNode) {
        CommonTree varsNode = (CommonTree) assignNode.getChild(0);
        CommonTree exprsNode = (CommonTree) assignNode.getChild(1);
        
        // Check that number of variables matches number of expressions
        if (varsNode.getChildCount() != exprsNode.getChildCount()) {
            errorCollector.addError("Number of variables does not match number of expressions",
                assignNode.getLine());
            return;
        }
        
        // Check each expression and add variables to symbol table
        for (int i = 0; i < varsNode.getChildCount(); i++) {
            String varName = varsNode.getChild(i).getText();
            symbolTable.addVariable(varName);
            checkExpression((CommonTree) exprsNode.getChild(i));
        }
    }


  /**
   * Checks the semantic validity of an if statement
   * @param ifNode The IF node from the AST
   */
  private void checkIfStatement(CommonTree ifNode) {
      // Check condition
      CommonTree conditionNode = (CommonTree) ifNode.getChild(0);
      checkExpression(conditionNode);
      
      // Enter new scope for then branch
      symbolTable.enterScope();
      CommonTree thenCommandsNode = (CommonTree) ifNode.getChild(1);
      checkCommands(thenCommandsNode);
      symbolTable.exitScope();
      
      // Check else branch if it exists
      if (ifNode.getChildCount() > 2) {
          symbolTable.enterScope();
          CommonTree elseCommandsNode = (CommonTree) ifNode.getChild(2);
          checkCommands(elseCommandsNode);
          symbolTable.exitScope();
      }
  }

  /**
   * Checks the semantic validity of a while loop
   * @param whileNode The WHILE node from the AST
   */
  private void checkWhileStatement(CommonTree whileNode) {
      // Check condition
      CommonTree conditionNode = (CommonTree) whileNode.getChild(0);
      checkExpression(conditionNode);
      
      // Enter new scope for loop body
      symbolTable.enterScope();
      CommonTree commandsNode = (CommonTree) whileNode.getChild(1);
      checkCommands(commandsNode);
      symbolTable.exitScope();
  }

  /**
   * Checks the semantic validity of a for loop
   * @param forNode The FOR node from the AST
   */
  private void checkForStatement(CommonTree forNode) {
      // Check loop condition/range expression
      CommonTree rangeExprNode = (CommonTree) forNode.getChild(0);
      checkExpression(rangeExprNode);
      
      // Make sure the range expression evaluates to a list type
      if (!isListExpression(rangeExprNode)) {
          errorCollector.addError("For loop range must be a list expression", 
              forNode.getLine());
      }
      
      // Enter new scope for loop body
      symbolTable.enterScope();
      CommonTree commandsNode = (CommonTree) forNode.getChild(1);
      checkCommands(commandsNode);
      symbolTable.exitScope();
  }

  /**
   * Checks the semantic validity of a foreach loop
   * @param foreachNode The FOREACH node from the AST
   */
  private void checkForEachStatement(CommonTree foreachNode) {
      // Get iterator variable
      String iteratorVar = foreachNode.getChild(0).getText();
      
      // Check collection expression
      CommonTree collectionExprNode = (CommonTree) foreachNode.getChild(1);
      checkExpression(collectionExprNode);
      
      // Make sure the collection expression evaluates to a list type
      if (!isListExpression(collectionExprNode)) {
          errorCollector.addError("Foreach loop collection must be a list expression", 
              foreachNode.getLine());
      }
      
      // Enter new scope for loop body and add iterator variable to symbol table
      symbolTable.enterScope();
      symbolTable.addVariable(iteratorVar);
      
      CommonTree commandsNode = (CommonTree) foreachNode.getChild(2);
      checkCommands(commandsNode);
      symbolTable.exitScope();
  }

  /**
   * Checks input parameters of a function
   * @param inputNode The INPUT node from the AST
   */
  private void checkInputParameters(CommonTree inputNode) {
      Set<String> paramNames = new HashSet<>();
      
      // Check each input parameter
      for (int i = 0; i < inputNode.getChildCount(); i++) {
          String paramName = inputNode.getChild(i).getText();
          
          // Check for duplicate parameter names
          if (paramNames.contains(paramName)) {
              errorCollector.addError("Duplicate input parameter name: " + paramName,
                  inputNode.getLine());
          } else {
              paramNames.add(paramName);
              symbolTable.addVariable(paramName);  // Add to current scope
          }
      }
  }

  /**
   * Checks output parameters of a function
   * @param outputNode The OUTPUT node from the AST
   */
  private void checkOutputParameters(CommonTree outputNode) {
      Set<String> paramNames = new HashSet<>();
      
      // Check each output parameter
      for (int i = 0; i < outputNode.getChildCount(); i++) {
          String paramName = outputNode.getChild(i).getText();
          
          // Check for duplicate parameter names
          if (paramNames.contains(paramName)) {
              errorCollector.addError("Duplicate output parameter name: " + paramName,
                  outputNode.getLine());
          } else {
              paramNames.add(paramName);
          }
          
          // Ensure output variables are defined in the function
          if (!symbolTable.isVariableDefined(paramName)) {
              errorCollector.addError("Output variable " + paramName + " is not defined in function",
                  outputNode.getLine());
          }
      }
  }

  /**
   * Helper method to check if an expression evaluates to a list type
   * @param exprNode The expression node to check
   * @return true if the expression is a list type
   */
  private boolean isListExpression(CommonTree exprNode) {
      String type = exprNode.getText();
      return type.equals("LIST") || type.equals("CONS") || 
            (type.equals("VARIABLE") && isVariableList(exprNode.getChild(0).getText())) ||
            (type.equals("FUNCTIONCALL") && isFunctionReturnsList(exprNode.getChild(0).getText()));
  }

  /**
   * Helper method to check if a variable contains a list
   * This is a simplified version - you might want to add proper type tracking
   */
  private boolean isVariableList(String varName) {
      // Add implementation based on your type system
      return true; // Simplified for example
  }

  /**
   * Helper method to check if a function returns a list
   * This is a simplified version - you might want to add proper return type tracking
   */
  private boolean isFunctionReturnsList(String functionName) {
      // Add implementation based on your type system
      return true; // Simplified for example
  }

  /**
   * Checks the semantic validity of an expression
   * @param exprNode The expression node from the AST
   */
  private void checkExpression(CommonTree exprNode) {
      switch (exprNode.getText()) {
          case "EXPRESSION":
              for (int i = 0; i < exprNode.getChildCount(); i++) {
                  checkExpression((CommonTree) exprNode.getChild(i));
              }
              break;
              
          case "EQUALS":
              checkExpression((CommonTree) exprNode.getChild(0));
              checkExpression((CommonTree) exprNode.getChild(1));
              break;
              
          case "VARIABLE":
              String varName = exprNode.getChild(0).getText();
              if (!symbolTable.isVariableDefined(varName)) {
                  errorCollector.addError("Undefined variable: " + varName,
                      exprNode.getLine());
              }
              break;
              
          case "FUNCTIONCALL":
              checkFunctionCall(exprNode);
              break;
              
          case "HEAD":
          case "TAIL":
              CommonTree listExpr = (CommonTree) exprNode.getChild(0);
              checkExpression(listExpr);
              if (!isListExpression(listExpr)) {
                  errorCollector.addError("Head/Tail operation requires a list expression",
                      exprNode.getLine());
              }
              break;
              
          case "CONS":
          case "LIST":
              // Check all expressions in the cons/list construction
              for (int i = 0; i < exprNode.getChildCount(); i++) {
                  checkExpression((CommonTree) exprNode.getChild(i));
              }
              break;
      }
  }

  /**
   * Checks a function call expression
   */
  private void checkFunctionCall(CommonTree functionCallNode) {
      String functionName = functionCallNode.getChild(0).getText();
      
      if (!functionTable.containsFunction(functionName)) {
          errorCollector.addError("Undefined function: " + functionName,
              functionCallNode.getLine());
          return;
      }
      
      CommonTree functionDef = functionTable.getFunction(functionName);
      CommonTree inputNode = findNode(functionDef, "INPUT");
      
      // Check number of arguments matches function definition
      int expectedArgs = (inputNode != null) ? inputNode.getChildCount() : 0;
      int actualArgs = functionCallNode.getChildCount() - 1; // Subtract 1 for function name
      
      if (expectedArgs != actualArgs) {
          errorCollector.addError("Function " + functionName + " expects " + expectedArgs + 
              " arguments but got " + actualArgs, functionCallNode.getLine());
      }
      
      // Check each argument expression
      for (int i = 1; i < functionCallNode.getChildCount(); i++) {
          checkExpression((CommonTree) functionCallNode.getChild(i));
      }
  }
      
      

    // Helper methods
    private CommonTree findNode(CommonTree tree, String type) {
      if (tree != null){
          if (tree.getText().equals(type)) {
            return tree;
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            CommonTree result = findNode((CommonTree) tree.getChild(i), type);
            if (result != null) {
                return result;
            }
        }
      }

        return null;
    }
}


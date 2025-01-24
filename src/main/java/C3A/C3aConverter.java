package C3A;
import org.antlr.runtime.tree.CommonTree;
import java.util.ArrayList;
import java.util.List;

public class C3aConverter {
    private int tempCounter = 0;
    private int labelCounter = 0;
    private List<String> threeAddressCode = new ArrayList<>();

    // add warnnings as generating 3AC, like using a cons without arguments

    private List<String> warnningCollector = new ArrayList<>();

        // Used in symbol, an explanation for using this attribute is in the PythonTranslator.java  
        private static String typeOutput;
            
            
                // Generate a new temporary variable
                private String newTemp() {
                    return "t" + tempCounter++;
                }
            
                // Generate a new label
                private String newLabel() {
                    return "L" + labelCounter++;
                }
            
                // Main conversion method
                public List<String> convert(CommonTree ast) {
                    threeAddressCode.clear();
                    tempCounter = 0;
                    labelCounter = 0;
            
                    
                    
                    if (ast == null) return threeAddressCode;
                    
                    System.out.println("ast id node : "+ast.getType());
            
            
                    switch (ast.getType()) {
                        case AST.ROOT:
                            convertRoot(ast);
                            break;
                        case AST.FUNCTION:
                            convertFunction(ast);
                            break;
                        default:
                            convertNode(ast);
                    }
                    
                    return threeAddressCode;
                }
            
                // get childs for a node (used for debugging)
                private void getChilds(CommonTree node) {
                    if (node == null) {
                        System.out.println("node is null");
                        return;
                    }
            
                    if (node.getChildCount() == 0) {
                        System.out.println("no child for " + node.getText());
                        return;
                    }
                    System.out.println("child count of " + node.getText() + " : " + node.getChildCount());
                    for (Object child : node.getChildren()) {
                        System.out.println("child of " + node.getText() +  " : " + child.toString());
                    }
                }
            
                // Convert root node (multiple functions)
                private void convertRoot(CommonTree root) {
            
                  getChilds(root);
            
                    for (Object child : root.getChildren()) {
                        convertFunction((CommonTree) child);
                    }
                }
            
                // Convert function definition
                private void convertFunction(CommonTree functionNode) {
            
            
            
                    getChilds(functionNode);
            
                    String functionName = functionNode.getChild(0).getText();
                    threeAddressCode.add("FUNCTION " + functionName + ":");
                    
                    // Process function definition
                    CommonTree funcDef = (CommonTree) functionNode.getChild(0);
            
                    System.out.println("funcDef id node : "+funcDef.getType() + "..." + funcDef.getText());
            
                    convertFunctionDefinition(funcDef);
                    
                    threeAddressCode.add("END FUNCTION " + functionName);
                }
            
                // Convert function definition
                private void convertFunctionDefinition(CommonTree funcDef) {
            
                    funcDef = (CommonTree) funcDef.getChild(0);
            
                    getChilds(funcDef);
            
                    // Handle input parameters
                    if (funcDef.getChild(0).getType() == AST.INPUT) {
                        // function has input parameters
                        convertInput((CommonTree) funcDef.getChild(0));
            
                        // Convert commands
                        convertCommands((CommonTree) funcDef.getChild(1));
            
                        // Handle output
                        convertOutput((CommonTree) funcDef.getChild(2));
            
                    }
            
                    else{
                      // no input parameters
                      // Convert commands
                      convertCommands((CommonTree) funcDef.getChild(0));
            
                      // Handle output
                      convertOutput((CommonTree) funcDef.getChild(1));
                    }
                    
                    
                    // // Handle output
                    // if (funcDef.getChild(1).getType() == AST.OUTPUT) {
                    //   convertOutput((CommonTree) funcDef.getChild(2));
                    // }
            
                    // // Convert commands
                    // convertCommands((CommonTree) funcDef.getChild(1));
                    
            
                }
            
                // convert node
                private String convertNode(CommonTree ast) {
                  if (ast == null) return null;
            
                  switch (ast.getType()) {
                      case AST.ROOT:
                          convertRoot(ast);
                          break;
                      case AST.FUNCTION:
                          convertFunction(ast);
                          break;
                      case AST.COMMANDS:
                          convertCommands(ast);
                          break;
                      case AST.COMMAND:
                          return convertCommand(ast);
                      case AST.EXPRESSION:
                          return convertExpression(ast);
                      case AST.HEAD:
                          return convertHead(ast);
                      case AST.TAIL:
                          return convertTail(ast);
                      case AST.LIST:
                          return convertList(ast);
                      default:
                          if (ast.getChildren() != null) {
                              for (Object child : ast.getChildren()) {
                                  convertNode((CommonTree) child);
                              }
                          }
                  }
                  return null;
              }
            
              // Convert head
              private String convertHead(CommonTree headNode) {
                String expr = convertExpression((CommonTree) headNode.getChild(0));
                String temp = newTemp();
                threeAddressCode.add(temp + " := HEAD(" + expr + ")");
                return temp;
              }
            
              // Convert tail
              private String convertTail(CommonTree tailNode) {
                String expr = convertExpression((CommonTree) tailNode.getChild(0));
                String temp = newTemp();
                threeAddressCode.add(temp + " := TAIL(" + expr + ")");
                return temp;
              }
            
              // Convert list
              private String convertList(CommonTree listNode) {
                String temp = newTemp();
                threeAddressCode.add(temp + " := nil");
                
                for (int i = listNode.getChildCount() - 1; i >= 0; i--) {
                    String elementTemp = convertExpression((CommonTree) listNode.getChild(i));
                    String newTemp = newTemp();
                    threeAddressCode.add(newTemp + " := CONS(" + elementTemp + ", " + temp + ")");
                    temp = newTemp;
                }
                
                return temp;
              }
            
              // convert forEach
              private String convertForEach(CommonTree foreachNode) {
                String iterVar = foreachNode.getChild(0).getText();
                String collection = convertExpression((CommonTree) foreachNode.getChild(1));
                CommonTree commands = (CommonTree) foreachNode.getChild(2);
                
                String currentTemp = newTemp();
                String startLabel = newLabel();
                String endLabel = newLabel();
                
                // Initialize current to collection
                threeAddressCode.add(currentTemp + " := " + collection);
                
                // Loop start
                threeAddressCode.add(startLabel + ":");
                
                // Check if current is nil
                String condTemp = newTemp();
                threeAddressCode.add(condTemp + " := " + currentTemp + " =? nil");
                threeAddressCode.add("IF " + condTemp + " GOTO " + endLabel);
                
                // Assign head to iterator
                threeAddressCode.add(iterVar + " := HEAD(" + currentTemp + ")");
                
                // Process loop body
                convertCommands(commands);
                
                // Move to next element
                threeAddressCode.add(currentTemp + " := TAIL(" + currentTemp + ")");
                threeAddressCode.add("GOTO " + startLabel);
                
                // Loop end
                threeAddressCode.add(endLabel + ":");
                
                return null;
              }
            
            
            
                // Convert input parameters
                private void convertInput(CommonTree inputNode) {
                    if (inputNode.getChildCount() > 0) {
                        for (Object var : inputNode.getChildren()) {
                            threeAddressCode.add("PARAM " + var.toString());
                        }
                    }
                }
            
                // Convert output parameters
                private void convertOutput(CommonTree outputNode) {
                    if (outputNode.getChildCount() > 0) {
                        for (Object var : outputNode.getChildren()) {
                            threeAddressCode.add("RETURN " + var.toString());
                        }
                    }
                }
            
                // Convert commands block
                private void convertCommands(CommonTree commandsNode) {
                    System.out.println();
                    getChilds(commandsNode);
                    for (Object cmdObj : commandsNode.getChildren()) {
                        CommonTree cmdNode = (CommonTree) cmdObj;
                        convertCommand(cmdNode);
                    }
                }
            
                // Convert individual command
                private String convertCommand(CommonTree cmdNode) {
                    switch (cmdNode.getType()) {
                        case AST.ASSIGN:
                            return convertAssignment(cmdNode);
                        case AST.IF:
                            return convertIf(cmdNode);
                        case AST.WHILE:
                            return convertWhile(cmdNode);
                        case AST.NOP:
                            threeAddressCode.add("NOP");
                            return null;
                        case AST.FOR:
                        return convertFor(cmdNode);
                        //  return "FOR";
            
                        case AST.FOREACH:
                          return convertForEach(cmdNode);
            
                        default:
                            throw new UnsupportedOperationException("Unsupported command type: " + cmdNode.getType());
                    }
                }
            
                // Convert assignment
                private String convertAssignment(CommonTree assignNode) {
                  CommonTree varsNode = (CommonTree) assignNode.getChild(0);
                  CommonTree exprsNode = (CommonTree) assignNode.getChild(1);
                  
                  if (varsNode.getChildCount() != exprsNode.getChildCount()) {
                      warnningCollector.add("Warning: Mismatch in number of variables and expressions");
                      return null;
                  }
            
                  // Process each assignment
                  for (int i = 0; i < varsNode.getChildCount(); i++) {
                      String varName = varsNode.getChild(i).getText();
                      String exprTemp = convertExpression((CommonTree) exprsNode.getChild(i));
                      threeAddressCode.add(varName + " := " + exprTemp);
                  }
                  
                  return null;
              }
            
                
            
                // Convert expression
                private String convertExpression(CommonTree exprNode) {
                  if (exprNode == null) return "nil";
            
                  // If it's an EXPRESSION node, get its child
                  CommonTree baseExpr = exprNode.getType() == AST.EXPRESSION ? 
                  (CommonTree) exprNode.getChild(0) : exprNode;
            
            
                  if (exprNode.getType() == AST.EXPRESSION) {
                      baseExpr = (CommonTree) exprNode.getChild(0);
                  } else {
                      baseExpr = exprNode;
                  }
            
                  switch (baseExpr.getType()) {
                      case AST.FUNCTIONCALL:
                          return convertFunctionCall(baseExpr);
                      
                      case AST.VARIABLE:
                      case AST.VARIABLE_LEX:
                          return baseExpr.getChild(0).getText();
                      
                      case AST.NIL:
                          return "nil";
                      
                      case AST.SYMBOL:
                        // Handle the case where it's a symbol leaf node
                        getChilds(exprNode);
                        this.typeOutput = exprNode.getChild(0).getText();
                        System.out.println("typeOutput : "+getTypeOutput());
                    if (baseExpr.getChild(0) != null) {
                        return baseExpr.getChild(0).getText();
                    }
                    // Handle the case where the symbol is directly in the node
                    return baseExpr.getText();
        
                  case AST.SYMBOL_LEX:
                    return baseExpr.getText();
                  
                  case AST.CONS:
                      return convertConsExpression(baseExpr);
                  
                  case AST.HEAD:
                      String headArgTemp = convertExpression((CommonTree) baseExpr.getChild(0));
                      String headResultTemp = newTemp();
                      threeAddressCode.add(headResultTemp + " := HEAD(" + headArgTemp + ")");
                      return headResultTemp;
                  
                  case AST.TAIL:
                      String tailArgTemp = convertExpression((CommonTree) baseExpr.getChild(0));
                      String tailResultTemp = newTemp();
                      threeAddressCode.add(tailResultTemp + " := TAIL(" + tailArgTemp + ")");
                      return tailResultTemp;
                  
                  default:
                      warnningCollector.add("Warning: Unsupported expression type: " + baseExpr.getType());
                      return "nil";
              }
            }
        
            // convert cons
            // Convert CONS expression
            // Convert CONS expression
            // Convert CONS expression
            // Convert CONS expression
            private String convertConsExpression(CommonTree consNode) {
              if (consNode.getChildCount() == 0) {
                  String temp = newTemp();
                  threeAddressCode.add(temp + " := CONS(nil, nil)");
                  return temp;
              }
        
              // Get the left and right arguments
              String leftTemp = null;
              String rightTemp = null;
        
              // Handle left argument
              if (consNode.getChild(0) != null) {
                  CommonTree leftNode = (CommonTree) consNode.getChild(0);
                  leftTemp = convertExpression(leftNode);
              } else {
                  leftTemp = "nil";
              }
        
              // Handle right argument
              if (consNode.getChildCount() > 1 && consNode.getChild(1) != null) {
                  CommonTree rightNode = (CommonTree) consNode.getChild(1);
                  rightTemp = convertExpression(rightNode);
              } else {
                  rightTemp = "nil";
              }
        
              // Create final CONS instruction
              String resultTemp = newTemp();
              threeAddressCode.add(resultTemp + " := CONS(" + leftTemp + ", " + rightTemp + ")");
              return resultTemp;
          }
        
            // Convert FOR loop
            private String convertFor(CommonTree forNode) {
              // Convert count expression
              String countTemp = convertExpression((CommonTree) forNode.getChild(0));
              
              // Create counter variable
              String counterTemp = newTemp();
              String startLabel = newLabel();
              String endLabel = newLabel();
              
              // Initialize counter
              threeAddressCode.add(counterTemp + " := 0");
              
              // Loop start
              threeAddressCode.add(startLabel + ":");
              
              // Condition check
              String condTemp = newTemp();
              threeAddressCode.add(condTemp + " := " + counterTemp + " < " + countTemp);
              threeAddressCode.add("IF NOT " + condTemp + " GOTO " + endLabel);
              
              // Loop body
              convertCommands((CommonTree) forNode.getChild(1));
              
              // Increment counter
              threeAddressCode.add(counterTemp + " := " + counterTemp + " + 1");
              threeAddressCode.add("GOTO " + startLabel);
              
              // Loop end
              threeAddressCode.add(endLabel + ":");
              
              return null;
          }
          
        
        
            // Convert function call
          //   private String convertFunctionCall(CommonTree funcCallNode) {
          //     String funcName = funcCallNode.getChild(0).getText();
          //     List<String> argTemps = new ArrayList<>();
              
          //     // Convert each argument to its own temporary
          //     for (int i = 1; i < funcCallNode.getChildCount(); i++) {
          //         String argTemp = convertExpression((CommonTree) funcCallNode.getChild(i));
          //         argTemps.add(argTemp);
          //     }
              
          //     // Create the function call
          //     String resultTemp = newTemp();
          //     if (argTemps.isEmpty()) {
          //         threeAddressCode.add(resultTemp + " := CALL " + funcName);
          //     } else {
          //         threeAddressCode.add(resultTemp + " := CALL " + funcName + "(" + String.join(", ", argTemps) + ")");
          //     }
              
          //     return resultTemp;
          // }
        
          private String convertFunctionCall(CommonTree funcCallNode) {
            String functionName = funcCallNode.getChild(0).getText();
            List<String> args = new ArrayList<>();
            
            // Get the function name node (which contains the arguments)
            CommonTree funcNameNode = (CommonTree) funcCallNode.getChild(0);
            
            // Process arguments which are children of the function name node
            for (int i = 0; i < funcNameNode.getChildCount(); i++) {
                CommonTree argNode = (CommonTree) funcNameNode.getChild(i);
                String argTemp = convertExpression(argNode);
                args.add(argTemp);
            }
            
            String temp = newTemp();
            if (args.isEmpty()) {
                warnningCollector.add("Warning: Function call to " + functionName + " with no arguments");
                threeAddressCode.add(temp + " := CALL " + functionName + "()");
            } else {
                threeAddressCode.add(temp + " := CALL " + functionName + "(" + String.join(", ", args) + ")");
            }
            return temp;
        }
        
            // Convert comparison expression
            // private String convertComparisonExpression(CommonTree exprNode) {
            //     if (exprNode.getType() == AST.EXPRESSION && exprNode.getChild(0).getType() == AST.EQUALS) {
            //         CommonTree equalsNode = (CommonTree) exprNode.getChild(0);
            //         String left = convertExpression((CommonTree) equalsNode.getChild(0));
            //         String right = convertExpression((CommonTree) equalsNode.getChild(1));
                    
            //         String temp = newTemp();
            //         threeAddressCode.add(temp + " := " + left + " = " + right);
            //         return temp;
            //     }
                
            //     // If not an equals expression, just convert the base expression
            //     return convertExpression((CommonTree) exprNode.getChild(0));
            // }
        
            // Convert if statement
            private String convertIf(CommonTree ifNode) {
                CommonTree condExpr = (CommonTree) ifNode.getChild(0);
                CommonTree thenCommands = (CommonTree) ifNode.getChild(1);
                CommonTree elseCommands = ifNode.getChildCount() > 2 ? 
                    (CommonTree) ifNode.getChild(2) : null;
                
                String condTemp = convertExpression(condExpr);
                String trueLabel = newLabel();
                String falseLabel = newLabel();
                String endLabel = newLabel();
                
                threeAddressCode.add("IF " + condTemp + " GOTO " + trueLabel);
                threeAddressCode.add("GOTO " + falseLabel);
                
                threeAddressCode.add(trueLabel + ":");
                convertCommands(thenCommands);
                threeAddressCode.add("GOTO " + endLabel);
                
                threeAddressCode.add(falseLabel + ":");
                if (elseCommands != null) {
                    convertCommands(elseCommands);
                }
                
                threeAddressCode.add(endLabel + ":");
                
                return null;
            }
        
            // Convert while statement
            private String convertWhile(CommonTree whileNode) {
                CommonTree condExpr = (CommonTree) whileNode.getChild(0);
                CommonTree commands = (CommonTree) whileNode.getChild(1);
                
                String startLabel = newLabel();
                String condLabel = newLabel();
                String endLabel = newLabel();
                
                threeAddressCode.add("GOTO " + condLabel);
                
                threeAddressCode.add(startLabel + ":");
                convertCommands(commands);
                
                threeAddressCode.add(condLabel + ":");
                String condTemp = convertExpression(condExpr);
                threeAddressCode.add("IF " + condTemp + " GOTO " + startLabel);
                threeAddressCode.add("GOTO " + endLabel);
                
                threeAddressCode.add(endLabel + ":");
                
                return null;
            }
        
            // Print generated three-address code
            public void printThreeAddressCode() {
                for (String line : threeAddressCode) {
                    System.out.println(line);
                }
            }
            
            // print found warnnings as generating 3AC
            public void getWarnnings(){
              for (String warnning : warnningCollector) {
                  System.out.println(warnning);
              }
            }
        
            // Get the type of the symbol
            public static String getTypeOutput(){
              return typeOutput;
    }
}

// Placeholder for AST token types (you'd typically generate this from ANTLR)
class AST {
  public static final int EOF=-1;
	public static final int T__37=37;
	public static final int T__38=38;
	public static final int T__39=39;
	public static final int T__40=40;
	public static final int T__41=41;
	public static final int T__42=42;
	public static final int T__43=43;
	public static final int T__44=44;
	public static final int T__45=45;
	public static final int T__46=46;
	public static final int T__47=47;
	public static final int T__48=48;
	public static final int T__49=49;
	public static final int T__50=50;
	public static final int T__51=51;
	public static final int T__52=52;
	public static final int T__53=53;
	public static final int T__54=54;
	public static final int T__55=55;
	public static final int T__56=56;
	public static final int T__57=57;
	public static final int T__58=58;
	public static final int T__59=59;
	public static final int T__60=60;
	public static final int T__61=61;
	public static final int T__62=62;
	public static final int T__63=63;
	public static final int T__64=64;
	public static final int T__65=65;
	public static final int ASSIGN=4;
	public static final int COMMAND=5;
	public static final int COMMANDS=6;
	public static final int CONS=7;
	public static final int DIGIT=8;
	public static final int ELSECOMMANDS=9;
	public static final int EQUALS=10;
	public static final int EXPRESSION=11;
	public static final int EXPRESSIONS=12;
	public static final int EXPRS=13;
	public static final int EXPR_BASE=14;
	public static final int FOR=15;
	public static final int FOREACH=16;
	public static final int FUNCDEF=17;
	public static final int FUNCTION=18;
	public static final int FUNCTIONCALL=19;
	public static final int HEAD=20;
	public static final int IF=21;
	public static final int INPUT=22;
	public static final int LIST=23;
	public static final int NIL=24;
	public static final int NOP=25;
	public static final int OUTPUT=26;
	public static final int ROOT=27;
	public static final int SYMBOL=28;
	public static final int SYMBOL_LEX=29;
	public static final int TAIL=30;
	public static final int TOKENS=31;
	public static final int VARIABLE=32;
	public static final int VARIABLE_LEX=33;
	public static final int VARS=34;
	public static final int WHILE=35;
	public static final int WS=36;
    
}
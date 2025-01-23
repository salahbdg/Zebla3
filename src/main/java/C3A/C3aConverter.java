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

      System.out.println("ast id node : "+ast.getType());

      switch (ast.getType()) {
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
          default:
              // For nodes without specific conversion, try processing children
              if (ast.getChildren() != null) {
                  for (Object child : ast.getChildren()) {
                      convertNode((CommonTree) child);
                  }
              }
      }
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
            // return convertFor(baseExpr);
              return "FOR";

            case AST.FOREACH:
              return "FOREACH";

            default:
                throw new UnsupportedOperationException("Unsupported command type: " + cmdNode.getType());
        }
    }

    // Convert assignment
    private String convertAssignment(CommonTree assignNode) {
        CommonTree varsNode = (CommonTree) assignNode.getChild(0);
        CommonTree exprsNode = (CommonTree) assignNode.getChild(1);
        
        List<String> vars = new ArrayList<>();
        List<String> temps = new ArrayList<>();
        
        // Convert variables
        for (Object varObj : varsNode.getChildren()) {
            vars.add(varObj.toString());
        }
        
        // Convert expressions
        for (Object exprObj : exprsNode.getChildren()) {
            CommonTree exprNode = (CommonTree) exprObj;
            String temp = convertExpression(exprNode);
            temps.add(temp);
        }
        
        // Generate assignments
        for (int i = 0; i < Math.min(vars.size(), temps.size()); i++) {
            threeAddressCode.add(vars.get(i) + " := " + temps.get(i));
        }
        
        return null;
    }

    

    // Convert expression
    private String convertExpression(CommonTree exprNode) {
        
        CommonTree baseExpr = (CommonTree) exprNode.getChild(0);

        if (baseExpr == null) {
            warnningCollector.add("Warning: Empty expression found");
            return null;
        }

        getChilds(baseExpr);
        
        switch (baseExpr.getType()) {
            case AST.FUNCTIONCALL:
                return convertFunctionCall(baseExpr);
            case AST.VARIABLE:
                System.out.println(baseExpr.getChild(0).getText());
                return baseExpr.getChild(0).getText();
            case AST.NIL:
                return "nil";
            case AST.SYMBOL:
                return baseExpr.getText();
            case AST.EXPRESSION:
                return convertComparisonExpression(baseExpr);

            case AST.CONS:
                return convertConsExpression(baseExpr);

            case AST.SYMBOL_LEX:
                return baseExpr.getText();

            
              
              // here 
              
           
            default:
                throw new UnsupportedOperationException("Unsupported expression type: " + baseExpr.getType());
        }
    }

    // convert cons
    // Convert CONS expression
    // Convert CONS expression
    // Convert CONS expression
    // Convert CONS expression
    private String convertConsExpression(CommonTree consNode) {
      String temp = newTemp();
      List<String> args = new ArrayList<>();

      // check if cons has no child
      if (consNode.getChildCount() == 0) {
          warnningCollector.add("Warning: CONS without arguments found");
          threeAddressCode.add(temp + " := CONS(nil)");
          return temp;
      }

      // Handle arguments if present
      if (consNode.getChildCount() > 0) {
          for (Object argObj : consNode.getChildren()) {
              CommonTree argNode = (CommonTree) argObj;
              String argTemp = convertExpression(argNode);
              args.add(argTemp);
          }
      }

      // // check if cons has NIL as argument
      // if (args.size() == 1 && args.get(0).equals("nil")) {
      //     return "nil";
      // }

      // Generate three-address code for CONS with any number of arguments
      if (args.isEmpty()) {
          threeAddressCode.add(temp + " := CONS(nil)");
      } else {
          String currentTemp = temp;
          for (int i = 0; i < args.size(); i++) {
              String arg = args.get(i);
              if (i == 0) {
                  threeAddressCode.add(currentTemp + " := CONS(" + arg + ")");
              } else {
                  String newTemp = newTemp();
                  threeAddressCode.add(newTemp + " := CONS(" + currentTemp + ", " + arg + ")");
                  currentTemp = newTemp;
              }
          }
      }

      return temp;
    }

// // Convert FOR loop
// private String convertFor(CommonTree forNode) {
//     CommonTree varNode = (CommonTree) forNode.getChild(0);
//     CommonTree rangeNode = (CommonTree) forNode.getChild(1);
//     CommonTree commandsNode = (CommonTree) forNode.getChild(2);


    // Convert function call
    private String convertFunctionCall(CommonTree funcCallNode) {
        String funcName = funcCallNode.getChild(0).getText();
        String temp = newTemp();
        
        // Handle arguments if present
        if (funcCallNode.getChildCount() > 1) {
            CommonTree argsNode = (CommonTree) funcCallNode.getChild(1);
            List<String> args = new ArrayList<>();
            
            for (Object argObj : argsNode.getChildren()) {
                CommonTree argNode = (CommonTree) argObj;
                String argTemp = convertExpression(argNode);
                args.add(argTemp);
            }
            
            threeAddressCode.add(temp + " := CALL " + funcName + "(" + String.join(", ", args) + ")");
        } else {
            threeAddressCode.add(temp + " := CALL " + funcName);
        }
        
        return temp;
    }

    // Convert comparison expression
    private String convertComparisonExpression(CommonTree exprNode) {
        if (exprNode.getType() == AST.EXPRESSION && exprNode.getChild(0).getType() == AST.EQUALS) {
            CommonTree equalsNode = (CommonTree) exprNode.getChild(0);
            String left = convertExpression((CommonTree) equalsNode.getChild(0));
            String right = convertExpression((CommonTree) equalsNode.getChild(1));
            
            String temp = newTemp();
            threeAddressCode.add(temp + " := " + left + " = " + right);
            return temp;
        }
        
        // If not an equals expression, just convert the base expression
        return convertExpression((CommonTree) exprNode.getChild(0));
    }

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
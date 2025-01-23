tree grammar ASTWalker;

options {
    tokenVocab=AST;
    ASTLabelType=CommonTree;
}

// The list to hold the generated three-address code
@members {
    List<String> threeAddressCode = new ArrayList<>();
    int tempCounter = 0;
    int labelCounter = 0;

    String newTemp() {
        return "t" + (tempCounter++);
    }

    String newLabel() {
        return "L" + (labelCounter++);
    }

    void emit(String code) {
        threeAddressCode.add(code);
    }

    void printThreeAddressCode() {
        for (String line : threeAddressCode) {
            System.out.println(line);
        }
    }
}

program: ^(ROOT function+) { printThreeAddressCode(); };

function: ^(FUNCTION SYMBOL_LEX ^(FUNCDEF input? ^(COMMANDS commands) ^(OUTPUT output))) {
    emit("# Function: " + $SYMBOL_LEX.text);
};

input: ^(INPUT inputsub?)?;

output: VARIABLE_LEX+ { emit("# Output: " + $VARIABLE_LEX.text); };

commands: command+;

command
    : ^(ASSIGN ^(VARS vars) ^(EXPRESSIONS exprs)) {
          String temp = newTemp();
          emit(temp + " = " + $exprs.text);
          emit($vars.text + " = " + temp);
      }
    | ^(IF expression ^(COMMANDS ifCommands) ^(ELSECOMMANDS elseCommands)?) {
          String trueLabel = newLabel();
          String endLabel = newLabel();
          emit("if " + $expression.text + " goto " + trueLabel);
          if ($elseCommands != null) {
              emit("goto " + endLabel);
              emit(trueLabel + ":");
              // Process ifCommands
              emit(endLabel + ":");
          } else {
              emit(trueLabel + ":");
              // Process ifCommands
          }
      }
    | ^(WHILE expression ^(COMMANDS commands)) {
          String startLabel = newLabel();
          String endLabel = newLabel();
          emit(startLabel + ":");
          emit("if not " + $expression.text + " goto " + endLabel);
          // Process commands
          emit("goto " + startLabel);
          emit(endLabel + ":");
      }
    ;

expression
    : ^(EXPRESSION ^(EQUALS e1=exprbase e2=exprbase)) {
          String temp1 = newTemp();
          String temp2 = newTemp();
          emit(temp1 + " = " + $e1.text);
          emit(temp2 + " = " + $e2.text);
          emit(temp1 + " == " + temp2);
      }
    | exprbase
    ;

exprbase
    : ^(FUNCTIONCALL SYMBOL_LEX lexpr?) {
          emit("# Function call: " + $SYMBOL_LEX.text);
      }
    | ^(HEAD exprbase) {
          String temp = newTemp();
          emit(temp + " = head(" + $exprbase.text + ")");
      }
    | ^(TAIL exprbase) {
          String temp = newTemp();
          emit(temp + " = tail(" + $exprbase.text + ")");
      }
    | ^(CONS lexpr?) {
          String temp = newTemp();
          emit(temp + " = cons(" + $lexpr.text + ")");
      }
    | NIL { emit("nil"); }
    | ^(VARIABLE VARIABLE_LEX) { emit($VARIABLE_LEX.text); }
    | ^(SYMBOL SYMBOL_LEX) { emit($SYMBOL_LEX.text); }
    ;

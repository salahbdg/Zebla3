package translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import C3A.C3aConverter;

/*
 * Here we encountered a problem of how to handle type of conversion,
 * meaning whether to simulate the tree as int, string, or any other type.
 * 
 * Our idea emerged from noticing that the last node SYMBOL in ast can be in three forms:
 * 1. int
 * 2. string
 * 3. bool
 * 4. or just a string ( like in cons.while where no type is specified ) 
 * 
 * so we will store type in typeOutput in C3aConverter.java
 * 
 * then based on typeOutput we will provide how binary tree is processed, thereby how translation is done.
 * For each type we will have a method in PythonTranslator.java
 */

public class PythonTranslator {


  // public String translate(List<String> threeAddressCode) {
  //   if (C3aConverter.getTypeOutput().equals("int")) {
  //     return translateInt(threeAddressCode);
  //   } else if (C3aConverter.getTypeOutput().equals("string")) {
  //     return translateString(threeAddressCode);
  //   } else if (C3aConverter.getTypeOutput().equals("bool")) {
  //     return translateBool(threeAddressCode);
  //   } else {
  //     return translateDefault(threeAddressCode);
  //   }
  // }

  // public String translateInt(List<String> threeAddressCode){
  //   return "int";
  // }

  // public String translateString(List<String> threeAddressCode){
  //   return "string";
  // }

  // public String translateBool(List<String> threeAddressCode){
  //   return "bool";
  // }

  // public String translateDefault(List<String> threeAddressCode){
  //   return "default";
  // }

  private List<String> pythonCode;
  private int indentLevel;
  private Map<String, String> labels;
  private boolean inLoop;
  private String currentLoopCondition;
  
  public PythonTranslator() {
      this.pythonCode = new ArrayList<>();
      this.indentLevel = 0;
      this.labels = new HashMap<>();
      this.inLoop = false;
  }
  
  public List<String> translate(List<String> threeAddressCode) {
      pythonCode.clear();
      
      // Add CONS helper function
      pythonCode.add("# Define the CONS function that creates a pair (a list with two elements)");
      pythonCode.add("def CONS(a, b):");
      pythonCode.add("    return (a, b)");
      pythonCode.add("");

      // add helper function to translate from while binary tree to python
      if (C3aConverter.getTypeOutput().equals("int")) {
        pythonCode.add("# Function to pretty-print a binary tree as an integer");
        pythonCode.add("def tree_to_int(tree):");
        pythonCode.add("    count = 0");
        pythonCode.add("    while tree is not None:");
        pythonCode.add("        count += 1");
        pythonCode.add("        tree = tree[1]");
        pythonCode.add("    return count");
        
      } else if (C3aConverter.getTypeOutput().equals("string")) {
        pythonCode.add("# Define the ADD function that concatenates two strings");
        pythonCode.add("def tree_to_string(tree):");
        pythonCode.add("    result = ''");
        pythonCode.add("    for element in tree:");
        pythonCode.add("        if element is None:");
        pythonCode.add("            continue");
        pythonCode.add("        elif isinstance(element, tuple):");
        pythonCode.add("            result += tree_to_string(element)");
        pythonCode.add("        else:");
        pythonCode.add("            result += str(element)");
        pythonCode.add("    return result");
      } else if (C3aConverter.getTypeOutput().equals("bool")) {
        pythonCode.add("# Define the ADD function that concatenates two strings");
        pythonCode.add("def tree_to_bool(tree):");
        pythonCode.add("    result = False");
        pythonCode.add("    for element in tree:");
        pythonCode.add("        if element is None:");
        pythonCode.add("            continue");
        pythonCode.add("        elif isinstance(element, tuple):");
        pythonCode.add("            result = result or tree_to_bool(element)");
        pythonCode.add("        else:");
        pythonCode.add("            result = result or element");
        pythonCode.add("    return result");
      } else {
        pythonCode.add("# Define the ADD function that concatenates two strings");
        pythonCode.add("def tree_to_default(tree):");
        pythonCode.add("    result = ''");
        pythonCode.add("    for element in tree:");
        pythonCode.add("        if element is None:");
        pythonCode.add("            continue");
        pythonCode.add("        elif isinstance(element, tuple):");
        pythonCode.add("            result += tree_to_default(element)");
        pythonCode.add("        else:");
        pythonCode.add("            result += str(element)");
        pythonCode.add("    return result");
      }
      
      for (String line : threeAddressCode) {
          translateLine(line.trim());
      }
      
      // Add main call and print at the end
      pythonCode.add("# Calling main and printing the result");
      pythonCode.add("result = main()");
      pythonCode.add("print(\"For:\", result)");
      
      return pythonCode;
  }
  
  private void translateLine(String line) {
      if (line.startsWith("FUNCTION")) {
          handleFunction(line);
      } else if (line.startsWith("PARAM")) {
          handleParam(line);
      } else if (line.startsWith("END FUNCTION")) {
          handleEndFunction(line);
      } else if (line.matches("L\\d+:")) {
          // Ignore label lines as they're handled differently
      } else if (line.startsWith("RETURN")) {
          handleReturn(line);
      } else if (line.startsWith("IF")) {
          handleIf(line);
      } else if (line.startsWith("GOTO")) {
          handleGoto(line);
      } else if (line.contains(":=")) {
          handleAssignment(line);
      }
  }
  

  
  private void handleParam(String line) {
      String param = line.substring(6);
      // Find last function definition and add parameter
      for (int i = pythonCode.size() - 1; i >= 0; i--) {
          if (pythonCode.get(i).startsWith("def ")) {
              String funcDef = pythonCode.get(i);
              funcDef = funcDef.replace("():", "(" + param + "):");
              pythonCode.set(i, funcDef);
              break;
          }
      }
  }
  
  private void handleEndFunction(String line) {
      indentLevel--;
      addLine("");  // Add blank line between functions
  }
  
  private void handleReturn(String line) {
      String value = line.substring(7);
      addLine("return " + value);
  }
  
  private void handleAssignment(String line) {
      String[] parts = line.split(":=");
      String target = parts[0].trim();
      String value = parts[1].trim();
      
      if (value.startsWith("CONS")) {
          value = value.replace("nil", "None");
          addLine(target + " = " + value);
      } else if (value.startsWith("CALL")) {
          value = value.substring(5);
          addLine(target + " = " + value);
      } else {
          if (value.contains("<")) {
              currentLoopCondition = target + " = " + translateCondition(value);
              return;
          }
          addLine(target + " = " + value);
      }
  }
  

  

  
  private void addLine(String line) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < indentLevel; i++) {
          sb.append("    ");
      }
      sb.append(line);
      pythonCode.add(sb.toString());
  }

  private void handleFunction(String line) {
    String funcName = line.substring(9, line.length() - 1);
    addLine("def " + funcName + "():");
    indentLevel++;
}

private void handleIf(String line) {
    if (line.startsWith("IF NOT")) {
        String condition = currentLoopCondition;
        addLine("while " + condition + ":");
        indentLevel++;
        inLoop = true;
    }
}

private void handleGoto(String line) {
    if (inLoop) {
        addLine("    continue");
    }
}

private String translateCondition(String condition) {
    if (condition.contains("<")) {
        String[] parts = condition.split("<");
        String left = parts[0].trim();
        String right = parts[1].trim();
        return left + " < " + right;
    }
    return condition;
}


}

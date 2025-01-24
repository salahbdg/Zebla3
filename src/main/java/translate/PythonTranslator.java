package translate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PythonTranslator {


  public String translate(List<String> threeAddressCode) {
    StringBuilder pythonCode = new StringBuilder();
    Map<String, String> functionCode = new HashMap<>();
    String currentFunction = "";

    for (String line : threeAddressCode) {
        if (line.startsWith("FUNCTION")) {
            currentFunction = line.split(" ")[1].split(":")[0];
            functionCode.put(currentFunction, "def " + currentFunction + "(");
        } else if (line.startsWith("PARAM")) {
            String param = line.split(" ")[1];
            functionCode.put(currentFunction, functionCode.get(currentFunction) + param + ", ");
        } else if (line.startsWith("END FUNCTION")) {
            String funcCode = functionCode.get(currentFunction);
            funcCode = funcCode.substring(0, funcCode.length() - 2) + "):\n";
            pythonCode.append(funcCode);
            currentFunction = "";
        } else if (line.startsWith("RETURN")) {
            String returnVar = line.split(" ")[1];
            pythonCode.append("    return ").append(returnVar).append("\n\n");
        } else if (line.contains(" := ")) {
            String[] parts = line.split(" := ");
            String leftVar = parts[0].trim();
            String rightExpr = parts[1].trim();

            if (rightExpr.startsWith("CONS")) {
                String[] consParams = rightExpr.substring(5, rightExpr.length() - 1).split(", ");
                String leftParam = translateParam(consParams[0]);
                String rightParam = translateParam(consParams[1]);
                pythonCode.append("    ").append(leftVar).append(" = (").append(leftParam).append(", ").append(rightParam).append(")\n");
            } else if (rightExpr.startsWith("CALL")) {
                String[] callParts = rightExpr.split("\\(");
                String funcName = callParts[0].substring(5);
                String[] callParams = callParts[1].substring(0, callParts[1].length() - 1).split(", ");
                pythonCode.append("    ").append(leftVar).append(" = ").append(funcName).append("(");
                for (String param : callParams) {
                    pythonCode.append(param).append(", ");
                }
                if (callParams.length > 0) {
                    pythonCode.delete(pythonCode.length() - 2, pythonCode.length());
                }
                pythonCode.append(")\n");
            } else {
                pythonCode.append("    ").append(leftVar).append(" = ").append(rightExpr).append("\n");
            }
        } else if (line.startsWith("IF")) {
            String[] parts = line.split(" ");
            String condition = parts[1];
            String label = parts[3];
            pythonCode.append("    if not ").append(condition).append(":\n");
            pythonCode.append("        break\n");
        } else if (line.startsWith("GOTO")) {
            // Ignore GOTO statements
        } else if (!line.isEmpty()) {
            pythonCode.append("    ").append(line).append("\n");
        }
    }

    // Add main function call at the end
    pythonCode.append("result = main()\n");
    pythonCode.append("print(result)\n");

    return pythonCode.toString();
  }

  private String translateParam(String param) {
      if (param.equals("nil")) {
          return "None";
      } else if (param.equals("int")) {
          return "'int'";
      } else {
          return param;
      }
  }

}

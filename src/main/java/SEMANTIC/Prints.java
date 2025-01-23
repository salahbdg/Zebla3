package SEMANTIC;

import org.antlr.runtime.tree.Tree;

public class Prints {


  public static void printBanner(String filepath) {
    System.out.println("\nSEMANTIC ANALYSIS : " + filepath + "\n");
  }

  public static void printReportBanner(int errorCount) {
    String message = "\nSEMANTIC REPORT" + "\n" + "-------------------";

    String reportmessage = errorCount + " ERROR(S) FOUND";

    System.out.println(message + "\n" + reportmessage + "\n");
  }

  public static void printError(String message, int line, int charPositionInLine) {
    System.err.println("Error: " + message + " at line " + line + " char " + charPositionInLine);
  }

  public static void printError(String message) {
    System.err.println("Error: " + message);
  }

  public static void Warnning(String message, Tree tree) {
    System.err.println("Warning: " + message + " at line " + tree.getLine() + " char " + tree.getCharPositionInLine());
  }
}

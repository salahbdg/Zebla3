package runner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.DOTTreeGenerator;
import org.antlr.runtime.tree.Tree;

import AST.ASTLexer;
import AST.ASTParser;
import C3A.C3aConverter;

import SEMANTIC.SemanticChecker;
import translate.PythonTranslator;

//import translate.PythonTranslator;

public class Main {

    // public static boolean execute = false;
    // public static boolean verbose = false;
    // public static boolean debug = false;

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("No input file provided\nUsage: java -jar <jarfile> <inputfile>");
            System.exit(1);
        }


        String filepath = args[0];
        File filepathAsFile = new File(filepath);
        //String program_while = filepathAsFile.getAbsolutePath();

        // file does not exist
        if (!filepathAsFile.exists()) {
            System.err.println("File " + filepath + " does not exist");
            System.exit(1);
        }

        // Parse the input file
        CharStream cs = new ANTLRFileStream(filepath);
        ASTLexer lexer = new ASTLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream();
        tokens.setTokenSource(lexer);
        ASTParser parser = new ASTParser(tokens);
        ASTParser.program_return result = parser.program();

        
        // For debugging purposes : VISUALIZE AST FOR PROGRAM IN ./ast.dot, add graphviz extension to preview it
        System.out.println("### VISUALIZE AST FOR PROGRAM IN ./ast.dot ###");
        CommonTree ast = (CommonTree) result.getTree();
        DOTTreeGenerator gen = new DOTTreeGenerator();
        StringTemplate dot =  gen.toDOT(ast);

        try (FileWriter writer = new FileWriter("./ast.dot")) {
          writer.write(dot.toString());
          System.out.println("DOT representation has been written to ast.dot");
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Check for semantic error by traversing the AST
        SemanticChecker checker = new SemanticChecker();
        if (checker.check(ast)){
            System.out.println("\n\033[1;32m\033[1m### Semantic check passed ###\033[0m\n");
        } else {
            System.out.println("\n\033[1;31m### Semantic check failed ###\033[0m\n");
            checker.printErrors();
            System.exit(1);
        }


        // Genrate 3-address code
        C3aConverter converter = new C3aConverter();
        List<String> threeAddressCode = converter.convert(ast);

        System.out.println("\n\033[1;33m### Warnings found during 3AC generation ###\033[0m\n");
        converter.getWarnnings();

        System.out.println("\n\033[1;34m### 3-Address Code ###\033[0m\n");
        System.out.println(threeAddressCode.size() + " lines of code");


        for (String line : threeAddressCode) {
            System.out.println(line);
        }


        // // Generate 3-address code
        System.out.println("\n\033[1;34m### Python translation ###\033[0m\n");
        PythonTranslator python = new PythonTranslator();
        List<String> pythoncode = python.translate(threeAddressCode);

        for (String line : pythoncode) {
            System.out.println(line);
        }

        // Write the Python code to a file named generatepython.py
        try (FileWriter writer = new FileWriter("generatepython.py")) {
          for (String line : pythoncode) {
            writer.write(line + System.lineSeparator());
          }
          System.out.println("Python code has been written to generatepython.py");
        } catch (IOException e) {
          e.printStackTrace();
        }
        //System.out.println(pythoncode);
        




        // // Generate target code from 3-address code
        // String basePythonFilePath = "resources/base.py";
        // if (verbose)
        //     basePythonFilePath = "resources/base_verbose.py";
        // BufferedReader reader = new BufferedReader(
        //         new InputStreamReader(new FileInputStream(basePythonFilePath), "UTF-8"));
        // String basePythonFile = reader.lines().reduce("", (a, b) -> a + b + "\n");
        // reader.close();
        // PythonTranslator python = new PythonTranslator();
        // String outputPython = basePythonFile.replaceFirst("# CODE INSERTED HERE", python.from(code3adress));

        // // Write output to file
        // Path path = Paths.get("output.py");
        // Files.write(path, outputPython.getBytes());
        // if (verbose)
        //     System.out.println("File output.py generated at " + path.toAbsolutePath().toString());

        // // Run the output file with python
        // if (execute) {
            
        //     ProcessBuilder OSRunner;
        //     if (System.getProperty("os.name").toLowerCase().contains("win")) {
        //       OSRunner =  new ProcessBuilder("python", "output.py"); 
        //     } else {
        //       OSRunner =  new ProcessBuilder("python3", "output.py");
        //     }
        //     OSRunner.inheritIO();
        //     Process process = OSRunner.start();
        //     process.waitFor();
        // }
    }
}

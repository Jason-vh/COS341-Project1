package Parser;

import Tokens.Token;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    private Stack<Token> tokens = new Stack<>();
    private SyntaxTree tree = new SyntaxTree();

    public void parse(ArrayList<Token> _tokens) {
        for (int x = _tokens.size()-1; x >= 0; x--) {
            tokens.push(_tokens.get(x));
        }

        if (tokens.size() > 0)
            parseProgram(tree.root);
//        tree.print();
//        tree.DFS();
        tree.generateGraph();
    }

    private void parseProgram(Node parent) {
        if (tokens.size() == 0)
            return;

        Node n = tree.addNode("PROG", "NT", parent);

        parseCode(n);
        parseProcedureDefinitions(n);
    }

    private void parseCode(Node parent) {
        Node n = tree.addNode("CODE", "NT", parent);
//        Node n = parent;
        if (tokens.size() == 0) {
            return;
        }

        String e = tokens.peek().getExpressionType();
        String i = tokens.peek().getExpression();
        System.out.println("Parsing CODE - " + i);
        switch (e) {
            case "Variable":
                parseInstruction(n);
                parseCode(n);
                break;
            case "Grouping":
                if (i.equals(";")) {
                    popToken(";", n);
                    parseCode(n);
                }
                break;
            case "IO":
            case "Halt":
            case "ControlStructure":
                if (i.equals("then") || i.equals("else")) {
                    error("Unexpected 'then' or 'else'");
                }
                parseInstruction(n);
                parseCode(n);
                break;
        }

    }

    private void parseProcedureDefinitions(Node parent) {
        Node n = parent;
        if (tokens.size() == 0)
            return;

        String i = tokens.peek().getExpression();

        if (i.equals(";")) {
            parseSemicolon(n);
            parseProcedure(n);
        }
        if (i.equals("proc")) {
            parseProcedureDefinition(n);
        }
    }

    private void parseProcedure(Node parent) {
        Node n = tree.addNode("PROC", "NT", parent);

        if (tokens.size() == 0)
            return;

        String i = tokens.peek().getExpression();
        if (i.equals("proc")) {
            popToken("Procedure", n);
            popToken("Variable", n);
            popToken("{", n);
            parseProgram(n);
            popToken("}", n);
            parseSemicolon(n);
        } else {
            error("Expected procedure definition.");
        }
    }

    private void parseProcedureDefinition(Node parent) {
        Node n = parent;
        if (tokens.size() == 0)
            return;

        String i = tokens.peek().getExpression();
        if (i.equals("proc")) {
            parseProcedure(n);
            parseProcedureDefinition(n);
        }
    }

    private void parseInstruction(Node parent) {
        Node n = tree.addNode("INSTR", "NT", parent);
        if (tokens.size() == 0) {
            error("Uh there should be something here");
        }

        String e = tokens.peek().getExpressionType();
        String i = tokens.peek().getExpression();
        if (e.equals("Variable")) {
            Token t = tokens.pop();
            if (tokens.peek() != null && tokens.peek().getExpressionType().equals("AssignmentOp")) {
                // Its an assign
                tokens.push(t);
                parseAssignment(n);
            } else {
                // It's a call
                tokens.push(t);
                popToken("Variable", n);
            }
        } else if (e.equals("IO")) {
            parseInputOutput(n);
        } else if (i.equals("halt")) {
            popToken("halt", n);
        } else if (i.equals("if")) {
            parseConditional(n);
        } else if (i.equals("while") || i.equals("for")) {
            parseLoop(n);
        } else {
            error("Expecting variable, IO instruction, halt, if, or loop statement.");
        }
        parseSemicolon(n);
    }

    private void parseAssignment(Node parent) {
        Node n = tree.addNode("ASSIGN", "NT", parent);

        if (tokens.size() == 0)
            error("Uh something should be here");

        String e = tokens.peek().getExpressionType();
        if (e.equals("Variable")) {
            popToken("Variable", n);
            popToken("AssignmentOp", n);
            parseVariable(n);
        } else {
            error("Expected a variable before the assignment operator, got " + e);
        }
    }

    private void parseVariable(Node parent) {
        Node n = parent;

        if (tokens.size() == 0)
            return;

        String e = tokens.peek().getExpressionType();
        switch (e) {
            case "Variable":
                n = tree.addNode("VAR", "NT", parent);
                popToken("Variable", n);
                break;
            case "ShortString":
                n = tree.addNode("SVAR", "NT", parent);
                popToken("ShortString", n);
                break;
            case "Integer":
            case "NumberOp":
                parseNumericalExpression(n);
                break;
            default:
                error("Expected variable name, string literal or numerical expression");
                break;
        }
    }

    private void parseNumericalOperation(Node parent) {
        Node n = tree.addNode("CALC", "NT", parent);

        if (tokens.size() == 0)
            return;

        String e = tokens.peek().getExpressionType();
        if (e.equals("NumberOp")) {
            popToken("NumberOp", n);
            popToken("(", n);
            parseNumericalExpression(n);
            popToken(",", n);
            parseNumericalExpression(n);
            popToken(")", n);
        } else {
            error("Expected numerical operation, got " + tokens.peek().getExpression());
        }
    }

    private void parseNumericalExpression(Node parent) {
        Node n = tree.addNode("NUMEXP", "NT", parent);
        if (tokens.size() == 0)
            return;

        String e = tokens.peek().getExpressionType();
        switch (e) {
            case "Variable":
                popToken("Variable", n);
                break;
            case "NumberOp":
                parseNumericalOperation(n);
                break;
            case "Integer":
                popToken("Integer", n);
                break;
            default:
                error("Expecting variable name, integer literal or numerical operator; got " + tokens.peek().getExpression());
                break;
        }
    }

    private void parseInputOutput(Node parent) {
        Node n = tree.addNode("IO", "NT", parent);

        if (tokens.size() == 0)
            error("Uh something should be here");

        String e = tokens.peek().getExpressionType();
        if (e.equals("IO")) {
            popToken("IO", n);
            popToken("(", n);
            popToken("Variable", n);
            popToken(")", n);
            parseSemicolon(n);
        } else {
            error("Error: Expecting IO instruction, got " + e);
        }
    }

    private void parseConditional(Node parent) {
        Node n = tree.addNode("COND_BRANCH", "NT", parent);

        if (tokens.size() == 0)
            error("Uh there should be more stuff here");

        String i = tokens.peek().getExpression();

        if (i.equals("if")) {
            popToken("if", n);
            popToken("(", n);
            parseBool(n);
            popToken(")", n);
            popToken("then", n);
            popToken("{", n);
            parseCode(n);
            popToken("}", n);
            parseElse(n);
        }

    }

    private void parseElse(Node parent) {
        Node n = tree.addNode("ELSE", "NT", parent);

        String i = tokens.peek().getExpression();
        if (i.equals("else")) {
            popToken("else", n);
            popToken("{", n);
            parseCode(n);
            popToken("}", n);
        }
    }

    private void parseBool(Node parent) {
        Node n = tree.addNode("BOOL", "NT", parent);

        if (tokens.size() == 0)
            error("Uh there should be something else here");

        String i = tokens.peek().getExpression();

        switch (i) {
            case "(":
                popToken("(", n);
//                popToken("Variable");
                parseVariable(n);
                parseEquality(n);
//                popToken("Variable");
                parseVariable(n);
                popToken(")", n);
                break;
            case "eq":
                popToken("eq", n);
                popToken("(", n);
                parseVariable(n);
                popToken(",", n);
                parseVariable(n);
                popToken(")", n);
                break;
            case "and":
            case "or":
                popToken("BooleanOp", n);
                popToken("(", n);
                parseBool(n);
                popToken(",", n);
                parseBool(n);
                popToken(")", n);
                break;
            case "not":
                popToken("not", n);
                parseBool(n);
                break;
            default:
                error("Expected boolean expression, got " + i);
        }
    }

    private void parseLoop(Node parent) {
        Node n = tree.addNode("COND_LOOP", "NT", parent);

        if (tokens.size() == 0) {
            error("Uh there should be something else here");
        }

        String i = tokens.peek().getExpression();

        switch (i) {
            case "while":
                popToken("while", n);
                popToken("(", n);
                parseBool(n);
                popToken(")", n);
                popToken("{", n);
                parseCode(n);
                popToken("}", n);
                break;
            case "for":
                popToken("for", n);
                popToken("(", n);
                parseVariable(n);
                popToken("=", n);
                popToken("0", n);
                popToken(";", n);
                parseVariable(n);
                popToken("=", n);
                popToken("add", n);
                popToken("(", n);
                parseVariable(n);
                popToken(",", n);
                popToken("1", n);
                popToken(")", n);
                popToken(")", n);

                popToken("{", n);
                parseCode(n);
                popToken("}", n);
                break;
            default:
                error("Expected for or while, got " + i);
                break;
        }
    }

    private void parseEquality(Node parent) {
        Node n = parent;
        if (tokens.size() == 0)
            error("Uh there should be something here");

        String i = tokens.peek().getExpression();
        switch (i) {
            case "<":
                popToken("<", n);
                break;
            case ">":
                popToken(">", n);
                break;
            default:
                error("Expected < or >, got " + i);
                break;
        }
    }

    private void parseSemicolon(Node parent) {
        Node n = parent;
        if (tokens.size() == 0)
            return;

        String i = tokens.peek().getExpression();
        if (i.equals(";")) {
            popToken(";", n);
        }
    }

    private void popToken(String t, Node n) {
        if (tokens.size() == 0) {
            error("Uh we ran out of things when we shouldn't have");
            return;
        }

        if (t.equals(tokens.peek().getExpression()) || t.equals(tokens.peek().getExpressionType())) {
            switch (t) {
                case ";":
                case "(":
                case ")":
                case "{":
                case "}":
                case ",":
                    tokens.pop();
                    return;
            }
            tree.addNode(tokens.peek().getExpressionType(), tokens.peek().getExpression(), n);
            tokens.pop();
        } else {
            error("Unexpected token " + tokens.peek().getExpression() + ", expected " + t);
        }

    }

    private void error(String s) {
        System.out.println("Error: " + s + " at " + tokens.peek().getLocation());
//        tree.print();
        System.exit(0);
    }

    public void saveTreeToFile(String filename) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");

            for (Node n : tree.tree) {
                writer.println(n.toString());
            }

            writer.close();
        } catch (IOException e) {
            System.err.format("Exception occurred trying to print to %s", filename);
            e.printStackTrace();
        }

    }
}

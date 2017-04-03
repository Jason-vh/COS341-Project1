
import Tokens.*;
import Lexer.Tokenizer;
import Parser.*;


public class COS341_p1 {
	
	public static void main(String[] args) {
	    System.out.println("Initializing Lexer");
		Tokenizer tokenizer = new Tokenizer();

        tokenizer.linkFactory("Tokens.Assignment", AssignmentOp::new);
        tokenizer.linkFactory("Tokens.BooleanOp", BooleanOp::new);
        tokenizer.linkFactory("Tokens.Comparison", Comparison::new);
        tokenizer.linkFactory("Tokens.ControlStructure", ControlStructure::new);
        tokenizer.linkFactory("Tokens.Grouping", Grouping::new);
        tokenizer.linkFactory("Tokens.Halt", Halt::new);
        tokenizer.linkFactory("Tokens.Integer", Tokens.Integer::new);
        tokenizer.linkFactory("Tokens.NumberOp", NumberOp::new);
        tokenizer.linkFactory("Tokens.IO", IO::new);
        tokenizer.linkFactory("Tokens.Procedure", Procedure::new);
        tokenizer.linkFactory("Tokens.ShortString", ShortString::new);
        tokenizer.linkFactory("Test", TestToken::new);
        tokenizer.linkFactory("Tokens.Variable", Variable::new);
        tokenizer.linkFactory("Tokens.WhiteSpace", Tokens.WhiteSpace::new);

        tokenizer.init("tokens.txt");

        System.out.println("Lexing complete, scanned in " + tokenizer.getTokens().size() + " tokens.");
        System.out.println("Writing tokens to file..");
		
		tokenizer.tokenize("data.txt");

		tokenizer.saveTokensToFile("lexerOutput.txt");

        System.out.println("\nInitializing Parser");
		Parser parser = new Parser();
		parser.parse(tokenizer.getTokens());

        System.out.println("Parsing complete. Writing to file..");
        parser.saveTreeToFile("parseOutput.txt");
	}
		
}

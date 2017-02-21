
import Tokens.*;


public class COS341_p1 {
	
	public static void main(String[] args) {
		if(args.length < 1){
			System.out.println("No file specified.");
			System.exit(1);
		}
		
		
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
		
		tokenizer.tokenize(args[0]);
		

		tokenizer.saveTokensToFile("output.txt");
		
	}
		
}

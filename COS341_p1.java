import java.io.*;
import java.util.HashMap;
import java.util.regex.*;

import java.util.function.*;

public class COS341_p1 {
	
	public static void main(String[] args) {
		 Function<String, Token> func = x -> new Token(x);
		
		Tokenizer tokenizer = new Tokenizer();
		tokenizer.linkFactory("Test",x -> new TestToken(x));
		tokenizer.linkFactory("Comparison",x -> new Comparison(x));
		tokenizer.init("tokens.txt");
		tokenizer.tokenize("data.txt");
		
	}
		
}
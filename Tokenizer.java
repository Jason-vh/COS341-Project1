import Tokens.Token;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Tokenizer {
	Tokenizer(){
	}
	void linkFactory(String key, Function<String, Token> factory){
		mInitTokenFactoryMap.put(key, factory);
	}
	void init(String tokenDefFilename){
		String globalExpression = "(^$)";
		int gorupCunter = 0;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(tokenDefFilename));
			String line;
			while ((line = reader.readLine()) != null){
				String key;
				String value;
				try{
					key = line.split(":")[0];
					value = line.substring(1 + key.length());
				}catch(Exception e){
					continue;
				}
				
				globalExpression += "|(" + value + ")";
				mGlobalPattern = Pattern.compile(globalExpression);
				Matcher matcher = mGlobalPattern.matcher("");
				Function<String, Token> factory = mInitTokenFactoryMap.get(key);
				if(factory == null){
					System.err.println("key " + key + " has no matching factory");
				}
				System.out.println(gorupCunter + ":" + matcher.groupCount() + " " + key + " => " + value);
				while(gorupCunter++ <= matcher.groupCount()){
					if(factory == null){
						mTokenFactoryVec.add(Token::new);
					}else{
						mTokenFactoryVec.add(factory);
					}
				}
				gorupCunter--;
			}
			reader.close();
		}catch (Exception e){
			System.err.format("Exception occurred trying to read '%s'.", tokenDefFilename);
			e.printStackTrace();
		}
	}
	
	void tokenize(String filename){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			for (int lineNum = 0; (line = reader.readLine()) != null; lineNum++){
				Matcher matcher = mGlobalPattern.matcher(line);
				int lineCursor = 0;
				while (matcher.find()) {
					for (int index = 1; index <= matcher.groupCount(); index++) {
						if(matcher.group(index) == null){
							continue;
						}
						if(lineCursor != matcher.start()){
							System.err.println(errUnexpectedStr(line.substring(lineCursor, matcher.start()), lineNum, lineCursor));
						}
						lineCursor = matcher.end();
						
						String test = matcher.group(index);
						Token token = mTokenFactoryVec.get(index).apply(matcher.group(index));
						if (token.isValidExpression()) {
                            mTokenVec.add(token);
                            token.debug(lineNum, matcher.start());
                        }
                        break;
					}
				}
				if(lineCursor < line.length()){
					System.err.println(errUnexpectedStr(line.substring(lineCursor), lineNum, lineCursor));
				}
			}
		}catch (Exception e){
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
		}
	}

	void saveTokensToFile(String filename) {
		int counter = 0;
		try {
			PrintWriter writer = new PrintWriter(filename, "UTF-8");

			for (Token t : mTokenVec) {
			    writer.format("[T%d,%s,%s]\n", counter, t.getExpressionType(), t.getExpression());
			}

			writer.close();
		} catch (IOException e) {
			System.err.format("Exception occurred trying to print to %s", filename);
			e.printStackTrace();
		}

	}

	private String errUnexpectedStr(String str, int line, int offest){
		return 	"@(" + (line + 1) + ":" + (offest + 1) + ") unexpected string |" + str + "|";
	}
	
	private HashMap	mInitTokenWordDefMap = new HashMap();
	private HashMap<String, Function<String, Token>> mInitTokenFactoryMap = new HashMap<>();
	
	private Vector<Function<String, Token>> mTokenFactoryVec = new Vector<>();//use to build tokens @ call tikenize
	
	private Vector<Token> mTokenVec = new Vector<>();
	
	private Pattern mGlobalPattern;
}

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
		String globalExpression = "(^$| )";//added a space to prevent detection as error 
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
				if(mDebugFlag)
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
		mTokenizeGood = true;
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
							tokenizeError(line, lineNum, lineCursor, matcher.start());
						}
						lineCursor = matcher.end();
						
						String test = matcher.group(index);
						Token token = mTokenFactoryVec.get(index).apply(matcher.group(index));
						if (token.isValidExpression()) {
                            mTokenVec.add(token);
                            if(mDebugFlag)
								token.debug(lineNum, matcher.start());
                        }
                        break;
					}
				}
				if(lineCursor < line.length()){
					tokenizeError(line, lineNum, lineCursor, line.length());
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
		if( ! mTokenizeGood){
			System.out.println("Detected one or more lexical errors, now terminating...");
			System.exit(1);
		}
	}

	protected String errUnexpectedStr(String str, int line, int offest){
		return 	"ERROR @(" + (line + 1) + ":" + (offest + 1) + ") Unexpected " + (str.length() == 1 ? "symbol " : "string ") + str;
	}
	protected void tokenizeError(String line, int lineNum, int offsetBegin, int offsetEnd){
		mTokenizeGood = false;
		System.out.println(errUnexpectedStr(line.substring(offsetBegin, offsetEnd), lineNum, offsetBegin));
		
		int shiftBegin = 0;
		int shiftEnd = offsetEnd;
		if(line.length() > 70){
			shiftBegin = Math.max(0, offsetBegin - 45);
			shiftEnd = Math.min(line.length(), shiftBegin + 70);
			line = (shiftBegin == 0 ? "" : "...") + line.substring(shiftBegin, shiftEnd) + (shiftEnd == line.length() ? "" : "...");
			if(shiftBegin > 0)
				shiftBegin -= 3;//for elips
		}
		System.out.println(line);
		while(offsetBegin-- > shiftBegin)
			System.out.print("-");
		System.out.println("^");
		int foo = 0;
		
	}
	
	private HashMap	mInitTokenWordDefMap = new HashMap();
	private HashMap<String, Function<String, Token>> mInitTokenFactoryMap = new HashMap<>();
	
	private Vector<Function<String, Token>> mTokenFactoryVec = new Vector<>();//use to build tokens @ call tikenize
	
	private Vector<Token> mTokenVec = new Vector<>();
	
	private Pattern mGlobalPattern;
	
	private boolean mDebugFlag = false;
	private boolean mTokenizeGood = true;
}

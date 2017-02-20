import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	Tokenizer(){
	}
	public void linkFactory(String key, Function<String, Token> factory){
		mInitTokenFactoryMap.put(key, factory);
	}
	public void init(String tokenDefFilename){
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
				Function<String, Token> factory = (Function<String, Token>)mInitTokenFactoryMap.get(key);
				if(factory == null){
					System.err.format("key " + key + " has no matching factory\n");
				}
				System.out.println(gorupCunter + ":" + matcher.groupCount() + " " + key + " => " + value);
				while(gorupCunter++ <= matcher.groupCount()){
					if(factory == null){
						mTokenFactoryVec.add(x->new Token(x));
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
	
	public void tokenize(String filename){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			for (int lineNum = 0; (line = reader.readLine()) != null; lineNum++){
				Matcher matcher = mGlobalPattern.matcher(line);
				
				while (matcher.find()) {
					for (int index = 1; index <= matcher.groupCount(); index++) {
						if(matcher.group(index) == null){
							continue;
						}
						String test = matcher.group(index);
						Token token = mTokenFactoryVec.get(index).apply(matcher.group(index));
						token.debug(lineNum, matcher.start());
						mTokenVec.add(token);
						break;
					}
				}
			}
		}catch (Exception e){
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
		}
	}
	
	
	private HashMap	mInitTokenWordDefMap = new HashMap();
	private HashMap	mInitTokenFactoryMap = new HashMap();
	
	private Vector<Function<String, Token>> mTokenFactoryVec = new Vector<Function<String, Token>>();//use to build tokens @ call tikenize
	
	private Vector<Token> mTokenVec = new Vector<Token>();
	
	private Pattern mGlobalPattern;
}

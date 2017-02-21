package Tokens;

public class Token {
	public void debug(int line, int offset){
		
		System.out.println(this.getClass().getSimpleName() + "(" + line + ":" + offset +")" + mValue);
	}

	public boolean isValidExpression() {
	    return true;
    }

    public String getExpressionType() {
	    return this.getClass().getSimpleName();
    }

    public String getExpression() {
	    return this.mValue;
    }

	public Token(String value){
		mValue = value;
	}
	
	private String mValue;
}

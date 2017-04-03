package Tokens;

public class Token {
    int lineNumber;
    int offset;

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

	public void setLocation(int line, int off) {
	    lineNumber = line+1;
	    offset = off;
    }

    public String getLocation() {
	    return "(" + lineNumber + ":" + offset + ")";
    }

	private String mValue;

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}

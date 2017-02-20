public class Token {
	public void debug(int line, int offset){
		
		System.out.println(this.getClass().getSimpleName() + "(" + line + ":" + offset +")" + mValue);
	}
	Token(String value){
		mValue = value;
	}
	
	private String mValue;
}

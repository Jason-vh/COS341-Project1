package Tokens;

public class WhiteSpace extends Token {
    public WhiteSpace(String value) {
        super(value);
    }

    @Override
    public boolean isValidExpression() {
        return false;
    }
}

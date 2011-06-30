package logic;

import java.util.Vector;
import java.util.Arrays;

/**
Formulas have a very simple grammar:

expr	:	atom
		|	comp
		;
		
atom	:	[A-Z].*
		;

comp	:	atom args
		;

args	:	LPAR seq RPAR
		;
		
seq		:	<empty>
		|	expr
		|	expr SEP seq
		;
		
Notice that "A," is a valid seq...
		
**/

class TokenStream {
	Vector<String> mTokens;
	int mStreamSize;
	int mPos;
	String mToken;
	String mLookAhead;
	
	public static String SEP = ",";
	public static String LPAR = "(";
	public static String RPAR = ")";
	public static String EOT = "\u0000";
		
	private static Vector<String> tokenize(String x) {
		String[] temp = x.replace(SEP, EOT+SEP+EOT)
			.replace(LPAR, EOT+LPAR+EOT)
			.replace(RPAR, EOT+RPAR+EOT)
			.replace(EOT+EOT, EOT)
			.split(EOT);
			
		Vector<String> tokens = new Vector<String>(Arrays.asList(temp));
		tokens.add(EOT);
		
		return tokens;
	}
	
	public boolean tokEOT() {
		return mToken.equals(EOT);
	}
	
	public boolean tokSEP() {
		return mToken.equals(SEP);
	}
	
	public boolean tokLPAR() {
		return mToken.equals(LPAR);
	}
	
	public boolean tokRPAR() {
		return mToken.equals(RPAR);
	}
	
	public boolean tokATOM() {
		return !(tokEOT() || tokSEP() || tokLPAR() || tokRPAR());
	}
	
	public boolean laSEP() {
		return mLookAhead.equals(SEP);
	}
	
	public boolean laEOT() {
		return mLookAhead.equals(EOT);
	}
	
	public boolean laLPAR() {
		return mLookAhead.equals(LPAR);
	}
	
	public boolean laRPAR() {
		return mLookAhead.equals(RPAR);
	}
	
	public boolean laATOM() {
		return !(laEOT() || laSEP() || laLPAR() || laRPAR());
	}
	
	public String token() {
		return mToken;
	}
	
	public String lookAhead() {
		return mLookAhead;
	}
	
	public String consume() {
		String t = mToken;
		next();
		return t;
	}
	
	public void next() {
		if (mPos < mStreamSize) {
			mPos++;
			mToken = mLookAhead;
			mLookAhead = mTokens.get(mPos);
		} else if (mPos == mStreamSize) {
			mToken = mLookAhead;
		}
	}

	public boolean end() {
		return tokEOT();
	}
	
	public void trace(String msg) {
		System.out.printf("TRACE:%s\n\ttoken: \"%s\"\tlookAhead: \"%s\"\n",msg,mToken,mLookAhead);
	}
	
	public void trace() {
		trace("");
	}
	
	public TokenStream(String x) {
		mTokens = tokenize(x);
		mStreamSize = mTokens.size() - 1;
		mPos = 1;
		mToken = mTokens.get(0);
		mLookAhead = mTokens.get(1);		
	}
}










public class Formula {
	
	private static Vector<Formula> parseSEQ(TokenStream t) {
		Vector<Formula> seq = new Vector<Formula>();

		
		if (t.tokRPAR()) {
			return seq;
		} else {
			
			Formula expr = parseEXPR(t);
			seq.add(expr);

			if (t.tokSEP()) {				
				t.consume();
				seq.addAll( parseSEQ(t) );
			}
			
			if (t.tokRPAR()) return seq;
			else return null;
		}
	}
	
	private static Vector<Formula> parseARGS(TokenStream t) {		
		Vector<Formula> args = new Vector<Formula>();
		
		if (t.tokLPAR()) {
			t.consume();
			args = parseSEQ(t);
			if (t.tokRPAR()) {
				t.consume();
				return args;
			} else return null;
		} else return null;
	}
	
	private static Formula parseCOMP(TokenStream t) {		
		if (t.tokATOM() && t.laLPAR()) {
			String op = t.consume();
			Vector<Formula> args = parseARGS(t);
			
			return new Formula(op, args);
		} else return null;
	}
	
	private static Formula parseATOM(TokenStream t) {
		if (t.tokATOM()) {
			Formula atom = new Formula(t.consume());
			return atom;
		} else return null;
	}
	
	private static Formula parseEXPR(TokenStream t) {
		if (t.laLPAR()) return parseCOMP(t);
		else return parseATOM(t);
	}
	
	public static Formula valueOf(String s) {	
		return parseEXPR(new TokenStream(s));
	}
	
	private String op;
	private Vector<Formula> args;
	

	public Formula(String op, Vector<Formula> args) {
		this.op = op;
		this.args = args;
	}
	
	public Formula(String op) {
		this(op, null);
	}
	
	public String toString() {
		String str = new String(op);
		if (args != null) {
			str += TokenStream.LPAR;
			for (Formula x : args) str += String.format("%s"+TokenStream.SEP,x);
			if (arity() > 0) str = str.substring(0, str.length()-1);
			str += TokenStream.RPAR;
		}
		
		return str;
	}
	
	public int arity() {
		return args.size();
	}
	
	public static void main(String[] args) {
		System.out.println(Formula.valueOf("Ola(Bom,Dia(DD,MM(),AAAA))"));
	}
}
package lexer;

import static control.Control.ConLexer.dump;

import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

import error.Error;
import lexer.Token.Kind;

public class Lexer
{
  String fname; // the input file name to be compiled
  InputStream fstream; // input stream for the above file
  
  
  private static int lineNum = 1;
  boolean isType = false;
  
  HashMap<Integer, Token> singals = new HashMap<Integer, Token>();
  HashMap<String, Token> nsingals = new HashMap<String, Token>();
  

  
  
  
	//数字
  private static Pattern patternNum = Pattern.compile("0|[1-9][0-9]*");
	//标识符
  private static Pattern patternAlpha = Pattern.compile("_|[A-Z]|[a-z]");
  private static Pattern patternAlphas = Pattern.compile("_|[A-Z]|[a-z]|[0-9]");
  //private static Pattern patternID = Pattern.compile("[_|[A-Z]|[a-z]][_|[0-9]|[A-Z]|[a-z]]*");
    //border
  
  
  public Lexer(String fname, InputStream fstream)
  {
    this.fname = fname;
    this.fstream = fstream;
    nsingals.put("boolean", new Token(Kind.TOKEN_BOOLEAN, lineNum));
    nsingals.put("class",new Token(Kind.TOKEN_CLASS, lineNum));
    nsingals.put("else",new Token(Kind.TOKEN_ELSE, lineNum));
    nsingals.put("extends",new Token(Kind.TOKEN_EXTENDS, lineNum));
    nsingals.put("false",new Token(Kind.TOKEN_FALSE, lineNum));
    nsingals.put("if",new Token(Kind.TOKEN_IF, lineNum));
    nsingals.put("int",new Token(Kind.TOKEN_INT, lineNum));
    nsingals.put("length",new Token(Kind.TOKEN_LENGTH, lineNum));
    nsingals.put("main",new Token(Kind.TOKEN_MAIN, lineNum));
    nsingals.put("new",new Token(Kind.TOKEN_NEW, lineNum));
    nsingals.put("out",new Token(Kind.TOKEN_OUT, lineNum));
    nsingals.put("println",new Token(Kind.TOKEN_PRINTLN, lineNum));
    nsingals.put("public",new Token(Kind.TOKEN_PUBLIC, lineNum));
    nsingals.put("return",new Token(Kind.TOKEN_RETURN, lineNum));
    nsingals.put("static",new Token(Kind.TOKEN_STATIC, lineNum));
    nsingals.put("String",new Token(Kind.TOKEN_STRING, lineNum));
    nsingals.put("System",new Token(Kind.TOKEN_SYSTEM, lineNum));
    nsingals.put("this",new Token(Kind.TOKEN_THIS, lineNum));
    nsingals.put("true",new Token(Kind.TOKEN_TRUE, lineNum));
    nsingals.put("void",new Token(Kind.TOKEN_VOID, lineNum));
    nsingals.put("while",new Token(Kind.TOKEN_WHILE, lineNum));
    
    singals.put(33, new Token(Kind.TOKEN_NOT, lineNum));		//'!'
    singals.put(40, new Token(Kind.TOKEN_LPAREN, lineNum));	//'('
    singals.put(41, new Token(Kind.TOKEN_RPAREN, lineNum));	//')'
    singals.put(42, new Token(Kind.TOKEN_TIMES, lineNum));	//'*'
    singals.put(43, new Token(Kind.TOKEN_ADD, lineNum));		//'+'
    singals.put(44, new Token(Kind.TOKEN_COMMER, lineNum));	//','
    singals.put(46, new Token(Kind.TOKEN_DOT, lineNum));		//'.'
    singals.put(59, new Token(Kind.TOKEN_SEMI, lineNum));		//';'
    singals.put(61, new Token(Kind.TOKEN_ASSIGN, lineNum));	//'='
    singals.put(45, new Token(Kind.TOKEN_SUB, lineNum));		//'-'
    singals.put(60, new Token(Kind.TOKEN_LT, lineNum));		//'<'
    singals.put(91, new Token(Kind.TOKEN_LBRACK, lineNum));	//'['
    singals.put(93, new Token(Kind.TOKEN_RBRACK, lineNum));		//']'
    singals.put(123, new Token(Kind.TOKEN_LBRACE, lineNum));	//'{'
    singals.put(125, new Token(Kind.TOKEN_RBRACE, lineNum));	//'}'


  }

  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception
  {
    int c = this.fstream.read();

    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c) {
    	if('\n' == c) ++lineNum;
    	c = this.fstream.read();
    }
    
    //处理注释
	while('/' == c){
		c = this.fstream.read();
		if(c == '/')
			while(c != '\n')
				c = this.fstream.read();
		else if(c == '*'){
			c = this.fstream.read();
			if('\n' == c) ++lineNum;
			while(true){
				if(c != '*') {
					c = this.fstream.read();
					if('\n' == c) ++lineNum;
					continue;
				}
				c = this.fstream.read();
				if(c != '/') {
					if('\n' == c) ++lineNum;
					continue;
				}
			}
		}else{
			System.out.println("invalid comment");
			System.exit(0);
		}
	    // skip all kinds of "blanks"
	    while (' ' == c || '\t' == c || '\n' == c) {
	    	if('\n' == c) ++lineNum;
	      c = this.fstream.read();
	    }
	}
	
    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c) {
    	if('\n' == c) ++lineNum;
      c = this.fstream.read();
    }
    
    //当遇到文件结束符"EOF"
    // The value for "lineNum" is now "null",
    // line number for the "EOF" token.
    if (-1 == c)
      return new Token(Kind.TOKEN_EOF, lineNum);
    
    switch (c) {
    case '+':
      return new Token(Kind.TOKEN_ADD, lineNum);
    case '-':
    	return new Token(Kind.TOKEN_SUB, lineNum);
    case '*':
    	return new Token(Kind.TOKEN_TIMES, lineNum);
    case '<':
    	return new Token(Kind.TOKEN_LT, lineNum);
    case '{':
    	return new Token(Kind.TOKEN_LBRACE, lineNum);
    case '}':
    	return new Token(Kind.TOKEN_RBRACE, lineNum);
    case '[':
    	return new Token(Kind.TOKEN_LBRACK, lineNum);
    case ']':
    	return new Token(Kind.TOKEN_RBRACK, lineNum);
    case '(':
    	return new Token(Kind.TOKEN_LPAREN, lineNum);
    case ')':
    	return new Token(Kind.TOKEN_RPAREN, lineNum);
    case '=':
    	return new Token(Kind.TOKEN_ASSIGN, lineNum);
    case ',':
    	return new Token(Kind.TOKEN_COMMER, lineNum);
    case '.':
    	return new Token(Kind.TOKEN_DOT, lineNum);
    case '!':
    	return new Token(Kind.TOKEN_NOT, lineNum);
    case ';':
    	return new Token(Kind.TOKEN_SEMI, lineNum);
	case '&':
		this.fstream.mark(1);
		c = this.fstream.read();
		if(c == '&'){
			return new Token(Kind.TOKEN_AND, lineNum);
		}else{
			this.fstream.reset();
			Error.error("error", "lexer", "'&' not allowed", lineNum);
			return new Token(Kind.TOKEN_AND, lineNum);
		}
    default:

    	String temp = "" + (char)c;
	    this.fstream.mark(1);
	    c = this.fstream.read();
	    	
	    if(patternNum.matcher(temp).matches()){
	    	//数字开头
	    	while(patternNum.matcher(""+(char)c).matches()){
			    temp += (char)c;
			    this.fstream.mark(1);
			    c = this.fstream.read();
			}
			this.fstream.reset();
			if(temp.startsWith("0") && 1 != temp.length()){
				Error.error("error", "lexer", "'&' not allowed", lineNum);
			    System.exit(0);
			}
			return new Token(Kind.TOKEN_NUM, lineNum, temp);
	    }else if(patternAlpha.matcher(temp).matches()){
	    	//字母或下滑线开头
			while(patternAlphas.matcher(""+(char)c).matches()){
			    temp += (char)c;
			    this.fstream.mark(1);
			    c = this.fstream.read();
			}
			this.fstream.reset();
			Token tt = nsingals.get(temp);
			if(null != tt) {
				tt.lineNum = lineNum;
				return tt;
			}
			return new Token(Kind.TOKEN_ID, lineNum, temp);
	    }else{
	    	Error.error("error", "lexer", "unrecognize char \"" + temp + "\"", lineNum);
	    	return nextTokenInternal();
	    	//return null;
	    }
	    }
    }
  
  public Token nextToken()
  {
	  
    Token t = null;

    try {
      t = this.nextTokenInternal();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (dump){
    	System.out.println(t.toString());
    	System.out.println("Lexer demp is ture");
    }
    return t;
  }
}

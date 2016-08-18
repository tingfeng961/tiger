package parser;

import java.util.LinkedList;

import org.omg.CORBA.Current;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;
import ast.Ast;
import ast.Ast.Dec;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Program.ProgramSingle;
import error.Error;

public class Parser {
	Lexer lexer;
	Token current;

	public Parser(String fname, java.io.InputStream fstream) {
		lexer = new Lexer(fname, fstream);
		current = lexer.nextToken();
	}

	// /////////////////////////////////////////////
	// utility methods to connect the lexer
	// and the parser.

	int head = 0;
	int pre = 0;
	Token[] tList = { null, null, null };

	private Token getToken(int index) {
		if (index > 0) {
			pre++;
			head = (++head) % 3;
			tList[head] = lexer.nextToken();
		}
		return tList[head];
	}

	private void advance() {
		 //System.out.println("" + current.kind + "   " + current.lineNum);
		if (0 == pre) {
			head = (++head) % 3;
			tList[head] = lexer.nextToken();
			current = tList[head];
		} else {
			pre--;
			current = tList[head];
		}
	}

	boolean isErr = false;
	Kind  kind1;
	
	private void eatToken(Kind kind) {
//		if(isErr)
//		if(kind != current.kind)
//		{
//			return;
//		}
//		isErr = false;
		if (kind == current.kind)
			advance();
		else {
//			Error.error("error", "Parse", "Expects: " + kind.toString()+". But got: " + current.kind.toString(), current.lineNum);
//			isErr = true;
//			while(current.kind != Token.Kind.TOKEN_SEMI && current.kind != Token.Kind.TOKEN_LBRACE && current.kind != Token.Kind.TOKEN_RBRACE)
//				advance();
//			System.out.println("ooooooooooooooooooooooo" + current.kind + "   " + current.lineNum);
			
			//System.exit(1);
		}
	}

	private void error() {
		Error.error("error", "Parse", "Syntax error: compilation aborting...", current.lineNum);
		//System.exit(1);
		return;
	}

	// ////////////////////////////////////////////////////////////
	// below are method for parsing.

	// A bunch of parsing methods to parse expressions. The messy
	// parts are to deal with precedence and associativity.

	// ExpList -> Exp ExpRest*
	// ->
	// ExpRest -> , Exp
	private LinkedList<ast.Ast.Exp.T> parseExpList() {
		if (current.kind == Kind.TOKEN_RPAREN)
			return null;
		LinkedList<ast.Ast.Exp.T> argsList = new LinkedList<ast.Ast.Exp.T>();
		argsList.add(parseExp());
		while (current.kind == Kind.TOKEN_COMMER) {
			advance();
			argsList.add(parseExp());
		}
		return argsList;
	}

	// AtomExp -> (exp)
	// -> INTEGER_LITERAL
	// -> true
	// -> false
	// -> this
	// -> id
	// -> new int [exp]
	// -> new id ()
	private ast.Ast.Exp.T parseAtomExp() {
		Integer lineNum;
		switch (current.kind) {
		case TOKEN_LPAREN:
			advance();
			ast.Ast.Exp.T exp = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			return exp;
		case TOKEN_NUM:
			int num = Integer.parseInt(current.lexeme);
			lineNum = current.lineNum;
			advance();
			return new ast.Ast.Exp.Num(num, lineNum);
		case TOKEN_TRUE:
			lineNum = current.lineNum;
			advance();
			return new ast.Ast.Exp.True(lineNum);
		case TOKEN_FALSE:
			lineNum = current.lineNum;
			advance();
			return new ast.Ast.Exp.False(lineNum);
		case TOKEN_THIS:
			advance();
			return new ast.Ast.Exp.This();
		case TOKEN_ID:
			String id = current.lexeme;
			lineNum = current.lineNum;
			advance();
			return new ast.Ast.Exp.Id(id, lineNum);
		case TOKEN_NEW: {
			advance();
			switch (current.kind) {
			case TOKEN_INT:
				lineNum = current.lineNum;
				advance();
				eatToken(Kind.TOKEN_LBRACK);
				ast.Ast.Exp.T exp1 = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.Ast.Exp.NewIntArray(exp1, lineNum);
			case TOKEN_ID:
				String id1 = current.lexeme;
				lineNum = current.lineNum;
				advance();
				eatToken(Kind.TOKEN_LPAREN);
				eatToken(Kind.TOKEN_RPAREN);
				return new ast.Ast.Exp.NewObject(id1, lineNum);
			default:
				error();
				return null;
			}
		}
		default:
			error();
			return null;
		}
	}

	// NotExp -> AtomExp
	// -> AtomExp .id (expList)
	// -> AtomExp [exp]
	// -> AtomExp .length
	private ast.Ast.Exp.T parseNotExp() {
		ast.Ast.Exp.T atomExp = parseAtomExp();
		while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {

			if (current.kind == Kind.TOKEN_DOT) {
				advance();
				Integer lineNum = current.lineNum;
				if (current.kind == Kind.TOKEN_LENGTH) {
					ast.Ast.Exp.T array = new ast.Ast.Exp.Length(atomExp);
					advance();
					return array;
				}

				String id = current.lexeme;
				eatToken(Kind.TOKEN_ID);
				eatToken(Kind.TOKEN_LPAREN);
				LinkedList<ast.Ast.Exp.T> args = parseExpList();
				eatToken(Kind.TOKEN_RPAREN);
				return new ast.Ast.Exp.Call(atomExp, id, args, lineNum);

			} else {
				advance();
				ast.Ast.Exp.T index = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.Ast.Exp.ArraySelect(atomExp, index);
			}
		}
		return atomExp;
	}

	// TimesExp -> ! TimesExp
	// -> NotExp
	private ast.Ast.Exp.T parseTimesExp() {
		while (current.kind == Kind.TOKEN_NOT) {
			advance();
			Integer lineNum = current.lineNum;
			return new ast.Ast.Exp.Not(parseExp(), lineNum);
		}
		return parseNotExp();
	}

	// AddSubExp -> TimesExp * TimesExp
	// -> TimesExp
	private ast.Ast.Exp.T parseAddSubExp() {
		ast.Ast.Exp.T timesExp1 = parseTimesExp();
		while (current.kind == Kind.TOKEN_TIMES) {
			Integer lineNum = current.lineNum;
			advance();
			ast.Ast.Exp.T timesExp = new ast.Ast.Exp.Times(timesExp1, parseExp(), lineNum);
			return timesExp;
		}
		return timesExp1;
	}

	// LtExp -> AddSubExp + AddSubExp
	// -> AddSubExp - AddSubExp
	// -> AddSubExp
	private ast.Ast.Exp.T parseLtExp() {
		ast.Ast.Exp.T andaddsubExp1 = parseAddSubExp();
		while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
			if (current.kind == Kind.TOKEN_ADD) {
				Integer lineNum = current.lineNum;
				advance();
				ast.Ast.Exp.T addExp = new ast.Ast.Exp.Add(andaddsubExp1,
						parseExp(), lineNum);
				return addExp;
			} else {
				Integer lineNum = current.lineNum;
				advance();
				ast.Ast.Exp.T subExp = new ast.Ast.Exp.Sub(andaddsubExp1,
						parseExp(), lineNum);
				return subExp;
			}
		}
		return andaddsubExp1;
	}

	// AndExp -> LtExp < LtExp
	// -> LtExp
	private ast.Ast.Exp.T parseAndExp() {
		ast.Ast.Exp.T ltExp1 = parseLtExp();
		while (current.kind == Kind.TOKEN_LT) {
			Integer lineNum = current.lineNum;
			advance();
			ast.Ast.Exp.Lt ltExp = new ast.Ast.Exp.Lt(ltExp1, parseExp(),
					lineNum);

			return ltExp;
		}
		return ltExp1;
	}

	// Exp -> AndExp && AndExp
	// -> AndExp
	private ast.Ast.Exp.T parseExp() {
		// System.out.println("-------------parseExp---------------");
		ast.Ast.Exp.T andExp1 = parseAndExp();
		while (current.kind == Kind.TOKEN_AND) {
			Integer lineNum = current.lineNum;
			advance();
			ast.Ast.Exp.And andExp = new ast.Ast.Exp.And(andExp1,
					parseExp(), lineNum);

			return andExp;

		}
		return andExp1;
	}

	// Statement -> { Statement* }
	// -> if ( Exp ) Statement else Statement
	// -> while ( Exp ) Statement
	// -> System.out.println ( Exp ) ;
	// -> id = Exp ;
	// -> id [ Exp ]= Exp ;
	private ast.Ast.Stm.T parseStatement() {

		//System.out.println("------------------parseStatement---------------------");
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a statement.
		if (current.kind == Kind.TOKEN_LBRACE) {
			advance();
			ast.Ast.Stm.T p = new ast.Ast.Stm.Block(parseStatements());
			eatToken(Kind.TOKEN_RBRACE);

			return p;
		} else if (current.kind == Kind.TOKEN_IF) {
			advance();
			eatToken(Kind.TOKEN_LPAREN);
			ast.Ast.Exp.T condition = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.Ast.Stm.T thenn = parseStatement();
			eatToken(Kind.TOKEN_ELSE);
			ast.Ast.Stm.T elsee = parseStatement();
			ast.Ast.Stm.T ifstm = new ast.Ast.Stm.If(condition, thenn, elsee);
			return ifstm;
		} else if (current.kind == Kind.TOKEN_WHILE) {
			advance();
			eatToken(Kind.TOKEN_LPAREN);
			ast.Ast.Exp.T condition = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			ast.Ast.Stm.T body = parseStatement();
			ast.Ast.Stm.T whileStm = new ast.Ast.Stm.While(condition, body);
			return whileStm;
		} else if (current.kind == Kind.TOKEN_SYSTEM) {
			advance();
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_OUT);
			eatToken(Kind.TOKEN_DOT);
			eatToken(Kind.TOKEN_PRINTLN);
			eatToken(Kind.TOKEN_LPAREN);
			ast.Ast.Exp.T exp = parseExp();
			eatToken(Kind.TOKEN_RPAREN);
			eatToken(Kind.TOKEN_SEMI);
			ast.Ast.Stm.T printStm = new ast.Ast.Stm.Print(exp);
			return printStm;
		} else if (current.kind == Kind.TOKEN_ID) {
			String id = current.lexeme;
			advance();
			if (current.kind == Kind.TOKEN_ASSIGN) {
				advance();
				ast.Ast.Exp.T exp = parseExp();
				ast.Ast.Stm.T assignStm = new ast.Ast.Stm.Assign(id, exp);
				eatToken(Kind.TOKEN_SEMI);
				return assignStm;
			} else {
				eatToken(Kind.TOKEN_LBRACK);
				ast.Ast.Exp.T index = parseExp();
				eatToken(Kind.TOKEN_RBRACK);
				eatToken(Kind.TOKEN_ASSIGN);
				ast.Ast.Exp.T exp = parseExp();
				eatToken(Kind.TOKEN_SEMI);
				ast.Ast.Stm.T assignStm = new ast.Ast.Stm.AssignArray(id,
						index, exp);
				return assignStm;
			}

		} else {
			error();

			return null;
		}
	}

	// Statements -> Statement Statements
	// ->
	private LinkedList<ast.Ast.Stm.T> parseStatements() {

		LinkedList<ast.Ast.Stm.T> stmList = new LinkedList<ast.Ast.Stm.T>();
		while (current.kind == Kind.TOKEN_LBRACE
				|| current.kind == Kind.TOKEN_IF
				|| current.kind == Kind.TOKEN_WHILE
				|| current.kind == Kind.TOKEN_SYSTEM
				|| current.kind == Kind.TOKEN_ID) {
			stmList.add(parseStatement());
		}
		return stmList;
	}

	// Type -> int []
	// -> boolean
	// -> int
	// -> id
	private ast.Ast.Type.T parseType() {
		Integer lineNum;
		switch (current.kind) {

		case TOKEN_ID:
			String id = current.lexeme;
			advance();
			lineNum = current.lineNum;
			return new ast.Ast.Type.ClassType(id, lineNum);
		case TOKEN_BOOLEAN:
			advance();
			lineNum = current.lineNum;
			return new ast.Ast.Type.Boolean(lineNum);
		case TOKEN_INT: {
			advance();
			if (current.kind == Kind.TOKEN_LBRACK) {
				advance();
				lineNum = current.lineNum;
				eatToken(Kind.TOKEN_RBRACK);
				return new ast.Ast.Type.IntArray(lineNum);
			} else {
				lineNum = current.lineNum;
				return new ast.Ast.Type.Int(lineNum);
			}
		}
		default: {
			error();
			return null;
		}
		}
		// new util.Todo();
	}

	// VarDecl -> Type id ;
	private Dec.T parseVarDecl() {
		// to parse the "Type" nonterminal in this method, instead of writing
		// a fresh one.
		//System.out.println("---------------parseVarDecl----------------");
		ast.Ast.Type.T type = parseType();
		String id = current.lexeme;
		Integer lineNum = current.lineNum;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_SEMI);
		ast.Ast.Dec.T var = new ast.Ast.Dec.DecSingle(type, id, lineNum);
		return var;
	}

	// VarDecls -> VarDecl VarDecls
	// ->
	private LinkedList<Dec.T> parseVarDecls() {
		LinkedList<ast.Ast.Dec.T> varList = new LinkedList<ast.Ast.Dec.T>();

		while (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {
			if (getToken(1).kind == Kind.TOKEN_ASSIGN) {
				break;
			}
			varList.add(parseVarDecl());
		}
		return varList;
	}

	// FormalList -> Type id FormalRest*
	// ->
	// FormalRest -> , Type id
	private LinkedList<ast.Ast.Dec.T> parseFormalList() {

		LinkedList<ast.Ast.Dec.T> formalList = new LinkedList<ast.Ast.Dec.T>();
		if (current.kind == Kind.TOKEN_INT
				|| current.kind == Kind.TOKEN_BOOLEAN
				|| current.kind == Kind.TOKEN_ID) {

			ast.Ast.Type.T typeDec = parseType();
			String id1 = current.lexeme;
			Integer lineNum = current.lineNum;
			eatToken(Kind.TOKEN_ID);
			formalList.add(new ast.Ast.Dec.DecSingle(typeDec, id1, lineNum));
			while (current.kind == Kind.TOKEN_COMMER) {
				advance();
				ast.Ast.Type.T typeDec2 = parseType();
				String id2 = current.lexeme;
				Integer lineNum_ = current.lineNum;
				eatToken(Kind.TOKEN_ID);

				formalList.add(new ast.Ast.Dec.DecSingle(typeDec2, id2,
						lineNum_));
			}
		}
		return formalList;
	}

	// Method -> public Type id ( FormalList )
	// { VarDecl* Statement* return Exp ;}
	private Method.T parseMethod() {
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a method.

		eatToken(Kind.TOKEN_PUBLIC);
		ast.Ast.Type.T retType = parseType();
		String id = current.lexeme;
		Integer lineNum = current.lineNum;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LPAREN);
		LinkedList<ast.Ast.Dec.T> formals = parseFormalList();
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);

		LinkedList<Dec.T> locals = parseVarDecls();

		LinkedList<ast.Ast.Stm.T> stms = parseStatements();

		eatToken(Kind.TOKEN_RETURN);

		ast.Ast.Exp.T retExp = parseExp();

		eatToken(Kind.TOKEN_SEMI);
		eatToken(Kind.TOKEN_RBRACE);

		return new ast.Ast.Method.MethodSingle(retType, id, formals, locals,
				stms, retExp, lineNum);
	}

	// MethodDecls -> MethodDecl MethodDecls
	// ->
	private LinkedList<Method.T> parseMethodDecls() {
		LinkedList<Method.T> methodList = new LinkedList<ast.Ast.Method.T>();
		//System.out.println("---------------parseMethodDecls----------------");
		while (current.kind == Kind.TOKEN_PUBLIC) {
			methodList.add(parseMethod());
		}
		return methodList;
	}

	// ClassDecl -> class id { VarDecl* MethodDecl* }
	// -> class id extends id { VarDecl* MethodDecl* }
	private ast.Ast.Class.T parseClassDecl() {
		eatToken(Kind.TOKEN_CLASS);
		String className = current.lexeme;
		String classEXTENDS = null;
		int linenum = current.lineNum;
		eatToken(Kind.TOKEN_ID);
		if (current.kind == Kind.TOKEN_EXTENDS) {
			eatToken(Kind.TOKEN_EXTENDS);
			classEXTENDS = current.lexeme;
			eatToken(Kind.TOKEN_ID);
		}
		eatToken(Kind.TOKEN_LBRACE);
		LinkedList<Dec.T> varDecls = parseVarDecls();

		LinkedList<Method.T> methods = parseMethodDecls();

		eatToken(Kind.TOKEN_RBRACE);
		ast.Ast.Class.T calssA = new ast.Ast.Class.ClassSingle(className, classEXTENDS, varDecls, methods, linenum);
		return calssA;
	}

	// ClassDecls -> ClassDecl ClassDecls
	// ->
	private LinkedList<ast.Ast.Class.T> parseClassDecls() {
		LinkedList<ast.Ast.Class.T> classlist = new LinkedList<ast.Ast.Class.T>();
		while (current.kind == Kind.TOKEN_CLASS) {
			classlist.add(parseClassDecl());
		}
		// return classlist;
		return classlist;// new util.Flist<ast.Ast.Class.T>().list(Fac.Doit);
	}

	// MainClass -> class id
	// {
	// public static void main ( String [] id )
	// {
	// Statement
	// }
	// }
	private MainClass.T parseMainClass() {
		// MainClass.T Sum = new MainClassSingle("Sum", "a", new Print(new
		// Call(new NewObject("Doit"), "doit",
		// new util.Flist<Exp.T>().list(new Num(101)))));
		// Lab1. Exercise 4: Fill in the missing code
		// to parse a main class as described by the
		// grammar above.
		// new util.Todo();

		eatToken(Kind.TOKEN_CLASS);
		String calssName = current.lexeme;
		int linenum = current.lineNum;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_LBRACE);
		eatToken(Kind.TOKEN_PUBLIC);
		eatToken(Kind.TOKEN_STATIC);
		eatToken(Kind.TOKEN_VOID);
		eatToken(Kind.TOKEN_MAIN);
		eatToken(Kind.TOKEN_LPAREN);
		eatToken(Kind.TOKEN_STRING);
		eatToken(Kind.TOKEN_LBRACK);
		eatToken(Kind.TOKEN_RBRACK);
		String calssArg = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		eatToken(Kind.TOKEN_RPAREN);
		eatToken(Kind.TOKEN_LBRACE);
		ast.Ast.Stm.T stm = parseStatement();
		eatToken(Kind.TOKEN_RBRACE);
		eatToken(Kind.TOKEN_RBRACE);

		return new MainClassSingle(calssName, calssArg, stm, linenum);
	}

	// Program -> MainClass ClassDecl*
	private ast.Ast.Program.T parseProgram() {
		ProgramSingle prog = new ProgramSingle(parseMainClass(),
				parseClassDecls());
		eatToken(Kind.TOKEN_EOF);
		return prog;
	}

	public ast.Ast.Program.T parse() {
		return parseProgram();
	}
}

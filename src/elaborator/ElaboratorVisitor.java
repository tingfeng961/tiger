package elaborator;

import java.util.LinkedList;

import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.False;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Length;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewIntArray;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Not;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.True;
import ast.Ast.MainClass;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Type.ClassType;
import control.Control.ConAst;
import error.Error;

public class ElaboratorVisitor implements ast.Visitor {
	public ClassTable classTable; // symbol table for class
	public MethodTable methodTable; // symbol table for each method
	public String currentClass; // the class name being elaborated
	public Type.T type; // type of the expression being elaborated

	public ElaboratorVisitor() {
		this.classTable = new ClassTable();
		this.methodTable = new MethodTable();
		this.currentClass = null;
		this.type = null;
	}

	private void error() {
		System.out.println("type mismatch");
		//System.exit(1);
	}

	// /////////////////////////////////////////////////////
	// expressions
	@Override
	public void visit(Add e) {
		//System.out.println("visit Adds");
		e.left.accept(this);
		Type.T leftty = this.type;
		e.right.accept(this);
		if (!(leftty instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the left operator of \"+\" must int", e.left.lineNum);
			//return;
		}
		if (!(this.type instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the right operator of \"+\" must int", e.right.lineNum);
			//return;
		}
		// if (!this.type.toString().equals(leftty.toString()))
		// error();
		this.type = new Type.Int(e.left.lineNum);
		return;
	}

	@Override
	public void visit(And e) {
		//System.out.println("visit And");

		e.left.accept(this);
		Type.T leftty = this.type;
		e.right.accept(this);
		if (!(leftty instanceof Type.Boolean)) {
			Error.error("error", "Elaborator", "the left operator of \"&&\" must Boolean", e.left.lineNum);
		}
		if (!(this.type instanceof Type.Boolean)) {
			Error.error("error", "Elaborator", "the right operator of \"&&\" must Boolean", e.right.lineNum);
			return;
		}

		// if (!this.type.toString().equals(leftty.toString()))
		// error();
		this.type = new Type.Boolean(e.left.lineNum);
		return;
	}

	@Override
	public void visit(ArraySelect e) {
		//System.out.println("visit ArraySelect");
		// e.array
		// e.index
		Type.T arr;
		Type.T index;

		e.array.accept(this);
		arr = this.type;

		e.index.accept(this);
		index = this.type;

		if ((arr instanceof Type.IntArray) && (index instanceof Type.Int)) {
			this.type = new Type.Int(e.array.lineNum);
			return;
		} else
			Error.error("error", "Elaborator", "only int[] can has select op at line", e.lineNum);
			//error();
		

	}

	@Override
	public void visit(Call e) {
		//System.out.println("visit Call");
		Type.T leftty;
		Type.ClassType ty = null;

		e.exp.accept(this);
		leftty = this.type;
		if (leftty instanceof ClassType) {
			ty = (ClassType) leftty;
			e.type = ty.id;
		} else {
			Error.error("error", "Elaborator", "call must be invoked by a ClassType", e.lineNum);
			//error();
		}
		MethodType mty = this.classTable.getm(ty.id, e.id);
		java.util.LinkedList<Type.T> argsty = new LinkedList<Type.T>();
		if (null != e.args)
			for (Exp.T a : e.args) {
				a.accept(this);
				argsty.addLast(this.type);
			}
		if (mty.argsType.size() != argsty.size())
			Error.error("error", "Elaborator", "args number is not correct at line", e.lineNum);
			//error();
		for (int i = 0; i < argsty.size(); i++) {
			Dec.DecSingle dec = (Dec.DecSingle) mty.argsType.get(i);
			if (dec.type.toString().equals(argsty.get(i).toString()))
				;
			else {
				String tt = argsty.get(i).toString();
				while (null != this.classTable.get(tt).extendss) {
					tt = this.classTable.get(tt).extendss;
					if (tt.equals(dec.type.toString())){
						this.type = mty.retType;
					    e.at = argsty;
					    e.rt = this.type;
					    return;
					}
				}
				Error.error("error", "Elaborator", "arg " + dec.id + " is type " + argsty.get(i).toString() + " but expect " + dec.type, e.lineNum);
			}
		}
		this.type = mty.retType;
		e.at = argsty;
		e.rt = this.type;
		return;
	}

	@Override
	public void visit(False e) {
		//System.out.println("visit False");
		this.type = new Type.Boolean(e.lineNum);
		return;
	}

	@Override
	public void visit(Id e) {
		//System.out.println("visit Id");
		// first look up the id in method table
		Type.T type = this.methodTable.get(e.id);
		// if search failed, then s.id must be a class field.
		if (type == null) {
			type = this.classTable.get(this.currentClass, e.id);
			// mark this id as a field id, this fact will be
			// useful in later phase.
			e.isField = true;
			//type.isUsed = true;
		}
		else{
			type.isUsed = true;
		}
		if (type == null) {
			Error.error("error", "Elaborator", "member " + e.id + " must be define before use", e.lineNum);
			//error();
		}else{
			type.isUsed = true;
		}
		this.type = type;
		// record this type on this node for future use.
		e.type = type;
		return;
	}

	@Override
	public void visit(Length e) {
		//System.out.println("visit Length");

		Type.T leftty;

		e.array.accept(this);
		leftty = this.type;
		if (leftty instanceof Type.IntArray) {
			this.type = new Type.Int(e.lineNum);
			return;
		} else {
			Error.error("error", "Elaborator", "only int arrary has length", e.array.lineNum);
			//error();
		}
	}

	@Override
	public void visit(Lt e) {
		//System.out.println("visit Lt");
		e.left.accept(this);
		Type.T ty = this.type;
		e.right.accept(this);

		if (!(ty instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the left operator of \"-\" must int", e.left.lineNum);
			// return;
		}
		if (!(this.type instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the right operator of \"-\" must int", e.right.lineNum);
			// return;
		}

		// if (!this.type.toString().equals(ty.toString()))
		// error();
		this.type = new Type.Boolean(e.lineNum);
		return;
	}

	@Override
	public void visit(NewIntArray e) {
		//System.out.println("visit NewIntArray");

		this.type = new Type.IntArray(e.lineNum);
		return;
		// e.

	}

	/**
	 * 声明一个新的类对象
	 */
	@Override
	public void visit(NewObject e) {
		//System.out.println("visit NewObject");

		ClassBinding type = this.classTable.get(e.id);
		// e.id

		if (type == null)
			Error.error("error", "Elaborator", e.id + " should be classType", e.lineNum);
		this.type = new Type.ClassType(e.id, e.lineNum);
		return;
	}

	@Override
	public void visit(Not e) {
		//System.out.println("visit Not---------------");
		e.exp.accept(this);
		if (this.type instanceof Type.Boolean) {
			this.type = new Type.Boolean(e.lineNum);
			return;
		} else
			error();
	}

	@Override
	public void visit(Num e) {
		//System.out.println("visit Num");
		this.type = new Type.Int(e.lineNum);
		return;
	}

	@Override
	public void visit(Sub e) {
		//System.out.println("visit Sub");
		e.left.accept(this);
		Type.T leftty = this.type;
		e.right.accept(this);
		if (!(leftty instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the left operator of \"-\" must int", e.left.lineNum);
			return;
		}
		if (!(this.type instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the right operator of \"-\" must int", e.right.lineNum);
			return;
		}
		// if (!this.type.toString().equals(leftty.toString()))
		// error();
		// this.type = new Type.Int();
		return;
	}

	@Override
	public void visit(This e) {
		//System.out.println("visit This");

		this.type = new Type.ClassType(this.currentClass, e.lineNum);
		return;
	}

	@Override
	public void visit(Times e) {
		//System.out.println("visit Times");
		e.left.accept(this);
		Type.T leftty = this.type;
		if (!(leftty instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the left operator of \"times\" must int", e.left.lineNum);
		}

		e.right.accept(this);
		if (!(this.type instanceof Type.Int)) {
			Error.error("error", "Elaborator", "the right operator of \"times\" must int", e.right.lineNum);
		}

		this.type = new Type.Int(e.left.lineNum);
		return;
	}

	@Override
	public void visit(True e) {
		//System.out.println("visit True");
		this.type = new Type.Boolean(e.lineNum);
		return;
	}

	// statements
	@Override
	public void visit(Assign s) {
		//System.out.println("visit Assign");
		// first look up the id in method table
		Type.T type = this.methodTable.get(s.id);
		// if search failed, then s.id must
		if (type == null)
			type = this.classTable.get(this.currentClass, s.id);
		else
			type.isUsed = true;
		if (type == null)
			Error.error("error", "Elaborator", s.id +" is not define before used ", s.exp.lineNum);
		else 
			type.isUsed = true;
		s.exp.accept(this);
		s.type = type;
		if (!this.type.toString().equals(type.toString())) {
			Error.error("error", "Elaborator", "\"=\" need two same type", s.exp.lineNum);
		}
		;
		return;
	}

	@Override
	public void visit(AssignArray s) {
		//System.out.println("visit AssignArray");
		s.index.accept(this);
		Type.T index = this.type;
		s.exp.accept(this);

		if (!(index instanceof Type.Int)) {
			Error.error("error", "Elaborator", "index of int array should be int", s.exp.lineNum);
			return;
		}
		if (!(this.type instanceof Type.Int)) {
			Error.error("error", "Elaborator", "rValue must be int", s.exp.lineNum);
			return;
		}
		return;
	}

	@Override
	public void visit(Block s) {
		//System.out.println("visit Block");
		for(Stm.T stm : s.stms)
			stm.accept(this);
	}

	@Override
	public void visit(If s) {
		//System.out.println("visit If");
		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean"))
			error();
		s.thenn.accept(this);
		s.elsee.accept(this);
		return;
	}

	@Override
	public void visit(Print s) {
		//System.out.println("visit Print");
		s.exp.accept(this);
		if (!this.type.toString().equals("@int")) {
			Error.error("error", "Elaborator", "only int is permit for print", s.exp.lineNum);
			error();
		}
		return;
	}

	@Override
	public void visit(While s) {
		//System.out.println("visit While");

		s.condition.accept(this);
		if (!this.type.toString().equals("@boolean")) {
			Error.error("error", "Elaborator", "the condition must be boolean", s.condition.lineNum);
			error();
		}
		s.body.accept(this);
		return;

	}

	// type
	@Override
	public void visit(Type.Boolean t) {
		//System.out.println("visit Boolean");
		this.type = new Type.Boolean(t.lineNum);
		return;
	}

	@Override
	public void visit(Type.ClassType t) {
		//System.out.println("visit ClassType");
		ClassBinding type = this.classTable.get(t.id);
		if (type == null)
			Error.error("error", "Elaborator", t.id + "should be classType", t.lineNum);
		this.type = new Type.ClassType(t.id, t.lineNum);
		return;
	}

	@Override
	public void visit(Type.Int t) {
		//System.out.println("visit Int");
		// System.out.println("aaaa");
		this.type = new Type.Int(t.lineNum);
		return;
	}

	@Override
	public void visit(Type.IntArray t) {
		//System.out.println("visit IntArray");

		this.type = new Type.IntArray(t.lineNum);
		return;

	}

	// dec
	@Override
	public void visit(Dec.DecSingle d) {
		//System.out.println("visit DecSingle-----------------");

		if (!"@int".equals(d.type.toString())
				&& !"@boolean".equals(d.type.toString())
				&& !"@int[]".equals(d.type.toString())) {
			if (null == this.classTable.get(d.type.toString())) {
				Error.error("error", "Elaborator", "only \"int\" \"boolean\" \"classType\" can define variable", d.lineNum);
			}
		}

		return;

	}

	// method
	@Override
	public void visit(Method.MethodSingle m) {
		//System.out.println("visit MethodSingle");
		// construct the method table
		this.methodTable.put(m.formals, m.locals);

		if (ConAst.elabMethodTable) {
			System.out.println("method " + m.id + " of class " + this.currentClass + " has fields:");
			this.methodTable.dump();
		}

		// 参数
		for (Dec.T formal : m.formals) {
			formal.accept(this);
		}
		// 局部变量
		for (Dec.T local : m.locals) {
			local.accept(this);
		}

		for (Stm.T s : m.stms)
			s.accept(this);
		
	    if(null != m.retExp){
	    	m.retExp.accept(this);
	    	if (!this.type.toString().equals(m.retType.toString())) 
	    		Error.error("error", "Elaborator", "return type if not suit", m.retExp.lineNum);
			m.retExp.accept(this);
	    }
	    else
	    	Error.error("error", "Elaborator", "must have return stm at method " + m.id, m.lineNum);


		
		//检测形参 和 局部变量是否被使用
		// 参数
		
		this.methodTable.checkUse();
		
		this.methodTable = new MethodTable();
		return;
	}

	// class
	@Override
	public void visit(Class.ClassSingle c) {
		//System.out.println("visit ClassSingle");
		this.currentClass = c.id;
		if (null != c.extendss)
			if (null == this.classTable.get(c.extendss)) {
				Error.error("error", "Elaborator", "class " + c.id + "'s parents " + c.extendss + " is not a class", c.linenum);
			}

		for (Dec.T dec : c.decs) {
			dec.accept(this);
		}

		for (Method.T m : c.methods) {
			m.accept(this);
		}
		return;
	}

	// main class
	@Override
	public void visit(MainClass.MainClassSingle c) {
		//System.out.println("visit MainClassSingle");
		this.currentClass = c.id;
		// "main" has an argument "arg" of type "String[]", but
		// one has no chance to use it. So it's safe to skip it...

		c.stm.accept(this);
		return;
	}

	// ////////////////////////////////////////////////////////
	// step 1: build class table
	// class table for Main class
	private void buildMainClass(MainClass.MainClassSingle main) {
		this.classTable.put(main.id, new ClassBinding(null, main.lineNum));
		this.classTable.get(main.id).isUsed = true;
	}

	// class table for normal classes
	private void buildClass(ClassSingle c) {
		//System.out.println("------ classes ------");
		this.classTable.put(c.id, new ClassBinding(c.extendss, c.linenum));
		for (Dec.T dec : c.decs) {
			Dec.DecSingle d = (Dec.DecSingle) dec;
			this.classTable.put(c.id, d.id, d.type, dec.lineNum);
		}
		for (Method.T method : c.methods) {
			MethodSingle m = (MethodSingle) method;
			this.classTable.put(c.id, m.id,
					new MethodType(m.retType, m.formals));
		}
	}

	// step 1: end
	// ///////////////////////////////////////////////////

	// program
	@Override
	public void visit(ProgramSingle p) {
		//System.out.println("visit ProgramSingle " + p.classes);
		// ////////////////////////////////////////////////
		// step 1: build a symbol table for class (the class table)
		// a class table is a mapping from class names to class bindings
		// classTable: className -> ClassBinding{extends, fields, methods}
		buildMainClass((MainClass.MainClassSingle) p.mainClass);
		for (Class.T c : p.classes) {
			buildClass((ClassSingle) c);
		}

		// we can double check that the class table is OK!
		// 检测类的合法性
		if (control.Control.ConAst.elabClassTable) {
			this.classTable.dump();
		}

		// ////////////////////////////////////////////////
		// step 2: elaborate each class in turn, under the class table
		// built above.
		p.mainClass.accept(this);
		for (Class.T c : p.classes) {
			c.accept(this);
		}
		
		this.classTable.checkUse();

	}
}
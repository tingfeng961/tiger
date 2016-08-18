package ast.optimizations;

import java.util.LinkedList;

import ast.Ast.Class;
import ast.Ast.Exp;
import ast.Ast.Method;
import ast.Ast.Stm;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
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
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;

// Algebraic simplification optimizations on an AST.

public class AlgSimp implements ast.Visitor
{
  private Class.T newClass;
  private MainClass.T mainClass;
  public Program.T program;
  
  private Stm.T stm;
  private Exp.T exp;
  private Method.T method;
  private LinkedList<ast.Ast.Class.T> classes = new LinkedList<ast.Ast.Class.T>();
  private LinkedList<Method.T> methods = new LinkedList<Method.T>();
  
  public AlgSimp()
  {
    this.newClass = null;
    this.mainClass = null;
    this.program = null;
  }

  // //////////////////////////////////////////////////////
  // 
  public String genId()
  {
    return util.Temp.next();
  }

  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
		e.left.accept(this);
		Exp.T left = this.exp;
		e.right.accept(this);
		Exp.T right = this.exp;
		this.exp = new Add(left, right, e.lineNum);
		return;
  }

  @Override
  public void visit(And e)
  {
	  e.left.accept(this);
	  Exp.T left = this.exp;
	  e.right.accept(this);
	  Exp.T right = this.exp;
	  this.exp = new And(left, right, e.lineNum);
	  return;
  }

  @Override
  public void visit(ArraySelect e)
  {
	  e.array.accept(this);
	  Exp.T array = this.exp;
	  e.index.accept(this);
	  Exp.T index = this.exp;
	  this.exp = new ArraySelect(array, index);
	  return;
  }

  @Override
  public void visit(Call e)
  {
		e.exp.accept(this);
		LinkedList<Exp.T> args = new LinkedList<Exp.T>();
		if (e.args != null)
			for (ast.Ast.Exp.T x : e.args) {
				x.accept(this);
				args.add(this.exp);
			}
		this.exp = new Call(e.exp, e.id, args, e.lineNum);
		return;
  }

  @Override
  public void visit(False e)
  {
	  this.exp = new Exp.False(e.lineNum);
	  return;
  }

  @Override
  public void visit(Id e)
  {
		this.exp = new Id(e.id, e.lineNum);
    return;
  }

  @Override
  public void visit(Length e)
  {
		e.array.accept(this);
		this.exp = new Length(this.exp);
		return;
  }

  @Override
  public void visit(Lt e)
  {
		e.left.accept(this);
		Exp.T left = this.exp;
		e.right.accept(this);
		Exp.T right = this.exp;
		this.exp = new Lt(left, right, e.lineNum);
		return;
  }

  @Override
  public void visit(NewIntArray e)
  {
		e.exp.accept(this);
		this.exp = new NewIntArray(this.exp, e.lineNum);
		return;
  }

  @Override
  public void visit(NewObject e)
  {
		this.exp = new NewObject(e.id, e.lineNum);
		return;
  }

  @Override
  public void visit(Not e)
  {
		e.exp.accept(this);
		this.exp = new Not(this.exp, e.lineNum);
		return;
  }

  @Override
  public void visit(Num e)
  {
		this.exp = new Num(e.num, e.lineNum);
		return;
  }

  @Override
  public void visit(Sub e)
  {
		e.left.accept(this);
		Exp.T left = this.exp;
		e.right.accept(this);
		Exp.T right = this.exp;
		this.exp = new Sub(left, right, e.lineNum);
		return;
  }

  @Override
  public void visit(This e)
  {
		this.exp = new This();
		return;
  }

  @Override
  public void visit(Times e)
  {
	  
	  if(e.left instanceof Exp.Num){
		  if(0==((Exp.Num)e.left).num){
			  Main.isChanged = true;
			  this.exp = new Num(0, e.lineNum);
			  return;
		  }else if(1==((Exp.Num)e.left).num){
			  Main.isChanged = true;
			  e.left.accept(this);
			  return;
		  }
	  }
	  if(e.right instanceof Exp.Num){
		  if(0==((Exp.Num)e.right).num){
			  Main.isChanged = true;
			  this.exp = new Num(0, e.lineNum);
			  return;
		  }else if(1==((Exp.Num)e.right).num){
			  Main.isChanged = true;
			  e.left.accept(this);
			  return;
		  }
	  }
	  
		e.left.accept(this);
		Exp.T left = this.exp;
		e.right.accept(this);
		Exp.T right = this.exp;
		this.exp = new Times(left, right, e.lineNum);
		return;
  }

  @Override
  public void visit(True e)
  {		
	  this.exp = new Exp.True(e.lineNum);
	  return;
  }

  // statements
  @Override
  public void visit(Assign s)
  {
		s.exp.accept(this);
		this.stm = new Assign(s.id, this.exp);
		return;
  }

  @Override
  public void visit(AssignArray s)
  {
		s.index.accept(this);
		Exp.T index = this.exp;
		s.exp.accept(this);
		this.stm = new AssignArray(s.id, index, this.exp);
		return;
  }

  @Override
  public void visit(Block s)
  {
		LinkedList<Stm.T> stms = new LinkedList<Stm.T>();
		for (Stm.T stm : s.stms) {
			stm.accept(this);
			stms.add(this.stm);
		}
		this.stm = new Block(stms);
		return;
  }

  @Override
  public void visit(If s)
  {
		s.condition.accept(this);
		Exp.T con = this.exp;
		s.thenn.accept(this);
		s.elsee.accept(this);
		this.stm = new If(con, s.thenn, s.elsee);
		return;
  }

  @Override
  public void visit(Print s)
  {
		s.exp.accept(this);
		this.stm = new Print(this.exp);
		return;
  }

  @Override
  public void visit(While s)
  {
		s.condition.accept(this);
		Exp.T condition = this.exp;
		s.body.accept(this);
		this.stm = new While(condition, this.stm);
		return;
  }

  // type
  @Override
  public void visit(Boolean t)
  {
  }

  @Override
  public void visit(ClassType t)
  {
  }

  @Override
  public void visit(Int t)
  {
  }

  @Override
  public void visit(IntArray t)
  {
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
    return;
  }

  // method
  @Override
  public void visit(MethodSingle m)
  {
		LinkedList<Stm.T> newStm = new LinkedList<Stm.T>();
		for (ast.Ast.Stm.T s : m.stms) {
			s.accept(this);
			newStm.add(this.stm);
		}
		m.retExp.accept(this);
		Exp.T retExp = this.exp;
		this.method = new MethodSingle(m.retType, m.id, m.formals, m.locals, newStm, retExp, m.lineNum);
		return;
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
		for (Method.T m : c.methods) {
			m.accept(this);
			this.methods.add(this.method);
		}
		this.classes.add(new ClassSingle(c.id, c.extendss, c.decs,
				this.methods, c.linenum));
		return;
  }

  // main class
  @Override
  public void visit(MainClassSingle c)
  {
		c.stm.accept(this);
		return;
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {

	MainClassSingle mainC = (MainClassSingle) p.mainClass;
	p.mainClass.accept(this);
	this.mainClass = new MainClassSingle(mainC.id, mainC.arg, this.stm, mainC.lineNum);

	for (ast.Ast.Class.T classs : p.classes)
		classs.accept(this);

	this.program = new ProgramSingle(this.mainClass, this.classes);

    if(control.Control.trace.size() > 0)
    //if (control.Control.trace.equals("ast.DeadCode")){
    if (control.Control.trace.getLast().equals("ast.AlgSimp")){
      System.out.println("before optimization:");
      ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
      p.accept(pp);
      System.out.println("after optimization:");
      this.program.accept(pp);
    }
    return;
  }
}

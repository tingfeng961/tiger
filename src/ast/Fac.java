package ast;

import ast.Ast.Dec;
import ast.Ast.Exp;
import ast.Ast.Exp.Call;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.T;
import ast.Ast.Type;

public class Fac
{

  // /////////////////////////////////////////////////////
  // To represent the "Fac.java" program in memory manually
  // this is for demonstration purpose only, and
  // no one would want to do this in reality (boring and error-prone).
  /*
   * class Factorial { public static void main(String[] a) {
   * System.out.println(new Fac().ComputeFac(10)); } } class Fac { public int
   * ComputeFac(int num) { int num_aux; if (num < 1) num_aux = 1; else num_aux =
   * num * (this.ComputeFac(num-1)); return num_aux; } }
   */

  // // main class: "Factorial"
  static MainClass.T factorial = new MainClassSingle(
      "Factorial", "a", new Print(new Call(
          new NewObject("Fac", 0), "ComputeFac",
          new util.Flist<Exp.T>().list(new Num(10, 0)), 0)), 0);

  // // class "Fac"
  static ast.Ast.Class.T fac = new ast.Ast.Class.ClassSingle(
		  "Fac", 
		  null,
		  
      new util.Flist<Dec.T>().list(),
      
      new util.Flist<Method.T>().list(new Method.MethodSingle(
          new Type.Int(0), "ComputeFac", new util.Flist<Dec.T>()
              .list(new Dec.DecSingle(new Type.Int(0), "num", 0)),
          new util.Flist<Dec.T>().list(new Dec.DecSingle(
              new Type.Int(0), "num_aux", 0)), new util.Flist<Stm.T>()
              .list(new If(new Lt(new Id("num", 0),
                  new Num(1, 0), 0), new Assign("num_aux",
                  new Num(1, 0)), new Assign("num_aux",
                  new Times(new Id("num", 0), new Call(
                      new This(), "ComputeFac",
                      new util.Flist<Exp.T>().list(new Sub(
                          new Id("num", 0), new Num(1, 0), 0)), 0), 0)))),
          new Id("num_aux", 0), 0)), 0);

  // program
  public static Program.T prog = new ProgramSingle(factorial,
      new util.Flist<ast.Ast.Class.T>().list(fac));

  
  // // main class: "Sum"
  static MainClass.T Sum = new MainClassSingle(
      "Sum", "a", new Print(new Call(
          new NewObject("Doit", 0), "doit",
          new util.Flist<Exp.T>().list(new Num(101, 0)), 0)), 0);
  
  
  // // class "Fac"
  public static ast.Ast.Class.T Doit = new ast.Ast.Class.ClassSingle("Doit", null,
      new util.Flist<Dec.T>().list(),
      new util.Flist<Method.T>().list(
    		  new Method.MethodSingle(new Type.Int(0), "doit", 
    				  new util.Flist<Dec.T>().list(new Dec.DecSingle(new Type.Int(0), "n", 0)),
    				  new util.Flist<Dec.T>().list(new Dec.DecSingle(new Type.Int(0), "sum", 0),
    						  new Dec.DecSingle(new Type.Int(0), "i", 0)), 
                      
                      new util.Flist<Stm.T>().list(
                    		  new Assign("i",new Num(0, 0)),
                    		  new Assign("sum",new Num(0, 0)),
                    		  new ast.Ast.Stm.While(new Lt(new Id("i", 0),new Id("n", 0), 0),
                    				  
                    				  new ast.Ast.Stm.Block(
                    						  new util.Flist<Stm.T>().list(
//                    		                    sum = sum + i;
//                    		                  	i = i+1;
                    						  new Assign("sum",new ast.Ast.Exp.Add(new Id("sum", 0), new Id("i", 0), 0)),
                    						  new Assign("i",new ast.Ast.Exp.Add(new Id("i", 0), new Num(1, 0), 0))
                    						  )
                    						  )
                    		  )
                      ),
          new Id("sum", 0), 0)), 0);
  
  
  // // class "Fac"
  static ast.Ast.Class.T test = new ast.Ast.Class.ClassSingle("Doit", null,
      new util.Flist<Dec.T>().list(new ast.Ast.Dec.DecSingle(new Type.Int(0), "d", 0), new ast.Ast.Dec.DecSingle(new Type.IntArray(0), "d", 0)),
      new util.Flist<Method.T>().list(
    		  new Method.MethodSingle(new Type.Int(0), "doit", 
    				  new util.Flist<Dec.T>().list(new Dec.DecSingle(new Type.Int(0), "n", 0)),
    				  new util.Flist<Dec.T>().list(new Dec.DecSingle(new Type.Int(0), "sum", 0),
    						  new Dec.DecSingle(new Type.Int(0), "i", 0)), 
                      
                      new util.Flist<Stm.T>().list(
                    		  new Assign("i",new Num(0, 0)),
                    		  new Assign("sum",new Num(0, 0)),
                    		  new ast.Ast.Stm.While(new Lt(new Id("i", 0),new Id("n", 0), 0),
                    				  
                    				  new ast.Ast.Stm.Block(
                    						  new util.Flist<Stm.T>().list(
//                    		                    sum = sum + i;
//                    		                  	i = i+1;
                    						  new Assign("sum",new ast.Ast.Exp.Add(new Id("sum", 0), new Id("i", 0), 0)),
                    						  //new Assign("i",new ast.Ast.Exp.And(new Id("i"), new Num(1)))
                    						  new ast.Ast.Stm.AssignArray(
                    								  "d", 
                    								  new Num(1, 0), 
                    								  new Num(1, 0)
                    								  ),
                    						  new ast.Ast.Stm.AssignArray(
                    								  "d", 
                    								  new Num(1, 0), 
                    								  new ast.Ast.Exp.ArraySelect(new Id("ss", 0), new Num(1, 0))
                    								  ),
                    						  new Assign("sum",new ast.Ast.Exp.Add(new Id("sum", 0), new ast.Ast.Exp.Length(new Id("d", 0)), 0))
                    						  )
                    						  )
                    		  )
                      ),

   
          new Id("sum", 0), 0)), 0);
  
  
  
  // program
  public static Program.T prog2 = new ProgramSingle(Sum,
      new util.Flist<ast.Ast.Class.T>().list(Doit));
  // program
  public static Program.T prog3 = new ProgramSingle(Sum,
			new util.Flist<ast.Ast.Class.T>().list(test));

}

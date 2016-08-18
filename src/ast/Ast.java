package ast;

import java.util.LinkedList;

public class Ast
{

  // ///////////////////////////////////////////////////////////
  // type
  public static class Type
  {
    public static abstract class T implements ast.Acceptable
    {
      // boolean: -1
      // int: 0
      // int[]: 1
      // class: 2
      // Such that one can easiyly tell who is who
    	public Integer lineNum;
    	public boolean isUsed = false;
      public abstract int getNum();
    }

    // boolean
    public static class Boolean extends T
    {
      public Boolean(Integer lineNum)
      {
    	  this.lineNum = lineNum;
      }

      @Override
      public String toString()
      {
        return "@boolean";
      }

      @Override
      public int getNum()
      {
        return -1;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    // class
    public static class ClassType extends T
    {
      public String id;

      public ClassType(String id, Integer lineNum)
      {
        this.id = id;
        this.lineNum = lineNum;
      }

      @Override
      public String toString()
      {
        return this.id;
      }

      @Override
      public int getNum()
      {
        return 2;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

    // int
    public static class Int extends T
    {
      public Int(Integer lineNum)
      {
    	  this.lineNum = lineNum;
      }

      @Override
      public String toString()
      {
        return "@int";
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }

      @Override
      public int getNum()
      {
        return 0;
      }
    }

    // int[]
    public static class IntArray extends T
    {
      public IntArray(Integer lineNum)
      {
    	  this.lineNum = lineNum;
      }

      @Override
      public String toString()
      {
        return "@int[]";
      }

      @Override
      public int getNum()
      {
        return 1;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }

  }

  // ///////////////////////////////////////////////////
  // dec
  public static class Dec
  {
    public static abstract class T implements ast.Acceptable
    {
    	public boolean isUsed = false;
        public Integer lineNum; 
    }

    public static class DecSingle extends T
    {
      public Type.T type;
      public String id;

      public DecSingle(Type.T type, String id, Integer lineNum)
      {
        this.type = type;
        this.id = id;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
  }

  // /////////////////////////////////////////////////////////
  // expression
  public static class Exp
  {
    public static abstract class T implements ast.Acceptable
    {
    	public Integer lineNum;
    }

    // +
    public static class Add extends T
    {
      public T left;
      public T right;

      public Add(T left, T right, Integer lineNum)
      {
        this.left = left;
        this.right = right;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // and
    public static class And extends T
    {
      public T left;
      public T right;

      public And(T left, T right, Integer lineNum)
      {
        this.left = left;
        this.right = right;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // ArraySelect
    public static class ArraySelect extends T
    {
      public T array;
      public T index;

      public ArraySelect(T array, T index)
      {
        this.array = array;
        this.index = index;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // Call
    public static class Call extends T
    {
      public T exp;
      public String id;
      public java.util.LinkedList<T> args;
      public String type; // type of first field "exp"
      public java.util.LinkedList<Type.T> at; // arg's type
      public Type.T rt;

      public Call(T exp, String id, java.util.LinkedList<T> args, Integer lineNum)
      {
        this.exp = exp;
        this.id = id;
        this.args = args;
        this.type = null;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // False
    public static class False extends T
    {
      public False(Integer lineNum)
      {
    	  this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // Id
    public static class Id extends T
    {
      public String id; // name of the id
      public Type.T type; // type of the id
      public boolean isField; // whether or not this is a class field

      public Id(String id, Integer lineNum)
      {
        this.id = id;
        this.type = null;
        this.isField = false;
        this.lineNum = lineNum;
      }

      public Id(String id, Type.T type, boolean isField, Integer lineNum)
      {
        this.id = id;
        this.type = type;
        this.isField = isField;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // length
    public static class Length extends T
    {
      public T array;

      public Length(T array)
      {
        this.array = array;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // <
    public static class Lt extends T
    {
      public T left;
      public T right;

      public Lt(T left, T right, Integer lineNum)
      {
        this.left = left;
        this.right = right;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // new int [e]
    public static class NewIntArray extends T
    {
      public T exp;

      public NewIntArray(T exp, Integer lineNum)
      {
        this.exp = exp;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // new A();
    public static class NewObject extends T
    {
      public String id;

      public NewObject(String id, Integer lineNum)
      {
        this.id = id;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // !
    public static class Not extends T
    {
      public T exp;

      public Not(T exp, Integer lineNum)
      {
        this.exp = exp;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // number
    public static class Num extends T
    {
      public int num;

      public Num(int num, Integer lineNum)
      {
        this.num = num;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // -
    public static class Sub extends T
    {
      public T left;
      public T right;
      
      public Sub(T left, T right, Integer lineNum)
      {
        this.left = left;
        this.right = right;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // this
    public static class This extends T
    {
      public This()
      {
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // *
    public static class Times extends T
    {
      public T left;
      public T right;

      public Times(T left, T right, Integer lineNum)
      {
        this.left = left;
        this.right = right;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

    // True
    public static class True extends T
    {
      public True(Integer lineNum)
      {
    	  this.lineNum = lineNum;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
        return;
      }
    }

  }// end of expression

  // /////////////////////////////////////////////////////////
  // statement
  public static class Stm
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    // assign
    public static class Assign extends T
    {
      public String id;
      public Exp.T exp;
      public Type.T type; // type of the id

      public Assign(String id, Exp.T exp)
      {
        this.id = id;
        this.exp = exp;
        this.type = null;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // assign-array
    public static class AssignArray extends T
    {
      public String id;
      public Exp.T index;
      public Exp.T exp;

      public AssignArray(String id, Exp.T index, Exp.T exp)
      {
        this.id = id;
        this.index = index;
        this.exp = exp;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // block
    public static class Block extends T
    {
      public java.util.LinkedList<T> stms;

      public Block(java.util.LinkedList<T> stms)
      {
        this.stms = stms;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // if
    public static class If extends T
    {
      public Exp.T condition;
      public T thenn;
      public T elsee;

      public If(Exp.T condition, T thenn, T elsee)
      {
        this.condition = condition;
        this.thenn = thenn;
        this.elsee = elsee;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // Print
    public static class Print extends T
    {
      public Exp.T exp;

      public Print(Exp.T exp)
      {
        this.exp = exp;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

    // while
    public static class While extends T
    {
      public Exp.T condition;
      public T body;

      public While(Exp.T condition, T body)
      {
        this.condition = condition;
        this.body = body;
      }

      @Override
      public void accept(ast.Visitor v)
      {
        v.visit(this);
      }
    }

  }// end of statement

  // /////////////////////////////////////////////////////////
  // method
  public static class Method
  {
    public static abstract class T implements ast.Acceptable
    {
    	public Integer lineNum;
    }

    public static class MethodSingle extends T
    {
      public Type.T retType;
      public String id;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Stm.T> stms;
      public Exp.T retExp;

      public MethodSingle(Type.T retType, String id,
          LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
          LinkedList<Stm.T> stms, Exp.T retExp, Integer lineNum)
      {
        this.retType = retType;
        this.id = id;
        this.formals = formals;
        this.locals = locals;
        this.stms = stms;
        this.retExp = retExp;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
  }

  // class
  public static class Class
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class ClassSingle extends T
    {
      public String id;
      public String extendss; // null for non-existing "extends"
      public java.util.LinkedList<Dec.T> decs;
      public java.util.LinkedList<ast.Ast.Method.T> methods;
      public Integer linenum;
      

      public ClassSingle(String id, String extendss,
          java.util.LinkedList<Dec.T> decs,
          java.util.LinkedList<ast.Ast.Method.T> methods, Integer linenum)
      {
        this.id = id;
        this.extendss = extendss;
        this.decs = decs;
        this.methods = methods;
        this.linenum = linenum;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
      }
    }
  }

  // main class
  public static class MainClass
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class MainClassSingle extends T
    {
      public String id;
      public String arg;
      public Stm.T stm;
      public Integer lineNum;

      public MainClassSingle(String id, String arg, Stm.T stm, Integer lineNum)
      {
        this.id = id;
        this.arg = arg;
        this.stm = stm;
        this.lineNum = lineNum;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
        return;
      }
    }

  }

  // whole program
  public static class Program
  {
    public static abstract class T implements ast.Acceptable
    {
    }

    public static class ProgramSingle extends T
    {
      public MainClass.T mainClass;
      public LinkedList<Class.T> classes;

      public ProgramSingle(MainClass.T mainClass, LinkedList<Class.T> classes)
      {
        this.mainClass = mainClass;
        this.classes = classes;
      }

      @Override
      public void accept(Visitor v)
      {
        v.visit(this);
        return;
      }
    }

  }
}

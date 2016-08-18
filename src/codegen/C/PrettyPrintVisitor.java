package codegen.C;

import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.Dec;
import codegen.C.Ast.Dec.DecSingle;
import codegen.C.Ast.Dec.T;
import codegen.C.Ast.Exp;
import codegen.C.Ast.Exp.Add;
import codegen.C.Ast.Exp.And;
import codegen.C.Ast.Exp.ArraySelect;
import codegen.C.Ast.Exp.Call;
import codegen.C.Ast.Exp.Id;
import codegen.C.Ast.Exp.Length;
import codegen.C.Ast.Exp.Lt;
import codegen.C.Ast.Exp.NewIntArray;
import codegen.C.Ast.Exp.NewObject;
import codegen.C.Ast.Exp.Not;
import codegen.C.Ast.Exp.Num;
import codegen.C.Ast.Exp.Sub;
import codegen.C.Ast.Exp.This;
import codegen.C.Ast.Exp.Times;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm;
import codegen.C.Ast.Stm.Assign;
import codegen.C.Ast.Stm.AssignArray;
import codegen.C.Ast.Stm.Block;
import codegen.C.Ast.Stm.If;
import codegen.C.Ast.Stm.Print;
import codegen.C.Ast.Stm.While;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Type.Int;
import codegen.C.Ast.Type.IntArray;
import codegen.C.Ast.Vtable;
import codegen.C.Ast.Vtable.VtableSingle;
import control.Control;

public class PrettyPrintVisitor implements Visitor
{
  private int indentLevel;
  private java.io.BufferedWriter writer;

  MethodSingle _method;
  MainMethodSingle _mmethod;
  boolean isMmethod = false;
  
  public PrettyPrintVisitor()
  {
    this.indentLevel = 2;
  }

  private void indent()
  {
    this.indentLevel += 2;
  }

  private void unIndent()
  {
    this.indentLevel -= 2;
  }

  private void printSpaces()
  {
    int i = this.indentLevel;
    while (i-- != 0){
    	this.say(" ");
    }
  }

  private void sayln(String s)
  {
    say(s);
    try {
      this.writer.write("\n");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void say(String s)
  {
    try {
      this.writer.write(s);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  
  private void args_id_Fix_print(String id){
	  String value = _method.hasFields(id);
	    if(null != value)
//	    	if("@int".equals(value))
	    		this.say(id);
//	    	else
//	    		this.say("frame." + id);
	    else
	    	this.say("this->" + id);
  }
  
  private void local_id_Fix_print(String id){
	  String value = _method.hasLocals(id);
	    if(null != value)
	    	if("@int".equals(value))
	    		this.say(id);
	    	else
	    		this.say("frame." + id);
	    else
//	    	this.say("this->" + id);
	    	args_id_Fix_print(id);
  }
  
  
  // /////////////////////////////////////////////////////
  // expressions
  @Override
  public void visit(Add e)
  {
	  //TODO
	    e.left.accept(this);
	    this.say(" + ");
	    e.right.accept(this);
	    return;
  }

  @Override
  public void visit(And e)
  {
	  //TODO
	    e.left.accept(this);
	    this.say(" && ");
	    e.right.accept(this);
	    return;
  }

  @Override
  public void visit(ArraySelect e)
  {
	  //TODO
	  e.array.accept(this);
	  this.say("[");
	  e.index.accept(this);
	  this.say("]");
  }

  @Override
  public void visit(Call e)
  {
    this.say("(frame." + e.assign + "=");
    e.exp.accept(this);
    this.say(", ");
    this.say("frame."+e.assign + "->vptr->" + e.id + "(frame." + e.assign);
	  
//	  this.say("(");
//	  local_id_Fix_print(e.assign);
//	  this.say("=");
//	  e.exp.accept(this);
//	  this.say(",");
//	  local_id_Fix_print(e.assign);
//	  this.say("->vptr->" + e.id+"(");
//	  local_id_Fix_print(e.assign);
	  
    int size = e.args.size();
    if (size == 0) {
      this.say("))");
      return;
    }
    for (Exp.T x : e.args) {
      this.say(", ");
      x.accept(this);
    }
    this.say("))");
    return;
  }

  @Override
  public void visit(Id e)
  {
	//TODO
//    if(_method.hasFields(e.id))
//    	this.say(e.id);
//    else
//    	this.say("this->" + e.id);
    local_id_Fix_print(e.id);
  }

  @Override
  public void visit(Length e)
  {
	  //TODO
	  //*((int*)((char*)this->number-5))
	  this.say("*((int*)((char*)");
	  e.array.accept(this);
	  this.say("-8))");
	  
  }

  @Override
  public void visit(Lt e)
  {
    e.left.accept(this);
    this.say(" < ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(NewIntArray e)
  {
	  //TODO	 
	  this.say("(int*)Tiger_new_array(");
	  e.exp.accept(this);
	  this.say(")");
	  return ;
  }

  @Override
  public void visit(NewObject e)
  {
    this.say("((struct " + e.id + "*)(Tiger_new (&" + e.id
        + "_vtable_, sizeof(struct " + e.id + "))))");
    return;
  }

  @Override
  public void visit(Not e)
  {
	  //TODO
	  this.say("!(");
	  e.exp.accept(this);
	  this.say(")");
	  return;
  }

  @Override
  public void visit(Num e)
  {
    this.say(Integer.toString(e.num));
    return;
  }

  @Override
  public void visit(Sub e)
  {
    e.left.accept(this);
    this.say(" - ");
    e.right.accept(this);
    return;
  }

  @Override
  public void visit(This e)
  {
    this.say("this");
  }

  @Override
  public void visit(Times e)
  {
    e.left.accept(this);
    this.say(" * ");
    e.right.accept(this);
    return;
  }

  // statements
  @Override
  public void visit(Assign s)
  {
    this.printSpaces();
//    if(_method.hasFields(s.id))
//    	this.say(s.id + " = ");
//    else
//    	this.say("this->" + s.id + " = ");
    local_id_Fix_print(s.id);
    this.say(" = ");
    s.exp.accept(this);
    this.sayln(";");
    return;
  }

  @Override
  public void visit(AssignArray s)
  {
	  //TODO
	    this.printSpaces();
	    
	    local_id_Fix_print(s.id);
	    this.say("[");
	    
//	    if(_method.hasFields(s.id))
//	    	this.say(s.id + "[");
//	    else
//	    	this.say("this->" + s.id + "[");
	    s.index.accept(this);
	    this.say("] = ");
	    s.exp.accept(this);
	    this.sayln(";");
	    return;
  }

  @Override
  public void visit(Block s)
  {
	  //TODO
	  this.unIndent();
	  this.printSpaces();
	  this.sayln("{");
	  this.indent();
	  for(Stm.T stm : s.stms)
		  stm.accept(this);
	  //this.unIndent();
	  this.printSpaces();
	  this.sayln("}");
	  return ;
  }

  @Override
  public void visit(If s)
  {
    this.printSpaces();
    this.say("if (");
    s.condition.accept(this);
    this.sayln(")");
    this.indent();
    s.thenn.accept(this);
    this.unIndent();
    this.sayln("");
    this.printSpaces();
    this.sayln("else");
    this.indent();
    s.elsee.accept(this);
    this.sayln("");
    this.unIndent();
    return;
  }

  
  @Override
  public void visit(Print s)
  {
    //this.printSpaces();
    this.say("  System_out_println (");
    s.exp.accept(this);
    this.sayln(");");
    return;
  }

  @Override
  public void visit(While s)
  {
	  //TODO
	  
	    this.printSpaces();
	    this.say("while (");
	    s.condition.accept(this);
	    this.sayln(")");
	    this.indent();
	    s.body.accept(this);
	    this.sayln("");
	    return;
	  
  }

  
  // type
  @Override
  public void visit(ClassType t)
  {
    this.say("struct " + t.id + " *");
  }

  @Override
  public void visit(Int t)
  {
    this.say("int");
  }

  @Override
  public void visit(IntArray t)
  {
	  //TODO
	  this.say("int* ");
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
	  //TODO
	  //d.id
	  //d.type
  }

  // method
  @Override
  public void visit(MethodSingle m)
  {
	  
	  this.say("char * " + m.classId +"_" + m.id + "_arguments_gc_map = \"");
	    for (Dec.T d : m.formals) {
	      DecSingle dec = (DecSingle) d;
	      if(dec.type instanceof Int)
	    	  this.say("0");
	      else
	    	  this.say("1");
	    }
	    this.sayln("\";");
	    
	  this.say("char * " + m.classId + "_" + m.id + "_locals_gc_map = \"");
	    for (Dec.T d : m.locals) {
	        DecSingle dec = (DecSingle) d;
		      if(dec.type instanceof Int)
		    	  this.say("0");
		      else
		    	  this.say("1");
	      }
	    this.sayln("\";");
	  
	    
//	    struct f_gc_frame{
//	    	  void *prev;                     // dynamic chain, pointing to f's caller's GC frame
//	    	  char *arguments_gc_map;         // should be assigned the value of "f_arguments_gc_map"
//	    	  int *arguments_base_address;    // address of the first argument
//	    	  char *locals_gc_map;            // should be assigned the value of "f_locals_gc_map"
//	    	  struct A *local1;               // remaining fields are method locals
//	    	  int local2;
//	    	  struct C *local3;
//	    	};
	    
	    //new stack the GC stack.
	    // a data structure declaration for f's GC frame
	    this.say("struct " + m.classId +"_" + m.id + "_gc_frame{\n  void \t*prev;\n  char \t*arguments_gc_map;\n  "
	    		+ "int \t*arguments_base_address;\n  char \t*locals_gc_map;\n");
	    for (Dec.T d : m.locals) {
	        DecSingle dec = (DecSingle) d;
	        if(!(dec.type instanceof Int)){
		        this.say("  ");
		        dec.type.accept(this);
		        this.say("\t" + dec.id + ";\n");
	        }
	      }
	    this.sayln("};\n");
	    
	    
	    
	_method = m;
    m.retType.accept(this);
    this.say(" " + m.classId + "_" + m.id + "(");
    int size = m.formals.size();
    for (Dec.T d : m.formals) {
      DecSingle dec = (DecSingle) d;
      size--;
      dec.type.accept(this);
      this.say(" " + dec.id);
      if (size > 0)
        this.say(", ");
    }
    this.sayln(")");
    this.sayln("{");

    
    this.sayln("  struct " + m.classId + "_" + m.id + "_gc_frame frame;");
    this.sayln("  frame.prev = prev;");
    this.sayln("  prev = &frame;");
    this.sayln("  frame.arguments_gc_map = " + m.classId  + "_" + m.id + "_arguments_gc_map;");
    this.sayln("  frame.arguments_base_address = &this;");
    this.sayln("  frame.locals_gc_map = " + m.classId +"_"+m.id + "_locals_gc_map;\n\n");
   
    
    for (Dec.T d : m.locals) {
        DecSingle dec = (DecSingle) d;
	    if(dec.type instanceof Int){
	    	this.say("  ");
	    	dec.type.accept(this);
	    	this.say(" " + dec.id + ";\n");
	    }else{
	    	this.say("  ");
	    	//dec.type.accept(this);
	    	this.say("frame." + dec.id + " = 0;\n");
	    }
      }  
    
//    for (Dec.T d : m.locals) {
//      DecSingle dec = (DecSingle) d;
//      this.say("  ");
//      dec.type.accept(this);
//      this.say(" " + dec.id + ";\n");
//    }
    this.sayln("");
    for (Stm.T s : m.stms)
      s.accept(this);
    
    this.sayln("  prev  = frame.prev;");
    this.say("  return ");
    m.retExp.accept(this);
    this.sayln(";");
    this.sayln("}");
    return;
  }

  @Override
  public void visit(MainMethodSingle m)
  {
	  
	  this.say("char * Tiger_main_locals_gc_map = \"");
	    for (Dec.T d : m.locals) {
	        DecSingle dec = (DecSingle) d;
		      if(dec.type instanceof Int)
		    	  this.say("0");
		      else
		    	  this.say("1");
	      }
	    this.sayln("\";");
	  
	  
	this.say("struct Tiger_main_gc_frame{\n  void \t*prev;\n  char \t*arguments_gc_map;\n  "
		+ "int \t*arguments_base_address;\n  char \t*locals_gc_map;\n");
    for (Dec.T d : m.locals) {
        DecSingle dec = (DecSingle) d;
        if(!(dec.type instanceof Int)){
        	this.say("  ");
        	dec.type.accept(this);
        	this.say("\t" + dec.id + ";\n");
        }
      }
	this.sayln("};\n");
	  
	  _mmethod = m;
    this.sayln("int Tiger_main ()");
    this.sayln("{");
    
    this.sayln("  struct Tiger_main_gc_frame frame;");
    this.sayln("  frame.prev = prev;");
    this.sayln("  prev = &frame;");
    this.sayln("  frame.arguments_gc_map = \"\";");
    this.sayln("  frame.arguments_base_address = \"\";");
    this.sayln("  frame.locals_gc_map = Tiger_main_locals_gc_map;\n\n");
    
    for (Dec.T d : m.locals) {
        DecSingle dec = (DecSingle) d;
        
        if(dec.type instanceof Int){
        	this.say("  ");
        	dec.type.accept(this);
        	this.say("\t" + dec.id + ";\n");
        }else{
          this.say("  ");
          this.say("frame." + dec.id + " = 0;\n");
        }
      }
    this.sayln("");
    
    isMmethod = true;
    m.stm.accept(this);
    isMmethod = false;
    this.sayln("}\n");
    return;
  }

  // vtables
  @Override
  public void visit(VtableSingle v)
  {
    this.sayln("struct " + v.id + "_vtable");
    this.sayln("{");
    this.sayln("  char *" + v.id + "_gc_map;");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      t.ret.accept(this);
      this.sayln(" (*" + t.id + ")();");
    }
    this.sayln("};\n");
    return;
  }

  private void outputVtable(VtableSingle v)
  {
    this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
    this.sayln("{");
    this.sayln( "  &" + v.id + "_gc_map,");
    for (codegen.C.Ftuple t : v.ms) {
      this.say("  ");
      this.sayln(t.classs + "_" + t.id + ",");
    }
    this.sayln("};\n");
    return;
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
	    this.say("\nchar * "+ c.id +"_gc_map = \"");
	  
	    for (Tuple d : c.decs) {
		      if(d.type instanceof Int)
		    	  this.say("0");
		      else
		    	  this.say("1");
		    }
	    this.sayln("\";\n");
	  
    this.sayln("struct " + c.id);
    this.sayln("{");
    this.sayln("  struct " + c.id + "_vtable *vptr;");
    for (codegen.C.Tuple t : c.decs) {
      this.say("  ");
      t.type.accept(this);
      this.say(" ");
      this.sayln(t.id + ";");
    }
    this.sayln("};");
    return;
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    // we'd like to output to a file, rather than the "stdout".
    try {
      String outputName = null;
      if (Control.ConCodeGen.outputName != null)
        outputName = Control.ConCodeGen.outputName;
      else if (Control.ConCodeGen.fileName != null)
        outputName = Control.ConCodeGen.fileName + ".c";
      else
        outputName = "a.c.c";

      this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
          new java.io.FileOutputStream(outputName)));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    this.sayln("// This is automatically generated by the Tiger compiler.");
    this.sayln("// Do NOT modify!\n");

    
    this.sayln("// structures");
    for (codegen.C.Ast.Class.T c : p.classes) {
      c.accept(this);
    }

    this.sayln("// vtables structures");
    for (Vtable.T v : p.vtables) {
      v.accept(this);
    }
    this.sayln("");
    

    this.sayln("// methods dec");
    for (Method.T m : p.methods) {
    	MethodSingle ms = (MethodSingle)m;
    	ms.retType.accept(this);
        this.say(" " + ms.classId + "_" + ms.id + "(");
        int size = ms.formals.size();
        for (Dec.T d : ms.formals) {
          DecSingle dec = (DecSingle) d;
          size--;
          dec.type.accept(this);
          this.say(" " + dec.id);
          if (size > 0)
            this.say(", ");
        }
        this.sayln(");");
    }

	this.sayln("\n\nextern void * prev;");
    
    this.sayln("// vtables");
    for (Vtable.T v : p.vtables) {
      outputVtable((VtableSingle) v);
    }
    this.sayln("");

    this.sayln("// methods");
    for (Method.T m : p.methods) {
      m.accept(this);
    }
    this.sayln("");

    this.sayln("// main method");
    p.mainMethod.accept(this);
    this.sayln("");

    this.say("\n\n");

    try {
      this.writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

  }

}

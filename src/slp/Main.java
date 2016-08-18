package slp;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

import slp.Slp.Exp;
import slp.Slp.Exp.Eseq;
import slp.Slp.Exp.Id;
import slp.Slp.Exp.Num;
import slp.Slp.Exp.Op;
import slp.Slp.Exp.T;
import slp.Slp.ExpList;
import slp.Slp.Stm;
import util.Bug;
import util.Todo;
import control.Control;

public class Main
{
  // ///////////////////////////////////////////
  // maximum number of args

  private int maxArgsExp(Exp.T exp)
  {
    new Todo();
    return -1;
  }

  int max = 0;
  int i = 0;
  private int maxArgsStm(Stm.T stm){
	  
	    if (stm instanceof Stm.Compound) {   	
	      Stm.Compound s = (Stm.Compound) stm;
	      int n1 = maxArgsStm(s.s1);
	      int n2 = maxArgsStm(s.s2);
	    } else if (stm instanceof Stm.Assign) {
	
	    	Stm.Assign s = (Stm.Assign) stm;
	    	Exp.T ss = (Exp.T)s.exp;
	    	dealExp(ss);
	    	
	    } else if (stm instanceof Stm.Print) {
	    	if(0 == max) max =1;
	    	Stm.Print s = (Stm.Print) stm;

	    	dealExplist(s.explist);
	    } else
	      new Bug();
	  	  
	  return max;
  }
  
  //dealExp
public void dealExp(Exp.T ss){
	if(ss instanceof Exp.Op)
		dealOp((Exp.Op)ss);
	if(ss instanceof Exp.Eseq)
		dealEseq((Exp.Eseq)ss);
  }
  
  //dealEseq
  public void dealEseq(Exp.Eseq ss){
	  maxArgsStm(((Exp.Eseq)ss).stm);
	  dealExp(((Exp.Eseq)ss).exp);
  }
  
  //dealOp
  public void dealOp(Exp.Op ss){
	  dealExp(((Exp.Op)ss).left);
	  dealExp(((Exp.Op)ss).right);
  }
  
  //dealExplist
  public void dealExplist(ExpList.T ss){
  	if(ss instanceof ExpList.Last)
		dealExp((Exp.T)(((ExpList.Last)(ss)).exp));
  	else{
  		dealExp(((ExpList.Pair)ss).exp);
  		dealPair((ExpList.Pair)ss);
  		dealExplist(((ExpList.Pair)ss).list);
  	}
  }
  
  //dealPair
  public void dealPair(ExpList.Pair ss){ 
	  	i = 1;
		ExpList.T temp = (ExpList.T)ss;
		while(temp instanceof ExpList.Pair){
			++i;
			temp = (ExpList.T)(((ExpList.Pair)temp).list);
		}
		if(i > max) max = i;
  }

  
  // ////////////////////////////////////////
  // interpreter
  
  HashMap<String, Integer> id_value = new HashMap<String, Integer>();

  private int interpExp(Exp.T exp)
  {
	  //数字
	  if(exp instanceof Exp.Num)
		  return ((Exp.Num)exp).num;
	  else if(exp instanceof Exp.Id){//标识符
		  //id_value.get(id);

		  return id_value.get(((Exp.Id)exp).id);
		  
	  }else if(exp instanceof Exp.Op){//计算
		  Exp.OP_T op = ((Exp.Op)exp).op;
		  Exp.T left = (Exp.T)(((Exp.Op)exp).left);
		  Exp.T right = (Exp.T)(((Exp.Op)exp).right);
		  
		  switch(op){
		  	case ADD :{
		  		return interpExp(left) + interpExp(right);
		  	}
		  	case SUB :{
		  		return interpExp(left) - interpExp(right);
		  	}
		  	case TIMES :{
		  		return interpExp(left) * interpExp(right);
		  	}
		  	default:return interpExp(left) / interpExp(right);
		  }

	  }else{ 
	  		interpStm(((Stm.T)(((Exp.Eseq)exp).stm)));
	  		return interpExp(((Exp.T)(((Exp.Eseq)exp).exp)));
	  }
	  
  }

  private void interpStm(Stm.T prog)
  {
    if (prog instanceof Stm.Compound) {
    	Stm.Compound s = (Stm.Compound) prog;
    	
    	interpStm(s.s1);
    	interpStm(s.s2);
    	
    } else if (prog instanceof Stm.Assign) {
    	
    	String t1 = ((Stm.Assign)prog).id;
    	Exp.T ss = (Exp.T)(((Stm.Assign)prog).exp);
    	int value = interpExp(ss);
    	
    	id_value.put(t1, value);
    	
    } else if (prog instanceof Stm.Print) {
    	ExpList.T ss = ((Stm.Print)prog).explist;	
    	interExplist(ss);
    	
    } else
      new Bug();
  }


  
 public void interExplist(ExpList.T ss){
	 
 	if(ss instanceof ExpList.Pair){
 		
		Exp.T tt2 = (Exp.T)(((ExpList.Pair)ss).exp);
		int value = interpExp(tt2);	
		System.out.print(value + "  ");
 		
		ExpList.T tt1 = (ExpList.T)(((ExpList.Pair)ss).list);
		interExplist(tt1);
	}
	
	if(ss instanceof ExpList.Last){
		Exp.T tt = (Exp.T)(((ExpList.Last)ss).exp);
		int value = interpExp(tt);
		
		System.out.println(value);
		
	}
	 
 }
  
  
  
 
 
  
  
  // ////////////////////////////////////////
  // compile
  HashSet<String> ids;
  StringBuffer buf;

  private void emit(String s)
  {
    buf.append(s);
  }

  private void compileExp(Exp.T exp)
  {
    if (exp instanceof Id) {
      Exp.Id e = (Exp.Id) exp;
      String id = e.id;

      emit("\tmovl\t" + id + ", %eax\n");
    } else if (exp instanceof Num) {
      Exp.Num e = (Exp.Num) exp;
      
      if(0 == e.num) 
      {
    	  System.out.println("divide by zero");
    	  System.exit(1);
      }
      int num = e.num;

      emit("\tmovl\t$" + num + ", %eax\n");
    } else if (exp instanceof Op) {
      Exp.Op e = (Exp.Op) exp;
      Exp.T left = e.left;
      Exp.T right = e.right;
      Exp.OP_T op = e.op;

      switch (op) {
      case ADD:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\taddl\t%edx, %eax\n");
        break;
      case SUB:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\tsubl\t%eax, %edx\n");
        emit("\tmovl\t%edx, %eax\n");
        break;
      case TIMES:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\timul\t%edx\n");
        break;
      case DIVIDE:
        compileExp(left);
        emit("\tpushl\t%eax\n");
        compileExp(right);
        emit("\tpopl\t%edx\n");
        emit("\tmovl\t%eax, %ecx\n");
        emit("\tmovl\t%edx, %eax\n");
        emit("\tcltd\n");
        emit("\tdiv\t%ecx\n");
        break;
      default:
        new Bug();
      }
    } else if (exp instanceof Eseq) {
      Eseq e = (Eseq) exp;
      Stm.T stm = e.stm;
      Exp.T ee = e.exp;

      compileStm(stm);
      compileExp(ee);
    } else
      new Bug();
  }

  private void compileExpList(ExpList.T explist)
  {
    if (explist instanceof ExpList.Pair) {
      ExpList.Pair pair = (ExpList.Pair) explist;
      Exp.T exp = pair.exp;
      ExpList.T list = pair.list;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
      compileExpList(list);
    } else if (explist instanceof ExpList.Last) {
      ExpList.Last last = (ExpList.Last) explist;
      Exp.T exp = last.exp;

      compileExp(exp);
      emit("\tpushl\t%eax\n");
      emit("\tpushl\t$slp_format\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  private void compileStm(Stm.T prog)
  {
    if (prog instanceof Stm.Compound) {
      Stm.Compound s = (Stm.Compound) prog;
      Stm.T s1 = s.s1;
      Stm.T s2 = s.s2;

      compileStm(s1);
      compileStm(s2);
    } else if (prog instanceof Stm.Assign) {
      Stm.Assign s = (Stm.Assign) prog;
      String id = s.id;
      Exp.T exp = s.exp;

      ids.add(id);
      compileExp(exp);
      emit("\tmovl\t%eax, " + id + "\n");
    } else if (prog instanceof Stm.Print) {
      Stm.Print s = (Stm.Print) prog;
      ExpList.T explist = s.explist;

      compileExpList(explist);
      emit("\tpushl\t$newline\n");
      emit("\tcall\tprintf\n");
      emit("\taddl\t$4, %esp\n");
    } else
      new Bug();
  }

  // ////////////////////////////////////////
  public void doit(Stm.T prog)
  {
    // return the maximum number of arguments
    if (Control.ConSlp.action == Control.ConSlp.T.ARGS) {
    	
  	  //System.out.println("Main doit");
  	  
      int numArgs = maxArgsStm(prog);
      System.out.println(numArgs);
    }

    // interpret a given program
    if (Control.ConSlp.action == Control.ConSlp.T.INTERP) {
    	//System.out.println("executu interp stm");
      interpStm(prog);
    }

    // compile a given SLP program to x86
    if (Control.ConSlp.action == Control.ConSlp.T.COMPILE) {
  	  //System.out.println("d3");
      ids = new HashSet<String>();
      buf = new StringBuffer();

      compileStm(prog);
      try {
        // FileOutputStream out = new FileOutputStream();
        FileWriter writer = new FileWriter("slp_gen.s");
        writer.write("// Automatically generated by the Tiger compiler, do NOT edit.\n\n");
        writer.write("\t.data\n");
        writer.write("slp_format:\n");
        writer.write("\t.string \"%d \"\n");
        writer.write("newline:\n");
        writer.write("\t.string \"\\n\"\n");
        for (String s : this.ids) {
          writer.write(s + ":\n");
          writer.write("\t.int 0\n");
        }
        writer.write("\n\n\t.text\n");
        writer.write("\t.globl main\n");
        writer.write("main:\n");
        writer.write("\tpushl\t%ebp\n");
        writer.write("\tmovl\t%esp, %ebp\n");
        writer.write(buf.toString());
        writer.write("\tleave\n\tret\n\n");
        writer.close();
        Process child = Runtime.getRuntime().exec("gcc slp_gen.s");
        child.waitFor();
        if (!Control.ConSlp.keepasm)
          Runtime.getRuntime().exec("rm -rf slp_gen.s");
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }
      // System.out.println(buf.toString());
    }
  }
}

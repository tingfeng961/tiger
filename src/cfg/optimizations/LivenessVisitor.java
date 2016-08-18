package cfg.optimizations;

import java.util.HashMap;
import java.util.HashSet;

import cfg.Cfg.Block;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand;
import cfg.Cfg.Operand.ArraySelect;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Length;
import cfg.Cfg.Operand.Not;
import cfg.Cfg.Operand.This;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm;
import cfg.Cfg.Stm.Add;
import cfg.Cfg.Stm.And;
import cfg.Cfg.Stm.InvokeVirtual;
import cfg.Cfg.Stm.Lt;
import cfg.Cfg.Stm.Move;
import cfg.Cfg.Stm.MoveArray;
import cfg.Cfg.Stm.NewIntArray;
import cfg.Cfg.Stm.NewObject;
import cfg.Cfg.Stm.Print;
import cfg.Cfg.Stm.Sub;
import cfg.Cfg.Stm.Times;
import cfg.Cfg.Transfer;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Type.ClassType;
import cfg.Cfg.Type.IntArrayType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;

public class LivenessVisitor implements cfg.Visitor
{
  // gen, kill for one statement
  private HashSet<String> oneStmGen;
  private HashSet<String> oneStmKill;

  // gen, kill for one transfer
  private HashSet<String> oneTransferGen;
  private HashSet<String> oneTransferKill;

  // gen, kill for statements
  private HashMap<Stm.T, HashSet<String>> stmGen;
  private HashMap<Stm.T, HashSet<String>> stmKill;

  // gen, kill for transfers
  private HashMap<Transfer.T, HashSet<String>> transferGen;
  private HashMap<Transfer.T, HashSet<String>> transferKill;

  // gen, kill for blocks
  private HashMap<Block.T, HashSet<String>> blockGen;
  private HashMap<Block.T, HashSet<String>> blockKill;

  // liveIn, liveOut for blocks
  private HashMap<Block.T, HashSet<String>> blockLiveIn;
  private HashMap<Block.T, HashSet<String>> blockLiveOut;

  // liveIn, liveOut for statements
  public HashMap<Stm.T, HashSet<String>> stmLiveIn;
  public HashMap<Stm.T, HashSet<String>> stmLiveOut;

  // liveIn, liveOut for transfer
  public HashMap<Transfer.T, HashSet<String>> transferLiveIn;
  public java.util.HashMap<Transfer.T, java.util.HashSet<String>> transferLiveOut;

  // will walk the tree for many times, so
  // it will be useful to recored which is which:
  enum Liveness_Kind_t
  {
    None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
  }

  private Liveness_Kind_t kind = Liveness_Kind_t.None;

  public LivenessVisitor()
  {
    this.oneStmGen = new HashSet<>();
    this.oneStmKill = new java.util.HashSet<>();

    this.oneTransferGen = new java.util.HashSet<>();
    this.oneTransferKill = new java.util.HashSet<>();

    this.stmGen = new java.util.HashMap<>();
    this.stmKill = new java.util.HashMap<>();

    this.transferGen = new java.util.HashMap<>();
    this.transferKill = new java.util.HashMap<>();

    this.blockGen = new java.util.HashMap<>();
    this.blockKill = new java.util.HashMap<>();

    this.blockLiveIn = new java.util.HashMap<>();
    this.blockLiveOut = new java.util.HashMap<>();

    this.stmLiveIn = new java.util.HashMap<>();
    this.stmLiveOut = new java.util.HashMap<>();

    this.transferLiveIn = new java.util.HashMap<>();
    this.transferLiveOut = new java.util.HashMap<>();

    this.kind = Liveness_Kind_t.None;
  }

  // /////////////////////////////////////////////////////
  // utilities

  private java.util.HashSet<String> getOneStmGenAndClear()
  {
    java.util.HashSet<String> temp = this.oneStmGen;
    this.oneStmGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneStmKillAndClear()
  {
    java.util.HashSet<String> temp = this.oneStmKill;
    this.oneStmKill = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferGenAndClear()
  {
    java.util.HashSet<String> temp = this.oneTransferGen;
    this.oneTransferGen = new java.util.HashSet<>();
    return temp;
  }

  private java.util.HashSet<String> getOneTransferKillAndClear()
  {
    java.util.HashSet<String> temp = this.oneTransferKill;
    this.oneTransferKill = new java.util.HashSet<>();
    return temp;
  }

  // /////////////////////////////////////////////////////
  // operand
  @Override
  public void visit(Int operand)
  {
    return;
  }

  @Override
  public void visit(Var operand)
  {
    this.oneStmGen.add(operand.id);
    return;
  }

  // statements
  @Override
  public void visit(Add s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }
  
  @Override
  public void visit(And m) {
  	// TODO
  	
  }

  @Override
  public void visit(InvokeVirtual s)
  {
    this.oneStmKill.add(s.dst);
    this.oneStmGen.add(s.obj);
    for (Operand.T arg : s.args) {
      arg.accept(this);
    }
    return;
  }

  @Override
  public void visit(Lt s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(Move s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.src.accept(this);
    return;
  }

  @Override
  public void visit(NewObject s)
  {
    this.oneStmKill.add(s.dst);
    return;
  }

  @Override
  public void visit(Print s)
  {
    s.arg.accept(this);
    return;
  }

  @Override
  public void visit(Sub s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  @Override
  public void visit(Times s)
  {
    this.oneStmKill.add(s.dst);
    // Invariant: accept() of operand modifies "gen"
    s.left.accept(this);
    s.right.accept(this);
    return;
  }

  // transfer
  @Override
  public void visit(If s)
  {
    // Invariant: accept() of operand modifies "gen"
    s.operand.accept(this);
    return;
  }

  @Override
  public void visit(Goto s)
  {
    return;
  }

  @Override
  public void visit(Return s)
  {
    // Invariant: accept() of operand modifies "gen"
    s.operand.accept(this);
    return;
  }

  // type
  @Override
  public void visit(ClassType t)
  {
  }

  @Override
  public void visit(IntType t)
  {
  }

  @Override
  public void visit(IntArrayType t)
  {
  }

  // dec
  @Override
  public void visit(DecSingle d)
  {
  }

  // utility functions:
  private void calculateStmTransferGenKill(BlockSingle b)
  {
    for (Stm.T s : b.stms) {
      this.oneStmGen = new java.util.HashSet<>();
      this.oneStmKill = new java.util.HashSet<>();      
      s.accept(this);
      this.stmGen.put(s, this.oneStmGen);
      this.stmKill.put(s, this.oneStmKill);
      if (control.Control.isTracing("liveness.step1")) {
        System.out.print("\ngen, kill for statement:");
        System.out.println(s.toString());
        //s.toString();
        System.out.print("\ngen is:");
        for (String str : this.oneStmGen) {
          System.out.print(str + ", ");
        }
        System.out.print("\nkill is:");
        for (String str : this.oneStmKill) {
          System.out.print(str + ", ");
        }
      }
    }
    this.oneTransferGen = new java.util.HashSet<>();
    this.oneTransferKill = new java.util.HashSet<>();
    b.transfer.accept(this);
    this.transferGen.put(b.transfer, this.oneTransferGen);
    this.transferKill.put(b.transfer, this.oneTransferGen);
    if (control.Control.isTracing("liveness.step1")) {
      System.out.print("\ngen, kill for transfer:");
      b.toString();
      System.out.print("\ngen is:");
      for (String str : this.oneTransferGen) {
        System.out.print(str + ", ");
      }
      System.out.println("\nkill is:");
      for (String str : this.oneTransferKill) {
        System.out.print(str + ", ");
      }
    }
    return;
  }
  
  
  // utility functions:
  private void calculateBlockGenKill(BlockSingle b)
  {
    java.util.HashSet<String> blockGen = new java.util.HashSet<>();
    java.util.HashSet<String> blockKill = new java.util.HashSet<>();
    
    this.oneStmGen = this.transferGen.get(b.transfer);
    this.oneStmKill = this.transferKill.get(b.transfer);
    
    for (String str : this.oneStmGen) {
    		blockGen.add(str);
      }
    for (String str : this.oneStmKill) {
    	blockKill.add(str);
  }
    
    for(int i = b.stms.size()-1; i>=0; --i ){
    	java.util.HashSet<String> pgen = this.stmGen.get(b.stms.get(i));
    	java.util.HashSet<String> pkill = this.stmKill.get(b.stms.get(i));
    	
        //gen[pn] = gen[n] u (gen[p] - kill[n])
        //kill[pn] = kill[p] u kill[n]

        for (String str : pgen) {
        	if(!blockKill.contains(str))
        		blockGen.add(str);
        		
          }
        
        for (String str : pkill) {
        	blockKill.add(str);
          }
    }
    
    this.blockGen.put(b, blockGen);
    this.blockKill.put(b, blockKill);
    
      if (control.Control.isTracing("liveness.step2")) {
        System.out.print("\ngen, kill for block:");
        
        System.out.print("\ngen is:");
        for (String str : blockGen) {
          System.out.print(str + ", ");
        }
        
        System.out.print("\nkill is:");
        for (String str : blockKill) {
          System.out.print(str + ", ");
        }
        System.out.println();
      }

    return;
  }
  

  // block
  @Override
  public void visit(BlockSingle b)
  {
    switch (this.kind) {
    case StmGenKill:
      calculateStmTransferGenKill(b);
      break;
    case BlockGenKill:
        calculateBlockGenKill(b);
        break;
    default:
      // 
      return;
    }
  }

  // method
  @Override
  public void visit(MethodSingle m)
  {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.


    this.kind = Liveness_Kind_t.BlockGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }
    
    
    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer

  }

  @Override
  public void visit(MainMethodSingle m)
  {
    // Four steps:
    // Step 1: calculate the "gen" and "kill" sets for each
    // statement and transfer
    this.kind = Liveness_Kind_t.StmGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }

    // Step 2: calculate the "gen" and "kill" sets for each block.
    // For this, you should visit statements and transfers in a
    // block in a reverse order.
    
    
    this.kind = Liveness_Kind_t.BlockGenKill;
    for (Block.T block : m.blocks) {
      block.accept(this);
    }
    
    
    
    
    

    // Step 3: calculate the "liveIn" and "liveOut" sets for each block
    // Note that to speed up the calculation, you should first
    // calculate a reverse topo-sort order of the CFG blocks, and
    // crawl through the blocks in that order.
    // And also you should loop until a fix-point is reached.
    // Your code here:

    // Step 4: calculate the "liveIn" and "liveOut" sets for each
    // statement and transfer
  }

  // vtables
  @Override
  public void visit(VtableSingle v)
  {
  }

  // class
  @Override
  public void visit(ClassSingle c)
  {
  }

  // program
  @Override
  public void visit(ProgramSingle p)
  {
    p.mainMethod.accept(this);
    for (Method.T mth : p.methods) {
      mth.accept(this);
    }
    return;
  }

@Override
public void visit(ArraySelect o) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(Not o) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(Length o) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(This o) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(MoveArray m) {
	// TODO Auto-generated method stub
	
}

@Override
public void visit(NewIntArray m) {
	// TODO Auto-generated method stub
	
}

}

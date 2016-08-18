package elaborator;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import error.Error;
import ast.Ast.Dec;
import ast.Ast.Type;
import util.Todo;


/**
 * 仅仅有一个hash成员变量、意图很明显哈
 */
public class MethodTable
{
  private java.util.Hashtable<String, Type.T> table;

  public MethodTable()
  {
    this.table = new java.util.Hashtable<String, Type.T>();
  }

  // Duplication is not allowed
  /**
   * 
   * @param formals 参数列表
   * @param locals 成员变量
   */
  public void put(LinkedList<Dec.T> formals, LinkedList<Dec.T> locals)
  {
    for (Dec.T dec : formals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
    	  Error.error("error", "elaborator", "duplicated parameter: " + decc.id, decc.lineNum);
        //System.exit(1);
        return;
      }
      this.table.put(decc.id, decc.type);
    }

    for (Dec.T dec : locals) {
      Dec.DecSingle decc = (Dec.DecSingle) dec;
      if (this.table.get(decc.id) != null) {
    	  Error.error("error", "elaborator", "duplicated variable: " + decc.id, decc.lineNum);
        //System.exit(1);
        return;
      }
      this.table.put(decc.id, decc.type);
    }

  }

  
  // return null for non-existing keys
  /**
   * 获得返回类型？？？
   * @param id
   * @return
   */
  public Type.T get(String id)
  {
    return this.table.get(id);
  }

  public void dump()
  {
	  Enumeration<String> methodEm = this.table.keys();
	  String name;
	  while(methodEm.hasMoreElements()){
		  name = methodEm.nextElement();
		  System.out.printf("%-9s %-7s%n", name, this.table.get(name));
		  
	  }
   // new Todo();
  }

  @Override
  public String toString()
  {
    return this.table.toString();
  }

public void checkUse() {
	// TODO Auto-generated method stub
	
		Enumeration<String> methodsEm = table.keys();
		while(methodsEm.hasMoreElements()){
			String type = methodsEm.nextElement();
			Type.T tt = table.get(type);
			if(!tt.isUsed)
				Error.error("warning", "elaborator", "the args " + type + " is never used", tt.lineNum);
		}    
	}
}

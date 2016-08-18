package elaborator;

import java.util.Hashtable;

import error.Error;
import ast.Ast.Type;



public class ClassBinding
{
  public String extendss; // null for non-existing extends 父类
  public java.util.Hashtable<String, Type.T> fields;	
  public java.util.Hashtable<String, MethodType> methods;
  public boolean isUsed = false;
  public int lineNum;

  /**
   * 
   * @param extendss
   */
  public ClassBinding(String extendss, Integer lineNum)
  {
    this.extendss = extendss;
    this.fields = new Hashtable<String, Type.T>();
    this.methods = new Hashtable<String, MethodType>();
    this.lineNum = lineNum;
  }

  
  /**
   * 
   * @param extendss
   * @param fields
   * @param methods
   */
  public ClassBinding(String extendss,
      java.util.Hashtable<String, Type.T> fields,
      java.util.Hashtable<String, MethodType> methods)
  {
    this.extendss = extendss;
    this.fields = fields;
    this.methods = methods;
  }

  
  /**
   * 
   * @param xid
   * @param type
   */
  public void put(String xid, Type.T type, Integer lineNum)
  {
    if (this.fields.get(xid) != null) {
    	Error.error("error", "elaborator", "duplicated class field: " + xid, lineNum);
      //System.exit(1);
      return;
    }
    this.fields.put(xid, type);
  }

  
  /**
   * 
   * @param mid
   * @param mt
   */
  public void put(String mid, MethodType mt)
  {
    if (this.methods.get(mid) != null) {
    	Error.error("error", "elaborator", "duplicated class method: " + mid, lineNum);
      //System.exit(1);
      return;
    }
    this.methods.put(mid, mt);
  }

  
  @Override
  public String toString()
  {
    System.out.print("extends: ");
    if (this.extendss != null)
      System.out.println(this.extendss);
    else
      System.out.println("<>");
    System.out.println("\nfields:\n  ");
    System.out.println(fields.toString());
    System.out.println("\nmethods:\n  ");
    System.out.println(methods.toString());

    return "";
  }

}

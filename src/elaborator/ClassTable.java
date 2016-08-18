package elaborator;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

import control.Control.ConAst;
import error.Error;
import ast.Ast.Dec;
import ast.Ast.Type;
import ast.Ast.Type;
import ast.Ast.Dec.DecSingle;
import util.Todo;

/**
 * 仅仅有一个hash成员变量、意图很明显哈
 */
public class ClassTable
{
  // map each class name (a string), to the class bindings.
  private  java.util.Hashtable<String, ClassBinding> table;

  public ClassTable()
  {
    this.table = new java.util.Hashtable<String, ClassBinding>();
  }

  // Duplication is not allowed
  /**
   * 绑定父类 不允许重复添加
   */
  public void put(String c, ClassBinding cb)
  {
    if (this.table.get(c) != null) {
      System.out.println("duplicated class: " + c);
      //System.exit(1);
    }
    this.table.put(c, cb);
  }

  // put a field into this table
  // Duplication is not allowed
  /**
   * 类的成员函数？
   * 将表项添加到其中
   */
  public void put(String c, String id, Type.T type, Integer lineNum)
  {
    ClassBinding cb = this.table.get(c);
    cb.put(id, type, lineNum);
    return;
  }

  // put a method into this table
  // Duplication is not allowed.
  // Also note that MiniJava does NOT allow overloading.
  /**
   * 类中的方法
   * 添加方法到表中 且不允许重载
   */
  public void put(String c, String id, MethodType type)
  {
    ClassBinding cb = this.table.get(c);
    cb.put(id, type);
    return;
  }

  // return null for non-existing class
  /**
   *由类名获得class
   */
  public ClassBinding get(String className)
  {
	  ClassBinding cls = this.table.get(className);
	  cls.isUsed = true;
    return cls;
  }

  // get type of some field
  // return null for non-existing field.
  /**
   * 获得类中的成员
   * get type of some field
   */
  public Type.T get(String className, String xid)
  {
    ClassBinding cb = this.table.get(className);
    Type.T type = cb.fields.get(xid);
    while (type == null) { // search all parent classes until found or fail
      if (cb.extendss == null)
        return type;

      cb = this.table.get(cb.extendss);
      type = cb.fields.get(xid);
    }
    return type;
  }

  
  // get type of some method
  // return null for non-existing method
  /**
   * 获得类中的方法
   * get type of some method
   */
  public MethodType getm(String className, String mid)
  {
    ClassBinding cb = this.table.get(className);
    MethodType type = cb.methods.get(mid);
    while (type == null) { // search all parent classes until found or fail
      if (cb.extendss == null)
        return type;

      cb = this.table.get(cb.extendss);
      type = cb.methods.get(mid);
    }
	type.isUsed = true;
    return type;
  }

  public void dump()
  {
	  Enumeration<String> classsesEm = this.table.keys();
	    
		while(classsesEm.hasMoreElements()){
			String key = classsesEm.nextElement();
			
	    	String _tableLine  = "---------------------------calss:" + key +"-----------------------------------------------------";
	    	System.out.println(_tableLine.subSequence(0, 75));
	    	
	    	ClassBinding calssSingal = this.table.get(key);
	    	
	    	//父类
	    	if(null != calssSingal.extendss) 
	    		System.out.println("extends from : " + calssSingal.extendss + "\n");
	    	else
	    		System.out.println("has no fathers" + "\n");
	    	
	    	//对象
	    	Hashtable<String, Type.T> fields = calssSingal.fields;
	    	if(0 == fields.size()) 
	    		System.out.println("has no fields" + "\n");
	    	else{
	    		System.out.println("has fields as follows");
	    		
	    		Enumeration<String> fieldsEm = fields.keys();
	    		while(fieldsEm.hasMoreElements()){
	    			String type = fieldsEm.nextElement();
	    			System.out.printf("%-7s %-7s%n", type, fields.get(type));
	    		}
	    		System.out.println();
	    	}
	    	
	    	//方法
	    	Hashtable<String,MethodType> methods = calssSingal.methods;
	    	if(0 == methods.size()) 
	    		System.out.println("has no methods" + "\n");
	    	else{
	    		System.out.println("has methods as follows");
	    		
	    		Enumeration<String> methodsEm = methods.keys();
	    		while(methodsEm.hasMoreElements()){
	    			String type = methodsEm.nextElement();
	    			System.out.printf("%-7s = %-7s%n", type, methods.get(type));
	    		}    
	    	}
	    System.out.println();
	    }
	  
    //new Todo();
  }
  
  public void checkUse(){
	  Enumeration<String> classsesEm = this.table.keys();
	    
		while(classsesEm.hasMoreElements()){
			String key = classsesEm.nextElement();
			
//	    	String _tableLine  = "---------------------------calss:" + key +"-----------------------------------------------------";
//	    	System.out.println(_tableLine.subSequence(0, 75));
	    	
	    	ClassBinding calssSingal = this.table.get(key);
	    	
	    	if(!calssSingal.isUsed)
	    		Error.error("warning", "elaborator", "calss " + key + " is never used", calssSingal.lineNum);
	    	
			//检测形参 和 局部变量是否被使用
	    	Hashtable<String, Type.T> fields = calssSingal.fields;
	    	if(0 != fields.size()){
	    		Enumeration<String> fieldsEm = fields.keys();
	    		while(fieldsEm.hasMoreElements()){
	    			String type = fieldsEm.nextElement();
	    			Type.T tt = fields.get(type);
					if(!tt.isUsed)
						Error.error("warning", "elaborator", "the arg " + type + " is never used", tt.lineNum);
	    		}
	    	}
	    	
	    	
	    	
	    	//方法
	    	Hashtable<String,MethodType> methods = calssSingal.methods;
	    	if(0 != methods.size()){
	    		Enumeration<String> methodsEm = methods.keys();
	    		while(methodsEm.hasMoreElements()){
	    			String type = methodsEm.nextElement();
	    			MethodType tt = methods.get(type);
					if(!tt.isUsed)
						Error.error("warning", "elaborator", "the method " + type + " is never used", tt.retType.lineNum);
	    		}    
	    	}
	    System.out.println();
	    }
  }
  

  @Override
  public String toString()
  {
    return this.table.toString();
  }
}

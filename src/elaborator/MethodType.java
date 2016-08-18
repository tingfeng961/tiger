package elaborator;

import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;

/**
 * 函数类型 由 返回类型 + 参数列表
 * @author wxg
 *
 */
public class MethodType
{
  public Type.T retType;
  public LinkedList<Dec.T> argsType;
  public boolean isUsed = false;

  /**
   * 
   * @param retType 返回类型
   * @param decs 参数列表
   */
  public MethodType(Type.T retType, LinkedList<Dec.T> decs)
  {
    this.retType = retType;
    this.argsType = decs;
  }

  @Override
  public String toString()
  {
    String s = "";
    for (Dec.T dec : this.argsType) {
    	Dec.DecSingle decc = (Dec.DecSingle) dec;
    	s = s + decc.type.toString() + " " + decc.id;
    	if(!dec.equals(this.argsType.get(this.argsType.size()-1)))
    		s += ", ";
    }
    s = "( " + s + " )" + " -> " + this.retType.toString();
    return s;
  }
}

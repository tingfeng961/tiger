package error;

public class Error {
	
	String eType;
	String eMessage;
	String lineNUm;
	
	public static void error(String type, String step, String eMss, Integer lineNum){
		
		System.out.printf("%-7s: in %-7s %-7s     at line %d\n", type, step ,eMss, lineNum);
	}

}

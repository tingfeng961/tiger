class Factorial { 
	public static void main(String[] a) {
        System.out.println(new Fac().ComputeFac(10));
    }
}

class Fac{
    int field1;                                                       
    int field1;                                                      //redefine fields field1(error)
    int field2;~                                                     //unrecognize char "~"(error)
                                                                     //field never used(warning)
    public int ComputeFac(int num) {
        int num_aux;
        boolean isTrue;
        

        if ((num < 1) & isTrue){
            num_aux = isTrue;                                        // '=' must have two same args(error)
            field1 = field1 + 1 * 1;                                 //no error
        }

        else
            num_aux = num * isTrue;                                  // the right of '*' must be 'int'(error)

        if (field1 + b)                                              //if condition must be boolean(error)
            num_aux = 1;                                             //b is undefine(error)
                                                                     //the right of '+' must be int(error)
        else
            {num_aux = num * (1);}
        return num_aux;

    }

    public int Unuse(int n) { return 0; }                           //method not used(warning)
    public int mInFather(FacChild child) { return 0; } 
    public int mInFather2(Fac father) { return 0; }            
}

class FacChild extends Fac{
    int tt;
    FacChild child;
    Fac father;

    public int ComputeFac() {
        tt = child.mInFather(child);                                //correct args
        tt = child.mInFather(father);                               //expect child father is not allow(error)
        tt = child.mInFather2(child);                               //nedd father but child is allow
        return 0;
    }

}



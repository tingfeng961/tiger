# tiger
The tiger compile is an experiment of fundamentals of compiling, we do it as follow steps:

Lab 1: Lexer and Parser 

Lab Overview
In these series of labs in this course, your job is to design and implement a compiler from scratch, called Tiger, 
for the MiniJava programming language. MiniJava is a non-trivial subset of the Java programming language, 
which is described in the appendix of the Tiger book. Meanwhile, you will use Java as the implementation programming 
language (so we are following the famous chicken and egg tradition in compiler design history). 
At the end of this course, you'll learn in depth how to implement a compiler for a modern OO language like Java, using Java.   

There are six labs and a final project planned:   

Lab 1: lexer and parser. In this lab, you'll design and implement a front-end (a lexer and a parser) for Tiger;   
Lab 2: abstract syntax trees and elaborator. This lab will require you to design and implement an abstract syntax tree and build an elaborator;   
Lab 3: code generator. In the 3rd lab, you'll design and implement several code generators: a C code generator, a Java bytecode generator, a Google Dalvik generator, and an x86 code generator;   
Lab 4: garbage collector. In the 4th lab, you'll build a garbage collector and link it into Tiger;   
Lab 5: Optimizations. This lab will require you to add optimizations to Tiger to enable it generator more efficient target code.   
Lab 6: Register allocator. In the 6th lab, you'll write a register allocator for Tiger so that your Tiger compiler will generate efficient production-quality native code.   
Finally, there is a final project, in which you are required to propose your own ideas and to do some non-trivial projects.   
This is the first lab. This lab consists of three parts: the Straight-Line Programming Language (SLP) and its interpreter and compiler; 
the Tiger lexer, and the Tiger parser. 

Software Setup
You should finish all labs on the Linux OS, for the Tiger compiler will make use of other tool chains on Linux. 

In this lab, the implementaion language you will use is Java, check here for the installation issues. Important note: make sure you've installed the latest Java compiler and Java runtime (version 8.0 or above, version 7.0 or below do NOT compile the Tiger code!). 

Eclipse is a very popular Java IDE. You can also use other IDEs, such as IDEA, netbeans, or others. 

Version control and Git
In each lab, we'll supply skeleton code for you to start with and modify. These files are controlled and distributed using the Git version control system. So, to access these files, you will need a Git client installed on your machine. On Linux, it's easy to install a Git client: 
  # apt-get install git

For those who have no experience in using Git before, don't forget to refer to the official manual when in doubt. 

Getting Started
We've decided to put the Tiger code repository on GitHub (this courseware is open-source). Now, you can start by checking out the code repository for lab 1 to your machine: 
  $ git clone https://github.com/bjhua/tiger.git

which will create a new folder tiger in which resides all the source files just checked out for Lab 1. 
Git is a distributed version control system, for it allows one to work with the local version of repository on her own machine. For instance, if you have finished exercise 5 of Lab 1, you can commit the changes by: 

  $ git commit -am 'my solution to lab1 exercise 5'

which will commit the changes to your local repository. 
Now import the project into your Eclipse and browse the source code: 

  src/Tiger.java:   the Tiger "Main" class
  src/lexer/*:      the Tiger lexer
  src/parser/*:     the Tiger parser
  src/control/*:    options to control the behavior of the Tiger compiler
  src/util/*:       utility classes
  src/slp/*:        the SLP interpreter and compiler
  test/*:           sample MiniJava programs as test cases

Now, build the Tiger compiler on your machine (in Eclipse). As a result of the building process, a new bin folder would be created. Now run the Tiger compiler on the prompt: 
  $ java -cp bin Tiger -help

which will output something like: 
The Tiger compiler. Copyright (C) 2013-, SSE of USTC.
Usage: java Tiger [options] <filename>

Available options:
   -help                      show this help information
   -lex                       dump the result of lexical analysis
   -slp {args|interp|compile} run the SLP interpreter
   -testlexer                 whether or not to test the lexer

Lab Requirement
There are two kinds of exercises: normal exercises and challenge ones. Challenge exercises may not be that hard, but may involve substantial code hacking. You are required to do ALL normal exercises. All challenge exercises are optional. 
Hand-in Procedure
When you finished your lab, zip you solutions and submit to the school's information system. 
Part A: SLP Interpreter and Compiler
In this part of the lab, you will do some programming exercises to warm you up. To be specific, you will write an interpreter and a compiler for a small programming language SLP, which stands for Straight-Line Programming language. The syntax for SLP is given in chapter 1 of the Tiger book. As its name illustrates, SLP is very simple in that it contains no control structures as found in most languages. 
This part of the lab serves as an introduction to environments (symbol tables mapping variable names to information about the variables); to abstract syntax (data structures representing the phrase structure of programs); to recursion over tree data structures, useful in many parts of a compiler; and to a functional style of programming without assignment statements. 

SLP Abstract Syntax
SLP contain just two syntactic forms: statements and expressions. You can read its syntax definition in chapter 1 of the Tiger book. Exercise 1. Read the SLP syntax in the chapter 1 of the Tiger book, make sure you understand how an SLP program is formed. As a specific example, explain to yourself why this program is legal according to SLP syntax: 

  a := 5+3;
  b := (print(a, a-1), 10*a); 
  print(b)

What's this program's output? 
The central idea of a compiler is to encode the language syntax with some data structures. We will delve into this topic in lab 2. For the purpose of this lab, we have offered you some Java classes which encode the SLP syntax. 

Exercise 2. Read the class definitions in src/slp/Slp.java, make sure you understand how these classes correspond to the SLP syntax. As a specific example, make sure you understand how the object src/slp/Samples.prog encodes the following SLP program: 

  a := 5+3;
  b := (print(a, a-1), 10*a); 
  print(b)


Maximum Argument Number
A program may contain zero, one or more print statements, each taking one or more arguments. For instance, the above SLP program has two print statements: the former one has two arguments and the latter one has one argument. The maximum argument number of a program is the maximum number of arguments from all print statements. Exercise 3. Write a Java function int maxargs(Stm s) that tells the maximum number of arguments of any print statement within any given statement. For example, maxargs(prog) returns 2. 
When you finish this exercise, you can run your compiler: 
  $ java -cp bin Tiger -slp args

which should output: 
  $ 2

your code is buggy, if your code output something other than this. Fix any bug before continuing. 
Interpreter
Generally speaking, there are two ways to run a program: interpretation and compilation. In this part, you will first write an interpreter for SLP, and in the next part, you will write a compiler so that you can gain deeper understanding of both the two ways. 
Basically, an interpreter runs a given program online, that is, it analyzes the program and mimic the behaviour of the program during the analysis. As SLP has assignment statement like x:=e, so the interpreter must abstract the memory, which keeps track of the current value of a variable. There are several ways to implement an abstract memory: you can use a imperative memory, or you can use functional memory. 

In an imperative memory, when a variable x is assigned a new value v, the memory chunk for x will be updated (thus, the old memory state is modified); whereas in a functional memory, when some variable is changed, a new memory is generated without modifying the old memory. Thus, a functional memory model, and generally speaking, a functional programming style, can make the code much more elegant and maintainable. 

Exercise 4. Write a Java function void interp(Stm s) that "interprets" a program in this language SLP. To write in a "functional programming" style - in which you never update old states, but generate new ones. 
When you finish this exercise, you can run your compiler: 
  $ java -cp bin Tiger -slp interp

which should output: 
  $ 8 7
    80

note that the number 7 is followed by a newline character. Fix any bug before continuing. 
A Compiler from SLP to x86
We have also offered you a small compiler which compiles an SLP program to x86 assembly. We have offered you all the code, you can run the compiler: 
  $ java -cp bin Tiger -slp compile

which should generate an executable a.out. Of course, you can see the generated x86 assembly via an undocumented option keepasm (uhhh, this is a backdoor? and you can think what can happen, if you don't have the source code of a compiler): 
  $ java -cp bin Tiger -slp compile -slp keepasm

which should output an assembly file. Exercise 5. Read the code to make sure you understand how an SLP program is compiled to x86 assembly. Now, you can try to compile another SLP program: src/slp/Samples/dividebyzero 

  $ java -cp bin Tiger -slp compile -slp div

What's the output? Modify the compiler's source code to exit more gracefully for "divide by zero". 
Part B: The Lexer
Lexical analysis is very first phase of a compiler, which reads as input the program source files and outputs a series of tokens recognized. And the first step in designing and implementing a lexer is to design good data structures to represent the input and output, nevertheless to say, this task is implementation language dependent. In Java, the input can be represented as some kind of (buffered) input stream established from the program source text file, and the output token needs to be represented as another data structure. 
The data structure defined for token can be found in the class Token in file lexer/Token.java. This data structure consists of three fields: 

kind: the kind of the token, which is of enumerable value as defined by the enum Kind type; 
lexeme: extra information that some kind of tokens may come with; 
lineNum: the position of the token in the source program (this information is crucial for later part of the compiler, such as error diagonising and profiling, etc..). For now, we just use a line number to represent current token's position. 
Exercise 6. There are a bunch of token types defined (TOKEN_ADD, TOKEN_AND, etc..) already. You should read the MiniJava specification carefully, and do these two tasks: first, determine whether or not these token kinds are enough to represent ALL possible tokens in the language, and supply other token kinds if necessary). Second, determine which kinds of tokens require an extra "lexeme" information. 

Generally, there are two approaches to implement a lexer: the hand-written approach and the automatic lexer generator approach. Both of them are very popular in complier implementations. In this lab, we will use a hand-written approach in implementing the lexer for Tiger, and this approach are also used by compilers such as GCC or LLVM. 

The method nextTokenInternal() in the file lexer/Lexer.java does the working of lexing. Each time being called, this method will recognize and return just one token. 

Exercise 7. Read the MiniJava specification and study carefully the forming rules for each kind of token. Then supply the missing code for the method nextTokenInternal(). (Don't forget to fill in the lineNum field of the token, or your code won't pass the test.) 

To this point, you've finished the lexer, and your Tiger compiler should compile ALL the test programs offered in the /test directory. (And you may want to write more test cases to test your Tiger compiler.) 

As a driver, there are code in Tiger.java which simply opens an input stream from the input file, and repeatedly recognizes tokens from the stream until reaching the special end of file token TOKEN_EOF. 

Before you continue, you should run your compiler on these test cases and make sure your lexer works properly, (for instance, to test your compiler on the input file /test/Fac.java): 

  $ java -cp bin Tiger ./test/Fac -testlexer

If your compiler fails on any test cases, check your code, fix the bugs and re-test your compiler. Exercise 8. Your lexer must be fast enough to lex (reasonably) large Java programs. To test how fast your lexer is, you should use some relatively large Java test cases. Nevertheless to say, it's boring and error-prone to write such test cases by hand. So we can apply some automatic approach to generate large test cases. Download this monster generator and compile it: 

  $ javac MonsterGen.java

then run it to produce a Monster.java code: 
  $ java MonsterGen 100000>Monster.java

And run your lexer on Monster.java to see the running time. What's the largest monster your lexer can deal with? Can Sun's javac compiler deal with it? Challenge! For now, we use an integer type to represent the lineNum field of the token. Though simple and easy to implement, this position information is coarse in that there maybe many tokens on the same line of the source code. A better design should encode more information, instead of just a line number. For instance, you may use a pair of the token's line number and column number. Design a much fancier data structure to represent the position information and implement it into the compiler. 
Challenge! Modify your lexer to lex full Java, instead of MiniJava. After you finish this, you can use your lexer to lex some nontrivial Java projects, such as the Apache's source code. 
Challenge! Use a lexer generator to build your Tiger lexer. For instance, you may try the JFlex. Which kind of lexer is faster, the hand-written one or the automatically generated one? 

Part C: The Parser
The task of a parser is to parse the input program, according to the given language syntax (production rules), and to answer whether or not the program beging parsed is legal (or meanwhile to generate some internal data structure as a result). In history, the parser may also generate target code directly, but recent compilers seldomly do this way, for the increasing complexivity of the languages and the increasing power of a modern computer. 
The parser we'll implement in this part of the lab will analyze the program being compiled and check whether or not the program is valid according to the MiniJava syntax. If so, the parser just succeeds and does nothing; if not, the compiler will output some error message indicating what the syntactic error is and where. (But in the next lab, we'll expand this parser by attaching semantic actions to let the parser generate an abstract syntax tree.) 

Just as the lexer, there are generally two approaches to implement a parser: hand-written or using an automatic parser generator. The parser we'll implement in this part for the Tiger compiler is a hand-written recursive descendent one. That is, all production rules become a bunch of recursive functions with (approximately) one function for one nonterminal. 

Exercise 9. Finish the recursive descedent parser in the file parser/Parser.java. We've offered most code for you, you should supply the missing code for those incomplete methods. 
To this point, your parser should be able to parse all legal MiniJava correctly meanwhile report errors for illegal programs. Remember to test your parser thoroughly using the test cases in directory /test and your own test case. Fix possible bugs. Exercise 10. The current implementation of the error handling and error recovery method error() only reports a error message then aborts. Modify this method to report more accurate error messages, such as the file name, the line number, the column number, some diagonostic message, even the related source code. You may take a look at the diagonostic messages generated by the Clang compiler to get some idea. Finally, you may try to write some code to recover from the error (that is, try to report all possible syntactic errors in the program, not just the first one). 
Exercise 11. Recompile the above Monster MiniJava program and study your Tiger compiler's time efficiency. What's the biggest size of Monster can your Tiger compile successfully? 
Challenge! Use some parser generators to re-implement the parser. For instance, you may use CUP, or AntLR, or SableCC, etc.. Again, this exercise may be not that difficult, but may require considerable modification to current code base and a lot of programming effort. 

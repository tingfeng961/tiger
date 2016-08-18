#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static struct timespec gc_start = {0, 0L};
static struct timespec gc_end = {0, 0L};
struct timespec gc_lasttime;


// The Gimple Garbage Collector.


//===============================================================//
// The Java Heap data structure.

/*   
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap
{
  int size;         // in bytes, note that this if for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize)
{
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"

  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.

  // #3: initialize the "from" field (with what value?)

  // #4: initialize the "fromFree" field (with what value?)

  // #5: initialize the "to" field (with what value?)

  // #6: initizlize the "toStart" field with NULL;

  // #7: initialize the "toNext" field with NULL;

  char* ptr = (char*)malloc(heapSize);
  if(0 == ptr)
  {
    printf("allocate failure\n");
    exit(0);    
  }
  //printf("heap size = %d %p\n", heapSize, ptr);
  heap.size = heapSize >> 1;
  heap.from = ptr;
  heap.fromFree = ptr;
  heap.to = heap.fromFree + heap.size;
  heap.toStart = heap.to;
  heap.toNext = heap.to;
  //printf("to address %p\n", heap.to);
  return;
}

// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
//void *prev = 0;
void *prev = 0;



//===============================================================//
// Object Model And allocation


// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      | vptr      ---|----> (points to the virtual method table)
      |--------------|
      | isObjOrArray | (0: for normal objects)
      |--------------|
      | length       | (this field should be empty for normal objects)
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| v_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new (void *vtable, int size)
{
  // Your code here:
    //check if space permit
    if((heap.fromFree + size + 16) >= heap.from + heap.size)
    {
        Tiger_gc();
        //try again
        if((heap.fromFree + size + 16) >= heap.from + heap.size)
        {
            printf("OutOfMemory\n");
            exit(1);
        }
    }
    //ok to allocate
    //printf("obj allocate    %d\n", size + 16);
    char* vptr = heap.fromFree;
    heap.fromFree = heap.fromFree + size + 16;
    *((int*)(vptr+4)) = 0;
    *((int*)(vptr+12)) = 0;
    *((int*)(vptr+16)) = (int)vtable;
    memset(vptr+20, 0, size);
    return vptr+16;
  
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      | vptr         | (this field should be empty for an array)
      |--------------|
      | isObjOrArray | (1: for array)
      |--------------|
      | length       |
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| e_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new_array (int length)
{
    //printf("array allocate    %d\n", length * sizeof(int) + 16);
    //check if space permit
    if((heap.fromFree + length * sizeof(int) + 16) >= heap.from + heap.size)
    {
        Tiger_gc();
        //try again
        if((heap.fromFree + length * sizeof(int) + 16) >= heap.from + heap.size)
        {
            printf("OutOfMemory\n");
            exit(1);
        }

    }

    char* vptr = heap.fromFree;
    heap.fromFree = heap.fromFree + length * sizeof(int) + 16;
    //char* vptr = (char*)malloc(length * sizeof(int) + 10);
    *((int*)(vptr+4)) = 1;
    *((int*)(vptr+8)) = length;
    *((int*)(vptr+12)) = 0;
    return vptr+16;
}


char * Forward(char* p)
{
    int* f;
    char* b_Next = heap.toNext;
    char* base;
    int i;
    int size;

    if(p == 0) return p;	                        //unInit
    if( p >= heap.from && p < heap.from + heap.size)    //if p points to form-space
    {
        f = *(int*)(p-4);
        if( f >= heap.to && f < heap.to + heap.size )   //p.f1 points to to-space
        {
            //printf("---------无需拷贝------------------------------------------\n");
            return f;
        }
        else
        {
            base = p - 16;
            if(*((int*)p-3) == 0)
            {//非数组  对象内部
                //printf("copy obj  ");
                //printf("base = %p \n", base);
                char *obj = (*(int*)(*(int*)(*((int*)p))));
                //类的gc_map  
                //printf("  class_map=%s  ", obj);
                size = 20;//需要拷贝空间大小
                while(*obj != '\0'){size += 4; ++obj;}
                for(i=0; i<size; ++i)
                    *(b_Next+i) = *(base+i);

            }
            else
            {//数组
                //printf("copy arr  ");
                //printf("base = %p \n", base);
                size = *((int*)p-2) * 4 + 16;
                //printf("size = %d \n", size);
                for(i=0; i<size; ++i)
                    *(b_Next+i) = *(base+i);
            }
            *((int*)(p-4)) = (b_Next+16);
            heap.toNext = heap.toNext + size;       //next <- next + sizeof record p
            return b_Next+16;
        }
    }
    else
        return p;
}




int round_gc = 0;
int size_gc;

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
//static 
void Tiger_gc ()
{
  // Your code here:
  clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &gc_start);

  heap.toStart = heap.to;
  int* p = (int*)prev;
  //copying
  while(p != 0)
  {
    //printf("prev = %p\n", p);
    char * args_map = *(p+1);			//args_gc_map
    int* pargs = (int*)*(int*)(p+2);
    //printf("args_map = %s\n", args_map);
    while(*args_map != '\0')
    {
      if(*args_map == '1')
      {//引用类型参数
        if(*pargs != 0)
            *pargs = Forward(*pargs);
      }
//      else
//      {//非引用类型参数 整形
//          printf("args int  %d\n", *pargs);
//      }
      ++pargs;
      ++args_map;
    }
    printf("\n");


    char * locals_map = *(p+3);			//locals_gc_map
    int* plocals = (int*)(p+4);
    //printf("locals_map = %s\n", locals_map);

    while(*locals_map != '\0')
    {
      if(*locals_map == '1')
      {
        //类型标志为
        if(*plocals != 0)
        {
             //printf("--------------------foward\n");
            *plocals = Forward(*plocals);
        }
        ++plocals;
      }
      ++locals_map;
    }
    printf("\n");
    p = *p;
  }

  //scan
  int flag;
  int size;
  while(heap.toStart < heap.toNext)
  {
    p = (int*)heap.toStart;
    flag = *(p+1);
    if(flag==1)//数组
    {
        //printf("............scan arr........................\n");
        size = *(p+2) * 4 + 16;
        heap.toStart = heap.toStart + size;
    }
    else if(flag==0)//普通对象
    {
        //printf("............scan obj........................\n");
        char *obj = (*(int*)(*(int*)(*(p+4))));
        //printf("\n%p\n", *(int*)(*(int*)(*(p+4))));
        //printf("oobbjj = %s\n", obj);
        size = 20;
        int* pf = p + 5;
        while(*obj != '\0')
        {
            if(*obj == '1')
                if(*pf != 0)
                *pf = Forward(*pf);
                //printf("--------------------foward %p\n", pf);
            //else 
            //    printf("xxx %d ", *pf);
            size += 4;
            ++obj;
            ++pf;
        }
        heap.toStart = heap.toStart + size;
    }
  }
  //printf("xxxxxxxxxGC_ENDxxxxxxxxxx%p == %p\n", heap.toStart, heap.toNext);

  size_gc =  (heap.fromFree - heap.from) - (heap.toNext - heap.to);
  // form <-> to
  char * temp = heap.from;
  heap.from = heap.to;
  heap.fromFree = heap.toNext;
  heap.to = temp;
  heap.toStart = heap.to;
  heap.toNext = heap.to;
  //print gc info
  clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &gc_end);
  gc_lasttime.tv_nsec = gc_end.tv_nsec - gc_start.tv_nsec;
  gc_lasttime.tv_sec = gc_end.tv_sec - gc_start.tv_sec;
  printf("%d round of GC: %lfms, collected %d bytes\n", ++round_gc, gc_lasttime.tv_nsec * 1.0 / 1000000, size_gc);

}


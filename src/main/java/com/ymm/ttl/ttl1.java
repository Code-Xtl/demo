package com.ymm.ttl;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 1.如果直接MAP<Thread, value>来实现线程本地变量，一:Thread越多，map越大；二：线程结束就销毁，但Map中还有它的引用，故无法释放内存，OOM
 * 因此ThreadLocal:本地线程变量：每个Thread有一个局部静态变量ThreadLocalMap，其key为ThreadLocal对象，value为值；多个ThreadLocal就多个entry
 * 不同线程拥有不同的ThreadLocalMap对象，虽然不同的线程之间threadlocal这个key值可以一样，但是不同的线程所拥有的ThreadLocalMap是独一无二的，
 * 也就是不同的线程间同一个ThreadLocal（key）对应存储的值(value)不一样，从而到达了线程间变量隔离的目的
 *
 *
 * 2. 内存泄漏
 *
 * THreadLocalMap的entry的key是对ThreadLocal对象的弱引用，value是对值的强引用
 *          之所以key为弱引用，是一个TL对象被创建出来，并且被一个线程放到自己的ThreadLocalMap里，也就是两个引用它
 *          假如TL对象失去原有的强引用，但是该线程还没有死亡，如果key不是弱引用，那么就意味着TL并不能被回收，会导致ThreadLocal对象本身也不会被回收，更容易内存泄漏
 *          现在key为弱引用，那么在TL失去强引用的时候，gc可以直接回收掉它，弱引用失效,这里的弱引用指向的值就为null
 * 正常情况下：当线程终止，保存在ThreadLocal的value会被回收，因为没有任何强引用了
 * 但是如果在栈中将TL对象的引用设置为null，此时只有弱引用指向TL对象；则下次gc时TL对象释放，那么key就为null，但是如果线程不终止（比如线程池）,这样就永远都有指向value的强引用，因此对应的value就不能被回收，但key已经为null，value成了一个永远也无法被访问
 *                                                         ThreadRef-》Thread-》ThreadLocalMap-》entry-》value（-》ThreadLocal）
 *                                                       ThreadLocalRef-》ThreadLocal
 *                                                      泄漏概率低：只要ThreadLocal没被回收，那么Entry中软引用指向就不会被回收
 *
 * jDK在set  remove rehash时会扫描key是否为null，如果是就同时把value也置位null，这样value就可以回收了
 * 然而如果这些方法没有使用，同时线程也不停止，那么就一直存在泄漏；
 * ThreadLocal会在以下过程中清理过期节点：
 *
 * 调用set()方法时，清理、全量清理，扩容时还会继续检查。https://bbs.csdn.net/topics/604237994
 * 调用get()方法，没有直接命中，向后环形查找时。
 * 调用remove()时，除了清理当前Entry，还会向后继续清理：开始往后逐个检查，k==null的清除掉，不等于null的要进行rehash
 *
 * 如何避免？使用完ThreadLocal后调用用remove方法：
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * set：
 *  0. 根据当前key计算出对应当前下标
 *  1.从当前hash下标开始往后遍历
 *     ①k=key 找到key相同的元素，直接更新value并return
 *     ②k=null 找到一个过期元素 执行replaceStaleEntry()方法，然后return
 *       1. replaceStaleEntry方法
 *          1.往前遍历(第一个for循环)，找到左侧第一个过期元素将slotToExpunge更新为其下标（1.①知前面不可能有等于key的k）
 *          2.往后遍历(第二个for循环)
 *              ①k=key 将value更新，将当前元素与当前过期元素位置替换，然后调用cleanSomeSlots(expungeStaleEntry(slotToExpunge), len)删除过期元素，return
 *              ②k=null && slotToExpunge == staleSlot 当前元素为过期元素，且步骤①没有找到其他过期元素时，将slotToExpunge更新为当前下标
 *          3.在步骤2中没有找到key相等的元素的话会走到这里，new Entry替换到当前位置过期的元素
 *          4.在步骤1中如果扫描到了其他过期元素，调用cleanSomeSlots(expungeStaleEntry(slotToExpunge), len)删除过期元素
 *
 *
 *
 * 2.步骤1遍历结束，如果没有找到当前元素也没有发现过期元素，直接创建一个新的Entry放进去，并++size
 *     ①进行启发式清理(cleanSomeSlots)，如果未清理任何数据，且当前散列数组中Entry的数量已经达到了列表的扩容阈值(len*2/3)，就开始执行rehash()方法
 *     ②进行探测式清理，如清理后size还是大于等于阈值的3/4，则执行resize() 扩容至原来的两倍
 *
 *
 *
 *
 *
 */
public class ttl1 {
    private static ThreadLocal context = new ThreadLocal<>();
    public static void main(String[] args) throws InterruptedException {
    }
}

package com.suning.storm.test.newfeature;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.storm.metric.Pair;


public class JVMTest {

	public static void main(String[] args) {
		
		MemoryUsage nonheapGen = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
		System.out.println("nonheapMax:" + nonheapGen.getMax() + "  nonheapUse:" + nonheapGen.getUsed());
		
		for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
			System.out.println("memoryPoolname:" + memoryPoolMXBean.getName());
            if (StringUtils.contains(memoryPoolMXBean.getName(), "PS Old Gen") || StringUtils.contains(memoryPoolMXBean.getName(), "Tenured Gen")) {
                // the size of memory in bytes
                long max = memoryPoolMXBean.getUsage().getMax();
                long used = memoryPoolMXBean.getUsage().getUsed();
                System.out.println("max:" + max + "  used:" + used);
            }
        }
		List<String> oldGenCollectorNames = new ArrayList<String>();
        oldGenCollectorNames.add("MarkSweepCompact");
        oldGenCollectorNames.add("PS MarkSweep");
        oldGenCollectorNames.add("ConcurrentMarkSweep");
        oldGenCollectorNames.add("Garbage collection optimized for short pausetimes Old Collector");
        oldGenCollectorNames.add("Garbage collection optimized for throughput Old Collector");
        oldGenCollectorNames.add("Garbage collection optimized for deterministic pausetimes Old Collector");
        //最新一次fullgc 总time
        long latestGCTime=0;
        //最新一次fullgc 总次数
        long latestGCCount=0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
        	System.out.println("--------------\n" + "gcName:" + gc.getName() + "  gcCount:" + gc.getCollectionCount() + " gcTime:" + gc.getCollectionTime());
            if (oldGenCollectorNames.contains(gc.getName())) {
                long currGCCount = gc.getCollectionCount();
                // milliseconds
                long currGCTime = gc.getCollectionTime();
                if (currGCCount > latestGCCount && currGCTime > latestGCTime) {
                    long time = currGCTime - latestGCTime;
                    long frequency = currGCCount - latestGCCount;
                    latestGCCount = currGCCount;
                    latestGCTime = currGCTime;
                }
            }
        }
	}

}
/**
JDK1.6内存区              								  |   JDK1.7内存区                                           	          |	  JDK1.8内存区
PS Eden Space堆区新生代对象产生区					  |Eden Space堆区新生代对象产生区                    	          |PS Eden Space（堆区新生代对象产生区）
PS Survivor Space堆区新生代对象存活区分为1区和2区		  |Survivor Space堆区新生代对象存活区分为1区和2区              |PS Survivor Space（堆区新生代对象存活区分为1区和2区）
PS Old Gen堆区old区用于存放多次YGC后仍然可用的对象		  |Tenured Gen堆区old区用于存放多次YGC后仍然可用的对象      |PS Old Gen（堆区old区用于存放多次YGC后仍然可用的对象）
Code Cache非堆区存放编译代码						  |Code Cache非堆区存放编译代码   					  |Code Cache非堆区存放编译代码
PS Perm Gen非堆永久区，常量池类加载的元数据等存在垃圾回收    |Perm Gen非堆永久区，常量池类加载的元数据等存在垃圾回收     |Metaspace（存放类的元数据信息，没有垃圾回收动态扩展）
												  |Perm Gen [shared-ro]          			      |Compressed Class Space（类指针压缩空间）
												  |Perm Gen [shared-rw]	
*/
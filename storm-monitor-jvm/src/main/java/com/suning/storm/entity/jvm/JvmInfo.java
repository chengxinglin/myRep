package com.suning.storm.entity.jvm;

/**
 * 
 * 〈一句话功能简述〉收集jvm信息集合<br>
 * 〈功能详细描述〉
 *
 * @author dxwang
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class JvmInfo {
    // 主机名
    private String hostname;
    // ip
    private String ip;
    // worker端口
    private int workerPort;
    // topology名字
    private String topologyName;
    // cpu使用率
    private double cpuUsage;
    // Full GC
    private FullGC fullGC;
    // old区
    private OldGen oldGen;
    // Java 虚拟机使用的非堆内存的当前内存使用量
    private String nonHeapMemoryUsage;
    // 是否存在死锁
    private boolean isExistDeadlockedThreads = false;
    // 当前时间
    private long date;
    // 堆内存容量
    private long heapMax;
    // 堆已使用内存
    private long heapUsed;
    // 非堆容量
    private long nonheapMax;
    // 非堆使用内存
    private long nonheapUsed;
    // old区最大容量
    private long oldMax;
    // old区已使用内存
    private long oldUsed;

    public long getHeapMax() {
		return heapMax;
	}

	public void setHeapMax(long heapMax) {
		this.heapMax = heapMax;
	}

	public long getHeapUsed() {
		return heapUsed;
	}

	public void setHeapUsed(long heapUsed) {
		this.heapUsed = heapUsed;
	}

	public long getNonheapMax() {
		return nonheapMax;
	}

	public void setNonheapMax(long nonheapMax) {
		this.nonheapMax = nonheapMax;
	}

	public long getNonheapUsed() {
		return nonheapUsed;
	}

	public void setNonheapUsed(long nonheapUsed) {
		this.nonheapUsed = nonheapUsed;
	}

	public long getOldMax() {
		return oldMax;
	}

	public void setOldMax(long oldMax) {
		this.oldMax = oldMax;
	}

	public long getOldUsed() {
		return oldUsed;
	}

	public void setOldUsed(long oldUsed) {
		this.oldUsed = oldUsed;
	}

	public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getWorkerPort() {
        return workerPort;
    }

    public void setWorkerPort(int workerPort) {
        this.workerPort = workerPort;
    }

    public String getTopologyName() {
        return topologyName;
    }

    public void setTopologyName(String topologyName) {
        this.topologyName = topologyName;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public FullGC getFullGC() {
        return fullGC;
    }

    public void setFullGC(FullGC fullGC) {
        this.fullGC = fullGC;
    }

    public OldGen getOldGen() {
        return oldGen;
    }

    public void setOldGen(OldGen oldGen) {
        this.oldGen = oldGen;
    }

    public String getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    public void setNonHeapMemoryUsage(String nonHeapMemoryUsage) {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }

    public boolean isExistDeadlockedThreads() {
        return isExistDeadlockedThreads;
    }

    public void setExistDeadlockedThreads(boolean isExistDeadlockedThreads) {
        this.isExistDeadlockedThreads = isExistDeadlockedThreads;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "JvmInfo [hostname=" + hostname + ", ip=" + ip + ", workerPort=" + workerPort + ", topologyName="
                + topologyName + ", cpuUsage=" + cpuUsage + ", fullGC=" + fullGC + ", oldGen=" + oldGen
                + ", nonHeapMemoryUsage=" + nonHeapMemoryUsage + ", isExistDeadlockedThreads="
                + isExistDeadlockedThreads + ", date=" + date + "]";
    }

}

/*
 * Copyright (C), 2002-2015, 苏宁易购电子商务有限公司
 * FileName: OldGen.java
 * Author:   zhang
 * Date:     2015年4月30日 上午10:40:17
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.storm.entity.jvm;

/**
 * 〈一句话功能简述〉<br>
 * 〈功能详细描述〉
 *
 * @author zhang
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class OldGen {

    // old区大小(return the size of memory in bytes)
    private long size;
    // old区使用率
    private double usage;

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the usage
     */
    public double getUsage() {
        return usage;
    }

    /**
     * @param usage the usage to set
     */
    public void setUsage(double usage) {
        this.usage = usage;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OldGen [size=" + size + ", usage=" + usage + "]";
    }

}

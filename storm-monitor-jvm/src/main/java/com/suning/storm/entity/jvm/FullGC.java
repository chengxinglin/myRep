/*
 * Copyright (C), 2002-2015, 苏宁易购电子商务有限公司
 * FileName: FullGC.java
 * Author:   zhang
 * Date:     2015年4月30日 上午10:35:03
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
public class FullGC {

    // 耗时  s
    private long time;
    // 频率 /s
    private double frequency;

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FullGC [time=" + time + ", frequency=" + frequency + "]";
    }

}

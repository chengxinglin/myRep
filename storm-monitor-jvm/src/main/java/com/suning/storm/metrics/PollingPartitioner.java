package com.suning.storm.metrics;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

/**
 * 
 * 〈一句话功能简述〉发送kafka轮询策略<br>
 * 〈功能详细描述〉
 *
 * @author dxwang
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class PollingPartitioner implements Partitioner {

    // PollingPartitioner index
    private static int index = 0;

    public PollingPartitioner(VerifiableProperties props) {
    }

    public int partition(Object obj, int numPartitions) {
        ++index;
        if (index >= numPartitions) {
            return index = 0;
        }
        return index;
    }

}

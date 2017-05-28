package com.suning.storm.metrics;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * 
 * 〈一句话功能简述〉jvm信息往kafka发送 <br>
 * 〈功能详细描述〉
 *
 * @author dxwang
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class JvmProducer {

    private String topic;

    public JvmProducer(String topic) {
        this.topic = topic;
    }

    // 生产者
    private Producer<String, String> producer;

    /**
     * 
     * 功能描述: 连接kafka,使用轮询策略<br>
     * 〈功能详细描述〉
     *
     * @param metadataBrokerList
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public void connKafka(String metadataBrokerList) {
        Properties props = new Properties();
        props.put("metadata.broker.list", metadataBrokerList);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("key.serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "com.suning.storm.metrics.PollingPartitioner");
        props.put("message.send.max.retries", "18");
        props.put("request.required.acks", "1");
        ProducerConfig config = new ProducerConfig(props);
        // 创建producer
        producer = new Producer<String, String>(config);
    }

    /**
     * 
     * 功能描述: 往kafka发送消息 <br>
     * 〈功能详细描述〉
     *
     * @param jvmInfo
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public void sendMsg(String jvmInfo) {
        producer.send(new KeyedMessage<String, String>(topic, jvmInfo));
    }

}

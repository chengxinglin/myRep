package com.suning.storm.metrics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.storm.metric.api.IMetricsConsumer;
import org.apache.storm.task.IErrorReporter;
import org.apache.storm.task.TopologyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.suning.storm.entity.jvm.FullGC;
import com.suning.storm.entity.jvm.JvmInfo;
import com.suning.storm.entity.jvm.OldGen;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 * 
 * 〈一句话功能简述〉利用metrics收集jvm信息 <br>
 * 〈功能详细描述〉
 *
 * @author dxwang
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class JvmMetricsConsumer implements IMetricsConsumer {

    private static Logger log = LoggerFactory.getLogger(JvmMetricsConsumer.class);

    // Topology name
    private String topologyName;
    // 向kafka发送数据的生产者
    private JvmProducer producer;

    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, Object registrationArgument, TopologyContext context,
            IErrorReporter errorReporter) {
        // 获取Topology name
        topologyName = (String) stormConf.get("topology.name");
        log.info("topologyName:[{}].", topologyName);
        // 连接kafka
        String metadataBrokerList = (String) stormConf.get("metadata.broker.list");
        log.info("metadataBrokerList:[{}].", metadataBrokerList);
        String topic = (String) stormConf.get("metrics.kafka.topic");
        log.info("topic:[{}].", topic);
        producer = new JvmProducer(topic);
        producer.connKafka(metadataBrokerList);
    }

    public void handleDataPoints(TaskInfo taskInfo, Collection<DataPoint> dataPoints) {
        for (DataPoint dp : dataPoints) {
            if (StringUtils.equals(dp.name, "self/metrics")) {
                JvmInfo info = new JvmInfo();
                info.setTopologyName(topologyName);
                info.setHostname(taskInfo.srcWorkerHost);
                info.setWorkerPort(taskInfo.srcWorkerPort);
                info.setDate(System.currentTimeMillis());
                info.setIp(getIP(taskInfo.srcWorkerHost));
                // cpuUsage + "," + isExistDeadlockedThreads + "," + pair.fst.getTime() + ","
                // + pair.fst.getFrequency() + "," + pair.snd.getSize() + "" + pair.snd.getUsage()
                String[] v = StringUtils.split(dp.value.toString(), ",");
                if (v.length == 12) {
                    // cpu usage
                    info.setCpuUsage(Double.valueOf(v[0]));
                    info.setExistDeadlockedThreads(v[1].equals("true") ? true : false);
                    // full gc info
                    FullGC fullGC = new FullGC();
                    fullGC.setTime(Long.valueOf(v[2]));
                    fullGC.setFrequency(Double.valueOf(v[3]));
                    info.setFullGC(fullGC);
                    // old gen info
                    OldGen oldGen = new OldGen();
                    oldGen.setSize(Long.valueOf(v[4]));
                    oldGen.setUsage(Double.valueOf(v[5]));
                    info.setOldGen(oldGen);
                    
                    info.setHeapMax(Long.valueOf(v[6]));
                    info.setHeapUsed(Long.valueOf(v[7]));
                    info.setNonheapMax(Long.valueOf(v[8]));
                    info.setNonheapUsed(Long.valueOf(v[9]));
                    info.setOldMax(Long.valueOf(v[10]));
                    info.setOldUsed(Long.valueOf(v[11]));
                }
                String toJson = toJson4JvmInfo(info);
                log.info("send kafka msg:[{}].", toJson);
                // send kafka
                try {
                    producer.sendMsg(toJson);
                } catch (Exception e) {
                    log.warn("Exception:[{}].", e.getMessage());
                }
            }
        }
    }

    /**
     * 
     * 功能描述: JvmInfo转换成json格式的信息 <br>
     * 〈功能详细描述〉
     *
     * @param info
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private String toJson4JvmInfo(JvmInfo info) {
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.alias("jvmInfo", JvmInfo.class);
        String toJson = xstream.toXML(info);
        return toJson;
    }

    /**
     * 
     * 功能描述: 获取ip地址 <br>
     * 〈功能详细描述〉
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private String getIP(String host) {
        String ip = "127.0.0.1";
        try {
            InetAddress addr = InetAddress.getByName(host);
            ip = addr.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public void cleanup() {

    }

}

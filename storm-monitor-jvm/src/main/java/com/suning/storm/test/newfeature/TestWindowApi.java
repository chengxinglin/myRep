package com.suning.storm.test.newfeature;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.IWindowedBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TimestampExtractor;
import org.apache.storm.windowing.TupleWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suning.storm.metrics.JvmMetricsConsumer;

/**
 * Copyright 2017 Suning Inc.
 * Created by ChengXinglin on 2017年4月11日
 */
public class TestWindowApi {

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(TestWindowApi.class);
		
		TopologyBuilder topologyBuilder = new TopologyBuilder();
		topologyBuilder.setSpout("windowSpout", new WindowSpout(), 6);
		topologyBuilder.setBolt("countBolt", new CountBolt(), 5).localOrShuffleGrouping("windowSpout");
		//通过IWindowBolt来实现滑动时间窗口
		topologyBuilder.setBolt("ShowWindowBolt", new ShowWindowBolt(), 3).localOrShuffleGrouping("countBolt");
		//通过BaseWindowBolt来实现滑动时间窗口（窗口时间，间隔时间）
		//topologyBuilder.setBolt("ShowWindowBolt", new ShowWindowBaswBolt().withWindow(Duration.seconds(3), Duration.seconds(1)), 1).localOrShuffleGrouping("countBolt");
		Config config = new Config();
		//config.put("topology.auto.task.hooks", Arrays.asList("com.suning.storm.test.newfeature.TopologyHook"));
		config.put("metadata.broker.list", "10.27.25.161:9092,10.27.25.163:9093");
		config.put("metrics.kafka.topic", "testkafka");
		//config.registerMetricsConsumer(JvmMetricsConsumer.class, 1);
		config.setNumWorkers(2);
		config.setMessageTimeoutSecs(5);
		config.setDebug(true);
		//config.put("storm.cluster.state.store", "org.apache.storm.pacemaker.pacemaker_state_factory");
		//config.put(Config.TOPOLOGY_CLASSPATH, "/data/storm/topologyCp/testDo.jar");
		try {
			//StormSubmitter.submitTopology("testWindowApi", config, topologyBuilder.createTopology());
			new LocalCluster().submitTopology("testWindowApi", config, topologyBuilder.createTopology());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("//////////////////提交topology任务异常////////////////////////" + e);
		}
	}

}
/**
 * Copyright 2017 Suning Inc.
 * Created by ChengXinglin on 2017年4月11日
 * WindowSpout
 */
class WindowSpout implements IRichSpout{

	Logger log = LoggerFactory.getLogger(WindowSpout.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4135360734199625721L;
	private TopologyContext topologyContext;
	private SpoutOutputCollector spoutOutputCollector;
	private Map conf;
	//可以用static final初始化messageId不一直（建议不使用static减少多线程之间的锁竞争）
	private final ConcurrentMap<UUID, Values> pendingMessages = new ConcurrentHashMap<UUID, Values>();
	private String[] spoutStr = {"hadoop learn next","hadoop learn next","hadoop learn next"};
	//private TestDo testDo;
	
	/**
	 * 初始化
	 */
	public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
		this.topologyContext = topologyContext;
		//添加topology的监控hook
		//this.topologyContext.addTaskHook(new TopologyHook());
		this.conf = conf;
		this.spoutOutputCollector = spoutOutputCollector;
		//testDo = new TestDo();
	    //testDo.doIt();
	}
	/**
	 * 消息数据源
	 */
	public void nextTuple() {
		//log.info("-----------------------------WindowSpout.nextTuple被调用----------------------------");
		UUID messageId = UUID.randomUUID();
		Values value = new Values(spoutStr[new Random().nextInt(spoutStr.length)]);
		pendingMessages.put(messageId, value);
		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		spoutOutputCollector.emit(value, messageId);
	}
	/**
	 * acker消息机制
	 */
	public void ack(Object messageId) {
		//log.info("-----------------------------WindowSpout.ack被调用"+ messageId.getClass() +"----------------------------");
		pendingMessages.remove(messageId);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>未处理完成的消息数：" + pendingMessages.size());
	}
	/**
	 * acker失败处理
	 */
	public void fail(Object messageId) {
		//log.info("-----------------------------WindowSpout.fail被调用"+ messageId.getClass() +"----------------------------");
		spoutOutputCollector.emit(pendingMessages.get(messageId), messageId);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>WindowSpout.fail被调用：" + pendingMessages.size());
	}
	/**
	 * 定义输出到下游字段名称（自定义数据类型须要实现序列化接口）
	 */
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declare(new Fields("line"));
	}
	
	public void activate() {
		
	}

	public void close() {
		
	}

	public void deactivate() {
		
	}

	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}
/**
 * Copyright 2017 Suning Inc.
 * Created by ChengXinglin on 2017年4月11日
 * CountBolt
 */
class CountBolt implements IRichBolt{
	
	Logger log = LoggerFactory.getLogger(CountBolt.class);
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -1100624254715359821L;
	private TopologyContext topologyContext;
	private OutputCollector outputCollector;
	private Map stormConf;
	
	/**
	 * 初始化bolt
	 */
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.stormConf = stormConf;
		this.topologyContext = context;
		this.outputCollector = collector;
	}
	/**
	 * 处理上游发射过来的数据并统计汇总
	 */
	public void execute(Tuple tuple) {
		//log.info("===========================ConutBolt.excute被调用=============================");
		//log.info("tuple信息：getSourceComponent:" + tuple.getSourceComponent() + "  getSourceStreamId:" + tuple.getSourceStreamId() + "  values" + tuple.getValues());
		String line = tuple.getStringByField("line");
		//split
		StringTokenizer lineTokenizer = new StringTokenizer(line, " ");
		List<String> words = new ArrayList<>();
		while(lineTokenizer.hasMoreTokens()){
			String word =  lineTokenizer.nextToken();
			words.add(word);
		}
		outputCollector.emit(tuple, new Values(words));
		outputCollector.ack(tuple);
	}

	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declare(new Fields("word"));
	}
	
	public void cleanup() {
		
	}
	
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
	
}

/**
 * Copyright 2017 Suning Inc.
 * Created by ChengXinglin on 2017年4月11日
 * SlodingWindowBolt---------IWindowedBolt
 */
class ShowWindowBolt implements IWindowedBolt{
	
	Logger log = LoggerFactory.getLogger(ShowWindowBolt.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 2230251068936714949L;
	private TopologyContext topologyContext;
	private OutputCollector outputCollector;
	private Map stormConf;
	private long lastTime = System.currentTimeMillis();
	/**
	 * 初始化bolt
	 */
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.stormConf = stormConf;
		this.topologyContext = context;
		this.outputCollector = collector;
	}
	/**
	 * 处理上游发射过来的window数据
	 */
	public void execute(TupleWindow inputWindow) {
		//log.info("===========================ShowWindowBolt.excute被调用=============================");
		//间隔滑动时间内，展示统计window长度内的单词统计数量
		List<Tuple> tuples = inputWindow.get();
		int count = 0;
		for(Tuple tuple : tuples){
			log.info("-----------------------------" + tuple.getValueByField("word"));
			List<String> words = (List<String>)tuple.getValueByField("word");
			count += words.size();
			//windowBolt无需手动ack，在获取到消息时系统已经对每条消息ack
			//outputCollector.ack(tuple);
		}
		long nowTime = System.currentTimeMillis();
		log.info("****************间隔时间" + (nowTime-lastTime)/1000 + "s统计结果：" + count + "STORM_EXT_CLASSPATH:" + System.getenv("STORM_EXT_CLASSPATH"));
		lastTime = nowTime;
		//-------------------JVM测试
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

	public TimestampExtractor getTimestampExtractor() {
		return null;
	}
	
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
	}

	public void cleanup() {
		
	}
	
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> windowConfig = new HashMap<>();
		windowConfig.put(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS, TimeUnit.SECONDS.toMillis(3));
		windowConfig.put(Config.TOPOLOGY_BOLTS_SLIDING_INTERVAL_DURATION_MS, TimeUnit.SECONDS.toMillis(1));
		return windowConfig;
	}
}

/**
 * Copyright 2017 Suning Inc.
 * Created by ChengXinglin on 2017年4月11日
 * ShowWindowBaswBolt-------BaseWindowedBolt
 */
class ShowWindowBaswBolt extends BaseWindowedBolt{
	
	Logger log = LoggerFactory.getLogger(ShowWindowBaswBolt.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -7958820964305852063L;
	private TopologyContext topologyContext;
	private OutputCollector outputCollector;
	private Map stormConf;
	private long lastTime = System.currentTimeMillis();
	
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.stormConf = stormConf;
		this.topologyContext = context;
		this.outputCollector = collector;
    }

	@Override
	public void execute(TupleWindow inputWindow) {

		log.info("===========================ShowWindowBolt.excute被调用=============================");
		//间隔滑动时间内，展示统计window长度内的单词统计数量
		List<Tuple> tuples = inputWindow.get();
		int count = 0;
		for(Tuple tuple : tuples){
			log.info("-----------------------------" + tuple.getValueByField("word"));
			List<String> words = (List<String>)tuple.getValueByField("word");
			count += words.size();
			//windowBolt无需手动ack，在获取到消息时系统已经对每条消息ack
			//outputCollector.ack(tuple);
		}
		long nowTime = System.currentTimeMillis();
		log.info("****************间隔时间" + (nowTime-lastTime)/1000 + "s统计结果：" + count);
		lastTime = nowTime;
	}
}
/**
 * Copyright 2017 Suning Inc.
 * Created by ChengXinglin on 2017年4月24日
 * RunTest
 */
class RunTest implements Runnable{
	
	Logger log = LoggerFactory.getLogger(RunTest.class);
	
	private Process process;
	private ProcessBuilder processBuilder;
	private List<String> commonds;
	private InputStream in;
	private InputStream error;
	private int exit;

	public RunTest() {
		//EventHandler<T>
		commonds = new ArrayList<>();
		String javaCmd = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		String cp = System.getProperty("java.class.path");
		commonds.add(javaCmd);
		commonds.add("-server");
		commonds.add("-cp");
		commonds.add(cp);
		commonds.add("-Dstorm.test=stromTest");
		commonds.add("com.suning.TimeWindow.Test");
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName("runTest-process");
		try {
			processBuilder = new ProcessBuilder(commonds);
			process = processBuilder.start();
			in = process.getErrorStream();
			error = process.getErrorStream();
			new Thread (new Runnable() {
				BufferedInputStream bufferIn = new BufferedInputStream(in);
				@Override
				public void run() {
					Thread.currentThread().setName("runTest-in");
					try {
						byte[] bu = new byte[4096];
						int i = bufferIn.read(bu);
						while (i != -1) {
							log.info(new String(bu, 0, i , "UTF-8"));
							i = bufferIn.read(bu);
						}
					}catch (Exception e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			}).start();
			
			new Thread (new Runnable() {
				BufferedInputStream bufferError = new BufferedInputStream(error);
				@Override
				public void run() {
					Thread.currentThread().setName("runTest-error");
					try {
						byte[] bu = new byte[4096];
						int i = bufferError.read(bu);
						while (i != -1) {
							log.info(new String(bu, 0, i, "UTF-8"));
							i = bufferError.read(bu);
						}
					} catch (Exception e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			}).start();
			exit = process.waitFor();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		System.exit(exit);
	}
	
}

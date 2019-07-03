package org.sinoc.shell.service;

import org.sinoc.shell.config.RpcEnabledCondition;
import org.sinoc.shell.model.dto.MethodCallDTO;
import org.sinoc.shell.jsonrpc.JsonRpc;
import org.sinoc.shell.util.AppConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Services for:
 *  - gathering statistics info of how many times RPC methods were called;
 *  - pushing updates to client side;
 *  - reading curl examples from conf file.
 */
@Service
@Conditional(RpcEnabledCondition.class)
public class JsonRpcUsageService implements ApplicationListener {
	
	Logger log = LoggerFactory.getLogger("jsonrpc");
	
    @Autowired
    JsonRpc jsonRpc;

    @Autowired
    ClientMessageService clientMessageService;

    private final Map<String, CallStats> stats = new ConcurrentHashMap();

    private void init(int port) {
        final String serverUrl = "http://localhost:" + port + AppConst.JSON_RPC_PATH;

        /**
         * Load conf file with curl examples per each JSON-RPC method.
         */
        Config config = ConfigFactory.load("json-rpc-help");
        if (config.hasPath("doc.curlExamples")) {

            Map<String, String> curlExamples = config.getAnyRefList("doc.curlExamples").stream()
                    .map(e -> (HashMap<String, String>) e)
                    .collect(Collectors.toMap(
                            e -> e.get("method"),
                            e -> e.get("curl").replace("${host}", serverUrl)));


            /**
             * Initialize empty stats for all methods.
             */
            Arrays.stream(jsonRpc.ethj_listAvailableMethods())
                    .forEach(line -> {
                        final String methodName = line.split(" ")[0];
                        String curlExample = curlExamples.get(methodName);
                        if (curlExample == null) {
                            curlExample = generateCurlExample(line) + " " + serverUrl;
//                            log.debug("Generate curl example for JSON-RPC method: " + methodName);
                        }
                        stats.put(methodName, new CallStats(methodName, 0l, null, curlExample));
                    });
        }
    }

    private String generateCurlExample(String line) {
        final String[] arr = line.split(" ");
        final String method = arr[0];
        final String paramsString = Stream.of(arr)
                .skip(1)
                .map(i -> "\"0x0\"")
                .collect(Collectors.joining(", "));

        return "curl -X POST --data '{\"jsonrpc\":\"2.0\",\"method\":\"" + method +
                "\",\"params\":[" + paramsString +
                "],\"id\":64}'";
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof EmbeddedServletContainerInitializedEvent) {
            int port = ((EmbeddedServletContainerInitializedEvent) event).getEmbeddedServletContainer().getPort();

            init(port);
        }
    }

    /**
     * Send stats to client side.
     */
    @Scheduled(fixedRate = 2000)
    private void doSendRpcUsage() {
        List<MethodCallDTO> items = stats.values()
                .stream()
                .map(stat -> new MethodCallDTO(
                        stat.name,
                        stat.count.longValue(),
                        stat.lastCall.longValue(),
                        stat.lastResult.get(),
                        stat.curl))
                .sorted((s1, s2) -> s1.getMethodName().compareTo(s2.getMethodName()))
                .collect(Collectors.toList());

        clientMessageService.sendToTopic("/topic/rpcUsage", items);
    }

    /**
     * Account method invocation into statistics.
     */
    public void methodInvoked(String methodName, String resultReturned) {
        final long timeNow = System.currentTimeMillis();

//        final CallStats callStats = stats.computeIfAbsent(methodName, k -> new CallStats(methodName, timeNow, resultReturned));
        CallStats callStats = stats.get(methodName);

        if (callStats != null) {
            callStats.count.increment();
            callStats.lastCall.set(timeNow);
            callStats.lastResult.set(resultReturned);
        }
        // do not track stats for non existing methods
    }

    static class CallStats {

        public String name;

        public LongAdder count = new LongAdder();

        // time called last time in ms
        public AtomicLong lastCall = new AtomicLong();

        public AtomicReference<String> lastResult = new AtomicReference<>();

        public String curl;

        public CallStats(String name, long lastCallTime, String lastResultString, String curl) {
            this.name = name;
            lastCall.set(lastCallTime);
            lastResult.set(lastResultString);
            this.curl = curl;
        }
    }

}

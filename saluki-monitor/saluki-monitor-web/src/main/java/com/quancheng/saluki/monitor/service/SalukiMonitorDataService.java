package com.quancheng.saluki.monitor.service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.quancheng.saluki.core.utils.NamedThreadFactory;
import com.quancheng.saluki.monitor.SalukiHost;
import com.quancheng.saluki.monitor.SalukiInvoke;
import com.quancheng.saluki.monitor.model.SalukiInvokeStatistics;
import com.quancheng.saluki.monitor.repository.ConsulRegistryRepository;
import com.quancheng.saluki.monitor.repository.SalukiInvokeMapper;

@Service
public class SalukiMonitorDataService {

    private static final Logger            log                       = LoggerFactory.getLogger(ConsulRegistryService.class);

    private final int                      cpu                       = Runtime.getRuntime().availableProcessors();

    private final ScheduledExecutorService scheduledSyncDataExecutor = Executors.newScheduledThreadPool(1,
                                                                                                        new NamedThreadFactory("SalukisyncData",
                                                                                                                               true));

    private final ExecutorService          syncDataExecutor          = Executors.newScheduledThreadPool(cpu,
                                                                                                        new NamedThreadFactory("SalukisyncData",
                                                                                                                               true));
    @Autowired
    private ConsulRegistryRepository       registryRepository;

    @Autowired
    private SalukiInvokeMapper             invokeMapper;

    private HttpClient                     httpClient;

    private Gson                           gson;

    @PostConstruct
    public void init() {
        httpClient = HttpClientBuilder.create().build();
        gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer()).create();
        scheduledSyncDataExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                syncAndClearData();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void syncAndClearData() {
        log.info("scheule to sys data froom consumer and provider begin");
        Map<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> servicesPassing = registryRepository.getAllPassingService();
        if (servicesPassing.isEmpty()) {
            registryRepository.loadAllServiceFromConsul();
            servicesPassing = registryRepository.getAllPassingService();
        }
        Set<String> minitorHosts = Sets.newHashSet();
        for (Map.Entry<String, Pair<Set<SalukiHost>, Set<SalukiHost>>> entry : servicesPassing.entrySet()) {
            Pair<Set<SalukiHost>, Set<SalukiHost>> providerAndConsumer = entry.getValue();
            Set<SalukiHost> providers = providerAndConsumer.getLeft();
            Set<SalukiHost> consumers = providerAndConsumer.getRight();
            for (SalukiHost provider : providers) {
                minitorHosts.add(provider.getHost() + ":" + provider.getHttpPort());
            }
            for (SalukiHost consumer : consumers) {
                minitorHosts.add(consumer.getHost() + ":" + consumer.getHttpPort());
            }
        }
        for (String host : minitorHosts) {
            syncDataExecutor.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        String syncDataUrl = "http://" + host + "/salukiMonitor/statistics";
                        HttpGet request = new HttpGet(syncDataUrl);
                        request.addHeader("content-type", "application/json");
                        request.addHeader("Accept", "application/json");
                        try {
                            HttpResponse httpResponse = httpClient.execute(request);
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                String minitorJson = EntityUtils.toString(httpResponse.getEntity());
                                List<SalukiInvoke> invokes = gson.fromJson(minitorJson,
                                                                           new TypeToken<List<SalukiInvoke>>() {
                                                                           }.getType());
                                if (invokes != null && invokes.size() != 0) {
                                    invokeMapper.addInvoke(invokes);
                                }
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    } finally {
                        String cleanDataUrl = "http://" + host + "/salukiMonitor/clean";
                        HttpGet request = new HttpGet(cleanDataUrl);
                        request.addHeader("content-type", "application/json");
                        request.addHeader("Accept", "application/json");
                        try {
                            httpClient.execute(request);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                }
            });
        }
        log.info("scheule to sys data froom consumer and provider end");
    }

    public Map<String, List<SalukiInvokeStatistics>> queryDataByMachines(String service, String type, String dataType,
                                                                         List<String> ips, Date from, Date to) {
        Map<String, List<SalukiInvokeStatistics>> datas = Maps.newHashMap();
        Map<String, String> paramter = Maps.newHashMap();
        java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        paramter.put("service", service);
        paramter.put("type", type);
        paramter.put("invokeDateFrom", formatter.format(from));
        paramter.put("invokeDateTo", formatter.format(to));
        if ("day".equals(dataType)) {
            paramter.put("interval", "day");
        } else if ("hour".equals(dataType)) {
            paramter.put("interval", "hour");
        }
        for (String ip : ips) {
            paramter.put("ip", ip);
            List<SalukiInvokeStatistics> invokes = analysisData(paramter);
            datas.put(ip, invokes);
        }
        return datas;
    }

    public List<SalukiInvokeStatistics> querySumDataByService(String service, String type, String dataType, Date from,
                                                              Date to) {
        Map<String, String> paramter = Maps.newHashMap();
        java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if ("day".equals(dataType)) {
            paramter.put("interval", "day");
        } else if ("hour".equals(dataType)) {
            paramter.put("interval", "hour");
        }
        paramter.put("invokeDateFrom", formatter.format(from));
        paramter.put("invokeDateTo", formatter.format(to));
        paramter.put("service", service);
        paramter.put("type", type);
        return analysisData(paramter);
    }

    private List<SalukiInvokeStatistics> analysisData(Map<String, String> paramter) {
        List<SalukiInvokeStatistics> statistics = invokeMapper.queryData(paramter);
        for (Iterator<SalukiInvokeStatistics> it = statistics.iterator(); it.hasNext();) {
            SalukiInvokeStatistics st = it.next();
            int sumConsurrent = st.getSumConcurrent();
            Double sumElapsed = st.getSumElapsed();
            int sumSuccess = st.getSumSuccess();
            int sumFailure = st.getSumFailure();
            Double sumInput = st.getSumInput();
            int totalCount = sumFailure + sumSuccess;
            BigDecimal averageElapsed = BigDecimal.valueOf(sumElapsed).divide(BigDecimal.valueOf(totalCount), 2,
                                                                              BigDecimal.ROUND_HALF_DOWN);
            BigDecimal averageInput = BigDecimal.valueOf(sumInput).divide(BigDecimal.valueOf(totalCount), 2,
                                                                          BigDecimal.ROUND_HALF_DOWN);
            // TPS=并发数/平均响应时间
            BigDecimal tps = new BigDecimal(sumConsurrent);
            if (!averageElapsed.equals(BigDecimal.ZERO)) {
                tps = tps.divide(averageElapsed, 2, BigDecimal.ROUND_HALF_DOWN);
                tps = tps.multiply(BigDecimal.valueOf(1000));
                st.setTps(tps.doubleValue());
            }
            // kbps=tps*平均每次传输的数据量
            BigDecimal kbps = new BigDecimal(st.getTps());
            if (!averageElapsed.equals(BigDecimal.ZERO) && !averageInput.equals(BigDecimal.ZERO)) {
                kbps = kbps.multiply(averageInput.divide(BigDecimal.valueOf(1024), 2, BigDecimal.ROUND_HALF_DOWN));
                st.setKbps(kbps.doubleValue());
            }
        }
        return statistics;
    }

    public class DateDeserializer implements JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement element, Type arg1,
                                JsonDeserializationContext arg2) throws JsonParseException {
            Long timestamp = element.getAsLong();
            Date date = new Date(timestamp);
            return date;
        }

    }
}

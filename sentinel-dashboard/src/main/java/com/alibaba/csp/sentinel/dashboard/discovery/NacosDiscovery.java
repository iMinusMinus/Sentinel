package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NaCos服务发现
 *
 * @author iMinusMinus
 */
public class NacosDiscovery implements MachineDiscovery {

    private static final Logger log = LoggerFactory.getLogger(NacosDiscovery.class);

    private final NamingService namingService;

    public NacosDiscovery(NamingService namingService) {
        this.namingService = namingService;
    }

    @Override
    public List<String> getAppNames() {
        List<ServiceInfo> serviceInfos = fetchAll();
        return serviceInfos.stream().map(ServiceInfo::getName).collect(Collectors.toList());
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        List<ServiceInfo> serviceInfos = fetchAll();
        return serviceInfos.stream().map(this::convert).collect(Collectors.toSet());
    }

    private List<ServiceInfo> fetchAll() {
        try {
            return namingService.getSubscribeServices();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public AppInfo getDetailApp(String app) {
        try {
            List<Instance> instances = namingService.getAllInstances(app);
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setName(app);
            serviceInfo.setHosts(instances);
            return convert(serviceInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private AppInfo convert(ServiceInfo serviceInfo) {
        AppInfo appInfo = new AppInfo(serviceInfo.getName());
        serviceInfo.getHosts().forEach(instance -> {
            MachineInfo machineInfo = MachineInfo.of(serviceInfo.getName(), instance.getIp(), instance.getPort());
//            machineInfo.setHostname(instance.getInstanceId());
//            machineInfo.setLastHeartbeat(); // FIXME
//            machineInfo.setVersion();
            appInfo.addMachine(machineInfo);
        });
        return appInfo;
    }

    @Override
    public void removeApp(String app) {
        // NO-OP
    }

    @Override
    public long addMachine(MachineInfo machineInfo) {
        // NO-OP
        return 0;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        // NO-OP
        return false;
    }

}

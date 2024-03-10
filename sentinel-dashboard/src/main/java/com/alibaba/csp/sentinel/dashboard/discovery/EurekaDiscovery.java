package com.alibaba.csp.sentinel.dashboard.discovery;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EurekaDiscovery implements MachineDiscovery {

    private final EurekaClient eurekaClient;

    public EurekaDiscovery(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }

    @Override
    public List<String> getAppNames() {
        return eurekaClient.getApplications().getRegisteredApplications().stream()
                .map(Application::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return eurekaClient.getApplications().getRegisteredApplications().stream()
                .map(this::convert)
                .collect(Collectors.toSet());
    }

    @Override
    public AppInfo getDetailApp(String app) {
        return convert(eurekaClient.getApplication(app));
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

    private AppInfo convert(Application application) {
        AppInfo appInfo = new AppInfo(application.getName());
        application.getInstances().forEach( instance -> {
            MachineInfo machineInfo = MachineInfo.of(instance.getAppName(), instance.getIPAddr(), instance.getPort());
            machineInfo.setHostname(instance.getHostName());
            machineInfo.setLastHeartbeat(instance.getLeaseInfo().getRenewalTimestamp());
//                        machineInfo.setVersion();
            appInfo.addMachine(machineInfo);
        });
        return appInfo;
    }

}

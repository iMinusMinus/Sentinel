/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.ConcurrentModificationException;
import java.util.Set;

import com.alibaba.csp.sentinel.dashboard.config.DashboardProperties;
import org.junit.Test;

import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.*;

public class AppInfoTest {

    @Test
    public void testConcurrentGetMachines() throws Exception {
        AppInfo appInfo = new AppInfo("testApp");
        appInfo.addMachine(genMachineInfo("hostName1", "10.18.129.91"));
        appInfo.addMachine(genMachineInfo("hostName2", "10.18.129.92"));
        Set<MachineInfo> machines = appInfo.getMachines();
        new Thread(() -> {
            try {
                for (MachineInfo m : machines) {
                    System.out.println(m);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
                fail();
            }

        }).start();
        Thread.sleep(100);
        try {
            appInfo.addMachine(genMachineInfo("hostName3", "10.18.129.93"));
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            fail();
        }
        Thread.sleep(1000);
    }

    private MachineInfo genMachineInfo(String hostName, String ip) {
        MachineInfo machine = MachineInfo.of("testApp", ip, 8719);
        machine.setHostname(hostName);
        machine.setVersion(String.valueOf(System.currentTimeMillis()));
        return machine;
    }

    @Test
    public void addRemoveMachineTest() {
        AppInfo appInfo = new AppInfo("default");
        assertEquals("default", appInfo.getApp());
        assertEquals(0, appInfo.getMachines().size());
        //add one
        {
            MachineInfo machineInfo = MachineInfo.of("default", "127.0.0.1", 3389);
            machineInfo.setHostname("bogon");
            machineInfo.setLastHeartbeat(System.currentTimeMillis());
            machineInfo.setHeartbeatVersion(1);
            machineInfo.setVersion("0.4.1");
            appInfo.addMachine(machineInfo);
        }
        assertEquals(1, appInfo.getMachines().size());
        //add duplicated one
        {
            MachineInfo machineInfo = MachineInfo.of("default", "127.0.0.1", 3389);
            machineInfo.setHostname("bogon");
            machineInfo.setLastHeartbeat(System.currentTimeMillis());
            machineInfo.setHeartbeatVersion(1);
            machineInfo.setVersion("0.4.2");
            appInfo.addMachine(machineInfo);
        }
        assertEquals(1, appInfo.getMachines().size());
        //add different one
        {
            MachineInfo machineInfo = MachineInfo.of("default", "127.0.0.1", 3390);
            machineInfo.setHostname("bogon");
            machineInfo.setLastHeartbeat(System.currentTimeMillis());
            machineInfo.setHeartbeatVersion(1);
            machineInfo.setVersion("0.4.3");
            appInfo.addMachine(machineInfo);
        }
        assertEquals(2, appInfo.getMachines().size());
        appInfo.removeMachine("127.0.0.1", 3389);
        assertEquals(1, appInfo.getMachines().size());
        appInfo.removeMachine("127.0.0.1", 3390);
        assertEquals(0, appInfo.getMachines().size());
    }

    @Test
    public void testHealthyAndDead() {
        DashboardConfig dashboardConfig = new DashboardConfig();
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        DashboardProperties dashboardProperties = new DashboardProperties();
        dashboardProperties.setRemoveAppNoMachineMillis(600000);
        dashboardProperties.setApp(new DashboardProperties.AppConfig());
        dashboardProperties.getApp().setHideAppNoMachineMillis(60000);
        Mockito.when(applicationContext.getBean(DashboardProperties.class)).thenReturn(dashboardProperties);
        dashboardConfig.setApplicationContext(applicationContext);

        String appName = "default";
        AppInfo appInfo = new AppInfo();
        appInfo.setApp(appName);
        {
            MachineInfo machineInfo = MachineInfo.of(appName, "127.0.0.1", 8801);
            machineInfo.setHeartbeatVersion(1);
            machineInfo.setLastHeartbeat(System.currentTimeMillis());
            appInfo.addMachine(machineInfo);
        }
        assertTrue(appInfo.isShown());
        assertFalse(appInfo.isDead());

        {
            MachineInfo machineInfo = MachineInfo.of(appName, "127.0.0.1", 8801);
            machineInfo.setHeartbeatVersion(1);
            machineInfo.setLastHeartbeat(System.currentTimeMillis() - 70000);
            appInfo.addMachine(machineInfo);
        }
        assertFalse(appInfo.isShown());
        assertFalse(appInfo.isDead());

        {
            MachineInfo machineInfo = MachineInfo.of(appName, "127.0.0.1", 8801);
            machineInfo.setHeartbeatVersion(1);
            machineInfo.setLastHeartbeat(System.currentTimeMillis() - 700000);
            appInfo.addMachine(machineInfo);
        }
        assertFalse(appInfo.isShown());
        assertTrue(appInfo.isDead());
    }

}


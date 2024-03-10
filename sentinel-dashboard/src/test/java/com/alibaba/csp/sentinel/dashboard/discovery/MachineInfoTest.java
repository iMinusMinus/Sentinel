/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

import static org.junit.Assert.*;

import com.alibaba.csp.sentinel.dashboard.config.DashboardProperties;
import org.junit.Test;

import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * @author Jason Joo
 */
public class MachineInfoTest {

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
        MachineInfo machineInfo = MachineInfo.of("", "", -1);
        machineInfo.setHeartbeatVersion(1);
        machineInfo.setLastHeartbeat(System.currentTimeMillis() - 10000);
        assertTrue(machineInfo.isHealthy());
        assertFalse(machineInfo.isDead());

        machineInfo.setLastHeartbeat(System.currentTimeMillis() - 100000);
        assertFalse(machineInfo.isHealthy());
        assertFalse(machineInfo.isDead());

        machineInfo.setLastHeartbeat(System.currentTimeMillis() - 1000000);
        assertFalse(machineInfo.isHealthy());
        assertTrue(machineInfo.isDead());
    }
}

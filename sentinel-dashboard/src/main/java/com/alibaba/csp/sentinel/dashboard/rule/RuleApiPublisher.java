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
package com.alibaba.csp.sentinel.dashboard.rule;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class RuleApiPublisher<E extends RuleEntity> implements DynamicRulePublisher<List<E>> {

    private final AppManagement appManagement;

    private final BiFunction<MachineInfo, List<E>, Boolean> function;

    public RuleApiPublisher(AppManagement appManagement, BiFunction<MachineInfo, List<E>, Boolean> function) {
        this.appManagement = appManagement;
        this.function = function;
    }

    @Override
    public void publish(String app, List<E> rules) throws Exception {
        if (StringUtil.isBlank(app)) {
            return;
        }
        if (rules == null) {
            return;
        }
        Set<MachineInfo> set = appManagement.getDetailApp(app).getMachines();

        for (MachineInfo machine : set) {
            if (!machine.isHealthy()) {
                continue;
            }
            function.apply(machine, rules);
        }
    }
}

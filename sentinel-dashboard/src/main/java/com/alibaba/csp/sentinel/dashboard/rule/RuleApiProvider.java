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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 */
public class RuleApiProvider<E extends RuleEntity> implements DynamicRuleProvider<List<E>> {

    private final AppManagement appManagement;

    private final Function<MachineInfo, List<E>> ruleFetcher;

    public RuleApiProvider(AppManagement appManagement, Function<MachineInfo, List<E>> ruleFetcher) {
        this.appManagement = appManagement;
        this.ruleFetcher = ruleFetcher;
    }

    @Override
    public List<E> getRules(String appName) throws Exception {
        if (StringUtil.isBlank(appName)) {
            return new ArrayList<>();
        }
        List<MachineInfo> list = appManagement.getDetailApp(appName).getMachines()
            .stream()
            .filter(MachineInfo::isHealthy)
            .sorted((e1, e2) -> Long.compare(e2.getLastHeartbeat(), e1.getLastHeartbeat())).collect(Collectors.toList());
        if (list.isEmpty()) {
            return new ArrayList<>();
        } else {
            return ruleFetcher.apply(list.get(0));
        }
    }
}

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
package com.alibaba.csp.sentinel.dashboard.controller.v2;

import java.util.Date;
import java.util.List;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/system")
public class SystemControllerV2 {

    private final Logger logger = LoggerFactory.getLogger(SystemControllerV2.class);

    @Autowired
    private RuleRepository<SystemRuleEntity, Long> repository;

    @Autowired
    @Qualifier("systemRuleDefaultProvider")
    private DynamicRuleProvider<List<SystemRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("systemRuleDefaultPublisher")
    private DynamicRulePublisher<List<SystemRuleEntity>> rulePublisher;

    @GetMapping("/rules.json")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<SystemRuleEntity>> apiQueryMachineRules(@RequestParam(value = "app", required = false) String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        try {
            List<SystemRuleEntity> rules = ruleProvider.getRules(app);
            rules = repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("Query machine system rules error", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    private int countNotNullAndNotNegative(Number... values) {
        int notNullCount = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && values[i].doubleValue() >= 0) {
                notNullCount++;
            }
        }
        return notNullCount;
    }

    @RequestMapping("/new.json")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<SystemRuleEntity> apiAdd(@RequestParam(value = "app", required = false) String app,
                                           @RequestParam(value = "ip", required = false) String ip,
                                           @RequestParam(value = "port", required = false) Integer port,
                                           @RequestParam(value = "highestSystemLoad", required = false) Double highestSystemLoad,
                                           @RequestParam(value = "highestCpuUsage", required = false) Double highestCpuUsage,
                                           @RequestParam(value = "avgRt", required = false) Long avgRt,
                                           @RequestParam(value = "maxThread", required = false) Long maxThread,
                                           @RequestParam(value = "qps", required = false) Double qps) {

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        int notNullCount = countNotNullAndNotNegative(highestSystemLoad, avgRt, maxThread, qps, highestCpuUsage);
        if (notNullCount != 1) {
            return Result.ofFail(-1, "only one of [highestSystemLoad, avgRt, maxThread, qps,highestCpuUsage] "
                    + "value must be set > 0, but " + notNullCount + " values get");
        }
        if (null != highestCpuUsage && highestCpuUsage > 1) {
            return Result.ofFail(-1, "highestCpuUsage must between [0.0, 1.0]");
        }
        SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(app.trim());
        entity.setIp(ip.trim());
        entity.setPort(port);
        // -1 is a fake value
        if (null != highestSystemLoad) {
            entity.setHighestSystemLoad(highestSystemLoad);
        } else {
            entity.setHighestSystemLoad(-1D);
        }

        if (null != highestCpuUsage) {
            entity.setHighestCpuUsage(highestCpuUsage);
        } else {
            entity.setHighestCpuUsage(-1D);
        }

        if (avgRt != null) {
            entity.setAvgRt(avgRt);
        } else {
            entity.setAvgRt(-1L);
        }
        if (maxThread != null) {
            entity.setMaxThread(maxThread);
        } else {
            entity.setMaxThread(-1L);
        }
        if (qps != null) {
            entity.setQps(qps);
        } else {
            entity.setQps(-1D);
        }
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            publishRules(app, ip, port);
        } catch (Throwable throwable) {
            logger.error("Add SystemRule error", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @GetMapping("/save.json")
    @AuthAction(PrivilegeType.WRITE_RULE)
    public Result<SystemRuleEntity> apiUpdateIfNotNull(@RequestParam(value = "id", required = false) Long id,
                                                       @RequestParam(value = "app", required = false) String app,
                                                       @RequestParam(value = "highestSystemLoad", required = false) Double highestSystemLoad,
                                                       @RequestParam(value = "highestCpuUsage", required = false) Double highestCpuUsage,
                                                       @RequestParam(value = "avgRt", required = false) Long avgRt,
                                                       @RequestParam(value = "maxThread", required = false) Long maxThread,
                                                       @RequestParam(value = "qps", required = false) Double qps) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        SystemRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "id " + id + " dose not exist");
        }

        if (StringUtil.isNotBlank(app)) {
            entity.setApp(app.trim());
        }
        if (highestSystemLoad != null) {
            if (highestSystemLoad < 0) {
                return Result.ofFail(-1, "highestSystemLoad must >= 0");
            }
            entity.setHighestSystemLoad(highestSystemLoad);
        }
        if (highestCpuUsage != null) {
            if (highestCpuUsage < 0) {
                return Result.ofFail(-1, "highestCpuUsage must >= 0");
            }
            if (highestCpuUsage > 1) {
                return Result.ofFail(-1, "highestCpuUsage must <= 1");
            }
            entity.setHighestCpuUsage(highestCpuUsage);
        }
        if (avgRt != null) {
            if (avgRt < 0) {
                return Result.ofFail(-1, "avgRt must >= 0");
            }
            entity.setAvgRt(avgRt);
        }
        if (maxThread != null) {
            if (maxThread < 0) {
                return Result.ofFail(-1, "maxThread must >= 0");
            }
            entity.setMaxThread(maxThread);
        }
        if (qps != null) {
            if (qps < 0) {
                return Result.ofFail(-1, "qps must >= 0");
            }
            entity.setQps(qps);
        }
        Date date = new Date();
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            publishRules(entity.getApp(), entity.getIp(), entity.getPort());
        } catch (Throwable throwable) {
            logger.error("save error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @RequestMapping("/delete.json")
    @AuthAction(PrivilegeType.DELETE_RULE)
    public Result<?> delete(@RequestParam(value = "id", required = false) Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }
        SystemRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }
        try {
            repository.delete(id);
            publishRules(oldEntity.getApp(), oldEntity.getIp(), oldEntity.getPort());
        } catch (Throwable throwable) {
            logger.error("delete error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(id);
    }

    private void publishRules(String app, String ip, Integer port) throws Exception {
        List<SystemRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        rulePublisher.publish(app, rules);
    }
}

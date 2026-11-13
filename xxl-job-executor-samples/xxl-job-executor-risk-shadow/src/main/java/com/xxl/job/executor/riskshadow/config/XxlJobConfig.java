package com.xxl.job.executor.riskshadow.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XxlJobConfig {
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor(
            @Value("${xxl.job.admin.addresses}") String adminAddresses,
            @Value("${xxl.job.admin.timeout:3}") int timeout,
            @Value("${xxl.job.executor.appname}") String appName,
            @Value("${xxl.job.executor.accessToken}") String accessToken,
            @Value("${xxl.job.executor.address}") String address,
            @Value("${xxl.job.executor.port:9999}") int port,
            @Value("${xxl.job.executor.logpath:/tmp/xxl-job}") String logPath,
            @Value("${xxl.job.executor.logretentiondays:7}") int logRetentionDays,
            @Value("${xxl.job.executor.glueenabled:false}") boolean glueEnabled) {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setTimeout(timeout);
        executor.setEnabled(true);
        executor.setAppname(appName);
        executor.setAccessToken(accessToken);
        executor.setIp("");
        executor.setPort(port);
        executor.setAddress(address);
        executor.setLogPath(logPath);
        executor.setLogRetentionDays(logRetentionDays);
        executor.setExcludedPackage("");
        executor.setGlueEnabled(glueEnabled);
        return executor;
    }
}

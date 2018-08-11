package com.github.iyboklee.config;

import java.util.Arrays;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ExpiringSession;

@Configuration
public class IgniteConfig {

    @Bean
    IgniteConfiguration igniteConfiguration() {
        IgniteConfiguration igniteCfg = new IgniteConfiguration();
        igniteCfg.setClientMode(false);

        // Logger
        igniteCfg.setGridLogger(new Slf4jLogger());
        igniteCfg.setMetricsLogFrequency(1000 * 10);

        // Cluster Discovery
        TcpDiscoverySpi spi  = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder  = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(Arrays.asList("127.0.0.1:47500..47509"));
        spi.setIpFinder(ipFinder);
        //spi.setSocketTimeout(100);
        igniteCfg.setDiscoverySpi(spi);

        // Cluster Communication
        TcpCommunicationSpi commSpi = new TcpCommunicationSpi();
        commSpi.setConnectTimeout(3000);
        commSpi.setTcpNoDelay(true);
        commSpi.setMessageQueueLimit(10000);
        igniteCfg.setCommunicationSpi(commSpi);

        // Memory Configuration
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(false);   // Only Memory
        storageCfg.getDefaultDataRegionConfiguration().setMaxSize(1024 * 1024 * 256);  // 256MB
        storageCfg.getDefaultDataRegionConfiguration().setMetricsEnabled(true);
        igniteCfg.setDataStorageConfiguration(storageCfg);

        // Cache Configuration
        CacheConfiguration<String, ExpiringSession> cacheCfg = new CacheConfiguration<>("session_cache");
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setStatisticsEnabled(true);
        cacheCfg.setOnheapCacheEnabled(true);
        cacheCfg.setEvictionPolicyFactory(new LruEvictionPolicyFactory<>(10_000));
        igniteCfg.setCacheConfiguration(cacheCfg);

        return igniteCfg;
    }

    @Bean(destroyMethod = "close")
    public Ignite ignite(IgniteConfiguration configuration) throws IgniteException {
        Ignite ignite = Ignition.start(configuration);
        ignite.cluster().active(true);
        return ignite;
    }

}
/*
 * Copyright (C) 2013 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dv.server;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.transaction.TransactionManager;

import io.syndesis.dv.metadata.MetadataInstance;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.metadata.internal.TeiidServer;
import io.syndesis.dv.openshift.EncryptionComponent;
import io.syndesis.dv.openshift.SyndesisConnectionSynchronizer;
import io.syndesis.dv.openshift.TeiidOpenShiftClient;
import io.syndesis.dv.repository.RepositoryManagerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.teiid.runtime.EmbeddedConfiguration;

import io.syndesis.dv.RepositoryManager;

@Configuration
@EnableConfigurationProperties({DvConfigurationProperties.class, SpringMavenProperties.class})
@ComponentScan(basePackageClasses = {RepositoryManagerImpl.class, DefaultMetadataInstance.class, SyndesisConnectionSynchronizer.class})
@EnableAsync
public class DvAutoConfiguration implements ApplicationListener<ContextRefreshedEvent>, AsyncConfigurer {

    @Value("${encrypt.key}")
    private String encryptKey;

    @Autowired(required=false)
    private TransactionManager transactionManager;

    @Autowired
    private DvConfigurationProperties config;

    @Autowired
    private SpringMavenProperties maven;

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    private MetadataInstance metadataInstance;

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    @Bean(name = "connectionExecutor")
    public ScheduledThreadPoolExecutor connectionExecutor() {
        return executor;
    }

    @Bean
    public TextEncryptor getTextEncryptor() {
        return Encryptors.text(encryptKey, "deadbeef");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            repositoryManager.findDataVirtualization("x");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public TeiidServer teiidServer() {

        // turning off PostgreSQL support
        System.setProperty("org.teiid.addPGMetadata", "false");
        System.setProperty("org.teiid.hiddenMetadataResolvable", "true");
        System.setProperty("org.teiid.allowAlter", "false");

        final TeiidServer server = new TeiidServer();

        EmbeddedConfiguration config = new EmbeddedConfiguration();
        if (this.transactionManager != null) {
            config.setTransactionManager(this.transactionManager);
        }
        server.start(config);
        return server;
    }

    @Bean
    @ConditionalOnMissingBean
    public TeiidOpenShiftClient openShiftClient(@Autowired RepositoryManager repositoryManager, @Autowired TextEncryptor enc) {
        return new TeiidOpenShiftClient(metadataInstance, new EncryptionComponent(enc),
                this.config, repositoryManager, this.maven == null ? null : this.maven.getRepositories());
    }

    @Bean
    protected WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setTaskExecutor(getAsyncExecutor());
            }
        };
    }

    @Override
    public AsyncTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor tpte = new ThreadPoolTaskExecutor();
        tpte.initialize();
        return tpte;
    }
}

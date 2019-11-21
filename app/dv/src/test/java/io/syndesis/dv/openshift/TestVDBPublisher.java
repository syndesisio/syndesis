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
package io.syndesis.dv.openshift;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.teiid.adminapi.Model;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.core.util.ObjectConverterUtil;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.syndesis.dv.KException;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.datasources.MySQLDefinition;
import io.syndesis.dv.datasources.PostgreSQLDefinition;
import io.syndesis.dv.metadata.MetadataInstance;
import io.syndesis.dv.server.AuthHandlingFilter;
import io.syndesis.dv.server.AuthHandlingFilter.OAuthCredentials;
import io.syndesis.dv.server.DvConfigurationProperties;

public class TestVDBPublisher {

    private VDBMetaData vdb;

    @Before
    public void setup() throws Exception {
        final InputStream vdbStream = getClass().getClassLoader().getResourceAsStream("myservice-vdb.xml");
        this.vdb = VDBMetadataParser.unmarshell(vdbStream);
    }

    private TeiidOpenShiftClient testDataSetup() {
        AuthHandlingFilter.threadOAuthCredentials.set(new OAuthCredentials("token", "user"));
        MetadataInstance metadata = Mockito.mock(MetadataInstance.class);

        HashSet<DefaultSyndesisDataSource> sources = new HashSet<>();
        sources.add(getMySQLDS());
        sources.add(getPostgreSQL());

        Map<String, String> repos = new HashMap<String, String>();
        repos.put("rh-ga", "https://maven.repository.redhat.com/ga/");

        TeiidOpenShiftClient client = new TeiidOpenShiftClient(metadata, new EncryptionComponent("blah"), new DvConfigurationProperties(), null, repos) {
            @Override
            public Set<DefaultSyndesisDataSource> getSyndesisSources() throws KException {
                return sources;
            }
            @Override
            public DefaultSyndesisDataSource getSyndesisDataSource(String dsName) throws KException {
                if (dsName.equals("accounts-xyz")) {
                    return getPostgreSQL();
                } else {
                    return getMySQLDS();
                }
            }
        };
        return client;
    }

    private DefaultSyndesisDataSource getMySQLDS() {
        DefaultSyndesisDataSource ds1 = new DefaultSyndesisDataSource();
        ds1.setSyndesisName("inventory-abc");
        ds1.setTeiidName("inventory-abc");
        ds1.setTranslatorName("mysql5");
        ds1.setDefinition(new MySQLDefinition());

        HashMap<String, String> credentialData = new HashMap<>();
        credentialData.put("password", "my-pass");
        credentialData.put("schema", "sampledb");
        credentialData.put("url", "jdbc:mysql://localhost:1521/sampledb");
        credentialData.put("user", "johnny");
        ds1.setProperties(credentialData);
        return ds1;
    }

    private DefaultSyndesisDataSource getPostgreSQL() {
        DefaultSyndesisDataSource ds2 = new DefaultSyndesisDataSource();
        ds2.setSyndesisName("accounts-xyz");
        ds2.setTeiidName("accounts-xyz");
        ds2.setTranslatorName("postgresql");
        ds2.setDefinition(new PostgreSQLDefinition());

        HashMap<String, String> credentialData = new HashMap<>();
        credentialData.put("password", "my-pass");
        credentialData.put("schema", "sampledb");
        credentialData.put("url", "jdbc:mysql://localhost:1521/sampledb");
        credentialData.put("user", "johnny");
        ds2.setProperties(credentialData);
        return ds2;
    }

    @Test
    public void testDecryption() throws Exception {
        EncryptionComponent ec = new EncryptionComponent("GpADvcFIBgqMUwSfvljdQ1N5qeQFNXaAToht2O4kgBW2bIalkcPWphs54C4e7mjq");
        Map<String, String> credentialData = new HashMap<>();
        credentialData.put("password", "»ENC:7965a258e2f0029b0e5e797b81917366ed11608f195755fc4fcfebecfca4781917de289fb8579d306741b5ec5680a686");
        credentialData.put("schema", "sampledb");
        credentialData.put("url", "jdbc:mysql://localhost:1521/sampledb");
        credentialData.put("user", "johnny");
        Map<String, String> decrypted = ec.decrypt(credentialData);
        assertThat(credentialData.get("password").equals(decrypted.get("password")), is(false));
    }

    @Test
    public void testGeneratePomXML() throws Exception {
        TeiidOpenShiftClient generator = testDataSetup();

        String pom = generator.generatePomXml(vdb, false);
        assertEquals(ObjectConverterUtil.convertFileToString(new File("src/test/resources/generated-pom.xml")), pom);
    }

    @Test
    public void testGenerateDataSource() throws Exception {
        TeiidOpenShiftClient generator = testDataSetup();

        generator.normalizeDataSourceNames(vdb);

        for (Model model: vdb.getModels()) {
            if (!model.isSource()) {
                continue;
            }
            GenericArchive archive = ShrinkWrap.create(GenericArchive.class, "contents.tar");
            generator.buildDataSourceBuilders(model, archive);
            InputStream dsIs = archive.get("/src/main/java/io/integration/DataSourcesaccountsxyz.java").getAsset().openStream();
            String ds = ObjectConverterUtil.convertToString(dsIs);
            assertEquals(ObjectConverterUtil.convertFileToString(new File("src/test/resources/generated-ds.txt")), ds);
        }
    }

    @Test
    public void testGenerateDeploymentYML() throws Exception {
        TeiidOpenShiftClient generator = testDataSetup();

        PublishConfiguration config = new PublishConfiguration();
        Collection<EnvVar> variables = generator
                .getEnvironmentVariablesForVDBDataSources(vdb, config, TeiidOpenShiftClient.getOpenShiftName(vdb.getName()));
        assertThat( variables.size(), is(9));

        String javaOptions=
                  " -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
                + " -Djava.net.preferIPv4Addresses=true -Djava.net.preferIPv4Stack=true"
                + " -XX:ParallelGCThreads=1 -XX:ConcGCThreads=1"
                + " -Djava.util.concurrent.ForkJoinPool.common.parallelism=1"
                + " -Dio.netty.eventLoopThreads=2"
                + " -Dorg.teiid.hiddenMetadataResolvable=false"
                + " -Dorg.teiid.allowAlter=false";

        assertThat(variables, hasItem(generator.envFromSecret("dv-myservice-secret", "spring.datasource.accounts-xyz.username")));
        assertThat(variables, hasItem(generator.envFromSecret("dv-myservice-secret", "spring.datasource.accounts-xyz.jdbc-url")));
        assertThat(variables, hasItem(generator.envFromSecret("dv-myservice-secret", "spring.datasource.accounts-xyz.password")));

//        assertThat(variables, hasItem(new EnvVarBuilder().withName(EncryptionComponent.SYNDESIS_ENC_KEY)
//                .withValueFrom(new EnvVarSourceBuilder().withConfigMapKeyRef(new ConfigMapKeySelectorBuilder()
//                        .withName("syndesis-server-config").withKey("encrypt.key").build()).build()).build()));

        assertThat(variables, hasItem(new EnvVar("GC_MAX_METASPACE_SIZE", "256", null)));

        assertThat(variables, hasItem(new EnvVar("JAVA_OPTIONS", javaOptions, null)));
    }

}

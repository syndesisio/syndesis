/*
 * Copyright (C) 2016 Red Hat, Inc.
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
package io.syndesis.server.endpoint.v1.handler.support;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigList;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigList;
import io.fabric8.openshift.api.model.DoneableBuildConfig;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.api.model.DoneableImageStreamTag;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.api.model.ImageStreamTagList;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.dsl.BuildConfigResource;
import io.fabric8.openshift.client.dsl.DeployableScalableResource;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import io.syndesis.common.model.EmptyListResult;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;
import io.syndesis.server.endpoint.v1.handler.integration.support.IntegrationSupportHandler;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SupportUtilTest {

    @Rule
    public OpenShiftServer openShiftServer = new OpenShiftServer();

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    public void createSupportZipFileTest(){
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        final Appender mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");

        root.addAppender(mockAppender);

        NamespacedOpenShiftClient client = mock(NamespacedOpenShiftClient.class);

        Config config = new Config();
        when(client.getConfiguration()).thenReturn( config );

        MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod,DoneablePod>> pods = mock(MixedOperation.class);
        when(pods.list()).thenReturn(new PodList());

        when(client.pods()).thenReturn(pods);

        MixedOperation<BuildConfig, BuildConfigList, DoneableBuildConfig, BuildConfigResource<BuildConfig, DoneableBuildConfig, Void, Build>> bcs = mock(MixedOperation.class);
        when(bcs.list()).thenReturn( new BuildConfigList() );

        when(client.buildConfigs()).thenReturn(bcs);

        MixedOperation<DeploymentConfig, DeploymentConfigList, DoneableDeploymentConfig, DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig>> dcs = mock(MixedOperation.class);
        when(dcs.list()).thenReturn( new DeploymentConfigList() );

        when(client.deploymentConfigs()).thenReturn(dcs);

        MixedOperation<ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>> cm = mock(MixedOperation.class);
        when(cm.list()).thenReturn( new ConfigMapList() );

        when(client.configMaps()).thenReturn(cm);

        MixedOperation<ImageStreamTag, ImageStreamTagList, DoneableImageStreamTag, Resource<ImageStreamTag, DoneableImageStreamTag>> ist = mock(MixedOperation.class);
        ImageStreamTagList istl = new ImageStreamTagList();
        List<ImageStreamTag> istList = new ArrayList<>();
        ImageStreamTag imageStreamTag = new ImageStreamTag();
        imageStreamTag.setKind("ImageStreamTag");
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName("ImageStreamTag1");
        imageStreamTag.setMetadata( objectMeta );
        istList.add(imageStreamTag);
        istl.setItems( istList );

        when(ist.list()).thenReturn( istl );

        when(client.imageStreamTags()).thenReturn(ist);

        IntegrationHandler integrationHandler = mock(IntegrationHandler.class);
        when(integrationHandler.list(any())).thenReturn(new EmptyListResult<IntegrationOverview>());
        IntegrationSupportHandler integrationSupportHandler = mock(IntegrationSupportHandler.class);

        SupportUtil supportUtil = new SupportUtil(client, integrationHandler, integrationSupportHandler);

        UriInfo uriInfo = mock(UriInfo.class);

        Map<String, Boolean> configurationMap = new HashMap<>(ImmutableMap.of("int1", true, "int2", true));

        supportUtil.createSupportZipFile( configurationMap, uriInfo);


        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockAppender, atLeast(0)).doAppend(argumentCaptor.capture());
        List<Object> capturedArgument = argumentCaptor.getAllValues();

        for(Object obj : capturedArgument){
            assertTrue(!((LoggingEvent)obj).getFormattedMessage().contains("Error adding resource"));
        }
    }

}

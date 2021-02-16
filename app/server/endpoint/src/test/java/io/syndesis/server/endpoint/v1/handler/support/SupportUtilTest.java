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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.ws.rs.core.UriInfo;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
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
import io.syndesis.common.model.EmptyListResult;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;
import io.syndesis.server.endpoint.v1.handler.integration.support.IntegrationSupportHandler;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class SupportUtilTest {

    @Test
    public void createSupportZipFileTest() throws IOException {
        final NamespacedOpenShiftClient client = mock(NamespacedOpenShiftClient.class);

        final MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods = mock(mixedOperationType());
        when(pods.list()).thenReturn(new PodList());

        when(client.pods()).thenReturn(pods);

        final MixedOperation<BuildConfig, BuildConfigList, DoneableBuildConfig, BuildConfigResource<BuildConfig, DoneableBuildConfig, Void, Build>> bcs = mock(
            mixedOperationType());
        when(bcs.list()).thenReturn(new BuildConfigList());

        when(client.buildConfigs()).thenReturn(bcs);

        final MixedOperation<DeploymentConfig, DeploymentConfigList, DoneableDeploymentConfig, DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig>> dcs = mock(
            mixedOperationType());
        when(dcs.list()).thenReturn(new DeploymentConfigList());

        when(client.deploymentConfigs()).thenReturn(dcs);

        final MixedOperation<ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>> cm = mock(mixedOperationType());
        when(cm.list()).thenReturn(new ConfigMapList());

        when(client.configMaps()).thenReturn(cm);

        final MixedOperation<ImageStreamTag, ImageStreamTagList, DoneableImageStreamTag, Resource<ImageStreamTag, DoneableImageStreamTag>> ist = mock(
            mixedOperationType());
        final ImageStreamTagList istl = new ImageStreamTagList();
        final List<ImageStreamTag> istList = new ArrayList<>();
        final ImageStreamTag imageStreamTag = new ImageStreamTag();
        imageStreamTag.setKind("ImageStreamTag");
        final ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName("ImageStreamTag1");
        imageStreamTag.setMetadata(objectMeta);
        istList.add(imageStreamTag);
        istl.setItems(istList);

        when(ist.list()).thenReturn(istl);

        when(client.imageStreamTags()).thenReturn(ist);

        final IntegrationHandler integrationHandler = mock(IntegrationHandler.class);
        when(integrationHandler.list(any())).thenReturn(new EmptyListResult<IntegrationOverview>());
        final IntegrationSupportHandler integrationSupportHandler = mock(IntegrationSupportHandler.class);

        final Logger log = mock(Logger.class);
        final SupportUtil supportUtil = new SupportUtil(client, integrationHandler, integrationSupportHandler, log);

        final UriInfo uriInfo = mock(UriInfo.class);

        final Map<String, Boolean> configurationMap = new HashMap<>(ImmutableMap.of("int1", true, "int2", true));

        final File output = supportUtil.createSupportZipFile(configurationMap, uriInfo);

        try (final ZipFile zip = new ZipFile(output)) {
            final ZipEntry imageStreamTag1 = zip.getEntry("descriptors/ImageStreamTag/ImageStreamTag1.YAML");
            assertThat(imageStreamTag1).isNotNull();
            AssertionsForClassTypes.assertThat(zip.getInputStream(imageStreamTag1)).hasContent(SupportUtil.YAML.dump(imageStreamTag));
        }

        verify(log).info("Created Support file: {}", output);
        // tests that we haven't logged any error messages
        verifyZeroInteractions(log);
    }

    private static <T, L, D, R extends Resource<T, D>> Class<MixedOperation<T, L, D, R>> mixedOperationType() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<MixedOperation<T, L, D, R>> type = (Class) MixedOperation.class;

        return type;
    }

}

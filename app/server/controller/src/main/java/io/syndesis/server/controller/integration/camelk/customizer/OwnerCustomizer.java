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
package io.syndesis.server.controller.integration.camelk.customizer;

import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.TraitSpec;
import io.syndesis.server.openshift.Exposure;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Configure owner trait
 */
@Component
public class OwnerCustomizer extends AbstractTraitCustomizer {
    public static final Collection<String> LABELS = Collections.unmodifiableCollection(Arrays.asList(
        OpenShiftService.COMPONENT_LABEL,
        OpenShiftService.INTEGRATION_ID_LABEL,
        OpenShiftService.DEPLOYMENT_VERSION_LABEL,
        OpenShiftService.USERNAME_LABEL,
        OpenShiftService.INTEGRATION_NAME_LABEL,
        "syndesis.io/type",
        "syndesis.io/app"
    ));

    public static final Collection<String> LABELS_3SCALE = Collections.unmodifiableCollection(Arrays.asList(
        "discovery.3scale.net"
    ));

    public static final Collection<String> ANNOTATIONS= Collections.unmodifiableCollection(Arrays.asList(
        "prometheus.io/port",
        "prometheus.io/scrape"
    ));

    public static final Collection<String> ANNOTATIONS_3SCALE= Collections.unmodifiableCollection(Arrays.asList(
        "discovery.3scale.net/scheme",
        "discovery.3scale.net/port",
        "discovery.3scale.net/description-path"
    ));

    @Override
    protected Map<String, TraitSpec> computeTraits(Integration integration, EnumSet<Exposure> exposure) {
        List<String> labels = new ArrayList<>(LABELS);
        List<String> annotations = new ArrayList<>(ANNOTATIONS);

        if (exposure.contains(Exposure._3SCALE)) {
            labels.addAll(LABELS_3SCALE);
            annotations.addAll(ANNOTATIONS_3SCALE);
        }

        return Collections.singletonMap(
            "owner",
            new TraitSpec.Builder()
                .putConfiguration("target-labels", String.join(",", labels))
                .putConfiguration("target-annotations", String.join(",", annotations))
                .build()
        );
    }
}

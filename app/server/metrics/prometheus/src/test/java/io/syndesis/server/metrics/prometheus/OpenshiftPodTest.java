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
package io.syndesis.server.metrics.prometheus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;

/**
 * Unit Tests to check Openshift's Pod reader
 */
public class OpenshiftPodTest {

    final static Map<String,String> LABELS = new HashMap<>();
    static { 
        LABELS.put("app", "syndesis");
        LABELS.put("component", "syndesis-server");
    }
    final static LabelSelector SELECTOR = new LabelSelector(null, LABELS);
    
    @Test
    public void readTest() throws ParseException {
 
       NamespacedOpenShiftClient client = new DefaultOpenShiftClient();

       Pod pod = client.pods().withLabelSelector(SELECTOR).list().getItems().get(0);
       String startTime = pod.getStatus().getStartTime();
       System.out.println("pod " + pod.getMetadata().getName() + " " + startTime);
       final DateFormat dateFormat = //2018-03-14T23:34:09Z
               new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
       Date startDate = dateFormat.parse(startTime);
       System.out.println(startDate);
       client.close();
    }
}

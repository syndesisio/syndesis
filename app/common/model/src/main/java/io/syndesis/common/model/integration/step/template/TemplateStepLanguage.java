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
package io.syndesis.common.model.integration.step.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.StringConstants;

/**
 * Provides languages model for use with the template step feature.
 * Each language has a name, maven dependency, camel header id
 * and {@link TemplateStepPreProcessor} implementation defined
 * for use by the template step handler and project generator.
 */
public class TemplateStepLanguage {

    public static final String LANGUAGE_PROPERTY = "language";

    public static final TemplateStepLanguage MUSTACHE = new TemplateStepLanguage(
             "MUSTACHE",
             "org.apache.camel:camel-mustache",
             "MustacheTemplate",
             new MustacheTemplatePreProcessor());

    public static final TemplateStepLanguage VELOCITY = new TemplateStepLanguage(
             "VELOCITY",
             "org.apache.camel:camel-velocity",
             "CamelVelocityTemplate",
             new VelocityTemplatePreProcessor());

    public static final TemplateStepLanguage FREEMARKER = new TemplateStepLanguage(
               "FREEMARKER",
               "org.apache.camel:camel-freemarker",
               "CamelFreeMarkerTemplate",
               new FreeMarkerTemplatePreProcessor());

    private final String name;

    private final String mvnDependency;

    private final String camelHeaderConstant;

    private final TemplateStepPreProcessor preProcessor;

    private TemplateStepLanguage(String name, String mvnDependency, String camelHeader, TemplateStepPreProcessor preProcessor) {
        this.name = name;
        this.mvnDependency = mvnDependency;
        this.camelHeaderConstant = camelHeader;
        this.preProcessor = preProcessor;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public String mavenDependency() {
        return this.mvnDependency;
    }

    public String camelHeader() {
        return this.camelHeaderConstant;
    }

    public String generateUri(String id) {
        return this.toString() + StringConstants.COLON + id;
    }

    public Map<String, Object> getUriParams() {
        return preProcessor.getUriParams();
    }

    public String preProcess(String template) throws TemplateProcessingException {
        preProcessor.reset();
        return preProcessor.preProcess(template);
    }

    public List<SymbolSyntax> getSymbolSyntaxes() {
        return preProcessor.getSymbolSyntaxes();
    }

    public SymbolSyntax getDefaultSymbolSyntax() {
        return preProcessor.getSymbolSyntaxes().get(0);
    }

    public boolean isSymbol(String symbol) {
        return preProcessor.isMySymbol(symbol);
    }

    public static Collection<TemplateStepLanguage> values() {
        List<TemplateStepLanguage> languages = new ArrayList<>();
        languages.add(MUSTACHE);
        languages.add(VELOCITY);
        languages.add(FREEMARKER);
        return Collections.unmodifiableList(languages);
    }

    public static TemplateStepLanguage stepLanguage(String language) {
        if (language == null) {
            return MUSTACHE;
        }

        for (TemplateStepLanguage sl : TemplateStepLanguage.values()) {
            if (sl.name().equalsIgnoreCase(language)) {
                return sl;
            }
        }

        return MUSTACHE;
    }

    /**
     * Update the integration by visiting its {@link Flow}s and their {@link Step}s
     *
     * @param integration the integration
     * @return the update integration
     */
    public static Integration updateIntegration(Integration integration) {
        Integration.Builder integrationBuilder = integration.builder();

        List<Flow> replacementFlows = new ArrayList<>(integration.getFlows());
        ListIterator<Flow> flows = replacementFlows.listIterator();
        while(flows.hasNext()) {
            Flow flow = flows.next();
            flows.set(updateFlow(flow));
        }

        return integrationBuilder
            .flows(replacementFlows)
            .build();
    }

    /**
     * Update the flow by visiting its {@link Step}s
     *
     * @param flow the flow
     * @return the newly updated flow
     */
    public static Flow updateFlow(Flow flow) {
        Flow.Builder flowBuilder = flow.builder();

        List<Step> replacementSteps = new ArrayList<>(flow.getSteps());
        ListIterator<Step> steps = replacementSteps.listIterator();

        while (steps.hasNext()) {
            Step source = steps.next();
            steps.set(updateStep(source));
        }

        return flowBuilder.steps(replacementSteps).build();
    }

    /**
     * Update the template step {@link Step} with required dependencies
     *
     * @param step the template step
     * @return the new step
     */
    public static Step updateStep(Step step) {
        if (StepKind.template != step.getStepKind()) {
            return step;
        }

        Map<String, String> properties = step.getConfiguredProperties();
        TemplateStepLanguage language = stepLanguage(
                                                                    properties.get(LANGUAGE_PROPERTY));

        Dependency dependency = Dependency.maven(language.mavenDependency());
        return step.builder()
                     .dependencies(Collections.singleton(dependency))
                     .build();
    }

    /**
     * The syntax of a template step language
     */
    public static class SymbolSyntax {

        private final String open;

        private final String close;

        public SymbolSyntax(String open, String close) {
            this.open = open;
            this.close = close;
        }

        public String open() {
            return open;
        }

        public String close() {
            return close;
        }
    }
}

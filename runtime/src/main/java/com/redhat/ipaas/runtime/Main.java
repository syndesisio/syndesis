/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.runtime;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.keycloak.Secured;
import org.wildfly.swarm.swagger.SwaggerArchive;
import org.wildfly.swarm.swagger.webapp.SwaggerWebAppFraction;
import org.wildfly.swarm.undertow.WARArchive;

public class Main {

    public static void main(String... args) throws Exception {
        Swarm swarm = new Swarm();

        SwaggerWebAppFraction swaggerFraction = new SwaggerWebAppFraction();
        swaggerFraction.addWebContent(System.getProperty("swarm.swagger.ui.resources", "com.redhat.ipaas:swagger-ui:" + com.redhat.ipaas.swaggerui.Version.getVersion()));
        swarm.fraction(swaggerFraction);

        // Create a SwaggerArchive using ShrinkWrap API
        SwaggerArchive archive = ShrinkWrap.create(SwaggerArchive.class).
            setVersion(Version.getVersion()).
            setContact("ipaas-dev <ipaas-dev@redhat.com>").
            setDescription("The Red Hat iPaaS REST API "
                + " connects to back-end services on the IPaaS and provides a single entry point"
                + " for the IpaaS Console. For console developement it can run in off-line mode"
                + " where it only serves responses from the response cache.").
            setLicense("Apache License, Version 2.0").
            setLicenseUrl("https://www.apache.org/licenses/LICENSE-2.0").
            setPrettyPrint(true).
            setTitle("Red Hat iPaaS API").
            setResourcePackages("com.redhat.ipaas.api.v1.rest");
        
//        archive.as(Secured.class).
//            protect( "/components/*").
//            withMethod( "GET" ).
//            withRole( "citizen_developer" );
        

        JAXRSArchive jaxrs = archive.as(JAXRSArchive.class).
            setContextRoot("v1").
            addAllDependencies();

        WARArchive staticContent = ShrinkWrap.create(WARArchive.class).
            setDefaultContextRoot().
            staticContent();

        swarm.
            start().
            deploy(staticContent).
            deploy(jaxrs);
    }

}

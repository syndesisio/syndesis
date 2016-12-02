/*
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
 *
 */

package com.redhat.ipaas.runtime;

import com.redhat.ipaas.api.Version;
import com.redhat.ipaas.rest.VersionEndpoint;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.swagger.SwaggerArchive;
import org.wildfly.swarm.swagger.webapp.SwaggerWebAppFraction;

public class Main {

    public static void main(String... args) throws Exception {
        Swarm swarm = new Swarm();
        
        SwaggerWebAppFraction swaggerFraction = new SwaggerWebAppFraction();
        swaggerFraction.addWebContent("target/runtime.jar");
        
     // Create a SwaggerArchive using ShrinkWrap API
        SwaggerArchive archive = ShrinkWrap.create(SwaggerArchive.class);
        
        // Now we can use the SwaggerArchive to fully customize the JSON output
        archive.setVersion(Version.getVersion()); // our API version
        archive.setContact("ipaas-dev <ipaas-dev@redhat.com>");  // set contact info
        archive.setLicense("Apache License, Version 2.0"); // set license
        archive.setDescription("IPaaS Client API for the IPaaS Console. The Client API"
        		+ " connects to back-end services on the IPaaS and provides a single entry point"
        		+ " for the IpaaS Console. For console developement it can run in off-line mode"
        		+ " where it only serves response from the response cache. ");
        archive.setLicenseUrl("https://www.apache.org/licenses/LICENSE-2.0");
        archive.setPrettyPrint(true);
        archive.setTitle("IPaas Client API");
        archive.setResourcePackages("com.redhat.ipaas.rest");
        
     // Make the SwaggerArchive JAX-RS friendly and add our api package
        JAXRSArchive deployment = archive.as(JAXRSArchive.class).addPackage("com.redhat.ipaas.rest");
        
        deployment.setContextRoot("v1");
        deployment.addClass(VersionEndpoint.class);
        deployment.addAllDependencies();
        swarm.fraction(swaggerFraction);
        swarm.start().deploy(deployment);
        
        
    }
    
    
}

package com.redhat.ipaas.runtime;

import com.redhat.ipaas.rest.VersionEndpoint;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

public class Main {

    public static void main(String... args) throws Exception {
        Swarm swarm = new Swarm();
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addClass(VersionEndpoint.class);
        deployment.addAllDependencies();
        swarm.start().deploy(deployment);
    }
}

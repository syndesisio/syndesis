package com.paypal.springboot.resteasy.sample;

import javax.ws.rs.core.Application;

/**
 * This application, although extending Application class,
 * is NOT annotated with ApplicationPath annotation, which
 * should prevent its registration
 *
 * Created by facarvalho on 11/25/15.
 */
public class TestApplication3 extends Application {
}

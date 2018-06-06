package com.paypal.springboot.resteasy.sample;

import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by facarvalho on 6/9/16.
 */
@Path("resource2")
@Component
public class TestResource2 {

    @GET
    public void get() {
        // Test get method
    }

}

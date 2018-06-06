package com.paypal.springboot.resteasy.sample;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

/**
 * Created by facarvalho on 6/9/16.
 */
@Component
@Provider
public class TestProvider1 implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        return null;
    }
}

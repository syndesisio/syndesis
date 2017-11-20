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
package io.syndesis.runtime;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import org.springframework.cglib.proxy.Enhancer;

/**
 * This class allows you to proxy other objects and record all
 * method invocation done on those objects so that your test cases
 * can assert the your object has been called as expected.
 */
public class Recordings {

    public static class Invocation {
        private final Method method;
        private final Object[] args;
        private Object result;
        private Throwable error;

        public Invocation(@Nonnull Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArgs() {
            return args;
        }

        public Object getResult() {
            return result;
        }

        public Throwable getError() {
            return error;
        }
    }

    public static class RecordingInvocationHandler implements InvocationHandler {
        private final Object target;
        private final List<Invocation> recordedInvocations = Collections.synchronizedList(new ArrayList<>());
        private volatile CountDownLatch latch = new CountDownLatch(1);

        public RecordingInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (method.getName().equals("getInvocationHandler$$$")) {
                RecordingInvocationHandler rc = this;
                return rc;
            }

            Invocation invocation = new Invocation(method, args);

            // Skip over toString since this could get hit as a byproduct of
            // running in a debugger.
            boolean skipped = true;
            if (!method.getName().equals("toString")) {
                recordedInvocations.add(invocation);
                skipped = false;
            }
            try {
                invocation.result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                invocation.error = e.getTargetException();
                throw invocation.error;
            } finally {
                if (!skipped) {
                    latch.countDown();
                }
            }
            return invocation.result;
        }
    }

    public interface RecordingProxy {
        // Use a weird method name to avoid conflicts with other methods the proxied class might declare.
        RecordingInvocationHandler getInvocationHandler$$$();
    }

    static public <T> T recorder(Object object, Class<T> as) {
        if (as.isInterface()) {
            // If it's just an interface, use standard java reflect proxying
            return as.cast(Proxy.newProxyInstance(as.getClassLoader(), new Class<?>[] { as }, new RecordingInvocationHandler(object)));
        }

        // If it's a class then use gclib to implement a subclass to implement proxying
        RecordingInvocationHandler ih = new RecordingInvocationHandler(object);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(as);
        enhancer.setInterfaces(new Class<?>[]{RecordingProxy.class});
        enhancer.setCallback(new org.springframework.cglib.proxy.InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                return ih.invoke(o, method, objects);
            }
        });
        return as.cast(enhancer.create());
    }

    static public CountDownLatch resetRecorderLatch(Object object, int count) {
        RecordingInvocationHandler ih = null;
        if (object instanceof RecordingProxy) {
            ih = ((RecordingProxy) object).getInvocationHandler$$$();
        } else {
            ih = (RecordingInvocationHandler) Proxy.getInvocationHandler(object);
        }
        CountDownLatch latch = new CountDownLatch(count);
        ih.latch = latch;
        return latch;
    }

    static public List<Invocation> recordedInvocations(Object object) {
        RecordingInvocationHandler ih = null;
        if (object instanceof RecordingProxy) {
            ih = ((RecordingProxy) object).getInvocationHandler$$$();
        } else {
            ih = (RecordingInvocationHandler) Proxy.getInvocationHandler(object);
        }
        return ih.recordedInvocations;
    }


}

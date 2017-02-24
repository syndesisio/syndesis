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

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by chirino on 2/24/17.
 */
public class Recordings {

    public static class Invocation {
        private final Method method;
        private final Object[] args;
        private Object result;
        private Throwable error;

        public Invocation(@Nonnull Method method, @Nonnull Object[] args) {
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

    private static class RecordingInvocationHandler implements InvocationHandler {

        private final Object target;
        private final List<Invocation> recordedInvocations = Collections.synchronizedList(new ArrayList<>());
        private volatile CountDownLatch latch = new CountDownLatch(1);

        public RecordingInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Invocation invocation = new Invocation(method, args);

            // Skip over toString since this could get hit as a byproduct of
            // running in a debugger.
            boolean skipped = true;
            if( !method.getName().equals("toString") ) {
                recordedInvocations.add(invocation);
                skipped = false;
            }
            try {
                invocation.result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                invocation.error = e.getTargetException();
                throw invocation.error;
            } finally {
                if( !skipped ) {
                    latch.countDown();
                }
            }
            return invocation.result;
        }
    }

    static public <T> T recorder(Object object, Class<T> as) {
        return as.cast(Proxy.newProxyInstance(as.getClassLoader(), new Class[]{as}, new RecordingInvocationHandler(object)));
    }

    static public CountDownLatch resetRecorderLatch(Object object, int count) {
        RecordingInvocationHandler ih = (RecordingInvocationHandler)Proxy.getInvocationHandler(object);
        CountDownLatch latch = new CountDownLatch(count);
        ih.latch = latch;
        return latch;
    }

    static public List<Invocation> recordedInvocations(Object object) {
        RecordingInvocationHandler ih = (RecordingInvocationHandler)Proxy.getInvocationHandler(object);
        return ih.recordedInvocations;
    }

}

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
package io.syndesis.dv.utils;

import org.apache.commons.logging.LogFactory;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;

import io.syndesis.dv.StringConstants;

public class KLog {

    @FormatMethod
    private static String format(@FormatString String message, Object... arguments ) {
        if (message == null) {
            message = StringConstants.EMPTY_STRING;
        }
        if (arguments == null || arguments.length == 0) {
            return message;
        }
        return String.format(message, arguments);
    }

    private static final KLog instance = new KLog();

    /**
     * @return singleton instance of this logger
     */
    public static KLog getLogger() {
        return instance;
    }

    private final org.apache.commons.logging.Log kLogger = LogFactory.getLog(KLog.class);

    @FormatMethod
    public void info(@FormatString String message, Object... args) {
        kLogger.info(format(message, args));
    }

    @FormatMethod
    public void info(Throwable throwable, @FormatString String message, Object... args) {
        kLogger.info(format(message, args), throwable);
    }

    public boolean isInfoEnabled() {
        return this.kLogger.isInfoEnabled();
    }

    @FormatMethod
    public void warn(@FormatString String message, Object... args) {
        kLogger.warn(format(message, args));
    }

    public void warn(String message, Throwable throwable) {
        kLogger.warn(message, throwable);
    }

    @FormatMethod
    public void warn(Throwable throwable, @FormatString String message, Object... args) {
        kLogger.warn(format(message, args), throwable);
    }

    public boolean isWarnEnabled() {
        return this.kLogger.isWarnEnabled();
    }

    @FormatMethod
    public void error(@FormatString String message, Object... args) {
        kLogger.error(format(message, args));
    }

    public void error(String message, Throwable throwable) {
        kLogger.error(message, throwable);
    }

    @FormatMethod
    public void error(Throwable throwable, @FormatString String message, Object... args) {
        kLogger.error(format(message, args), throwable);
    }

    public boolean isErrorEnabled() {
        return this.kLogger.isErrorEnabled();
    }

    public void debug(String message, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        kLogger.debug(message, throwable);
    }

    @FormatMethod
    public void debug(@FormatString String message, Object... args) {
        if (!isDebugEnabled()) {
            return;
        }
        kLogger.debug(format(message, args));
    }

    @FormatMethod
    public void debug(Throwable throwable, @FormatString String message, Object... args) {
        if (!isDebugEnabled()) {
            return;
        }
        kLogger.debug(format(message, args), throwable);
    }

    public boolean isDebugEnabled() {
        return this.kLogger.isDebugEnabled();
    }

    @FormatMethod
    public void trace(@FormatString String message, Object... args) {
        if (!isTraceEnabled()) {
            return;
        }
        kLogger.trace(format(message, args));
    }

    @FormatMethod
    public void trace(Throwable throwable, @FormatString String message, Object... args) {
        if (!isTraceEnabled()) {
            return;
        }
        kLogger.trace(format(message, args), throwable);
    }

    public boolean isTraceEnabled() {
        return this.kLogger.isTraceEnabled();
    }
}

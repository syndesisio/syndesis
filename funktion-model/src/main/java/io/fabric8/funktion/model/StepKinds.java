/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.funktion.model;

/**
 */
public class StepKinds {
    public static final String CHOICE = "choice";
    public static final String ENDPOINT = "endpoint";
    public static final String FILTER = "filter";
    public static final String FUNCTION = "function";
    public static final String FLOW = "flow";
    public static final String OTHERWISE = "otherwise";
    public static final String SET_BODY = "setBody";
    public static final String SET_HEADERS = "setHeaders";
    public static final String SPLIT = "split";
    public static final String THROTTLE = "throttle";
}

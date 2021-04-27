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

package io.syndesis.server.endpoint.v1.handler.meta;

class Person {
    private String name;
    private int age;

    /**
     * Obtains the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Specifies the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtains the age.
     */
    public int getAge() {
        return age;
    }

    /**
     * Specifies the age.
     */
    public void setAge(int age) {
        this.age = age;
    }
}

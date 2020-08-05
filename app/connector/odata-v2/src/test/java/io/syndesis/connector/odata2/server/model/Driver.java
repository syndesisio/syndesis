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
package io.syndesis.connector.odata2.server.model;

import java.util.Calendar;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;

/**
 * Taken from the tutorial:
 * http://olingo.apache.org/doc/odata2/tutorials/OlingoV2BasicClientSample.html
 */
@EdmEntityType(namespace = "CarService")
@EdmEntitySet(name = "Drivers")
public class Driver {

    @EdmKey
    @EdmProperty
    private Long id;

    @EdmProperty
    private String name;
    @EdmProperty
    private String lastname;
    @EdmProperty
    private String nickname;
    @EdmNavigationProperty(name = "Car")
    private Car car;
    @EdmProperty(type = EdmType.DATE_TIME)
    private Calendar birthday;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Calendar getUpdated() {
        return birthday;
    }

    public void setBirthday(Calendar birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "Driver{id=" + id + ", name=" + name + ", lastname=" + lastname + ", nickname=" + nickname
            + ", car id=" + car.getId() + ", updated=" + birthday + '}';
    }

}

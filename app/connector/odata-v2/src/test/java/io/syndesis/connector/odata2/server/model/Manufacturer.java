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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty;
import org.apache.olingo.odata2.api.annotation.edm.EdmNavigationProperty.Multiplicity;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;

/**
 * Taken from the tutorial:
 * http://olingo.apache.org/doc/odata2/tutorials/OlingoV2BasicClientSample.html
 */
@EdmEntityType(namespace = "CarService")
@EdmEntitySet(name = "Manufacturers")
public class Manufacturer {

    @EdmKey
    @EdmProperty
    private String id;
    @EdmProperty
    private String name;
    @EdmProperty
    private Calendar founded;
    @EdmProperty
    private Address address;
    @EdmNavigationProperty(name = "Cars", toType = Car.class, toMultiplicity = Multiplicity.MANY)
    private List<Car> cars = new ArrayList<Car>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getUpdated() {
        return founded;
    }

    public void setFounded(Calendar updated) {
        this.founded = updated;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    @Override
    public String toString() {
        return "Manufacturer{" + "id=" + id + ", name=" + name + ", updated=" + founded +
            ", address=" + address + ", cars=" + cars.size() + '}';
    }
}

/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.server.endpoint;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.openshift.BuildStatus;

@SuppressWarnings( {"javadoc", "nls"} )
public final class RestDataserviceTest {

    private static final String DATASERVICE_NAME = "MyDataservice";
    private static final String DESCRIPTION = "my description";
    private static final String SERVICE_VIEW_MODEL = "serviceViewModel";
    private static final String SERVICE_VIEW1 = "serviceView1";
    private static final String SERVICE_VIEW2 = "serviceView2";
    private static final String DATASERVICE_PUBLISHED_STATE = BuildStatus.Status.NOTFOUND.name();

    private RestDataVirtualization dataservice;

    private RestDataVirtualization copy() {
        final RestDataVirtualization copy = new RestDataVirtualization();

        copy.setName(dataservice.getName());
        copy.setId(dataservice.getId());
        copy.setDescription(dataservice.getDescription());
        copy.setServiceViewModel(this.dataservice.getServiceViewModel());
        copy.setPublishedState(this.dataservice.getPublishedState());

        return copy;
    }

    @Before
    public void init() throws Exception {
        DataVirtualization theDataservice = Mockito.mock(DataVirtualization.class);
        Mockito.when(theDataservice.getName()).thenReturn(DATASERVICE_NAME);

        this.dataservice = new RestDataVirtualization(theDataservice);
        this.dataservice.setName(DATASERVICE_NAME);
        this.dataservice.setDescription(DESCRIPTION);
        this.dataservice.setServiceViewModel(SERVICE_VIEW_MODEL);
        this.dataservice.setPublishedState(DATASERVICE_PUBLISHED_STATE);
        String[] viewNames = new String[2];
        viewNames[0] = SERVICE_VIEW1;
        viewNames[1] = SERVICE_VIEW2;
    }

    @Test
    public void shouldBeEqual() {
        final RestDataVirtualization thatDataservice = copy();
        assertEquals(this.dataservice, thatDataservice);
    }

    @Test
    public void shouldBeEqualWhenComparingEmptyDataservices() {
        final RestDataVirtualization empty1 = new RestDataVirtualization();
        final RestDataVirtualization empty2 = new RestDataVirtualization();
        assertEquals(empty1, empty2);
    }

    @Test
    public void shouldHaveSameHashCode() {
        final RestDataVirtualization thatDataservice = copy();
        assertEquals(this.dataservice.hashCode(), thatDataservice.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenNameIsDifferent() {
        final RestDataVirtualization thatDataservice = copy();
        thatDataservice.setName(this.dataservice.getName() + "blah");
        assertNotEquals(this.dataservice, thatDataservice);
    }

}

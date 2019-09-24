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
package io.syndesis.connector.aws.ddb;

import java.util.List;
import io.syndesis.common.model.integration.Step;
import org.junit.Ignore;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@Ignore("Make sure the AWSDDBConfiguration has the proper credentials before running this test")
public class AWSDDBInsertItemTest extends AWSDDBGenericOperation {

    @Override
    String getConnectorId() {
        return "io.syndesis:aws-ddb-putitem-to-connector";
    }

    @Override
    String getCustomizer() {
        return "io.syndesis.connector.aws.ddb.customizer" +
                ".DDBConnectorCustomizerPutItem";
    }


    /**
     * Extend the steps to add an intermediate putitem
     *
     * @return
     */
    @Override
    protected List<Step> createSteps() {

        List<Step> result = super.createSteps();

        addExtraOperation(result, "io.syndesis:aws-ddb-removeitem-to-connector", "io.syndesis.connector.aws.ddb" +
            ".customizer.DDBConnectorCustomizerRemoveItem", 3);

        return result;
    }
}

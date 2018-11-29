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

package io.syndesis.connector.sheets;

import java.util.HashMap;
import java.util.Map;

import com.google.api.services.sheets.v4.model.AddChartRequest;
import com.google.api.services.sheets.v4.model.BasicChartSpec;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.EmbeddedChart;
import io.syndesis.connector.sheets.model.GoogleChart;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class GoogleSheetsAddChartCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsAddChartCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new GoogleSheetsAddChartCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());
        options.put("title", "SyndesisChart");
        options.put("subtitle", "Some subtitle");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), options.get("apiName"));
        Assert.assertEquals("batchUpdate", options.get("methodName"));

        Assert.assertNotNull(inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));
        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader("CamelGoogleSheets.batchUpdateSpreadsheetRequest");
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();

        Assert.assertEquals("SyndesisChart", addChartRequest.getChart().getSpec().getTitle());
        Assert.assertEquals("Some subtitle", addChartRequest.getChart().getSpec().getSubtitle());
        Assert.assertTrue(addChartRequest.getChart().getPosition().getNewSheet());

    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GoogleChart model = new GoogleChart();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setTitle("SyndesisChart");
        model.setSubtitle("Some subtitle");
        model.setSheetId(1);

        GoogleChart.BasicChart basicChart = new GoogleChart.BasicChart();
        basicChart.setAxisTitleBottom("Product Names");
        basicChart.setAxisTitleLeft("Sales Numbers");

        GoogleChart.ChartSource domainSource = new GoogleChart.ChartSource();
        domainSource.setFromRow(0);
        domainSource.setFromRow(10);
        domainSource.setColumnIndex(0);
        domainSource.setSheetId(1);
        basicChart.setDomainSource(domainSource);

        GoogleChart.ChartSource firstColumnSource = new GoogleChart.ChartSource();
        firstColumnSource.setFromRow(0);
        firstColumnSource.setFromRow(10);
        firstColumnSource.setColumnIndex(1);
        firstColumnSource.setSheetId(1);
        basicChart.getDataSources().add(firstColumnSource);

        GoogleChart.ChartSource secondColumnSource = new GoogleChart.ChartSource();
        secondColumnSource.setFromRow(0);
        secondColumnSource.setFromRow(10);
        secondColumnSource.setColumnIndex(2);
        secondColumnSource.setSheetId(1);
        basicChart.getDataSources().add(secondColumnSource);

        model.setBasicChart(basicChart);

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assert.assertNotNull(inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));
        Assert.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader("CamelGoogleSheets.batchUpdateSpreadsheetRequest");
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();
        EmbeddedChart chart = addChartRequest.getChart();
        Assert.assertEquals("SyndesisChart", chart.getSpec().getTitle());
        Assert.assertEquals("Some subtitle", chart.getSpec().getSubtitle());
        Assert.assertEquals(Integer.valueOf(1), chart.getPosition().getSheetId());

        BasicChartSpec chartSpec = chart.getSpec().getBasicChart();
        Assert.assertNotNull(chartSpec);
        Assert.assertEquals(2, chartSpec.getAxis().size());
        Assert.assertEquals("Product Names", chartSpec.getAxis().get(0).getTitle());
        Assert.assertEquals("BOTTOM_AXIS", chartSpec.getAxis().get(0).getPosition());
        Assert.assertEquals("Sales Numbers", chartSpec.getAxis().get(1).getTitle());
        Assert.assertEquals("LEFT_AXIS", chartSpec.getAxis().get(1).getPosition());
    }
}

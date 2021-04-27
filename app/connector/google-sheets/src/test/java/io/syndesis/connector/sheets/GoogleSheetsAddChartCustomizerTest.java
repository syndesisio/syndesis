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
import com.google.api.services.sheets.v4.model.BasicChartSeries;
import com.google.api.services.sheets.v4.model.BasicChartSpec;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.ChartData;
import com.google.api.services.sheets.v4.model.EmbeddedChart;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.PieChartSpec;
import io.syndesis.connector.sheets.model.GoogleChart;
import io.syndesis.connector.support.util.ConnectorOptions;

import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.impl.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoogleSheetsAddChartCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsAddChartCustomizer customizer;

    @BeforeEach
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

        Assertions.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assertions.assertEquals("batchUpdate", ConnectorOptions.extractOption(options, "methodName"));

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();

        Assertions.assertEquals("SyndesisChart", addChartRequest.getChart().getSpec().getTitle());
        Assertions.assertEquals("Some subtitle", addChartRequest.getChart().getSpec().getSubtitle());
        Assertions.assertTrue(addChartRequest.getChart().getPosition().getNewSheet());

    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GoogleChart model = new GoogleChart();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setTitle("SyndesisChart");
        model.setSubtitle("Some subtitle");
        model.setSourceSheetId(0);
        model.setSheetId(1);
        model.setOverlayPosition("D2");

        GoogleChart.BasicChart basicChart = new GoogleChart.BasicChart();
        basicChart.setAxisTitleBottom("Product Names");
        basicChart.setAxisTitleLeft("Sales Numbers");

        basicChart.setDomainRange("A1:A10");
        basicChart.setDataRange("B1:B10");

        model.setBasicChart(basicChart);

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();
        EmbeddedChart chart = addChartRequest.getChart();
        Assertions.assertEquals("SyndesisChart", chart.getSpec().getTitle());
        Assertions.assertEquals("Some subtitle", chart.getSpec().getSubtitle());
        Assertions.assertEquals(Integer.valueOf(1), chart.getPosition().getOverlayPosition().getAnchorCell().getSheetId());
        Assertions.assertEquals(Integer.valueOf(1), chart.getPosition().getOverlayPosition().getAnchorCell().getRowIndex());
        Assertions.assertEquals(Integer.valueOf(3), chart.getPosition().getOverlayPosition().getAnchorCell().getColumnIndex());

        BasicChartSpec chartSpec = chart.getSpec().getBasicChart();
        Assertions.assertNotNull(chartSpec);
        Assertions.assertEquals(2, chartSpec.getAxis().size());
        Assertions.assertEquals("Product Names", chartSpec.getAxis().get(0).getTitle());
        Assertions.assertEquals("BOTTOM_AXIS", chartSpec.getAxis().get(0).getPosition());
        Assertions.assertEquals("Sales Numbers", chartSpec.getAxis().get(1).getTitle());
        Assertions.assertEquals("LEFT_AXIS", chartSpec.getAxis().get(1).getPosition());

        Assertions.assertEquals(1, chartSpec.getDomains().size());
        Assertions.assertEquals(1, chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().size());
        Assertions.assertEquals(Integer.valueOf(0), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getStartRowIndex());
        Assertions.assertEquals(Integer.valueOf(10), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getEndRowIndex());
        Assertions.assertEquals(Integer.valueOf(0), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getStartColumnIndex());
        Assertions.assertEquals(Integer.valueOf(1), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getEndColumnIndex());

        Assertions.assertEquals(1, chartSpec.getSeries().size());

        Assertions.assertEquals("LEFT_AXIS", chartSpec.getSeries().get(0).getTargetAxis());
        BasicChartSeries basicChartSeries = chartSpec.getSeries().get(0);
        Assertions.assertEquals(1, basicChartSeries.getSeries().getSourceRange().getSources().size());
        GridRange gridRange = basicChartSeries.getSeries().getSourceRange().getSources().get(0);
        Assertions.assertEquals(Integer.valueOf(0), gridRange.getStartRowIndex());
        Assertions.assertEquals(Integer.valueOf(10), gridRange.getEndRowIndex());
        Assertions.assertEquals(Integer.valueOf(1), gridRange.getStartColumnIndex());
        Assertions.assertEquals(Integer.valueOf(2), gridRange.getEndColumnIndex());
    }

    @Test
    public void testBarChartWithMultipleSeries() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GoogleChart model = new GoogleChart();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setTitle("SyndesisBars");
        model.setSheetId(0);

        GoogleChart.BasicChart basicChart = new GoogleChart.BasicChart();
        basicChart.setAxisTitleBottom("Product Names");
        basicChart.setAxisTitleLeft("Sales Numbers");

        basicChart.setDomainRange("A1:A10");
        basicChart.setDataRange("B1:B10,C1:C10");

        model.setBasicChart(basicChart);

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();
        Assertions.assertTrue(addChartRequest.getChart().getPosition().getNewSheet());
        BasicChartSpec chartSpec = addChartRequest.getChart().getSpec().getBasicChart();
        Assertions.assertNotNull(chartSpec);
        Assertions.assertEquals(2, chartSpec.getSeries().size());

        Assertions.assertEquals("LEFT_AXIS", chartSpec.getSeries().get(0).getTargetAxis());
        BasicChartSeries basicChartSeries = chartSpec.getSeries().get(0);
        Assertions.assertEquals(1, basicChartSeries.getSeries().getSourceRange().getSources().size());
        GridRange gridRange = basicChartSeries.getSeries().getSourceRange().getSources().get(0);
        Assertions.assertEquals(Integer.valueOf(0), gridRange.getStartRowIndex());
        Assertions.assertEquals(Integer.valueOf(10), gridRange.getEndRowIndex());
        Assertions.assertEquals(Integer.valueOf(1), gridRange.getStartColumnIndex());
        Assertions.assertEquals(Integer.valueOf(2), gridRange.getEndColumnIndex());

        Assertions.assertEquals("LEFT_AXIS", chartSpec.getSeries().get(1).getTargetAxis());
        basicChartSeries = chartSpec.getSeries().get(1);
        Assertions.assertEquals(1, basicChartSeries.getSeries().getSourceRange().getSources().size());
        gridRange = basicChartSeries.getSeries().getSourceRange().getSources().get(0);
        Assertions.assertEquals(Integer.valueOf(0), gridRange.getStartRowIndex());
        Assertions.assertEquals(Integer.valueOf(10), gridRange.getEndRowIndex());
        Assertions.assertEquals(Integer.valueOf(2), gridRange.getStartColumnIndex());
        Assertions.assertEquals(Integer.valueOf(3), gridRange.getEndColumnIndex());
    }

    @Test
    public void testPieChart() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GoogleChart model = new GoogleChart();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setTitle("SyndesisPie");
        model.setSheetId(0);

        GoogleChart.PieChart pieChart = new GoogleChart.PieChart();

        pieChart.setDomainRange("A1:A5");
        pieChart.setDataRange("B1:B5");

        model.setPieChart(pieChart);

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();
        PieChartSpec chartSpec = addChartRequest.getChart().getSpec().getPieChart();
        Assertions.assertNotNull(chartSpec);
        Assertions.assertEquals("RIGHT_LEGEND", chartSpec.getLegendPosition());
        Assertions.assertEquals(1, chartSpec.getSeries().size());

        ChartData domain = chartSpec.getDomain();
        Assertions.assertEquals(1, domain.getSourceRange().getSources().size());
        GridRange domainRange = domain.getSourceRange().getSources().get(0);
        Assertions.assertEquals(Integer.valueOf(0), domainRange.getStartRowIndex());
        Assertions.assertEquals(Integer.valueOf(5), domainRange.getEndRowIndex());
        Assertions.assertEquals(Integer.valueOf(0), domainRange.getStartColumnIndex());
        Assertions.assertEquals(Integer.valueOf(1), domainRange.getEndColumnIndex());

        ChartData data = chartSpec.getSeries();
        Assertions.assertEquals(1, data.getSourceRange().getSources().size());
        GridRange dataRange = data.getSourceRange().getSources().get(0);
        Assertions.assertEquals(Integer.valueOf(0), dataRange.getStartRowIndex());
        Assertions.assertEquals(Integer.valueOf(5), dataRange.getEndRowIndex());
        Assertions.assertEquals(Integer.valueOf(1), dataRange.getStartColumnIndex());
        Assertions.assertEquals(Integer.valueOf(2), dataRange.getEndColumnIndex());
    }
}

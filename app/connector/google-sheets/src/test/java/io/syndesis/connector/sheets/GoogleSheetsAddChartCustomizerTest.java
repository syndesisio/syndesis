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

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("batchUpdate", ConnectorOptions.extractOption(options, "methodName"));

        Assert.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
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

        Assert.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assert.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();
        EmbeddedChart chart = addChartRequest.getChart();
        Assert.assertEquals("SyndesisChart", chart.getSpec().getTitle());
        Assert.assertEquals("Some subtitle", chart.getSpec().getSubtitle());
        Assert.assertEquals(Integer.valueOf(1), chart.getPosition().getOverlayPosition().getAnchorCell().getSheetId());
        Assert.assertEquals(Integer.valueOf(1), chart.getPosition().getOverlayPosition().getAnchorCell().getRowIndex());
        Assert.assertEquals(Integer.valueOf(3), chart.getPosition().getOverlayPosition().getAnchorCell().getColumnIndex());

        BasicChartSpec chartSpec = chart.getSpec().getBasicChart();
        Assert.assertNotNull(chartSpec);
        Assert.assertEquals(2, chartSpec.getAxis().size());
        Assert.assertEquals("Product Names", chartSpec.getAxis().get(0).getTitle());
        Assert.assertEquals("BOTTOM_AXIS", chartSpec.getAxis().get(0).getPosition());
        Assert.assertEquals("Sales Numbers", chartSpec.getAxis().get(1).getTitle());
        Assert.assertEquals("LEFT_AXIS", chartSpec.getAxis().get(1).getPosition());

        Assert.assertEquals(1, chartSpec.getDomains().size());
        Assert.assertEquals(1, chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().size());
        Assert.assertEquals(Integer.valueOf(0), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getStartRowIndex());
        Assert.assertEquals(Integer.valueOf(10), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getEndRowIndex());
        Assert.assertEquals(Integer.valueOf(0), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getStartColumnIndex());
        Assert.assertEquals(Integer.valueOf(1), chartSpec.getDomains().get(0).getDomain().getSourceRange().getSources().get(0).getEndColumnIndex());

        Assert.assertEquals(1, chartSpec.getSeries().size());

        Assert.assertEquals("LEFT_AXIS", chartSpec.getSeries().get(0).getTargetAxis());
        BasicChartSeries basicChartSeries = chartSpec.getSeries().get(0);
        Assert.assertEquals(1, basicChartSeries.getSeries().getSourceRange().getSources().size());
        GridRange gridRange = basicChartSeries.getSeries().getSourceRange().getSources().get(0);
        Assert.assertEquals(Integer.valueOf(0), gridRange.getStartRowIndex());
        Assert.assertEquals(Integer.valueOf(10), gridRange.getEndRowIndex());
        Assert.assertEquals(Integer.valueOf(1), gridRange.getStartColumnIndex());
        Assert.assertEquals(Integer.valueOf(2), gridRange.getEndColumnIndex());
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
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();
        Assert.assertTrue(addChartRequest.getChart().getPosition().getNewSheet());
        BasicChartSpec chartSpec = addChartRequest.getChart().getSpec().getBasicChart();
        Assert.assertNotNull(chartSpec);
        Assert.assertEquals(2, chartSpec.getSeries().size());

        Assert.assertEquals("LEFT_AXIS", chartSpec.getSeries().get(0).getTargetAxis());
        BasicChartSeries basicChartSeries = chartSpec.getSeries().get(0);
        Assert.assertEquals(1, basicChartSeries.getSeries().getSourceRange().getSources().size());
        GridRange gridRange = basicChartSeries.getSeries().getSourceRange().getSources().get(0);
        Assert.assertEquals(Integer.valueOf(0), gridRange.getStartRowIndex());
        Assert.assertEquals(Integer.valueOf(10), gridRange.getEndRowIndex());
        Assert.assertEquals(Integer.valueOf(1), gridRange.getStartColumnIndex());
        Assert.assertEquals(Integer.valueOf(2), gridRange.getEndColumnIndex());

        Assert.assertEquals("LEFT_AXIS", chartSpec.getSeries().get(1).getTargetAxis());
        basicChartSeries = chartSpec.getSeries().get(1);
        Assert.assertEquals(1, basicChartSeries.getSeries().getSourceRange().getSources().size());
        gridRange = basicChartSeries.getSeries().getSourceRange().getSources().get(0);
        Assert.assertEquals(Integer.valueOf(0), gridRange.getStartRowIndex());
        Assert.assertEquals(Integer.valueOf(10), gridRange.getEndRowIndex());
        Assert.assertEquals(Integer.valueOf(2), gridRange.getStartColumnIndex());
        Assert.assertEquals(Integer.valueOf(3), gridRange.getEndColumnIndex());
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
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        AddChartRequest addChartRequest = batchUpdateRequest.getRequests().get(0).getAddChart();
        PieChartSpec chartSpec = addChartRequest.getChart().getSpec().getPieChart();
        Assert.assertNotNull(chartSpec);
        Assert.assertEquals("RIGHT_LEGEND", chartSpec.getLegendPosition());
        Assert.assertEquals(1, chartSpec.getSeries().size());

        ChartData domain = chartSpec.getDomain();
        Assert.assertEquals(1, domain.getSourceRange().getSources().size());
        GridRange domainRange = domain.getSourceRange().getSources().get(0);
        Assert.assertEquals(Integer.valueOf(0), domainRange.getStartRowIndex());
        Assert.assertEquals(Integer.valueOf(5), domainRange.getEndRowIndex());
        Assert.assertEquals(Integer.valueOf(0), domainRange.getStartColumnIndex());
        Assert.assertEquals(Integer.valueOf(1), domainRange.getEndColumnIndex());

        ChartData data = chartSpec.getSeries();
        Assert.assertEquals(1, data.getSourceRange().getSources().size());
        GridRange dataRange = data.getSourceRange().getSources().get(0);
        Assert.assertEquals(Integer.valueOf(0), dataRange.getStartRowIndex());
        Assert.assertEquals(Integer.valueOf(5), dataRange.getEndRowIndex());
        Assert.assertEquals(Integer.valueOf(1), dataRange.getStartColumnIndex());
        Assert.assertEquals(Integer.valueOf(2), dataRange.getEndColumnIndex());
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.api.services.sheets.v4.model.AddChartRequest;
import com.google.api.services.sheets.v4.model.BasicChartAxis;
import com.google.api.services.sheets.v4.model.BasicChartDomain;
import com.google.api.services.sheets.v4.model.BasicChartSeries;
import com.google.api.services.sheets.v4.model.BasicChartSpec;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.ChartData;
import com.google.api.services.sheets.v4.model.ChartSourceRange;
import com.google.api.services.sheets.v4.model.ChartSpec;
import com.google.api.services.sheets.v4.model.EmbeddedChart;
import com.google.api.services.sheets.v4.model.EmbeddedObjectPosition;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.OverlayPosition;
import com.google.api.services.sheets.v4.model.PieChartSpec;
import com.google.api.services.sheets.v4.model.Request;
import io.syndesis.connector.sheets.model.CellCoordinate;
import io.syndesis.connector.sheets.model.GoogleChart;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsAddChartCustomizer implements ComponentProxyCustomizer {

    private String spreadsheetId;
    private String title;
    private String subtitle;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        spreadsheetId = (String) options.get("spreadsheetId");
        title = (String) options.get("title");
        subtitle = (String) options.get("subtitle");

        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName());
        options.put("methodName", "batchUpdate");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final GoogleChart model = exchange.getIn().getBody(GoogleChart.class);

        if (model != null) {
            if (ObjectHelper.isNotEmpty(model.getSpreadsheetId())) {
                spreadsheetId = model.getSpreadsheetId();
            }
            if (ObjectHelper.isNotEmpty(model.getTitle())) {
                title = model.getTitle();
            }
            if (ObjectHelper.isNotEmpty(model.getSubtitle())) {
                subtitle = model.getSubtitle();
            }
        }

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setIncludeSpreadsheetInResponse(true);
        batchUpdateRequest.setRequests(new ArrayList<>());

        AddChartRequest addChartRequest = new AddChartRequest();
        batchUpdateRequest.getRequests().add(new Request().setAddChart(addChartRequest));

        ChartSpec chartSpec = createChartSpec();
        EmbeddedChart embeddedChart = createEmbeddedChart(model, chartSpec);
        addChartRequest.setChart(embeddedChart);

        if (model != null) {
            if (model.getBasicChart() != null) {
                addBasicChart(chartSpec, model);
            } else if (model.getPieChart() != null) {
                addPieChart(chartSpec, model);
            }
        }

        in.setHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID, spreadsheetId);
        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest", batchUpdateRequest);
    }

    private ChartSpec createChartSpec() {
        ChartSpec chartSpec = new ChartSpec();
        if (ObjectHelper.isNotEmpty(title)) {
            chartSpec.setTitle(title);
        }
        if (ObjectHelper.isNotEmpty(subtitle)) {
            chartSpec.setSubtitle(subtitle);
        }
        return chartSpec;
    }

    private EmbeddedChart createEmbeddedChart(GoogleChart model, ChartSpec chartSpec) {
        EmbeddedChart embeddedChart = new EmbeddedChart();

        EmbeddedObjectPosition position = new EmbeddedObjectPosition();

        if (model != null && ObjectHelper.isNotEmpty(model.getOverlayPosition())) {
            CellCoordinate cellCoordinate = CellCoordinate.fromCellId(model.getOverlayPosition());
            position.setOverlayPosition(new OverlayPosition().setAnchorCell(new GridCoordinate()
                                                                                .setSheetId(Optional.ofNullable(model.getSheetId()).orElse(0))
                                                                                .setColumnIndex(cellCoordinate.getColumnIndex())
                                                                                .setRowIndex(cellCoordinate.getRowIndex())));
        } else if (model != null && ObjectHelper.isNotEmpty(model.getSheetId())) {
            position.setSheetId(model.getSheetId());
        } else {
            position.setNewSheet(true);
        }

        embeddedChart.setPosition(position);

        embeddedChart.setSpec(chartSpec);
        return embeddedChart;
    }

    private void addPieChart(ChartSpec chartSpec, GoogleChart model) {
        Integer defaultSheetId = Optional.ofNullable(model.getSheetId()).orElse(0);
        GoogleChart.PieChart pieChart = model.getPieChart();

        PieChartSpec pieChartSpec = new PieChartSpec();
        pieChartSpec.setLegendPosition(pieChart.getLegendPosition());
        pieChartSpec.setDomain(new ChartData().setSourceRange(getDomainSourceRange(Optional.ofNullable(model.getSourceSheetId()).orElse(defaultSheetId), pieChart.getDomainRange())));

        ChartSourceRange sourceRange = new ChartSourceRange();
        GridRange gridRange = new GridRange();
        gridRange.setSheetId(Optional.ofNullable(model.getSourceSheetId()).orElse(defaultSheetId));
        RangeCoordinate coordinates = RangeCoordinate.fromRange(pieChart.getDataRange());
        gridRange.setStartRowIndex(coordinates.getRowStartIndex());
        gridRange.setEndRowIndex(coordinates.getRowEndIndex());
        gridRange.setStartColumnIndex(coordinates.getColumnStartIndex());
        gridRange.setEndColumnIndex(coordinates.getColumnEndIndex());
        sourceRange.setSources(Collections.singletonList(gridRange));

        pieChartSpec.setSeries(new ChartData().setSourceRange(sourceRange));

        chartSpec.setPieChart(pieChartSpec);
    }

    private void addBasicChart(ChartSpec chartSpec, GoogleChart model) {
        GoogleChart.BasicChart basicChart = model.getBasicChart();

        BasicChartSpec basicChartSpec = new BasicChartSpec();
        basicChartSpec.setHeaderCount(1);

        BasicChartAxis bottomAxis = new BasicChartAxis();
        bottomAxis.setPosition("BOTTOM_AXIS");
        bottomAxis.setTitle(Optional.ofNullable(basicChart.getAxisTitleBottom())
                .orElse("X-Axis"));

        BasicChartAxis leftAxis = new BasicChartAxis();
        leftAxis.setPosition("LEFT_AXIS");
        leftAxis.setTitle(Optional.ofNullable(basicChart.getAxisTitleLeft())
                .orElse("Y-Axis"));
        basicChartSpec.setAxis(Arrays.asList(bottomAxis, leftAxis));

        BasicChartDomain chartDomain = new BasicChartDomain();

        Integer defaultSheetId = Optional.ofNullable(model.getSheetId()).orElse(0);

        chartDomain.setDomain(new ChartData().setSourceRange(getDomainSourceRange(Optional.ofNullable(model.getSourceSheetId()).orElse(defaultSheetId), basicChart.getDomainRange())));
        basicChartSpec.setDomains(Collections.singletonList(chartDomain));
        basicChartSpec.setChartType(basicChart.getType());

        List<BasicChartSeries> series = new ArrayList<>();
        String dataRange = Optional.ofNullable(basicChart.getDataRange()).orElse("");
        Stream.of(dataRange.split(",", -1)).forEach(range -> {
            ChartSourceRange sourceRange = new ChartSourceRange();
            GridRange gridRange = new GridRange();
            gridRange.setSheetId(Optional.ofNullable(model.getSourceSheetId()).orElse(defaultSheetId));
            RangeCoordinate coordinate = RangeCoordinate.fromRange(range);
            gridRange.setStartRowIndex(coordinate.getRowStartIndex());
            gridRange.setEndRowIndex(coordinate.getRowEndIndex());
            gridRange.setStartColumnIndex(coordinate.getColumnStartIndex());
            gridRange.setEndColumnIndex(coordinate.getColumnEndIndex());
            sourceRange.setSources(Collections.singletonList(gridRange));

            BasicChartSeries basicChartSeries = new BasicChartSeries();
            basicChartSeries.setTargetAxis("LEFT_AXIS");
            basicChartSeries.setSeries(new ChartData().setSourceRange(sourceRange));
            series.add(basicChartSeries);
        });

        basicChartSpec.setSeries(series);
        chartSpec.setBasicChart(basicChartSpec);
    }

    private ChartSourceRange getDomainSourceRange(int sourceSheetId, String domainRange) {
        ChartSourceRange domainSourceRange = new ChartSourceRange();
        GridRange domainGridRange = new GridRange();
        domainGridRange.setSheetId(sourceSheetId);
        RangeCoordinate coordinate = RangeCoordinate.fromRange(domainRange);
        domainGridRange.setStartRowIndex(coordinate.getRowStartIndex());
        domainGridRange.setEndRowIndex(coordinate.getRowEndIndex());
        domainGridRange.setStartColumnIndex(coordinate.getRowStartIndex());
        domainGridRange.setEndColumnIndex(coordinate.getColumnEndIndex());
        domainSourceRange.setSources(Collections.singletonList(domainGridRange));
        return domainSourceRange;
    }
}

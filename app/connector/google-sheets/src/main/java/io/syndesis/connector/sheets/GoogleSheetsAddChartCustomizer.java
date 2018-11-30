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
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import io.syndesis.connector.sheets.model.GoogleChart;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsAddChartCustomizer implements ComponentProxyCustomizer {

    private String spreadsheetId;
    private Integer sheetId;
    private String title;
    private String subtitle;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        spreadsheetId = (String) options.get("spreadsheetId");
        sheetId = (Integer) options.get("sheetId");
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
            if (ObjectHelper.isNotEmpty(model.getSheetId())) {
                sheetId = model.getSheetId();
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
        EmbeddedChart embeddedChart = createEmbeddedChart(chartSpec);
        addChartRequest.setChart(embeddedChart);

        if (model != null && model.getBasicChart() != null) {
            addBasicChart(chartSpec, model);
        }

        in.setHeader("CamelGoogleSheets.spreadsheetId", spreadsheetId);
        in.setHeader("CamelGoogleSheets.batchUpdateSpreadsheetRequest", batchUpdateRequest);
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

    private EmbeddedChart createEmbeddedChart(ChartSpec chartSpec) {
        EmbeddedChart embeddedChart = new EmbeddedChart();

        embeddedChart.setPosition(Optional.ofNullable(sheetId)
                .map(id -> new EmbeddedObjectPosition().setSheetId(id))
                .orElse(new EmbeddedObjectPosition().setNewSheet(true)));


        embeddedChart.setSpec(chartSpec);
        return embeddedChart;
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
        ChartSourceRange domainSource = new ChartSourceRange();
        GridRange domainGridRange = new GridRange();
        domainGridRange.setSheetId(Optional.ofNullable(basicChart.getDomainSource().getSheetId())
                .orElse(model.getSheetId()));
        domainGridRange.setStartRowIndex(basicChart.getDomainSource().getFromRow());
        domainGridRange.setEndRowIndex(basicChart.getDomainSource().getToRow());
        domainGridRange.setStartColumnIndex(basicChart.getDomainSource().getColumnIndex());
        domainGridRange.setEndColumnIndex(basicChart.getDomainSource().getColumnIndex() + 1);
        domainSource.setSources(Collections.singletonList(domainGridRange));

        chartDomain.setDomain(new ChartData().setSourceRange(domainSource));
        basicChartSpec.setDomains(Collections.singletonList(chartDomain));
        basicChartSpec.setChartType(basicChart.getType());

        List<BasicChartSeries> series = new ArrayList<>();
        basicChart.getDataSources().forEach(dataSource -> {
            ChartSourceRange sourceRange = new ChartSourceRange();
            GridRange gridRange = new GridRange();
            gridRange.setSheetId(Optional.ofNullable(dataSource.getSheetId())
                    .orElse(model.getSheetId()));
            gridRange.setStartRowIndex(dataSource.getFromRow());
            gridRange.setEndRowIndex(dataSource.getToRow());
            gridRange.setStartColumnIndex(dataSource.getColumnIndex());
            gridRange.setEndColumnIndex(dataSource.getColumnIndex() + 1);
            sourceRange.setSources(Collections.singletonList(gridRange));

            BasicChartSeries basicChartSeries = new BasicChartSeries();
            basicChartSeries.setTargetAxis(dataSource.getTargetAxis());
            basicChartSeries.setSeries(new ChartData().setSourceRange(sourceRange));
            series.add(basicChartSeries);
        });

        basicChartSpec.setSeries(series);
        chartSpec.setBasicChart(basicChartSpec);
    }
}

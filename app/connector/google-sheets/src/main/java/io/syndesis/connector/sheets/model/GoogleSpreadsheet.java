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
package io.syndesis.connector.sheets.model;

import java.util.List;

public class GoogleSpreadsheet {

    private String spreadsheetId;
    private String title;
    private String timeZone;
    private String locale;

    private String url;

    private List<GoogleSheet> sheets;

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    /**
     * Specifies the spreadsheetId.
     *
     * @param spreadsheetId
     */
    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Specifies the title.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Specifies the url.
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Specifies the timeZone.
     *
     * @param timeZone
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getLocale() {
        return locale;
    }

    /**
     * Specifies the locale.
     *
     * @param locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<GoogleSheet> getSheets() {
        return sheets;
    }

    /**
     * Specifies the sheets.
     *
     * @param sheets
     */
    public void setSheets(List<GoogleSheet> sheets) {
        this.sheets = sheets;
    }

    @Override
    public String toString() {
        return String.format("%s [spreadsheetId=%s, title=%s, url=%s, sheets=%s]", GoogleSpreadsheet.class.getSimpleName(), spreadsheetId, title, url, sheets);
    }

}

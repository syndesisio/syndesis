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
package org.apache.camel.component.google.sheets.stream;

/**
 * Constants used in Camel Google Sheets Stream
 */
@SuppressWarnings("PMD.ConstantsInInterface")
public interface GoogleSheetsStreamConstants {

    String PROPERTY_PREFIX = "CamelGoogleSheets";

    String SPREADSHEET_ID =  PROPERTY_PREFIX + "SpreadsheetId";
    String SPREADSHEET_URL =  PROPERTY_PREFIX + "SpreadsheetUrl";
    String MAJOR_DIMENSION = PROPERTY_PREFIX + "MajorDimension";
    String RANGE = PROPERTY_PREFIX + "Range";
}

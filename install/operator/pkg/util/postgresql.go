/*
 * Copyright (C) 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util

import (
	"database/sql"
	"fmt"
	"regexp"
	"strconv"

	_ "github.com/lib/pq"
)

var versionMatch = regexp.MustCompile(`^\d+\.\d+`)

// PostgreSQLVersionAt determines the version of a PotgreSQL database running at hostname and port
func PostgreSQLVersionAt(username string, password string, database string, hostname string, port int) (float64, error) {
	log.Info(fmt.Sprintf("Connecting to PostgreSQL server running at %s:%d", hostname, port))
	db, err := sql.Open("postgres", fmt.Sprintf("postgres://%s:%s@%s:%d/%s?sslmode=disable", username, password, hostname, port, database))
	if err != nil {
		return 0, err
	}
	defer db.Close()

	return fetchVersion(db)
}

func fetchVersion(db *sql.DB) (float64, error) {
	var version string
	err := db.QueryRow("SHOW server_version").Scan(&version)
	if err != nil {
		return 0, err
	}

	majorMinor := versionMatch.FindString(version)
	return strconv.ParseFloat(majorMinor, 64)
}

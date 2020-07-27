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
	"context"
	"fmt"
	"testing"
	"time"

	"github.com/DATA-DOG/go-sqlmock"
	"github.com/docker/go-connections/nat"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

func Test_fetchVersion(t *testing.T) {
	db, mock, err := sqlmock.New()
	if err != nil {
		t.Fatalf("an error '%s' was not expected when opening a stub database connection", err)
	}
	defer db.Close()

	mock.ExpectQuery("SHOW server_version").WillReturnRows(sqlmock.NewRows([]string{"current_setting"}).AddRow("10.6 (Debian 10.6-1.pgdg90+1)"))

	version, err := fetchVersion(db)
	if err != nil {
		t.Fatalf("an error '%s' was not expected when fetching version from the database", err)
	}

	if version != 10.6 {
		t.Errorf("Expected version 10.6 got %f", version)
	}
}

func Test_PostgreSQLVersionAt(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping test in short mode.")
	}

	tests := []struct {
		image   string
		version float64
	}{
		{"centos/postgresql-96-centos7", 9.6},
		{"postgres:10.6", 10.6},
	}

	ctx := context.Background()

	dbURL := func(port nat.Port) string {
		return fmt.Sprintf("postgres://syndesis:password@localhost:%s/syndesis?sslmode=disable", port.Port())
	}

	for _, test := range tests {

		req := testcontainers.ContainerRequest{
			Image:        test.image,
			ExposedPorts: []string{"5432/tcp"},
			AutoRemove:   false,
			Env: map[string]string{
				"POSTGRES_USER":       "syndesis",
				"POSTGRESQL_USER":     "syndesis",
				"POSTGRES_PASSWORD":   "password",
				"POSTGRESQL_PASSWORD": "password",
				"POSTGRES_DB":         "syndesis",
				"POSTGRESQL_DATABASE": "syndesis",
			},
			WaitingFor: wait.ForSQL(nat.Port("5432/tcp"), "postgres", dbURL).Timeout(time.Second * 15),
		}
		postgres, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
			ContainerRequest: req,
			Started:          true,
		})
		if err != nil {
			t.Error(err)
		}
		defer postgres.Terminate(ctx)

		port, err := postgres.MappedPort(ctx, "5432/tcp")
		if err != nil {
			t.Error(err)
		}

		url := fmt.Sprintf("postgresql://localhost:%s", port)

		version, err := PostgreSQLVersionAt("syndesis", "password", "syndesis", url)

		if err != nil {
			t.Fatalf("an error '%s' was not expected when fetching version from the database", err)
		}

		if version != test.version {
			t.Errorf("Expected version %f got %f", test.version, version)
		}
	}
}

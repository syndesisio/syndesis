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

package upgrade

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

type stepTestOk struct{ step }

func (stepTestOk) run() (err error)      { return nil }
func (stepTestOk) rollback() (err error) { return nil }
func (stepTestOk) infoRun()              {}
func (stepTestOk) infoRollback()         {}

type stepTestFail struct{ step }

func (stepTestFail) run() (err error)      { return fmt.Errorf("") }
func (stepTestFail) rollback() (err error) { return nil }
func (stepTestFail) infoRun()              {}
func (stepTestFail) infoRollback()         {}

func TestUpgrade_InstallFailed(t *testing.T) {
	u := &upgrade{attempts: []result{}}
	u.InstallFailed()
	assert.NotEmpty(t, u.attempts)
	assert.IsType(t, install{}, u.attempts[0].step())
}

func TestUpgrade_Upgrade(t *testing.T) {
	// Test successful upgrade
	u := &upgrade{
		steps:    []stepRunner{stepTestOk{}, stepTestOk{}},
		attempts: []result{},
	}

	err := u.Upgrade()
	assert.NoError(t, err)
	assert.NotEmpty(t, u.attempts)
	assert.IsType(t, succeed{}, u.attempts[0])

	// Test failed upgrade
	u = &upgrade{
		steps:    []stepRunner{stepTestOk{}, stepTestFail{}, stepTestOk{}},
		attempts: []result{},
	}
	err = u.Upgrade()
	assert.Error(t, err)
	assert.NotEmpty(t, u.attempts)
	assert.IsType(t, failure{}, u.attempts[0])
	assert.IsType(t, stepTestFail{}, u.attempts[0].step())
}

func TestUpgrade_Rollback(t *testing.T) {
	u := &upgrade{
		steps:    []stepRunner{stepTestOk{}, stepTestFail{}, stepTestOk{}},
		attempts: []result{},
	}
	err := u.Upgrade()
	assert.Error(t, err)

	err = u.Rollback()
	assert.NoError(t, err)
	assert.Empty(t, u.attempts)
}

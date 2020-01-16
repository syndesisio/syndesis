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
	"testing"

	"github.com/stretchr/testify/assert"
)

func Test_scale_down(t *testing.T) {
	for _, s := range []scale{scale{dir: up}, scale{dir: down}} {
		s.down()
		assert.EqualValues(t, down, s.dir)
		assert.EqualValues(t, "Scale down", s.name)
	}
}

func Test_scale_up(t *testing.T) {
	for _, s := range []scale{scale{dir: up}, scale{dir: down}} {
		s.up()
		assert.EqualValues(t, up, s.dir)
		assert.EqualValues(t, "Scale up", s.name)
	}
}

func Test_scale_dirToS(t *testing.T) {
	assert.EqualValues(t, "up", dirToS(up))
	assert.EqualValues(t, "down", dirToS(down))
}

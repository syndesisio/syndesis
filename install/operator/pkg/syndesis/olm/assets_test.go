package olm_test

import (
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
)

func TestAssetsReadFSDir(t *testing.T) {

	testCases := []struct {
		name   string
		path   string
		expect int
	}{
		{
			"community",
			"assets/community",
			1,
		},
		{
			"productized",
			"assets/productized",
			1,
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			files, err := olm.ReadDir(tc.path)
			require.NoError(t, err)
			assert.Equal(t, len(files), tc.expect)
		})
	}
}

func TestAssetsRead(t *testing.T) {

	testCases := []struct {
		name        string
		path        string
		hasError    bool
		expectStart string
	}{
		{
			"community",
			"assets/community",
			true, "",
		},
		{
			"productized",
			"assets/productized",
			true, "",
		},
		{
			"community-description",
			"assets/community/description",
			false,
			"### Syndesis operator",
		},
		{
			"productized-description",
			"assets/productized/description",
			false,
			"Fuse Online is a flexible",
		},
		{
			"alm-examples",
			"assets/alm-examples",
			false,
			"[{",
		},
		{
			"icon",
			"assets/icon",
			false,
			"PHN2ZyBpZ",
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			f, err := olm.Read(tc.path)
			if !tc.hasError {
				require.NoError(t, err)
				assert.True(t, strings.HasPrefix(f, tc.expectStart))
			} else {
				assert.NotNil(t, err)
			}
		})
	}
}

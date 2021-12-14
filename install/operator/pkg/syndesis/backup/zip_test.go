package backup

import (
	"testing"
)

func Test_unzip(t *testing.T) {
	b := Backup{}

	tmpdir := t.TempDir()

	if err := b.unzip("test_evilbackup.zip", tmpdir); err == nil {
		t.Errorf("unzip not error on evil backup")
	}
}

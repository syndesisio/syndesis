// +build windows

package sh

import (
	"os"
)

// Exec tries to emulate the posix exec system call to execute the sh.Sh command. Exec panics if called on a sh.Sh
// that has the default Stdout, Stderr, or Stdin changed.  Returns an error if the process cannot be executed.
// When the process exits, Exec calls os.Exit() with the exit status of the executed process.
func (sh *Sh) Exec() (err error) {
	if sh.stdout != os.Stdout {
		panic("sh.Stdout and sh.Exec cannot be used on the same command") // easier to find invalid usage.
	}
	if sh.stderr != os.Stderr {
		panic("sh.Stderr and sh.Exec cannot be used on the same command") // easier to find invalid usage.
	}
	if sh.stdin != os.Stdin {
		panic("sh.Stdin and sh.Exec cannot be used on the same command") // easier to find invalid usage.
	}

	os.Exit(sh.ExitCode())
	return nil
}

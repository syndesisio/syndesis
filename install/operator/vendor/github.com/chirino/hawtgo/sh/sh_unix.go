// +build !windows

package sh

import (
	"fmt"
	"os"
	"syscall"
)

// Exec uses the exec system call to execute the sh.Sh command. When Exec is called the previous go
// process is replaced by the executed command. Exec panics if called on a sh.Sh that has the
// default Stdout, Stderr, or Stdin changed.  Returns an error if the process cannot be executed.
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
	c := sh.Cmd()
	if sh.commandLog != nil {
		fmt.Fprintf(sh.commandLog, "%s%s\n", sh.commandLogPrefix, sh.String())
	}
	return syscall.Exec(c.Path, c.Args, c.Env)
}

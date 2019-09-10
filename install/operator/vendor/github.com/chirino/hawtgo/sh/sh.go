/*
 * Copyright (C) 2018 Red Hat, Inc.
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

//
// Package sh helps you to more easily execute processes.
package sh

import (
	"bytes"
	"fmt"
	"github.com/chirino/hawtgo/sh/line"
	magesh "github.com/magefile/mage/sh"
	"io"
	"os"
	"os/exec"
	"regexp"
	"strings"
)

var needsQuote = regexp.MustCompile(`'|"| |\t|\r|\n`)

/////////////////////////////////////////////////////////////////////////
//
// Expander related bits..
//
/////////////////////////////////////////////////////////////////////////

// An Expander is used to expand/resolve a variable name to a value
type Expander interface {
	// Expand retrieves the value of the variable named
	// by the key. If the variable is found the
	// value (which may be empty) is returned and the boolean is true.
	// Otherwise the returned value will be empty and the boolean will
	// be false.
	Expand(key string) (value string, ok bool)
}

// Expand replaces ${var} or $var in the string based on the Expander.
func Expand(value string, expander Expander) string {
	return os.Expand(value, func(v string) string {
		if v, ok := expander.Expand(v); ok {
			return v
		}
		return ""
	})
}

// ExpandNotFound returns an Expander that never finds the value
// being expanded.
func ExpandNotFound() Expander {
	return notFound(1)
}

type notFound byte

func (notFound) Expand(key string) (string, bool) {
	return "", false
}

// ExpandDisabled returns an Expander that evaluates to the same string
// that describes the expansion.
func ExpandDisabled() Expander {
	return expandDisabled(2)
}

type expandDisabled byte

func (expandDisabled) Expand(key string) (string, bool) {
	return "${" + key + "}", true
}

// ExpandEnv returns an Expander that expands values from the
// operating system environment.
func ExpandEnv() Expander {
	return expandEnv(3)
}

type expandEnv byte

func (expandEnv) Expand(key string) (string, bool) {
	return os.LookupEnv(key)
}

// ExpandPanic returns an Expander that panics when used.
func ExpandPanic() Expander {
	return expandPanic(4)
}

type expandPanic byte

func (expandPanic) Expand(key string) (string, bool) {
	panic(fmt.Errorf("can not find value to expand '${%s}'", key))
}

// ExpandMap returns an Expander that values found found in the map.
func ExpandMap(m map[string]string) Expander {
	return expandMap(m)
}

type expandMap map[string]string

func (m expandMap) Expand(key string) (string, bool) {
	v, ok := m[key]
	return v, ok
}

// Expanders creates an Expander that expands using
// the provided list of Expanders in order.
//
// You can use this to customize how key not found scenarios are handled.
// If you want to panic if the key is not found in the OS Env you could
// build that expander like:
//
// exp := ChainExpanders(ExpandEnv(), ExpandPanic())
//
func ChainExpanders(v ...Expander) Expander {
	return expanders(v)
}

type expanders []Expander

func (next expanders) Expand(key string) (string, bool) {
	for _, f := range next {
		if v, ok := f.Expand(key); ok {
			return v, ok
		}
	}
	return "", false
}

/////////////////////////////////////////////////////////////////////////
//
// Sh struct  bits
//
/////////////////////////////////////////////////////////////////////////

// Sh contains all the settings needed to execute process. It is
// guarded by an immutable builder access model.
type Sh struct {
	args             []line.Arg
	expanders        Expander
	env              map[string]string
	dir              string
	commandLog       io.Writer
	commandLogPrefix string
	stdout           io.Writer
	stderr           io.Writer
	stdin            io.Reader
}

// New returns a new sh.Sh
func New() *Sh {
	return &Sh{expanders: ExpandEnv(), stdout: os.Stdout, stderr: os.Stderr, stdin: os.Stdin}
}

// Stdout returns a new sh.Sh configured to write process stdout with the specified writer.
func (this *Sh) Stdout(writer io.Writer) *Sh {
	var sh = *this
	sh.stdout = writer
	return &sh
}

// Stderr returns a new sh.Sh configured to write process stderr with the specified writer.
func (this *Sh) Stderr(writer io.Writer) *Sh {
	var sh = *this
	sh.stderr = writer
	return &sh
}

// Stdin returns a new sh.Sh configured feed process stdin with the specified reader.
func (this *Sh) Stdin(reader io.Reader) *Sh {
	var sh = *this
	sh.stdin = reader
	return &sh
}

// Line returns a new sh.Sh with the command specified as a single command.  The command line is
// parsed into command line arguments.  You can use single and double quotes like you do in bash
// to group command line arguments.  Single quoted strings will have variable expansion disabled.
func (this *Sh) Line(commandLine string) *Sh {
	var sh = *this
	sh.args = line.Parse(commandLine)
	return &sh
}

// Line returns a new sh.Sh with the specified command line arguments.
func (this *Sh) LineArgs(commandLIne ...string) *Sh {
	var sh = *this
	sh.args = make([]line.Arg, len(commandLIne))
	for i, value := range commandLIne {
		arg := line.Arg{}
		arg = append(arg, line.ArgPart{value, true})
		sh.args[i] = arg
	}
	return &sh
}

// Line returns a new sh.Sh configured with an Expander to control variable expansion.
// Use Expand(ExpandDisabled()) to disable expanding variables.
func (this *Sh) Expand(expander Expander) *Sh {
	var sh = *this
	sh.expanders = expander
	return &sh
}

// Line returns a new sh.Sh configured with additional env variables to pass to the executed process
func (this *Sh) Env(env map[string]string) *Sh {
	var sh = *this
	sh.env = env
	return &sh
}

// Dir returns a new sh.Sh configured with the directory to run the executed process.
func (this *Sh) Dir(dir string) *Sh {
	var sh = *this
	sh.dir = dir
	return &sh
}

// CommandLog returns a new sh.Sh configured io.Writer that will receive the fully expanded command when the process is executed.
func (this *Sh) CommandLog(commandLog io.Writer) *Sh {
	var sh = *this
	sh.commandLog = commandLog
	return &sh
}

// CommandLogPrefix returns a new sh.Sh configured with a prfefix to use when logging executed commands.
func (this *Sh) CommandLogPrefix(prefix string) *Sh {
	var sh = *this
	sh.commandLogPrefix = prefix
	return &sh
}

// Cmd returns a new exec.Cmd configured with all the settings collected in the sh.Sh
func (sh *Sh) Cmd() *exec.Cmd {
	args := sh.expandArgs()
	path := ""
	if len(args) >= 1 {
		path = args[0]
		args = args[1:]
	}
	c := exec.Command(path, args...)
	c.Env = os.Environ()

	if sh.env != nil {
		for k, v := range sh.env {
			c.Env = append(c.Env, k+"="+v)
		}
	}
	c.Dir = sh.dir
	c.Stderr = sh.stderr
	c.Stdout = sh.stdout
	c.Stdin = sh.stdin
	return c
}

func (sh *Sh) expandArgs() []string {
	args := make([]string, len(sh.args))

	// Should we expand variables?
	if sh.expanders == ExpandDisabled() {
		for i, value := range sh.args {
			args[i] = value.String()
		}
	} else {
		exp := sh.expanders

		// If there is an env, then lets resolve from that first.
		if sh.env != nil {
			exp = ChainExpanders(ExpandMap(sh.env), sh.expanders)
		}

		for i, value := range sh.args {
			args[i] = value.Expand(exp.Expand)
		}
	}
	return args
}

func (sh *Sh) String() string {
	args := sh.expandArgs()
	t := make([]string, len(args))
	for i, arg := range args {
		if needsQuote.MatchString(arg) {
			arg = strings.ReplaceAll(arg, `\`, `\\`)
			arg = strings.ReplaceAll(arg, "\r", `\r`)
			arg = strings.ReplaceAll(arg, "\n", `\n`)
			arg = strings.ReplaceAll(arg, "\t", `\t`)
			arg = strings.ReplaceAll(arg, `"`, `\"`)
			arg = `"` + arg + `"`
		}
		t[i] = arg
	}
	return strings.Join(t, " ")
}

type OutputOptions struct {
	NoStderr bool
	NoStdout bool
	NoTrim   bool
}

func (sh *Sh) Run() error {
	c := sh.Cmd()
	if sh.commandLog != nil {
		fmt.Fprintf(sh.commandLog, "%s%s\n", sh.commandLogPrefix, sh.String())
	}
	return c.Run()
}

func (sh *Sh) Output(opt ...OutputOptions) (output string, exitCode int, err error) {
	buf := &bytes.Buffer{}
	captureStdout := true
	captureStderr := true
	trim := true
	for _, o := range opt {
		if o.NoStdout {
			captureStdout = false
		}
		if o.NoStderr {
			captureStderr = false
		}
		if o.NoTrim {
			trim = false
		}
	}
	if captureStdout {
		sh = sh.Stdout(buf)
	}
	if captureStderr {
		sh = sh.Stderr(buf)
	}
	if sh.commandLog != nil {
		fmt.Fprintf(sh.commandLog, "%s%s\n", sh.commandLogPrefix, sh.String())
	}
	err = sh.Run()
	output = buf.String()
	exitCode = magesh.ExitStatus(err)
	if trim {
		output = strings.TrimSuffix(output, "\n")
	}
	return
}

// ExitStatus runs the command and returns the process exit code, or 1 if any other error occured.
func (sh *Sh) ExitCode() int {
	return magesh.ExitStatus(sh.Run())
}

// MustExec runs the process and panics if it returns a non zero exit code..
func (sh *Sh) MustZeroExit() {
	rc := sh.ExitCode()
	if rc != 0 {
		panic(fmt.Errorf("<%s> failed: exit code=%d", sh.String(), rc))
	}
}

// MustExec runs the process and panics if it returns a non zero exit code..
func (sh *Sh) MustExec() {
	err := sh.Exec()
	if err != nil {
		panic(fmt.Errorf("<%s>, failed: %s", sh.String(), err))
	}
}

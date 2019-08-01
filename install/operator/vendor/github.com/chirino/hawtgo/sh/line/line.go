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

// Package line lets you parse command line strings into arguments.
package line

import (
    "bufio"
    "hash/adler32"
    "os"
    "strings"
)

/////////////////////////////////////////////////////////////////////
// Tokens
/////////////////////////////////////////////////////////////////////

type token byte

const (
    eofToken token = iota
    wsToken
    charToken
    dquoteToken
    squotToken
    escapeToken
)

/////////////////////////////////////////////////////////////////////
// Lexical Scanner
/////////////////////////////////////////////////////////////////////

var eofRune = rune(0)

type scanner struct {
    r *bufio.Reader
}

func (s *scanner) scan() (tok token, lit rune) {
    // Read the next rune.
    ch, _, err := s.r.ReadRune()
    if err != nil {
        ch = eofRune
    }

    switch ch {
    case eofRune:
        return eofToken, eofRune
    case '\'':
        return squotToken, ch
    case '"':
        return dquoteToken, ch
    case '\\':
        return escapeToken, ch
    case ' ':
        fallthrough
    case '\t':
        fallthrough
    case '\r':
        fallthrough
    case '\n':
        return wsToken, ch
    default:
        return charToken, ch
    }
}

/////////////////////////////////////////////////////////////////////
// Parser
/////////////////////////////////////////////////////////////////////

type parser struct {
    s   *scanner

    unScanned bool
    lastTok   token
    lastLit   rune
}

func (p *parser) scan() (tok token, lit rune) {
    if p.unScanned {
        p.unScanned = false
        return p.lastTok, p.lastLit
    }
    tok, lit = p.s.scan()
    p.lastTok = tok
    p.lastLit = lit
    return
}

func (p *parser) unscan() { p.unScanned = true }

func (p *parser) parseArg() Arg {

    arg := Arg{}
    var done = false
    var current, raw, squoted, dquoted func()

    part := ArgPart{}
    part.CanExpand = true

    partFlushCheck := func(canExpand bool) {
        if part.CanExpand != canExpand {
            if part.String != "" {
                arg = append(arg, part)
                part = ArgPart{}
            }
            part.CanExpand = canExpand
        }
    }

    raw = func() {
        partFlushCheck(true)

        t, l := p.scan()
        switch (t) {
        case wsToken:
            fallthrough
        case eofToken:
            done = true

        case charToken:
            part.String += string(l)
        case escapeToken:
            t2, l2 := p.scan()
            if t2 == eofToken {
                part.String += string(l)
            } else {
                part.String += string(l2)
            }

        case squotToken:
            current = squoted

        case dquoteToken:
            current = dquoted

        }
    }

    dquoted = func() {
        partFlushCheck(true)

        t, l := p.scan()
        switch (t) {
        case eofToken:
            fallthrough
        case dquoteToken:
            current = raw // done...

        case escapeToken:
            t2, l2 := p.scan()
            if t2 == eofToken {
                part.String += string(l)
            } else {
                part.String += string(l2)
            }
        case squotToken:
            fallthrough
        case wsToken:
            fallthrough
        case charToken:
            part.String += string(l)

        }
    }

    squoted = func() {
        partFlushCheck(false)

        t, l := p.scan()
        switch (t) {
        case escapeToken:
            fallthrough
        case wsToken:
            fallthrough
        case dquoteToken:
            fallthrough
        case charToken:
            part.String += string(l)

        case eofToken:
            fallthrough
        case squotToken:
            current = raw

        }
    }

    current = raw;
    for {
        current();
        if done {
            partFlushCheck(!part.CanExpand)
            return arg
        }
    }
}

type ArgPart struct {
    String    string
    CanExpand bool
}

type Arg []ArgPart

func (arg Arg) String() string {
    s := make([]string, len(arg))
    for i, value := range arg {
        s[i] = value.String
    }
    return strings.Join(s, "")
}

func (arg Arg) Expand(expander func(string) (string, bool)) string {
    adler32.New()
    s := make([]string, len(arg))
    for i, value := range arg {
        if (value.CanExpand) {
            s[i] = os.Expand(value.String, func(k string) string {
                if v, ok := expander(k); ok {
                    return v
                }
                return ""
            })
        } else {
            s[i] = value.String
        }
    }
    return strings.Join(s, "")
}

func Parse(line string) []Arg {
    p := &parser{s: &scanner{r: bufio.NewReader(strings.NewReader(line))}}
    args := []Arg{}
    for {
        // Skip to the next arg...
        t, _ := p.scan()
        switch t {
        case wsToken: // ignore it...
        case eofToken:
            return args
        default:
            p.unscan()
            arg := p.parseArg()
            args = append(args, arg)
        }
    }
}

package main

import (
	"bufio"
	"fmt"
	"os"
	"path/filepath"
	"reflect"
	"regexp"
	"sort"
	"strings"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
)

type MySymbol struct {
	myName    string
	myType    string
	myDefault string
	freq      int
}

var KEYWORDS = []string{"Syndesis", "Env", "Tags", "Images"}
var templatePattern, err = regexp.Compile(`{{([\.a-zA-Z_ \"]+?)}}`)
var sortedIndex = make([]string, 0)
var symbolMap = make(map[string]MySymbol)

func main() {

	currDir, err := os.Getwd()
	if err != nil {
		exitOnError(err)
	}

	walkFiles(currDir + "/../../generator/assets/addons")
	walkFiles(currDir + "/../../generator/assets/infrastructure")
	walkFiles(currDir + "/../../generator/assets/install")
	walkFiles(currDir + "/../../generator/assets/route")
	walkFiles(currDir + "/../../generator/assets/upgrade")

	walkFiles(currDir + "/../template")

	sort.Strings(sortedIndex)

	createSyndesisType(symbolMap, sortedIndex)

	for _, k := range sortedIndex {
		fmt.Printf("%d\t\t%s\t\t'%s'\n", symbolMap[k].freq, k, symbolMap[k].myType)
	}
}

func createSyndesisType(symbols map[string]MySymbol, index []string) {
	syndesis := v1alpha1.Syndesis{}
	synType := reflect.TypeOf(syndesis)
	fmt.Printf("Syndesis Type: %s\n", synType.Kind())

	for _, symName := range index {
		if !strings.HasPrefix(symName, "Syndesis") {
			continue // Only do Syndesis object for now
		}

		symType := findType(strings.TrimPrefix(symName, "Syndesis."), synType)

		// Update the symbol map
		mySymbol := symbols[symName]
		mySymbol.myType = symType
		symbols[symName] = mySymbol
	}
}

func findType(symPath string, symType reflect.Type) string {

	pathUnit, symPath := extractUnit(symPath)

	if symType.Kind() == reflect.Map {
		return symType.Key().String() + ", " + symType.Elem().String()
	} else if symType.Kind() == reflect.Struct {
		//
		// symType is a struct so need to look inside at its fields
		//
		field, ok := symType.FieldByName(pathUnit)
		if ok {
			symType = field.Type
		} else {
			fmt.Printf("Err: Failed to find '%s' in type '%s'\n", field.Name, symType.Kind().String())
			return "<unknown>"
		}

		if len(pathUnit) > 0 && pathUnit != symPath {
			// Recurse down to next level
			return findType(symPath, symType)
		}

		//
		// Gone as far down as we can so return
		// the type of the field
		//
		if symType.Kind() == reflect.Ptr {
			return symType.Elem().String()
		} else {
			return symType.Kind().String()
		}

	} else {
		fmt.Printf("Err: Type is not a struct '%s (%T)'\n", symType, symType)
		return "<unknown>"
	}
}

func extractUnit(path string) (string, string) {
	if strings.Contains(path, ".") {
		s := strings.Split(path, ".")
		return s[0], strings.TrimPrefix(path, s[0]+".")
	}

	// unit of path is the same as the total path
	return path, path
}

func scanFile(filePath string) {
	file, err := os.Open(filePath)
	exitOnError(err)
	defer file.Close()

	scanner := bufio.NewScanner(file)

	exitOnError(err)

	for scanner.Scan() {
		line := scanner.Text()

		// Analyse for template-like variables, eg. {{.Syndesis.Spec.x}}
		templateVars(line)
	}
}

func templateVars(text string) {
	if templatePattern.MatchString(text) {
		// fmt.Printf("\nMatched: %s\n", line)

		found := templatePattern.FindAllStringSubmatch(text, -1)
		for _, symbol := range found {
			mySymbol := strings.TrimSpace(symbol[1])

			if !hasKeyword(mySymbol) {
				continue
			}

			if mySymbol == "." || mySymbol == "end" || mySymbol == "else" {
				continue
			}

			// Remove any conditional prefixes
			mySymbol = strings.TrimPrefix(mySymbol, "if ")
			mySymbol = strings.TrimPrefix(mySymbol, "or ")
			mySymbol = strings.TrimPrefix(mySymbol, "range ")

			// Rearrange any function calling symbols
			mySymbol = chkTemplateFunction(mySymbol)

			// Trim leading .
			mySymbol = strings.TrimPrefix(mySymbol, ".")

			// fmt.Printf("Line: %s\n \t\tSymbol: %s\n", line, mySymbol)
			if currSymbol, ok := symbolMap[mySymbol]; ok {
				currSymbol.freq = currSymbol.freq + 1
				symbolMap[mySymbol] = currSymbol
			} else {
				// Add to the map
				newSymbol := MySymbol{}
				newSymbol.myName = mySymbol
				newSymbol.freq = 1
				symbolMap[mySymbol] = newSymbol
				// Add to the sorted index
				sortedIndex = append(sortedIndex, mySymbol)
			}
		}
	}
}

func hasKeyword(text string) bool {
	for _, keyword := range KEYWORDS {
		if strings.Contains(text, keyword+".") {
			return true
		}
	}
	return false
}

func chkTemplateFunction(symbol string) string {
	// FuncMemoryLimit
	if strings.HasPrefix(symbol, generator.FuncMemoryLimit) {
		symbol = strings.TrimPrefix(symbol, generator.FuncMemoryLimit+" ") + ".Memory"
		// FuncAddonsValue
	} else if strings.HasPrefix(symbol, generator.FuncAddonsValue) {
		symbol = strings.TrimPrefix(symbol, generator.FuncAddonsValue+" ")
		s := strings.Split(symbol, " ")
		symbol = s[0] + "." + s[1] + "." + s[2]
	}

	return symbol
}

func walkFiles(dir string) {
	_, err := os.Stat(dir)
	exitOnError(err)

	var files []string
	err = filepath.Walk(dir,
		func(path string, info os.FileInfo, err error) error {
			files = append(files, path)
			exitOnError(err)
			return nil
		})
	exitOnError(err)

	for _, file := range files {
		fmt.Printf("\n=== Scanning file: %s ===\n", file)
		scanFile(file)
	}
}

func exitOnError(err error) {
	if err != nil {
		fmt.Println("error:", err)
		os.Exit(1)
	}
}

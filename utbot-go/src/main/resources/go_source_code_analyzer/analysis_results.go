package main

import "go/token"

type AnalyzedType interface {
	GetName() string
}

type AnalyzedInterfaceType struct {
	Name            string `json:"name"`
	ImplementsError bool   `json:"implementsError"`
	PackageName     string `json:"packageName"`
	PackagePath     string `json:"packagePath"`
}

func (t AnalyzedInterfaceType) GetName() string {
	return t.Name
}

type AnalyzedPrimitiveType struct {
	Name string `json:"name"`
}

func (t AnalyzedPrimitiveType) GetName() string {
	return t.Name
}

type AnalyzedField struct {
	Name       string       `json:"name"`
	Type       AnalyzedType `json:"type"`
	IsExported bool         `json:"isExported"`
}

type AnalyzedStructType struct {
	Name            string          `json:"name"`
	PackageName     string          `json:"packageName"`
	PackagePath     string          `json:"packagePath"`
	ImplementsError bool            `json:"implementsError"`
	Fields          []AnalyzedField `json:"fields"`
}

func (t AnalyzedStructType) GetName() string {
	return t.Name
}

type AnalyzedArrayType struct {
	Name        string       `json:"name"`
	ElementType AnalyzedType `json:"elementType"`
	Length      int64        `json:"length"`
}

func (t AnalyzedArrayType) GetName() string {
	return t.Name
}

type AnalyzedFunctionParameter struct {
	Name string       `json:"name"`
	Type AnalyzedType `json:"type"`
}

type AnalyzedFunction struct {
	Name                                string                      `json:"name"`
	ModifiedName                        string                      `json:"modifiedName"`
	Parameters                          []AnalyzedFunctionParameter `json:"parameters"`
	ResultTypes                         []AnalyzedType              `json:"resultTypes"`
	RequiredImports                     []Import                    `json:"requiredImports"`
	ModifiedFunctionForCollectingTraces string                      `json:"modifiedFunctionForCollectingTraces"`
	NumberOfAllStatements               int                         `json:"numberOfAllStatements"`
	position                            token.Pos
}

type AnalysisResult struct {
	AbsoluteFilePath           string             `json:"absoluteFilePath"`
	SourcePackage              Package            `json:"sourcePackage"`
	AnalyzedFunctions          []AnalyzedFunction `json:"analyzedFunctions"`
	NotSupportedFunctionsNames []string           `json:"notSupportedFunctionsNames"`
	NotFoundFunctionsNames     []string           `json:"notFoundFunctionsNames"`
}

type AnalysisResults struct {
	IntSize int              `json:"intSize"`
	Results []AnalysisResult `json:"results"`
}

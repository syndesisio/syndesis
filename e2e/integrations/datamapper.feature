Feature: Features testing data mapper
	in create integration flow

  Background:
    Given credentials for "Camilla"

  @datamapper
  Scenario: add data mapper step to integration
    Given application state "datamapper-integration-base.json"

    When "Camilla" navigates to the "Integrations" page
    Then Integration "DataMapper integration base" is present in integrations list

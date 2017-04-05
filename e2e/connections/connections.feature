# Created by jludvice at 29.3.17
@smoke
Feature: Connections smoke test
  # Enter feature description here

  Background:
    Given details for "Twitter" connection:
      | accessToken       | randomly generated token        |
      | accessTokenSecret | this is just the biggest secret |
      | consumerKey       | rrrrrrrrrrrrrrrrrrrrrrr333      |
      | consumerSecret    | ssssssssssssssssssssssss333     |

    Given credentials for "Camilla"
    And clean application state

  Scenario: Test connections
    When "Camilla" navigates to the "Connections" page
    And click on the "Create" button
    And Camilla selects the "Twitter" connection
    And type "my sample twitter connection" into connection name
    And type "this connection is awesome" into connection description
    And click on the "Next" button
    Then she is presented with the "Validate" button

    When she fills "Twitter" connection details


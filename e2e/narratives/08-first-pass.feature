# Created by jludvice at 8.3.17
@narrative
Feature: Sprint 8 narrative
  https://issues.jboss.org/browse/IPAAS-153

  Scenario: First pass at login, homepage
    When "Camilla" logs into the Syndesis URL for her installation (e.g. rh-syndesis.[openshift online domain].com)
    Then Camilla is presented with the Syndesis page "Dashboard"

  Scenario: Explore connections
    When "Camilla" navigates to the "Connections" page to see what's available in Syndesis
    And Camilla selects the "Twitter Example" connection to view the configuration details for that connection.
    Then Camilla is presented with "Twitter Example" connection details

  Scenario: Create integration
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    And she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "Salesforce Example" connection
    And she selects "Create Opportunity" integration action
    Then she is presented with the "Add a Step" button
    And click on the "Save" button

#    try to publish without integration name will open basics page
    When Camilla clicks on the "Save as Draft" button
    And she defines integration name "My awesome first integration"
    And click on the "Publish" button
    Then Camilla is presented with the Syndesis page "Integrations"
    And Integration "My awesome first integration" is present in integrations list

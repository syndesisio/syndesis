@dashboard
Feature: Test to verify Dashboard links
  https://issues.jboss.org/browse/IPAAS-289

  Scenario: First pass at login, homepage
    When "Camilla" logs into Syndesis URL for her installation (e.g. rh-syndesis.[openshift online domain].com)
    Then Camilla is presented with Syndesis page "Dashboard"
    
  Scenario: Verify dashboard cards existence
    When "Camilla" navigates to the "Dashboard" page to see what's available in Syndesis
    Then she is presented with the "top-integrations,integration-board,recent-updates,syndesis-dashboard-connections" elements

  Scenario: Verify dashboard links existence
    When "Camilla" navigates to the "Dashboard" page to see what's available in Syndesis
    Then she is presented with the "Create Integration" button
    And Camilla is presented with the "View All Integrations" link

  Scenario: Verify View "All Integrations" link
    When Camilla clicks on the "View All Integrations" link
    Then Camilla is presented with Syndesis page "Integrations"

  Scenario: Verify "Create integration" button
    When "Camilla" navigates to the "Dashboard" page to see what's available in Syndesis
    And Camilla clicks on the "Create Integration" button
    Then she is presented with a visual integration editor
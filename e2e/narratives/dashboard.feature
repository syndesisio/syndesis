@dashboard
Feature: Test to verify Dashboard links
  https://issues.jboss.org/browse/IPAAS-289

  Scenario: First pass at login, homepage
    When "Camilla" logs into the iPaaS URL for her installation (e.g. rh-ipaas.[openshift online domain].com)
    Then Camilla is presented with the iPaaS page "Dashboard"
    
  Scenario: Verify dashboard cards existence
    When "Camilla" navigates to the "Dashboard" page to see what's available in the iPaaS
    Then she is presented with the "top-integrations,integration-board,recent-updates,ipaas-dashboard-connections" elements

  Scenario: Verify dashboard links existence
    When "Camilla" navigates to the "Dashboard" page to see what's available in the iPaaS
    Then she is presented with the "Create Integration" button
    And Camilla is presented with the "View All Integrations" link

  Scenario: Verify View "All Integrations" link
    When Camilla clicks on the "View All Integrations" link
    Then Camilla is presented with the iPaaS page "Integrations"

  Scenario: Verify "Create integration" button
    When "Camilla" navigates to the "Dashboard" page to see what's available in the iPaaS
	  And Camilla clicks on the "Create Integration" button
    Then she is presented with a visual integration editor
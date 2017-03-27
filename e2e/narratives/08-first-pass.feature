# Created by jludvice at 8.3.17
@narrative
Feature: Sprint 8 narrative
  https://issues.jboss.org/browse/IPAAS-153

  Background:
    Given details for "Twitter Example" connection:
      | accessToken       | some access token   |
      | accessTokenSecret | some awesome secret |

    And details for "Salesforce" connection:
      | loginUrl | http://redhat.com/demo |
      | clientId | ssldkfjslkdfj3343      |

  Scenario: First pass at login, homepage
    When "Camilla" logs into the iPaaS URL for her installation (e.g. rh-ipaas.[openshift online domain].com)
    Then Camilla is presented with the iPaaS homepage "Dashboard"

  Scenario: Explore connections
    When "Camilla" navigates to the "Connections" page to see what's available in the iPaaS
    And Camilla selects an existing "Twitter Example" connection to view the configuration details for that connection.
    Then Camilla is presented with "Twitter Example" connection details

  Scenario: Create connection
    When "Camilla" navigates to the "Home" page
    And clicks on a "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    And she fills "Twitter" connection details
    And click "Next" button
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "Salesforce" connection
    And she fills "Salesforce" connection details
    And click "Create" button
    # todo create button wasn't enabled

  Scenario: Camilla wants to confirm that her integration is available
    When Camilla navigates to the "Integrations" page
    And selects the "Camilla testing integration" integration
    Then she is presented with "Camilla testing integration" integration detail


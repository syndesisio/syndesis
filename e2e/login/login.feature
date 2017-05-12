# Created by jludvice at 1.3.17
@login
Feature: First pass at login, homepage, connections
  https://issues.jboss.org/browse/IPAAS-153
  Camila (citizen developer) first explores

  Background:
    Given credentials for "Camilla"

  Scenario: User Camila logins
    When "Camilla" logs into the Syndesis
    And "Camilla" navigate to the "Connections" page
    Then she is presented with at least "2" connections

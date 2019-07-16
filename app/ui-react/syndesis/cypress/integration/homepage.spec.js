describe('The Home Page', () => {
  beforeEach(function() {
    cy.visit('/');
  });

  /**
   * Happy Path
   * 1. User is able to view Metrics Cards on the dashboard.
   * 2. User is able to view Top 5 Integrations on the dashboard.
   * 3. User is able to view an Integration Board on the dashboard.
   * 4. User is able to view Recent Updates on the dashboard.
   * 5. User is able to view Connections on the dashboard.
   * 6. User is able to see and use a navigation bar at the top of the dashboard.
   * 7. User is able to see and use a vertical navigation bar / side bar to the left of the dashboard.
   */

  it('loads the dashboard successfully', () => {
    cy.visit('/');

    cy.get('[data-testid|=connection-card]').should('have.length', 6);

    /**
     * Metrics Cards
     * We won't be counting the exact number for now, until we get data stubs or fixtures set up.
     */
    cy.get('[data-testid=dashboard-page-total-integrations]').should(
      'contain',
      'Integrations'
    );

    cy.get('[data-testid=dashboard-page-total-connections]').should(
      'contain',
      'Connections'
    );

    cy.get('[data-testid=dashboard-page-total-messages]').should(
      'contain',
      'Messages'
    );

    cy.get('[data-testid=dashboard-page-metrics-uptime]').should(
      'contain',
      'Uptime'
    );

    /**
     * Top 5 Integrations
     */
    cy.get('[data-testid=dashboard-top-integrations]').should(
      'contain',
      'Top 5 Integrations'
    );

    /**
     * Integration Board
     */
    cy.get('[data-testid=dashboard-integration-board]').should(
      'contain',
      'Integration Board'
    );

    /**
     * Recent Updates
     */
    cy.get('[data-testid=dashboard-recent-updates]').should(
      'contain',
      'Recent Updates'
    );

    /**
     * Connections
     */
    //expect(cy.get('[data-testid|=connections')).to.exist;

    /**
     * Top Navigation Bar
     */
    expect(cy.get('data-testid=appTopMenu')).to.exist;

    /**
     * Vertical Navigation / Sidebar
     */
    expect(cy.get('<header>')).to.exist;

    /**
     * User clicks Create Integration
     */
  });

  /**
   * Alternative Path: User clicks on View All Integrations
   */
  it('', () => {});

  /**
   * Alternative Path: User clicks on a Connection from the Connections widget
   */
  it('', () => {});

  /**
   * Alternative Path: User clicks on View All Connections
   */
  it('', () => {});

  /**
   * Alternative Path: User clicks on Create Connection
   */
  it('', () => {});

  /**
   * Alternative Path: User clicks on an item in the sidebar
   */
  it('', () => {});
});

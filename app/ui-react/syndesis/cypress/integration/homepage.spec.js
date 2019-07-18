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
    /**
     * This value is not always going to be 8
    cy.get('.connection-card').should('have.length', 8);
     **/
    cy.get('.connection-card').should('exist');

    /**
     * Top Navigation Bar
     */
    expect(cy.get('[data-testid=appTopMenu]')).to.exist;

    /**
     * Vertical Navigation / Sidebar
     */
    expect(cy.get('<header>')).to.exist;
  });

  /**
   * Alternative Path: User clicks Create Integration
   */
  it('should render the Create Integration page', () => {
    cy.get('[data-testid=dashboard-create-integration-button]').click();
    cy.location('pathname').should(
      'contain',
      '/integrations/create/new-integration/start/'
    );
  });

  /**
   * Alternative Path: User clicks on View All Integrations
   */
  it('should render the Integrations list page', () => {
    cy.get('[data-testid=dashboard-integrations-link]').click();
    cy.location('pathname').should('eq', '/integrations/');
  });

  /**
   * Alternative Path: User clicks on a Connection from the Connections widget
   */
  it('should render the Connection detail page', () => {
    cy.get('.connection-card')
      .eq(0)
      .click();

    cy.get('.pf-c-breadcrumb').should('contain', 'Connection Details');
    /**
     * Not available until next release of Cypress
     * https://github.com/cypress-io/cypress/issues/3684
    cy.location('pathname').should('contain', '/connections/');
     **/
  });

  /**
   * Alternative Path: User clicks on View All Connections
   */
  it('should render the Connections list page', () => {
    cy.get('[data-testid=dashboard-connections-link]').click();
    cy.location('pathname').should('eq', '/connections/');
  });

  /**
   * Alternative Path: User clicks on Create Connection
   */
  it('should render the Create Connection page', () => {
    cy.get('[data-testid=dashboard-create-connection-button]').click();
    cy.location('pathname').should(
      'contain',
      '/connections/create/connection-basics'
    );
  });

  /**
   * Alternative Path: User clicks on an item in the sidebar,
   * namely, the Integrations link
   */
  it('should render the Integrations list page', () => {
    cy.get('[data-testid=ui-integrations]').click();
    cy.location('pathname').should('eq', '/integrations/');
  });
});

describe('Create an Integration', () => {
  /**
   * Setup
   */
  before(function() {
    // Runs once before all tests in the block

    /**
     * Create DB Connection
     */
    cy.visit('/');

    cy.get('[data-testid=dashboard-create-connection-button]').click();

    cy.location('pathname').should(
      'contain',
      '/connections/create/connection-basics'
    );

    cy.get('[data-testid=connection-card-database-card]').click();

    cy.get('[data-testid=url]')
      .clear()
      .type('jdbc:postgresql://syndesis-db:5432/e2edb')
      .should('have.value', 'jdbc:postgresql://syndesis-db:5432/e2edb');

    cy.get('[data-testid=user]')
      .clear()
      .type('e2edb')
      .should('have.value', 'e2edb');

    cy.get('[data-testid=password]')
      .clear()
      .type('e2edb')
      .should('have.value', 'e2edb');

    cy.get('[data-testid=schema]')
      .clear()
      .type('e2edb')
      .should('have.value', 'e2edb');

    cy.get('[data-testid=connection-creator-layout-next-button]')
      .should('not.be.disabled')
      .click();

    cy.get('[data-testid=name]')
      .clear()
      .type('E2E PostgreSQL DB')
      .should('have.value', 'E2E PostgreSQL DB');

    cy.get('[data-testid=description]')
      .clear()
      .type('Subscribe for and publish messages.')
      .should('have.value', 'Subscribe for and publish messages.');

    cy.get('[data-testid=connection-creator-layout-next-button]')
      .should('not.be.disabled')
      .click();

    cy.location('pathname').should('eq', '/connections/');

    cy.get('.list-view-toolbar > input.form-control').type(
      'E2E PostgreSQL DB{enter}'
    );
    cy.get('[data-testid=e2e-postgresql-db]').should('exist');
  });

  /**
   * Teardown
   */
  after(function() {
    // Runs once after all tests in the block
    /**
     * Delete DB Connection
     */
  });

  /**
   * Happy Path
   *
   * 1. User navigates to Create Integration page from the Dashboard.
   * 2. User is able to see a list of Connections.
   * 3. User selects a Connection, and is redirected to Choose an Action.
   * 4. User selects one of the connection actions available and is redirected to the next step.
   * 5. User configures the action selected, with validation.
   * 6. User is redirected to select an end Connection.
   * 7. User selects an action for the end Connection.
   * 8. User configures the action selected, with validation.
   * 8. User is given the option of adding a step in between start and end Connections, or to Save or Publish.
   * 9. User is able to name the Integration and then click Save and publish to create Integration.
   * 10. User is redirected to Integrations list page and sees newly created Integration.
   */

  it('loads the editor and prompts users to select a start connection', () => {
    cy.visit('/');

    cy.get('[data-testid=dashboard-create-integration-button]').click();

    cy.location('pathname').should(
      'contain',
      '/integrations/create/new-integration/start/'
    );

    /**
     * Select Timer connection as starting
     */
    cy.get('[data-testid=connection-card-timer-card]').click();
  });

  it('prompts users to select an action for the start connection', () => {
    cy.location('pathname').should('contain', '/connection/timer');

    /**
     * Select the Simple Timer
     */
    cy.get(
      '[data-testid=integration-editor-actions-list-item-cron-list-item]'
    ).within(() => {
      cy.get('[data-testid=select-action-page-select-button]').click();
    });
  });

  it('loads Configure Action for the start connection', () => {
    cy.get('[data-testid=cron]').should('not.be.empty');

    cy.get('button#integration-editor-form-next-button').should(
      'not.be.disabled'
    );

    cy.get('button#integration-editor-form-next-button').should('be.visible');

    cy.wait(2000);

    cy.get('button#integration-editor-form-next-button').click();
  });

  it.skip('prompts users to select an end connection', () => {
    /**
     * Select Log connection
     */
    cy.get('[data-testid=connection-card-log-card]').click();
  });

  it.skip('prompts users to select an action for the end connection', () => {
    cy.get('[data-testid=contextloggingenabled]').should('be.visible');
    cy.get('[data-testid=bodyloggingenabled]').should('be.visible');
    cy.get('[data-testid=customtext]').should('be.visible');
    cy.get('[data-testid=bodyloggingenabled]').check();
    cy.get('button#integration-editor-form-next-button').click();
  });

  it.skip('loads Configure Action for the end connection', () => {
    cy.location('pathname').should('contain', '/add-step');

    cy.get(
      '[data-testid=integration-editor-steps-list-item-cron-list-item]'
    ).should('exist');
    cy.get('[data-testid=integration-flow-add-step-add-step-link]').should(
      'be.visible'
    );
    cy.get(
      '[data-testid=integration-editor-steps-list-item-log-list-item]'
    ).should('exist');
  });

  it.skip('prompts users to add a step', () => {});

  it.skip('allows users to set a name and description', () => {});

  it.skip('loads integration list with newly created integration', () => {
    cy.get('[data-testid=integration-status-detail]').should('be.visible');
  });

  /**
   * Alternative Path
   */

  it.skip('', () => {
    cy.visit('/');
  });

  /**
   * Exception Path
   */

  it.skip('', () => {
    cy.visit('/');
  });
});

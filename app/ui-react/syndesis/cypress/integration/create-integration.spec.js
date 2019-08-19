describe('Create an Integration', () => {
  const randomInteger = () => {
    return Math.floor(Math.random() * (100 + 1));
  };
  const nameInt = randomInteger();
  const connectionName = 'E2E Todo Connection';
  const integrationName = 'E2E Todo Integration ' + nameInt;
  const connectionSlug = 'e2e-todo-connection';
  const integrationSlug = 'e2e-todo-integration-' + nameInt;

  /**
   * SETUP
   *
   * Runs once before all tests in the block
   * Check that at least one E2E connection is available,
   * otherwise create one.
   */
  before(function() {
    cy.visit('/connections');

    cy.get('.form-control').type(connectionName + '{enter}');
    const testCnx = Cypress.$(
      '[data-testid|=connection-card-' + connectionSlug + ']'
    );
    if (testCnx.length === 0) {
      cy.createConnection({ name: connectionName, slug: connectionSlug });
    }
  });

  /**
   * TEARDOWN
   *
   * Runs once after all tests in the block
   * Delete items created in this test
   */
  after(function() {
    /**
     * TODO: We need to keep these for now until db snapshot & restore is implemented from tests.
     */
    //cy.deleteIntegration({ slug: integrationSlug });
    //cy.deleteConnection({ slug: connectionSlug });
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
     * Select the Simple Timer action
     */
    cy.get(
      '[data-testid=integration-editor-actions-list-item-cron-list-item]'
    ).within(() => {
      cy.get('[data-testid=select-action-page-select-button]').click();
    });
  });

  it('checks that the page renders properly', () => {
    /**
     * Sidebar should be collapsed
     */
    cy.get('.pf-c-page__sidebar-body').should('be.hidden');

    /**
     * Test Cancellation modal
     */
    cy.get('a#integration-editor-cancel-button')
      .should('exist')
      .click();
    cy.get('.modal-dialog').should('be.visible');
    cy.get('.modal-footer > .btn-default').click();
    cy.get('.modal-dialog').should('not.be.visible');
  });

  it('loads Configure Action for the start connection', () => {
    cy.get('button#integration-editor-form-next-button').should(
      'not.be.disabled'
    );

    cy.get('button#integration-editor-form-next-button')
      .should('be.visible')
      .click();
  });

  it('prompts users to select an end connection', () => {
    /**
     * Select Log connection
     */
    cy.wait(200);
    cy.get('[data-testid=connection-card-log-card]').click();
  });

  it('loads Configure Action for the end connection', () => {
    cy.get('[data-testid=contextloggingenabled]').should('be.visible');
    cy.get('[data-testid=bodyloggingenabled]').should('be.visible');
    cy.get('[data-testid=customtext]').should('be.visible');
    cy.get('[data-testid=bodyloggingenabled]').check();
    cy.get('button#integration-editor-form-next-button').click();
  });

  it('loads Add to Integration page', () => {
    cy.location('pathname').should('contain', '/add-step');

    cy.get(
      '[data-testid=integration-editor-steps-list-item-cron-list-item]'
    ).should('exist');

    cy.get(
      '[data-testid=integration-editor-steps-list-item-log-list-item]'
    ).should('exist');

    cy.get('[data-testid=integration-flow-add-step-add-step-link]')
      .should('be.visible')
      .click();
  });

  it('prompts users to add a step', () => {
    /**
     * Use connection created earlier
     */
    cy.get('[data-testid|=connection-card-' + connectionSlug + ']')
      .should('be.visible')
      .click();

    cy.get(
      '[data-testid=integration-editor-actions-list-item-list-all-tasks-list-item]'
    ).within(() => {
      cy.get('[data-testid=select-action-page-select-button]').click();
    });

    cy.wait(200);
    cy.get(
      '[data-testid=integration-editor-nothing-to-configure-next-button]'
    ).click();

    /**
     * Ensure all Connections are visible
     */
    cy.get(
      '[data-testid=integration-editor-steps-list-item-cron-list-item]'
    ).should('exist');

    cy.get(
      '[data-testid=integration-editor-steps-list-item-list-all-tasks-list-item]'
    ).should('exist');

    cy.get(
      '[data-testid=integration-editor-steps-list-item-log-list-item]'
    ).should('exist');

    cy.get('#integration-editor-publish-button')
      .should('exist')
      .click();
  });

  it('allows users to set a name and description', () => {
    cy.location('pathname').should('contain', '/editor/save');
    cy.get('[data-testid=name]')
      .clear()
      .type(integrationName);
    cy.get('[data-testid=description]')
      .clear()
      .type('This was created from an E2E test.');
    cy.get('#integration-editor-publish-button')
      .should('exist')
      .click();
  });

  it('loads integration detail page with newly created integration', () => {
    cy.get('.toast-pf.alert-info').should('be.visible');
    cy.location('pathname').should('contain', 'details');
  });
});

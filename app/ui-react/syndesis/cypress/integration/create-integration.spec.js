describe('Create an Integration', () => {
  const randomInteger = () => {
    return Math.floor(Math.random() * (100 + 1));
  };
  const nameInt = randomInteger();
  const connectionName = 'E2E Todo Connection ' + nameInt;
  const integrationName = 'E2E Todo Integration ' + nameInt;
  const connectionSlug = 'e2e-todo-connection-' + nameInt;
  const integrationSlug = 'e2e-todo-integration-' + nameInt;

  function createConnection() {
    /**
     * Create To Do Connection
     * NOTE: This will break if a Connection with the same name already exists.
     */
    cy.visit('/');

    cy.get('[data-testid=dashboard-create-connection-button]').click();

    cy.location('pathname').should(
      'contain',
      '/connections/create/connection-basics'
    );

    cy.get('[data-testid=connection-card-todo-app-api-card]').click();

    cy.location('pathname').should('contain', '/configure-fields');

    cy.get('[data-testid=username]')
      .clear()
      .type(Cypress.env('connectorTodoUser'))
      .should('have.value', Cypress.env('connectorTodoUser'));

    cy.get('[data-testid=password]')
      .clear()
      .type(Cypress.env('connectorTodoPassword'))
      .should('have.value', Cypress.env('connectorTodoPassword'));

    cy.get('[data-testid=connection-creator-layout-next-button]')
      .should('not.be.disabled')
      .click();

    cy.get('[data-testid=name]')
      .clear()
      .type(connectionName);

    cy.get('[data-testid=description]')
      .clear()
      .type('Subscribe for and publish messages.');

    cy.get('[data-testid=connection-creator-layout-next-button]').click();

    cy.location('pathname').should('eq', '/connections/');

    cy.get('.form-control').type(connectionName + '{enter}');
    cy.get('[data-testid|=connection-card-' + connectionSlug + ']').should(
      'exist'
    );
  }

  function deleteConnection() {
    cy.visit('/connections');

    cy.get('[data-testid|=connection-card-' + connectionSlug + ']').within(
      () => {
        cy.get('[data-testid=connection-card-kebab]').click();
        cy.get('[data-testid=connection-card-delete-action]').click();
      }
    );

    cy.get('#deleteConfirmationDialogContent').should('be.visible');
    cy.get('.modal-footer')
      .contains('Delete')
      .click();

    cy.get('.toast-pf.alert-success').should('be.visible');
  }

  function deleteIntegration() {
    cy.visit('/integrations');

    cy.get(
      '[data-testid|=integrations-list-item-' + integrationSlug + ']'
    ).within(() => {
      cy.get('.dropdown-toggle').click();
      cy.get('[data-testid=integration-actions-delete]').click();
    });
    cy.get('.modal-dialog').should('be.visible');
    cy.get('.modal-dialog').within(() => {
      cy.get('.modal-footer')
        .contains('Delete')
        .click();
    });
  }

  /**
   * SETUP
   *
   * Runs once before all tests in the block
   */
  before(function() {
    createConnection();
  });

  /**
   * TEARDOWN
   *
   * Runs once after all tests in the block
   * Delete items created in this test
   */
  after(function() {
    deleteIntegration();
    deleteConnection();
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
    /**
     * May not be a good test, re-assess as very dependent on time.
     */

    cy.get('.toast-pf.alert-info').should('be.visible');
    cy.get('.integration-status-detail').should('exist');
    cy.location('pathname').should('contain', 'details');
  });
});

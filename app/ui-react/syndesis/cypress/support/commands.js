// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

/**
 * CREATE CONNECTION
 * HTTP Connection
 */
Cypress.Commands.add('createConnection', cnx => {
  cy.visit('/');

  cy.get('[data-testid=dashboard-create-connection-button]').click();

  cy.location('pathname').should(
    'contain',
    '/connections/create/connection-basics'
  );

  cy.get('[data-testid=connection-card-http-card]').click();

  cy.location('pathname').should('contain', '/configure-fields');

  cy.get('[data-testid=baseurl]')
    .type('www.redhat.com')
    .should('have.value', 'www.redhat.com');

  cy.get('[data-testid=connection-creator-layout-next-button]')
    .should('not.be.disabled')
    .click();

  cy.get('[data-testid=name]')
    .clear()
    .type(cnx.name);

  cy.get('[data-testid=description]')
    .clear()
    .type('Subscribe for and publish messages.');

  cy.get('[data-testid=connection-creator-layout-next-button]').click();

  cy.location('pathname').should('eq', '/connections/');

  cy.get('.form-control').type(cnx.name + '{enter}');
  cy.get('[data-testid|=connection-card-' + cnx.slug + ']').should('exist');
});

/**
 * CREATE INTEGRATION
 */
Cypress.Commands.add('createIntegration', data => {
  cy.visit('/');

  cy.get('[data-testid=dashboard-create-integration-button]').click();
  cy.get('[data-testid=connection-card-timer-card]').click();

  /**
   * Select the Simple Timer action
   */
  cy.get(
    '[data-testid=integration-editor-actions-list-item-cron-list-item]'
  ).within(() => {
    cy.get('[data-testid=select-action-page-select-button]').click();
  });

  cy.get('button#integration-editor-form-next-button').click();

  /**
   * Select Log connection
   */
  cy.wait(200);
  cy.get('[data-testid=connection-card-log-card]').click();

  cy.get('[data-testid=bodyloggingenabled]').check();
  cy.get('button#integration-editor-form-next-button').click();

  cy.get('[data-testid=integration-flow-add-step-add-step-link]').click();

  /**
   * Use connection created earlier
   */
  cy.get('[data-testid|=connection-card-' + data.connectionSlug + ']').click();

  cy.get('[data-testid=select-action-page-select-button]').within(() => {
    cy.get('[data-testid=select-action-page-select-button]').click();
  });

  cy.wait(200);
  cy.get(
    '[data-testid=integration-editor-nothing-to-configure-next-button]'
  ).click();

  /**
   * Set name and description
   */
  cy.get('[data-testid=name]')
    .clear()
    .type(data.integrationName);
  cy.get('[data-testid=description]')
    .clear()
    .type('This was created from an E2E test.');
  cy.get('#integration-editor-publish-button').click();
});

/**
 * DELETE CONNECTION
 */
Cypress.Commands.add('deleteConnection', cnx => {
  cy.visit('/connections');

  cy.get('[data-testid=connection-card-' + cnx.slug + '-card]').within(() => {
    cy.get('[data-testid=connection-card-kebab]').click();
    cy.get('[data-testid=connection-card-delete-action]').click();
  });

  cy.get('.modal-footer')
    .contains('Delete')
    .click();
});

/**
 * DELETE INTEGRATION
 */
Cypress.Commands.add('deleteIntegration', int => {
  cy.visit('/integrations');

  cy.get('[data-testid|=integrations-list-item-' + int.slug + ']').within(
    () => {
      cy.get('.dropdown-toggle').click();
      cy.get('[data-testid=integration-actions-delete]').click();
    }
  );
  cy.get('.modal-dialog').should('be.visible');
  cy.get('.modal-dialog').within(() => {
    cy.get('.modal-footer')
      .contains('Delete')
      .click();
  });
});

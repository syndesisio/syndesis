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
 */
Cypress.Commands.add('createConnection', cnx => {
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

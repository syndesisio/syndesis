/**
 * 01: CREATE DATA
 *
 * The purpose of this "test" is to setup data as the user would in the UI.
 * First, the database is reset, and then custom Cypress commands are used
 * to create the data manually.
 */

const connectionName = 'E2E Todo Connection';
const integrationName = 'E2E Todo Integration';
const connectionSlug = 'e2e-todo-connection';
const integrationSlug = 'e2e-todo-integration';

describe('Create Data', () => {
  before(function() {
    cy.log('Resetting the database...');
    cy.request('GET', 'api/v1/test-support/reset-db').then(resp => {
      cy.log('Database has been reset. Creating new data...');
    });
  });

  it('creates a connection', () => {
    cy.visit('/connections');

    cy.get('.form-control').type(connectionName + '{enter}');
    const testCnx = Cypress.$(
      '[data-testid|=connection-card-' + connectionSlug + ']'
    );
    if (testCnx.length === 0) {
      cy.createConnection({ name: connectionName, slug: connectionSlug });
    }
  });

  it('creates an integration', () => {
    cy.visit('/connections');

    cy.get('.form-control').type(connectionName + '{enter}');
    const testCnx = Cypress.$(
      '[data-testid|=connection-card-' + connectionSlug + ']'
    );
    if (testCnx.length === 0) {
      cy.createConnection({ name: connectionName, slug: connectionSlug });
    }
  });
});

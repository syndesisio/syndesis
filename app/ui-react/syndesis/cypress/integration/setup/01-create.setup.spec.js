/**
 * 01: CREATE DATA
 *
 * The purpose of this "test" is to setup data as the user would in the UI.
 * First, the database is reset, and then custom Cypress commands are used
 * to create the data manually.
 */

const randomInteger = () => {
  return Math.floor(Math.random() * (100 + 1));
};
const nameInt = randomInteger();
const connectionName = 'E2E Todo Connection';
const integrationName = 'E2E Todo Integration ' + nameInt;
const connectionSlug = 'e2e-todo-connection';
const integrationSlug = 'e2e-todo-integration-' + nameInt;

describe('Create Data', () => {
  before(function() {
    cy.visit('/');
  });

  it('resets the database', () => {});

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

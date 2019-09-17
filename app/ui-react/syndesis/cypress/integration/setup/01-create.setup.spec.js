/**
 * 01: CREATE DATA
 *
 * The purpose of this "test" is to setup data as the user would in the UI.
 * First, the database is reset, and then custom Cypress commands are used
 * to create the data manually.
 */

const constants = require('../../fixtures/constants');

describe('Create Data', () => {
  it('creates a connection', () => {
    cy.visit('/connections');

    cy.get('.form-control').type(constants.CONNECTION_NAME + '{enter}');
    const testCnx = Cypress.$(
      '[data-testid|=connection-card-' + constants.CONNECTION_SLUG + ']'
    );
    if (testCnx.length === 0) {
      cy.createConnection({
        name: constants.CONNECTION_NAME,
        slug: constants.CONNECTION_SLUG,
      });
    }
  });

  /**
   * Hold off on creating an integration,
   * until we know for sure we will need this.
   */
  it.skip('creates an integration', () => {
    cy.createIntegration({
      connectionSlug: constants.CONNECTION_NAME,
      integrationName: constants.INTEGRATION_NAME,
    });
  });
});

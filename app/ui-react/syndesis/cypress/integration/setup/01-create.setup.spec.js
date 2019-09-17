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
    cy.createConnection({
      name: constants.CONNECTION_NAME,
      slug: constants.CONNECTION_SLUG,
    });
  });

  it('creates an integration', () => {
    cy.createIntegration({
      connectionSlug: constants.CONNECTION_SLUG,
      integrationName: constants.INTEGRATION_NAME,
    });
  });
});

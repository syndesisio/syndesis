/**
 * 00: RESET DB
 *
 * The purpose of this "test" is to reset the DB.
 * This test was separated from the one used to create the data for
 * added flexibility.
 */

describe('Reset the DB', () => {
  it('reset the database', () => {
    cy.log('Resetting the database...');
    cy.request('GET', 'api/v1/test-support/reset-db').then(resp => {
      cy.log('resp: ' + JSON.stringify(resp));
      cy.log('Database has been reset. Creating new data...');
    });
  });
});

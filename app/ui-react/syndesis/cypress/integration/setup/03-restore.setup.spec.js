/**
 * 03: RESTORE SNAPSHOT/DATA
 *
 * The purpose of this "test" is to restore the snapshot and, therefore, data
 * to the database. We first reset the database to increase the likelihood
 * of having a predictable test environment, then test to see if the data
 * exists as expected, from the perspective of the user.
 */

describe('Restore Snapshot', () => {
  before(function() {
    cy.log('Retrieving latest snapshot...');
    cy.task('getSnapshot');
  });

  it('test', () => {
    cy.task('test');
  });

  it.skip('restores the snapshot', () => {
    cy.request('POST', 'api/v1/test-support/restore-db').then(resp => {
      cy.log('Database has been restored with snapshot...');
    });
  });
});

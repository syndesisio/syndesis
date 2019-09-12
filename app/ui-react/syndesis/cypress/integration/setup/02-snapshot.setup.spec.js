/**
 * 02: CREATE SNAPSHOT
 *
 * The purpose of this "test" is to create a snapshot of the database.
 * This test was separated from the one used to create the data for
 * added flexibility.
 */

describe('Create Snapshot', () => {
  it('created a snapshot', () => {
    cy.request('GET', 'api/v1/test-support/snapshot-db').then(resp => {
      expect(resp.status).to.eq(200);
      cy.task('storeSnapshot', resp).then(content => {
        expect(content).not.to.be.null;
        cy.log('Snapshot created');
      });
    });
  });
});

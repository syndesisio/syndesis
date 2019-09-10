/**
 * 02: CREATE SNAPSHOT
 *
 * The purpose of this "test" is to create a snapshot of the database.
 * This test was separated from the one used to create the data for
 * added flexibility.
 */

describe('Create Snapshot', () => {
  it('writes to a file', () => {
    cy.task('storeSnapshot', 'snap').then(content => {
      // expects here
      cy.log('Was the task completed?');
    });
  });

  it.skip('created a snapshot', () => {
    /**
     * TODO: Handle snapshot
     */
    cy.createSnapshot().then(response => {
      cy.log('Snapshot created: ' + JSON.stringify(response));
    });
    cy.log('Not sure what to put here..');
  });
});

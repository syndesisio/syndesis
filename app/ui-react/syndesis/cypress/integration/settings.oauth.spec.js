describe('Settings: OAuth Application Management', () => {
  beforeEach(function() {
    cy.visit('/settings/oauth-apps/');
  });

  it('loads the page successfully', () => {
    cy.get('[data-testid=oauth-title]').should('be.visible');
  });

  /**
   * TODO: Find out more information about what is provided OOTB, what can be tested.
   * Example: Unconfigured app, credentials for shared apps, creating/deleting an app, etc.
   */
});

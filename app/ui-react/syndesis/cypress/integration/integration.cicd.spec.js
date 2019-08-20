describe('Integration CI.CD', () => {
  const randomInteger = () => {
    return Math.floor(Math.random() * (100 + 1));
  };
  const nameInt = randomInteger();
  const envName = 'E2E Test Env ' + nameInt;
  const envSlug = 'e2e-test-env-' + nameInt;
  const integrationName = 'E2E Todo Integration';
  const integrationSlug = 'e2e-todo-integration';

  /**
   * SETUP
   *
   * Runs once before all tests in the block
   * Check that at least one E2E connection is available,
   * otherwise create one.
   */
  before(function() {
    cy.visit('/integrations/manageCicd/');

    const testEnv = Cypress.$('[data-testid|=cicd-list-item-' + envSlug + ']');
    if (testEnv.length) {
      /**
       * Delete the environment to properly test creating
       */
      cy.get('[data-testid|=cicd-list-item-' + envSlug + ']').within(() => {
        cy.get('[data-testid=cicd-list-item-remove-button]').click();
      });
      cy.get('.modal-content').should('be.visible');
      cy.get('.modal-footer').within(() => {
        /**
         * Terrible way to select buttons, need to improve this
         */
        cy.get('.btn-primary').click();
      });
    }
  });

  it('loads the integration list from the sidebar', () => {
    cy.visit('/');
    cy.get('[data-testid=ui-integrations]')
      .should('be.visible')
      .click();

    cy.location('pathname').should('contain', '/integrations');
  });

  it('loads the Manage CI/CD page', () => {
    cy.get('[data-testid=integrations-list-view-manage-cicd-button]')
      .should('exist')
      .click();
    cy.location('pathname').should('contain', 'manageCicd');
    cy.get('[data-testid=simple-page-header-title]').should(
      'contain',
      'Manage CI/CD'
    );
  });

  it('creates a new env', () => {
    cy.get('[data-testid$=-add-new-button]').click();
    cy.get('[data-testid=cicd-edit-dialog]').should('be.visible');
    cy.get('[data-testid=cicd-edit-dialog-save-button]').should('be.disabled');
    cy.get('[data-testid=cicd-edit-dialog-tag-name]')
      .click()
      .type(envName);
    cy.get('[data-testid=cicd-edit-dialog-save-button]')
      .should('not.be.disabled')
      .click();
    cy.wait(200);
    cy.get('[data-testid|=cicd-list-item-' + envSlug + ']').should('exist');
  });

  it('tags integration for release', () => {
    cy.visit('/integrations');

    cy.get('.form-control').type(integrationName + '{enter}');
    cy.get('[data-testid|=integrations-list-item-' + integrationSlug + ']')
      .eq(0)
      .should('exist');
    cy.get('[data-testid|=integrations-list-item-' + integrationSlug + ']')
      .eq(0)
      .within(() => {
        cy.get('.dropdown-toggle').click();
        cy.get('[data-testid=integration-actions-manage-ci-cd]').click();
      });
    cy.get('.modal-content')
      .should('be.visible')
      .within(() => {
        cy.get('.modal-body')
          .should('contain', envName)
          .within(() => {
            cy.get(
              '[data-testid=tag-integration-list-item-' +
                envSlug +
                '-selected-input]'
            ).click();
          });
        cy.get('[data-testid=tag-integration-dialog-save-button]').click();
      });
  });
});

describe('Create, Publish & Delete a Data Virtualization', () => {
  const randomInteger = () => {
    return Math.floor(Math.random() * (100 + 1));
  };
  const nameInt = randomInteger();

  const name = 'E2E_' + nameInt;

  it('loads the data virtualization page', () => {
    cy.visit('/data/virtualizations/');
    cy.contains('h1', 'Data Virtualizations');
  });

  it('loads the create data virtualization page', () => {
    cy.wait(100);
    cy.get(
      '[data-testid=virtualization-list-create-virtualization-button]'
    ).click();
    cy.location('pathname').should('contain', '/data/virtualizations/create');
    cy.contains('main', 'Create New Data Virtualization');
    cy.get('[data-testid=virtname]')
      .clear()
      .type(name)
      .should('have.value', name);

    cy.get('[data-testid=virtdescription]')
      .clear()
      .type('This is a test.')
      .should('have.value', 'This is a test.');
  });

  it('should create a data virtualization', () => {
    cy.get('[data-testid=virtualization-create-page-create-button]')
      .should('be.visible')
      .click();

    cy.wait(100);
    cy.get('.alert.toast-pf.alert-success').should('be.visible');

    cy.location('pathname').should('contain', 'views');
    /**
     * TODO:
     * - Test data virt with duplicate name
     * - Detail page not working
     */
  });

  /**
   * TODO: Need to provide a DV with "views" in order to publish
   */
  it.skip('should publish a virtualization', () => {
    cy.visit('/data/virtualizations/');
    cy.get('[data-testid=virtualization-list-item-' + name + '-dropdown-kebab]')
      .should('exist')
      .click()
      .within(() => {
        cy.contains('a', 'Publish').click();
      });

    cy.get('.modal-dialog')
      .should('be.visible')
      .within(() => {
        cy.contains('button', 'Confirm').click();
      });
  });

  it('should delete unpublished virtualization', () => {
    cy.visit('/data/virtualizations/');
    cy.get('button#virtualization-' + name + '-action-menu')
      .should('exist')
      .click();

    cy.get('[data-testid=virtualization-list-item-' + name + '-delete]')
      .should('be.visible')
      .click();

    cy.get('.modal-dialog')
      .should('be.visible')
      .within(() => {
        cy.contains('button', 'Delete')
          .should('be.visible')
          .click();
      });
  });
});

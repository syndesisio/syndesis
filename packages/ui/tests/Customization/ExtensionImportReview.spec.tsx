import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { fireEvent, render } from 'react-testing-library';
import {
  ExtensionImportReview,
  IImportAction,
} from '../../src/Customization/ExtensionImportReview';

const mockOnImport = jest.fn();

export default describe('ExtensionImportReview', () => {
  const actions = [
    {
      name: 'Action 1',
      description: 'The description for action 1',
    } as IImportAction,
    {
      name: 'Action 2',
      description: 'The description for action 2',
    } as IImportAction,
    {
      name: 'Action 3',
      description: 'The description for action 3',
    } as IImportAction,
    {
      name: 'Action 4',
      description: 'The description for action 4',
    } as IImportAction,
  ] as IImportAction[];

  const actionText = (name: string, description: string) => {
    return `${name} - ${description}`;
  };

  const cancelBtnText = 'Cancel';
  const cancelLink = '/extensions';
  const description = 'An extension to Syndesis to do Logging';
  const descriptionLabel = 'Description';
  const id = 'io.syndesis.extensions:syndesis-extension-log';
  const idLabel = 'ID';
  const importBtnText = 'Import Extension';
  const name = 'Log';
  const nameLabel = 'Name';
  const stepsActionsLabel = 'Steps';
  const title = 'Import Review';
  const typeLabel = 'Type';
  const typeMsg = 'Step Extension';

  // description
  const componentWithDescription = (
    <MemoryRouter>
      <ExtensionImportReview
        actions={actions}
        cancelLink={cancelLink}
        extensionDescription={description}
        extensionId={id}
        extensionName={name}
        i18nActionsLabel={stepsActionsLabel}
        i18nCancel={cancelBtnText}
        i18nDescriptionLabel={descriptionLabel}
        i18nExtensionTypeMessage={typeMsg}
        i18nIdLabel={idLabel}
        i18nImport={importBtnText}
        i18nNameLabel={nameLabel}
        i18nTitle={title}
        i18nTypeLabel={typeLabel}
        i18nActionText={actionText}
        onImport={mockOnImport}
      />
    </MemoryRouter>
  );

  // description
  const componentWithoutDescription = (
    <MemoryRouter>
      <ExtensionImportReview
        actions={actions}
        cancelLink={cancelLink}
        extensionId={id}
        extensionName={name}
        i18nActionsLabel={stepsActionsLabel}
        i18nCancel={cancelBtnText}
        i18nDescriptionLabel={descriptionLabel}
        i18nExtensionTypeMessage={typeMsg}
        i18nIdLabel={idLabel}
        i18nImport={importBtnText}
        i18nNameLabel={nameLabel}
        i18nTitle={title}
        i18nTypeLabel={typeLabel}
        i18nActionText={actionText}
        onImport={mockOnImport}
      />
    </MemoryRouter>
  );

  it('Should show correct text content', () => {
    const { getByTestId } = render(componentWithDescription);
    expect(getByTestId('title')).toHaveTextContent(title);
    expect(getByTestId('id-label')).toHaveTextContent(idLabel);
    expect(getByTestId('id-value')).toHaveTextContent(id);
    expect(getByTestId('name-label')).toHaveTextContent(nameLabel);
    expect(getByTestId('name-value')).toHaveTextContent(name);
    expect(getByTestId('description-label')).toHaveTextContent(
      descriptionLabel
    );
    expect(getByTestId('description-value')).toHaveTextContent(description);
    expect(getByTestId('type-label')).toHaveTextContent(typeLabel);
    expect(getByTestId('type-value')).toHaveTextContent(typeMsg);
    expect(getByTestId('actions-label')).toHaveTextContent(stepsActionsLabel);
    expect(getByTestId('import-button')).toHaveTextContent(importBtnText);
    expect(getByTestId('cancel-button')).toHaveTextContent(cancelBtnText);
    expect(getByTestId('cancel-button').getAttribute('href')).toBe(cancelLink);
  });

  it('Should show correct text content when there is no description', () => {
    const { getByTestId } = render(componentWithoutDescription);
    expect(getByTestId('title')).toHaveTextContent(title);
    expect(getByTestId('id-label')).toHaveTextContent(idLabel);
    expect(getByTestId('id-value')).toHaveTextContent(id);
    expect(getByTestId('name-label')).toHaveTextContent(nameLabel);
    expect(getByTestId('name-value')).toHaveTextContent(name);
    expect(getByTestId('description-label')).toHaveTextContent(
      descriptionLabel
    );
    expect(getByTestId('description-value')).toHaveTextContent('');
    expect(getByTestId('type-label')).toHaveTextContent(typeLabel);
    expect(getByTestId('type-value')).toHaveTextContent(typeMsg);
    expect(getByTestId('actions-label')).toHaveTextContent(stepsActionsLabel);
    expect(getByTestId('import-button')).toHaveTextContent(importBtnText);
    expect(getByTestId('cancel-button')).toHaveTextContent(cancelBtnText);
    expect(getByTestId('cancel-button').getAttribute('href')).toBe(cancelLink);
  });

  it('Should show all actions', () => {
    const { getByTestId } = render(componentWithDescription);
    expect(getByTestId('actions-container').childElementCount).toBe(
      actions.length
    );
    getByTestId('actions-container').childNodes.forEach((actionNode, index) => {
      expect(actionNode).toHaveTextContent(
        actionText(actions[index].name, actions[index].description)
      );
    });
  });

  it('Test import click event', () => {
    const { getByTestId } = render(componentWithDescription);
    fireEvent.click(getByTestId('import-button'));
    expect(mockOnImport).toHaveBeenCalled();
  });
});

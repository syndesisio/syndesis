import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { fireEvent, render } from 'react-testing-library';
import {
  ExtensionImportReview,
  IExtensionImportReviewProps,
  IImportAction,
} from '../../src/Customization';

export default describe('ExtensionImportReview', () => {
  const mockOnImport = jest.fn();
  const mockActionText = jest.fn();

  const fourActions = [
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

  const oneAction = [fourActions[1]] as IImportAction[];

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
  const uid = 'uid';

  const props = {
    actions: fourActions,
    cancelLink: cancelLink,
    extensionDescription: description,
    extensionId: id,
    extensionName: name,
    extensionUid: uid,
    i18nActionsLabel: stepsActionsLabel,
    i18nCancel: cancelBtnText,
    i18nDescriptionLabel: descriptionLabel,
    i18nExtensionTypeMessage: typeMsg,
    i18nIdLabel: idLabel,
    i18nImport: importBtnText,
    i18nNameLabel: nameLabel,
    i18nTitle: title,
    i18nTypeLabel: typeLabel,
    i18nActionText: mockActionText,
    onImport: mockOnImport,
  } as IExtensionImportReviewProps;

  const { extensionDescription, ...noDescriptionProps } = props;

  beforeEach(() => {
    mockOnImport.mockReset();
    mockActionText.mockReset();
  });

  it('Should render correctly with description', () => {
    const comp = (
      <MemoryRouter>
        <ExtensionImportReview {...props} />
      </MemoryRouter>
    );

    const { getByText, queryAllByText } = render(comp);

    // title
    expect(queryAllByText(title)).toHaveLength(1);

    // id label
    expect(queryAllByText(idLabel)).toHaveLength(1);

    // id value
    expect(queryAllByText(id)).toHaveLength(1);

    // name label
    expect(queryAllByText(nameLabel)).toHaveLength(1);

    // name value
    expect(queryAllByText(name)).toHaveLength(1);

    // description label
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);

    // description value
    expect(queryAllByText(description)).toHaveLength(1);

    // import button
    expect(queryAllByText(importBtnText)).toHaveLength(1);
    fireEvent.click(getByText(importBtnText));
    expect(mockOnImport).toHaveBeenCalledTimes(1);

    // cancel button
    expect(queryAllByText(cancelBtnText)).toHaveLength(1);
    const cancelBtn = getByText(cancelBtnText);
    fireEvent.click(cancelBtn);
    expect(mockOnImport).toHaveBeenCalledTimes(1);
    expect(cancelBtn).toHaveAttribute('href', cancelLink);

    // actions
    fourActions.map((a, index) => {
      expect(mockActionText.mock.calls[index][0]).toBe(a.name);
      expect(mockActionText.mock.calls[index][1]).toBe(a.description);
    });
  });

  it('Should render correctly without a description', () => {
    const comp = (
      <MemoryRouter>
        <ExtensionImportReview {...noDescriptionProps} actions={oneAction} />
      </MemoryRouter>
    );

    const { getByText, queryAllByText, queryByText } = render(comp);

    // title
    expect(queryAllByText(title)).toHaveLength(1);

    // id label
    expect(queryAllByText(idLabel)).toHaveLength(1);

    // id value
    expect(queryAllByText(id)).toHaveLength(1);

    // name label
    expect(queryAllByText(nameLabel)).toHaveLength(1);

    // name value
    expect(queryAllByText(name)).toHaveLength(1);

    // description label
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);

    // description value
    expect(queryByText(description)).toBeNull();

    // import button
    expect(queryAllByText(importBtnText)).toHaveLength(1);
    fireEvent.click(getByText(importBtnText));
    expect(mockOnImport).toHaveBeenCalledTimes(1);

    // cancel button
    expect(queryAllByText(cancelBtnText)).toHaveLength(1);
    const cancelBtn = getByText(cancelBtnText);
    fireEvent.click(cancelBtn);
    expect(mockOnImport).toHaveBeenCalledTimes(1);
    expect(cancelBtn).toHaveAttribute('href', cancelLink);

    // actions
    oneAction.map((a, index) => {
      expect(mockActionText.mock.calls[index][0]).toBe(a.name);
      expect(mockActionText.mock.calls[index][1]).toBe(a.description);
    });
  });

  it('Should render correctly without actions', () => {
    const comp = (
      <MemoryRouter>
        <ExtensionImportReview {...noDescriptionProps} actions={undefined} />
      </MemoryRouter>
    );

    const { getByText, queryAllByText, queryByText } = render(comp);

    // title
    expect(queryAllByText(title)).toHaveLength(1);

    // id label
    expect(queryAllByText(idLabel)).toHaveLength(1);

    // id value
    expect(queryAllByText(id)).toHaveLength(1);

    // name label
    expect(queryAllByText(nameLabel)).toHaveLength(1);

    // name value
    expect(queryAllByText(name)).toHaveLength(1);

    // description label
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);

    // description value
    expect(queryByText(description)).toBeNull();

    // import button
    expect(queryAllByText(importBtnText)).toHaveLength(1);
    fireEvent.click(getByText(importBtnText));
    expect(mockOnImport).toHaveBeenCalledTimes(1);

    // cancel button
    expect(queryAllByText(cancelBtnText)).toHaveLength(1);
    const cancelBtn = getByText(cancelBtnText);
    fireEvent.click(cancelBtn);
    expect(mockOnImport).toHaveBeenCalledTimes(1);
    expect(cancelBtn).toHaveAttribute('href', cancelLink);
  });
});

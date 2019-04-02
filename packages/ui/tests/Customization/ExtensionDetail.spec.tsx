import * as React from 'react';
import { fireEvent, render, waitForElement } from 'react-testing-library';
import {
  ExtensionDetail,
  IExtensionDetailProps,
} from '../../src/Customization';

export default describe('ExtensionDetail', () => {
  const mockOnDelete = jest.fn();
  const mockOnUpdate = jest.fn();

  const cancelLabel = 'Cancel';
  const deleteLabel = 'Delete';
  const deleteModalMessage = 'Are you sure you want to delete the extension?';
  const deleteModalTitle = 'Confirm Delete?';
  const deleteTip = 'Delete this extension';
  const name = 'Loop';
  const extensionId = 'io.syndesis.extensions:syndesis-extension-loop';
  const idMsg = '(ID: ' + extensionId + ')';
  const overviewLabel = 'Overview';
  const supportedStepsText = 'Supported Steps';
  const updateLabel = 'Update';
  const updateTip = 'Update this extension';
  const usageLabel = 'Usage';
  const uses = 2;

  const props = {
    extensionName: name,
    extensionUses: uses,
    i18nCancelText: cancelLabel,
    i18nDelete: deleteLabel,
    i18nDeleteModalMessage: deleteModalMessage,
    i18nDeleteModalTitle: deleteModalTitle,
    i18nDeleteTip: deleteTip,
    i18nIdMessage: idMsg,
    i18nOverviewSectionTitle: overviewLabel,
    i18nSupportsSectionTitle: supportedStepsText,
    i18nUpdate: updateLabel,
    i18nUpdateTip: updateTip,
    i18nUsageSectionTitle: usageLabel,
    integrationsSection: <div />,
    onDelete: mockOnDelete,
    onUpdate: mockOnUpdate,
    overviewSection: <div />,
    supportsSection: <div />,
  } as IExtensionDetailProps;

  beforeEach(() => {
    mockOnDelete.mockReset();
    mockOnUpdate.mockReset();
  });

  it('Should render correctly', () => {
    const comp = <ExtensionDetail {...props} />;
    const { getByText, queryAllByText } = render(comp);

    // extension name
    expect(queryAllByText(name)).toHaveLength(1);

    // extension ID message
    expect(queryAllByText(idMsg)).toHaveLength(1);

    // update button
    expect(queryAllByText(updateLabel)).toHaveLength(1);
    fireEvent.click(getByText(updateLabel));
    expect(mockOnUpdate).toHaveBeenCalledTimes(1);

    // delete button
    expect(queryAllByText(deleteLabel)).toHaveLength(1);
    const deleteButton = getByText(deleteLabel);
    expect(deleteButton).toHaveAttribute('disabled'); // delete should be disabled

    // overview section title
    expect(queryAllByText(overviewLabel)).toHaveLength(1);

    // supports section title
    expect(queryAllByText(supportedStepsText)).toHaveLength(1);

    // usage section title
    expect(queryAllByText(usageLabel)).toHaveLength(1);
  });

  it('Should open delete confirmation modal', async () => {
    // need to set extensionUses to zero so that the delete button is enabled
    const comp = <ExtensionDetail {...props} extensionUses={0} />;
    const { baseElement, getByText } = render(comp);
    const deleteButton = getByText(deleteLabel);
    expect(deleteButton).not.toHaveAttribute('disabled'); // delete should be enabled

    // click the delete button so that the delete confirmation dialog opens
    fireEvent.click(deleteButton);

    // wait for the delete dialog to show by looking for the delete button
    const elements = await waitForElement(() => {
      // find the delete button
      return baseElement.getElementsByClassName('btn btn-danger');
    });
    expect(elements).toHaveLength(1);

    // click the confirmation dialog delete button and make sure callback is called
    fireEvent.click(elements[0]);
    expect(mockOnDelete).toHaveBeenCalledTimes(1);
  });
});

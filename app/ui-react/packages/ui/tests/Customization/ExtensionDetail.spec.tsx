import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  fireEvent,
  getByText as getDialogButton,
  render,
  wait,
} from 'react-testing-library';
import {
  ExtensionDetail,
  IExtensionDetailProps,
} from '../../src/Customization';

export default describe('ExtensionDetail', () => {
  const mockOnDelete = jest.fn();

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
    extensionIcon: <div />,
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
    linkUpdateExtension: '/extensions/update',
    onDelete: mockOnDelete,
    overviewSection: <div />,
    supportsSection: <div />,
  } as IExtensionDetailProps;

  beforeEach(() => {
    mockOnDelete.mockReset();
  });

  it('Should render correctly', () => {
    const comp = (
      <Router>
        <ExtensionDetail {...props} />
      </Router>
    );

    const { getByText, queryAllByText } = render(comp);

    // extension name
    expect(queryAllByText(name)).toHaveLength(1);

    // extension ID message
    expect(queryAllByText(idMsg)).toHaveLength(1);

    // update button
    expect(queryAllByText(updateLabel)).toHaveLength(1);

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

  it('Should click delete button on the delete confirmation modal', () => {
    // need to set extensionUses to zero so that the delete button is enabled
    const comp = (
      <Router>
        <>
          <ExtensionDetail {...props} extensionUses={0} />;
        </>
      </Router>
    );

    const { getAllByRole, getByText } = render(comp);
    const deleteButton = getByText(deleteLabel);
    expect(deleteButton).not.toHaveAttribute('disabled'); // delete should be enabled

    // click the delete button so that the delete confirmation dialog opens
    fireEvent.click(deleteButton);

    // click the confirmation dialog delete button and make sure callback is called
    const dialog = getAllByRole('dialog')[0];
    fireEvent.click(getDialogButton(dialog, deleteLabel));
    expect(mockOnDelete).toHaveBeenCalledTimes(1);
  });

  it('Should click cancel button on the delete confirmation modal', async () => {
    // need to set extensionUses to zero so that the delete button is enabled
    const comp = (
      <Router>
        <ExtensionDetail {...props} extensionUses={0} />
      </Router>
    );
    const { getAllByRole, getByText, queryByRole } = render(comp);
    const deleteButton = getByText(deleteLabel);
    expect(deleteButton).not.toHaveAttribute('disabled'); // delete should be enabled

    // click the delete button so that the delete confirmation dialog opens
    fireEvent.click(deleteButton);

    // click the confirmation dialog cancel button and make sure dialog disappears
    const dialog = getAllByRole('dialog')[0];
    fireEvent.click(getDialogButton(dialog, cancelLabel));
    await wait(() => expect(queryByRole('dialog')).toBeNull());
  });
});

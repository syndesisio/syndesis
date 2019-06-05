import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  fireEvent,
  getByText as getDialogButton,
  render,
  wait,
} from 'react-testing-library';
import {
  ExtensionListItem,
  IExtensionListItemProps,
} from '../../src/Customization';

export default describe('ExtensionListItem', () => {
  const extensionDescription = 'Add a loop';
  const extensionId = 'i-LWCfT7kHEGQFjGiGu8-z';
  const extensionName = 'Loop';
  const extensionType = 'Step Extension';

  const cancelText = 'Cancel';
  const deleteText = 'Delete';
  const deleteModalMessage = 'Are you sure you want to delete the extension?';
  const deleteModalTitle = 'Confirm Delete?';
  const deleteTip = 'Delete this extension';
  const detailsLink = '/extensions/' + extensionId;
  const detailsText = 'Details';
  const detailsTip = 'View extension details';
  const updateText = 'Update';
  const updateTip = 'Update this extension';
  const usedByFive = 5;
  const usedByFiveMsg = 'Used by 5 integrations';
  const usedByZero = 0;
  const usedByZeroMsg = 'Not used by any integrations';

  const mockOnDelete = jest.fn();

  const props = {
    detailsPageLink: detailsLink,
    extensionDescription: extensionDescription,
    extensionIcon: <div />,
    extensionId: extensionId,
    extensionName: extensionName,
    i18nCancelText: cancelText,
    i18nDelete: deleteText,
    i18nDeleteModalMessage: deleteModalMessage,
    i18nDeleteModalTitle: deleteModalTitle,
    i18nDeleteTip: deleteTip,
    i18nDetails: detailsText,
    i18nDetailsTip: detailsTip,
    i18nExtensionType: extensionType,
    i18nUpdate: updateText,
    i18nUpdateTip: updateTip,
    i18nUsedByMessage: usedByFiveMsg,
    linkUpdateExtension: '/extensions/update',
    onDelete: mockOnDelete,
    usedBy: usedByFive,
  } as IExtensionListItemProps;

  beforeEach(() => {
    mockOnDelete.mockReset();
  });

  it('Should render correctly for in-use extension', () => {
    const comp = (
      <Router>
        <ExtensionListItem {...props} />
      </Router>
    );

    const { getByText, queryAllByText } = render(comp);

    // extension type
    expect(queryAllByText(extensionType)).toHaveLength(1);

    // used by message
    expect(queryAllByText(usedByFiveMsg)).toHaveLength(1);

    // description
    expect(queryAllByText(extensionDescription)).toHaveLength(1);

    // name
    expect(queryAllByText(extensionName)).toHaveLength(1);

    // update button
    expect(queryAllByText(updateText)).toHaveLength(1);

    // delete button
    expect(queryAllByText(deleteText)).toHaveLength(1);
    const deleteButton = getByText(deleteText);
    expect(deleteButton).toHaveAttribute('disabled'); // delete should be disabled
  });

  it('Should render correctly for NOT in-use extension', () => {
    const comp = (
      <Router>
        <ExtensionListItem
          {...props}
          usedBy={usedByZero}
          i18nUsedByMessage={usedByZeroMsg}
        />
      </Router>
    );

    const { getAllByRole, getByText, queryAllByText } = render(comp);

    // extension type
    expect(queryAllByText(extensionType)).toHaveLength(1);

    // used by message
    expect(queryAllByText(usedByZeroMsg)).toHaveLength(1);

    // description
    expect(queryAllByText(extensionDescription)).toHaveLength(1);

    // name
    expect(queryAllByText(extensionName)).toHaveLength(1);

    // update button
    expect(queryAllByText(updateText)).toHaveLength(1);

    // delete button
    const deleteButton = getByText(deleteText);
    expect(deleteButton).not.toHaveAttribute('disabled'); // delete should be enabled

    // click the delete button so that the delete confirmation dialog opens
    fireEvent.click(deleteButton);

    // click the confirmation dialog delete button and make sure callback is called
    const dialog = getAllByRole('dialog')[0];
    fireEvent.click(getDialogButton(dialog, deleteText));
    expect(mockOnDelete).toHaveBeenCalledTimes(1);
  });

  it('Should click cancel button on the delete confirmation modal', async () => {
    // need to set extensionUses to zero so that the delete button is enabled
    const comp = (
      <Router>
        <ExtensionListItem {...props} usedBy={usedByZero} />
      </Router>
    );
    const { getAllByRole, getByText, queryByRole } = render(comp);
    const deleteButton = getByText(deleteText);
    expect(deleteButton).not.toHaveAttribute('disabled'); // delete should be enabled

    // click the delete button so that the delete confirmation dialog opens
    fireEvent.click(deleteButton);

    // click the confirmation dialog cancel button and make sure dialog disappears
    const dialog = getAllByRole('dialog')[0];
    fireEvent.click(getDialogButton(dialog, cancelText));
    await wait(() => expect(queryByRole('dialog')).toBeNull());
  });
});

import * as React from 'react';
import { fireEvent, render } from 'react-testing-library';
import renderer from 'react-test-renderer';
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

    // test snapshot
    const snapshot = renderer.create(comp).toJSON();
    expect(snapshot).toMatchSnapshot();
  });

  it('Should open delete confirmation modal', () => {
    const comp = <ExtensionDetail {...props} extensionUses={0} />;
    const { baseElement, getByText } = render(comp);
    const deleteButton = getByText(deleteLabel);
    expect(deleteButton).not.toHaveAttribute('disabled'); // delete should be enabled
    fireEvent.click(deleteButton); // open delete confirmation dialog
    expect(baseElement.classList).toContain('modal-open');

    // test snapshot
    const snapshot = renderer.create(comp).toJSON();
    expect(snapshot).toMatchSnapshot();
  });
});

import * as React from 'react';
import { render } from 'react-testing-library';
import {
  ExtensionImportCard,
  IExtensionImportCardProps,
} from '../../src/Customization';

export default describe('ExtensionImportCard', () => {
  const dndInstructions =
    "Drag 'n' drop a file here, or click to select a file using a file chooser dialog.";
  const helpMessage = 'Accepted file type: .jar';
  const noFileSelectedMessage = 'no file selected';
  const selectedFileLabel = 'Selected file:';

  const mockOnUploadAccepted = jest.fn();
  const mockOnUploadRejected = jest.fn();

  const props = {
    dndDisabled: false,
    i18nAlertMessage: undefined,
    i18nDndHelpMessage: helpMessage,
    i18nDndInstructions: dndInstructions,
    i18nDndNoFileSelectedMessage: noFileSelectedMessage,
    i18nDndSelectedFileLabel: selectedFileLabel,
    i18nDndUploadFailedMessage: undefined,
    i18nDndUploadSuccessMessage: undefined,
    onDndUploadAccepted: mockOnUploadAccepted,
    onDndUploadRejected: mockOnUploadRejected,
  } as IExtensionImportCardProps;

  it('Should render correctly', () => {
    const comp = <ExtensionImportCard {...props} />;

    const { queryAllByText } = render(comp);

    // Dnd help message
    expect(queryAllByText(helpMessage)).toHaveLength(1);

    // Dnd instructions
    expect(queryAllByText(dndInstructions)).toHaveLength(1);

    // selected file label
    expect(queryAllByText(selectedFileLabel)).toHaveLength(1);

    // no file selected message is shown initially
    expect(queryAllByText(noFileSelectedMessage)).toHaveLength(1);
  });

  it('Should render alert', () => {
    const alertMessage = 'This is an alert message';
    const comp = (
      <ExtensionImportCard {...props} i18nAlertMessage={alertMessage} />
    );

    const { queryAllByText } = render(comp);

    // alert
    expect(queryAllByText(alertMessage)).toHaveLength(1);
  });

  it('Should disable DndFileChooser', () => {
    const comp = <ExtensionImportCard {...props} dndDisabled={true} />;

    const { container } = render(comp);

    // make sure disabled
    const chooser = container.querySelector('.dnd-file-chooser');
    expect(chooser).toBeDefined();
    expect(chooser).toHaveAttribute('disabled');
  });
});

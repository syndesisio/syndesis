import { Card, CardBody } from '@patternfly/react-core';
import { Alert } from 'patternfly-react';
import * as React from 'react';
import { DndFileChooser } from '../../Shared/DndFileChooser';

export interface IExtensionImportCardProps {
  /**
   * `true` if the dropzone should be disabled.
   */
  dndDisabled: boolean;

  /**
   * A localized alert message when a jar file cannot be processed as an extension.
   */
  i18nAlertMessage?: string;

  /**
   * The localized text that appears below the selected file information at the bottom of the drop area. This
   * message should identify the accepted file extension.
   */
  i18nDndHelpMessage: string;

  /**
   * The localized text (may include HTML tags) that appears above the selected file information at the
   * top of the drop area. This message should describe how the DnD works.
   */
  i18nDndInstructions: string;

  /**
   * The localized text that appears when no file has been selected.
   */
  i18nDndNoFileSelectedMessage: string;

  /**
   * The localized text for the label that appears before the selected file information.
   */
  i18nDndSelectedFileLabel: string;

  /**
   * A general, localized message for when a file upload failed. This message will be shown
   * along with an error icon and appears next to the selected file message. If this property
   * is set then `i18nDndUploadSuccessMessage` should not be set.
   */
  i18nDndUploadFailedMessage?: string;

  /**
   * A general, localized message for when a file upload was successful. This message will be shown
   * along with an OK icon and appear next to the selected file message. If this property
   * is set then `i18nDndUploadFailedMessage` should not be set.
   */
  i18nDndUploadSuccessMessage?: string;

  /**
   * Callback for when one or more file uploads have been accepted. Caller should handler processing of the files.
   */
  onDndUploadAccepted(files: File[]): void;

  /**
   * The localized text (may include HTML tags) that appears when the file upload fails.
   */
  onDndUploadRejected(fileName: string): string;
}

export const ExtensionImportCard: React.FunctionComponent<
  IExtensionImportCardProps
> = props => {
  return (
    <Card>
      <CardBody>
        {props.i18nAlertMessage ? (
          <Alert type={'error'}>
            <span>{props.i18nAlertMessage}</span>
          </Alert>
        ) : null}
        <DndFileChooser
          disableDropzone={props.dndDisabled}
          fileExtensions={'.jar'}
          i18nHelpMessage={props.i18nDndHelpMessage}
          i18nInstructions={props.i18nDndInstructions}
          i18nNoFileSelectedMessage={props.i18nDndNoFileSelectedMessage}
          i18nSelectedFileLabel={props.i18nDndSelectedFileLabel}
          i18nUploadFailedMessage={props.i18nDndUploadFailedMessage}
          i18nUploadSuccessMessage={props.i18nDndUploadSuccessMessage}
          onUploadAccepted={props.onDndUploadAccepted}
          onUploadRejected={props.onDndUploadRejected}
        />
      </CardBody>
    </Card>
  );
};

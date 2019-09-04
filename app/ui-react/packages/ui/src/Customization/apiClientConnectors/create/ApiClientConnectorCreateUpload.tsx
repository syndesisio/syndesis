import { Card, CardBody, CardHeader, Title } from '@patternfly/react-core';
import * as React from 'react';
import { DndFileChooser } from '../../../Shared';

export interface IApiClientConnectorCreateUploadProps {
  /**
   * `true` if the dropzone should be disabled.
   */
  dndDisabled: boolean;

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
   * The localized title text that appears above the DnD component.
   */
  i18nTitle: string;

  /**
   * Callback for when one or more file uploads have been accepted. Caller should handler processing of the files.
   */
  onDndUploadAccepted(files: File[]): void;

  /**
   * The localized text (may include HTML tags) that appears when the file upload fails.
   */
  onDndUploadRejected(fileName: string): string;
}

export class ApiClientConnectorCreateUpload extends React.Component<
  IApiClientConnectorCreateUploadProps
> {
  public render() {
    return (
      /**
       * TODO: Move drag and drop stuff to its own component,
       * keep this is as a list of options: Upload or Use a URL
       */
      <Card>
        <CardHeader>
          <Title size="lg">{this.props.i18nTitle}</Title>
        </CardHeader>
        <CardBody>
          <DndFileChooser
            disableDropzone={this.props.dndDisabled}
            fileExtensions={'.json'}
            i18nHelpMessage={this.props.i18nDndHelpMessage}
            i18nInstructions={this.props.i18nDndInstructions}
            i18nNoFileSelectedMessage={this.props.i18nDndNoFileSelectedMessage}
            i18nSelectedFileLabel={this.props.i18nDndSelectedFileLabel}
            i18nUploadFailedMessage={this.props.i18nDndUploadFailedMessage}
            i18nUploadSuccessMessage={this.props.i18nDndUploadSuccessMessage}
            onUploadAccepted={this.props.onDndUploadAccepted}
            onUploadRejected={this.props.onDndUploadRejected}
          />
        </CardBody>
      </Card>
    );
  }
}

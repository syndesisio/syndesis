import { Text } from '@patternfly/react-core';
import { FormControl, ListView } from 'patternfly-react';
import * as React from 'react';

import './ApiProviderSelectMethod.css';
import { Container } from '../../../Layout';
import { DndFileChooser } from '../../../Shared';

export interface IApiProviderSelectMethodProps {
  /**
   * Description of the page
   */
  i18nDescription?: string;
  /**
   * Locale string for each method available for retrieving the
   * OpenAPI specification.
   */
  i18nMethodFromFile: string;
  i18nMethodFromUrl: string;
  i18nMethodFromScratch: string;
  /**
   * Notice for the input field if opting to provide a URL
   */
  i18nUrlNote: string;

  /**
   * Indicates if multiple files can be added. Defaults to `false`.
   */
  allowMultiple?: boolean;

  /**
   * `true` if the dropzone should be disabled. Defaults to `false`.
   */
  disableDropzone: boolean;

  /**
   * A comma delimited string of file extensions. For example, '.jar,.txt'. Defaults to any file extension.
   */
  fileExtensions?: string;

  /**
   * The localized text that appears below the selected file information at the bottom of the drop area.
   */
  i18nHelpMessage?: string;

  /**
   * The localized text (may include HTML tags) that appears above the selected file information at the
   * top of the drop area.
   */
  i18nInstructions: string;

  /**
   * The localized text that appears when no file has been selected.
   */
  i18nNoFileSelectedMessage: string;

  /**
   * The localized text for the label that appears before the selected file information.
   */
  i18nSelectedFileLabel: string;

  /**
   * A general, localized message for when a file upload failed. This message will be shown
   * along with an error icon and appears next to the selected file message. If this property
   * is set then `i18nUploadSuccessMessage` should not be set.
   */
  i18nUploadFailedMessage?: string;

  /**
   * A list of error messages for failed uploads
   */
  i18nUploadFailedMessages?: string[];

  /**
   * A general, localized message for when a file upload was successful. This message will be shown
   * along with an OK icon and appear next to the selected file message. If this property
   * is set then `i18nUploadFailedMessage` should not be set.
   */
  i18nUploadSuccessMessage?: string;

  /**
   * A list of error messages for successful uploads
   */
  i18nUploadSuccessMessages?: string[];

  /**
   * Obtains the localized text (may include HTML tags) that appears when the file upload was rejected. This
   * will occur when a DnD of a file with the wrong extension is dropped. This message is presented
   * as a timed toast notification.
   */
  onUploadRejected(fileName: string): string;

  /**
   * Callback for when one or more file uploads have been accepted. Caller should handler processing of the files.
   */
  onUploadAccepted(file: File[]): void;
}

export class ApiProviderSelectMethod extends React.Component<
  IApiProviderSelectMethodProps
> {
  public render() {
    return (
      <>
        <Text>{this.props.i18nDescription}</Text>
        <ListView
          id={'api-provider-select-method__list'}
          className={'api-provider-select-method__list'}
        >
          <ListView.Item
            key={'1'}
            checkboxInput={<input type="radio" />}
            heading={this.props.i18nMethodFromFile}
            stacked={false}
          >
            <Container style={{ margin: '50px' }}>
              <DndFileChooser
                disableDropzone={this.props.disableDropzone}
                fileExtensions={this.props.fileExtensions}
                i18nHelpMessage={this.props.i18nHelpMessage}
                i18nInstructions={this.props.i18nInstructions}
                i18nNoFileSelectedMessage={this.props.i18nNoFileSelectedMessage}
                i18nSelectedFileLabel={this.props.i18nSelectedFileLabel}
                i18nUploadFailedMessage={this.props.i18nUploadFailedMessage}
                i18nUploadSuccessMessage={this.props.i18nUploadSuccessMessage}
                onUploadAccepted={this.props.onUploadAccepted}
                onUploadRejected={this.props.onUploadRejected}
              />
            </Container>
          </ListView.Item>
          <ListView.Item
            key={'2'}
            checkboxInput={<input type="radio" />}
            heading={this.props.i18nMethodFromUrl}
            stacked={false}
          >
            <div>
              <FormControl type={'text'} />
            </div>
            <div>
              <span className={'url-note'}>{this.props.i18nUrlNote}</span>
            </div>
          </ListView.Item>
          <ListView.Item
            key={'3'}
            checkboxInput={<input type="radio" />}
            heading={this.props.i18nMethodFromScratch}
            stacked={false}
          />
        </ListView>
      </>
    );
  }
}

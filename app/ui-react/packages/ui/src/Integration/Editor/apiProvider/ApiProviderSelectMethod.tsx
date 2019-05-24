import { Text } from '@patternfly/react-core';
import { FormControl, ListView } from 'patternfly-react';
import * as React from 'react';

import { Container } from '../../../Layout';
import { DndFileChooser } from '../../../Shared';
import './ApiProviderSelectMethod.css';

export interface IApiProviderSelectMethodProps {
  allowMultiple?: boolean;
  disableDropzone: boolean;
  fileExtensions?: string;
  /**
   * Localized strings to be displayed.
   */
  i18nDescription?: string;
  i18nHelpMessage?: string;
  i18nInstructions: string;
  i18nMethodFromFile: string;
  i18nMethodFromUrl: string;
  i18nMethodFromScratch: string;
  i18nNoFileSelectedMessage: string;
  i18nSelectedFileLabel: string;
  i18nUploadFailedMessage?: string;
  i18nUploadFailedMessages?: string[];
  i18nUploadSuccessMessage?: string;
  i18nUploadSuccessMessages?: string[];
  i18nUrlNote: string;
  /**
   * Callback for when one or more file uploads have been accepted.
   */
  onUploadAccepted(file: File[]): void;
  /**
   * Obtains the localized text (may include HTML tags) that appears when the file upload was rejected. This
   * will occur when a DnD of a file with the wrong extension is dropped. This message is presented
   * as a timed toast notification.
   */
  onUploadRejected(fileName: string): string;
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
                allowMultiple={this.props.allowMultiple}
                disableDropzone={this.props.disableDropzone}
                fileExtensions={this.props.fileExtensions}
                i18nHelpMessage={this.props.i18nHelpMessage}
                i18nInstructions={this.props.i18nInstructions}
                i18nNoFileSelectedMessage={this.props.i18nNoFileSelectedMessage}
                i18nSelectedFileLabel={this.props.i18nSelectedFileLabel}
                i18nUploadFailedMessage={this.props.i18nUploadFailedMessage}
                i18nUploadFailedMessages={this.props.i18nUploadFailedMessages}
                i18nUploadSuccessMessage={this.props.i18nUploadSuccessMessage}
                i18nUploadSuccessMessages={this.props.i18nUploadSuccessMessages}
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

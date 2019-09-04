import { Card, CardBody } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../Layout';
import { DndFileChooser, SimplePageHeader } from '../../Shared';

export interface IImportPageUIProps {
  i18nPageTitle: string;
  i18nPageDescription: string;
  i18nNoFileSelectedMessage: string;
  i18nSelectedFileLabel: string;
  i18nInstructions: string;
  i18nUploadFailedMessages?: string[];
  i18nUploadSuccessMessages?: string[];
  i18nHelpMessage: string;
  onUploadRejected(fileName: string): string;
  onUploadAccepted(file: File[]): void;
}

export class ImportPageUI extends React.Component<IImportPageUIProps> {
  public render() {
    return (
      <>
        <SimplePageHeader
          i18nTitle={this.props.i18nPageTitle}
          i18nDescription={this.props.i18nPageDescription}
        />
        <PageSection>
          <Card>
            <CardBody data-testid={'import-page'}>
              <DndFileChooser
                allowMultiple={true}
                fileExtensions={'.zip'}
                onUploadRejected={this.props.onUploadRejected}
                disableDropzone={false}
                onUploadAccepted={this.props.onUploadAccepted}
                i18nNoFileSelectedMessage={this.props.i18nNoFileSelectedMessage}
                i18nSelectedFileLabel={this.props.i18nSelectedFileLabel}
                i18nInstructions={this.props.i18nInstructions}
                i18nUploadFailedMessages={this.props.i18nUploadFailedMessages}
                i18nUploadSuccessMessages={this.props.i18nUploadSuccessMessages}
                i18nHelpMessage={this.props.i18nHelpMessage}
              />
            </CardBody>
          </Card>
        </PageSection>
      </>
    );
  }
}

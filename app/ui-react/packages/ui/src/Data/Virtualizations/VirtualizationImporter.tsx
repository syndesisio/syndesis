import { Card, CardBody } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../Layout';
import { DndFileChooser, SimplePageHeader } from '../../Shared';

export interface IVirtualizationImporterProps {
  disableDnd: boolean;
  i18nHelpMessage: string;
  i18nInstructions: string;
  i18nNoFileSelectedMessage: string;
  i18nPageDescription: string;
  i18nPageTitle: string;
  i18nSelectedFileLabel: string;
  i18nUploadFailedMessages?: string[];
  i18nUploadSuccessMessages?: string[];
  onUploadAccepted(file: File[]): void;
  onUploadRejected(fileName: string): string;
}

export const VirtualizationImporter: React.FunctionComponent<
  IVirtualizationImporterProps
> = props => {
  return (
    <>
      <SimplePageHeader
        i18nTitle={props.i18nPageTitle}
        i18nDescription={props.i18nPageDescription}
      />
      <PageSection>
        <Card>
          <CardBody data-testid={'import-page'}>
            <DndFileChooser
              allowMultiple={false}
              disableDropzone={props.disableDnd}
              fileExtensions={'.zip'}
              i18nHelpMessage={props.i18nHelpMessage}
              i18nInstructions={props.i18nInstructions}
              i18nNoFileSelectedMessage={props.i18nNoFileSelectedMessage}
              i18nSelectedFileLabel={props.i18nSelectedFileLabel}
              i18nUploadFailedMessages={props.i18nUploadFailedMessages}
              i18nUploadSuccessMessages={props.i18nUploadSuccessMessages}
              onUploadRejected={props.onUploadRejected}
              onUploadAccepted={props.onUploadAccepted}
            />
          </CardBody>
        </Card>
      </PageSection>
    </>
  );
};

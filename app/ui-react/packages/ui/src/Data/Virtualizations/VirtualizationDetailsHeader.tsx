import { Split, SplitItem, Stack, StackItem } from '@patternfly/react-core';
import { Icon } from 'patternfly-react';
import * as React from 'react';
import { Loader, PageSection } from '../../Layout';
import { InlineTextEdit } from '../../Shared';
import { VirtualizationPublishState } from './models';
import { PublishStatusWithProgress } from './PublishStatusWithProgress';
import './VirtualizationDetailsHeader.css';

export interface IVirtualizationDetailsHeaderProps {
  i18nDescriptionPlaceholder: string;
  i18nDraft: string;
  i18nError: string;
  i18nInUseText: string;
  i18nPublished: string;
  i18nPublishInProgress: string;
  i18nUnpublishInProgress: string;
  i18nPublishLogUrlText: string;
  odataUrl?: string;
  publishedState: VirtualizationPublishState | 'Loading';
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
  virtualizationDescription?: string;
  virtualizationName: string;
  isWorking: boolean;
  onChangeDescription: (newDescription: string) => Promise<boolean>;
}

/**
 * Line 1: name and status
 * Line 2: description label and value
 */
export const VirtualizationDetailsHeader: React.FunctionComponent<
  IVirtualizationDetailsHeaderProps
> = props => {
  return (
    <PageSection variant={'light'}>
      <Stack gutter="md">
        <StackItem>
          <Split gutter="md" className={'virtualization-details-header__row'}>
            <SplitItem className="virtualization-details-header__virtualizationName">
              {props.virtualizationName}
            </SplitItem>
            <SplitItem>
              {props.publishedState !== 'Loading' ? (
                <PublishStatusWithProgress
                  publishedState={props.publishedState}
                  i18nError={props.i18nError}
                  i18nPublished={props.i18nPublished}
                  i18nUnpublished={props.i18nDraft}
                  i18nPublishInProgress={props.i18nPublishInProgress}
                  i18nUnpublishInProgress={props.i18nUnpublishInProgress}
                  i18nPublishLogUrlText={props.i18nPublishLogUrlText}
                  publishingCurrentStep={props.publishingCurrentStep}
                  publishingLogUrl={props.publishingLogUrl}
                  publishingTotalSteps={props.publishingTotalSteps}
                  publishingStepText={props.publishingStepText}
                />
              ) : (
                <Loader size={'sm'} inline={true} />
              )}
              {props.odataUrl && props.publishedState !== 'Loading' && (
                <span>
                  <a
                    data-testid={'virtualization-details-header-odataUrl'}
                    target="_blank"
                    href={props.odataUrl}
                  >
                    {props.odataUrl}
                    <Icon
                      className={
                        'virtualization-details-header-odata-link-icon'
                      }
                      name={'external-link'}
                    />
                  </a>
                </span>
              )}
            </SplitItem>
            <SplitItem>
              {props.i18nInUseText}
            </SplitItem>
          </Split>
        </StackItem>
        <StackItem>
          <InlineTextEdit
            value={props.virtualizationDescription || ''}
            allowEditing={!props.isWorking}
            i18nPlaceholder={props.i18nDescriptionPlaceholder}
            isTextArea={true}
            onChange={props.onChangeDescription}
          />
        </StackItem>
      </Stack>
    </PageSection>
  );
};

import { Split, SplitItem, Stack, StackItem } from '@patternfly/react-core';
import { Icon } from 'patternfly-react';
import * as React from 'react';
import { Loader, PageSection } from '../../Layout';
import { InlineTextEdit } from '../../Shared';
import { VirtualizationPublishState } from './models';
import { PublishStatusWithProgress } from './PublishStatusWithProgress';
import './VirtualizationDetailsHeader.css';

export interface IVirtualizationDetailsHeaderProps {
  isProgressWithLink: boolean;
  i18nPublishState: string;
  i18nDescriptionPlaceholder: string;
  i18nInUseText: string;
  i18nPublishLogUrlText: string;
  labelType: 'danger' | 'primary' | 'default';
  modified: boolean;
  odataUrl?: string;
  publishedState: VirtualizationPublishState;
  publishedVersion?: number;
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
    <PageSection className={'virtualization-details-header'} variant={'light'}>
      <Stack gutter="md">
        <StackItem>
          <Split gutter="md" className={'virtualization-details-header__row'}>
            <SplitItem className="virtualization-details-header__virtualizationName">
              {props.virtualizationName}
            </SplitItem>
            <SplitItem>
              {!props.isWorking ? (
                <PublishStatusWithProgress
                  isProgressWithLink={props.isProgressWithLink}
                  i18nPublishState={props.i18nPublishState}
                  i18nPublishLogUrlText={props.i18nPublishLogUrlText}
                  labelType={props.labelType}
                  modified={props.modified}
                  publishingCurrentStep={props.publishingCurrentStep}
                  publishingLogUrl={props.publishingLogUrl}
                  publishingTotalSteps={props.publishingTotalSteps}
                  publishingStepText={props.publishingStepText}
                  publishVersion={props.publishedVersion}
                />
              ) : (
                <Loader size={'sm'} inline={true} />
              )}
              {props.odataUrl && props.publishedState && (
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
            <SplitItem
              className={'virtualization-details-header__usedByMessage'}
              isFilled={true}
            >
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

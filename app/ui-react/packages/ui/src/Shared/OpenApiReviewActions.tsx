import {
  Label,
  Stack,
  StackItem,
  Text,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  TextVariants,
  Title,
} from '@patternfly/react-core';
import {
  global_danger_color_100,
  global_warning_color_100,
} from '@patternfly/react-tokens';
import * as React from 'react';

import './OpenApiReviewActions.css';

export interface IApiProviderReviewActionsProps {
  actions?: React.ReactNode;
  alert?: React.ReactNode;
  apiProviderDescription?: string;
  apiProviderName?: string;
  errorMessages?: string[];
  i18nApiDefinitionHeading: string;
  i18nDescriptionLabel: string;
  i18nErrorsHeading?: string;
  i18nImportedHeading: string;
  i18nNameLabel: string;
  i18nOperationsHtmlMessage: string;
  i18nOperationTagHtmlMessages?: string[];
  i18nValidationFallbackMessage?: string;
  i18nWarningsHeading?: string;
  warningMessages?: string[];
}

export const OpenApiReviewActions: React.FunctionComponent<
  IApiProviderReviewActionsProps
> = (
  {
    actions,
    alert,
    errorMessages,
    i18nApiDefinitionHeading,
    i18nDescriptionLabel,
    i18nErrorsHeading,
    i18nImportedHeading,
    i18nNameLabel,
    i18nOperationsHtmlMessage,
    i18nOperationTagHtmlMessages,
    i18nValidationFallbackMessage,
    i18nWarningsHeading,
    apiProviderDescription,
    apiProviderName,
    warningMessages,
  }) => {
  return (
    <Stack className={'open-api-review-actions'}>
      <StackItem>
        {i18nValidationFallbackMessage ? (
          <h5 className={'review-actions__validationFallbackMessage'}>
            {i18nValidationFallbackMessage}
          </h5>
        ) : (
          <TextContent>
            {alert && alert}
            <Title
              headingLevel={'h5'}
              size={'md'}
              className={'review-actions__heading'}
            >
              {i18nApiDefinitionHeading}
            </Title>
            <div className={'review-actions__name-description'}>
              <TextList component={TextListVariants.dl}>
                <TextListItem component={TextListItemVariants.dt}>
                  {i18nNameLabel}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dd}>
                  {apiProviderName}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dt}>
                  {i18nDescriptionLabel}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dd}>
                  {apiProviderDescription}
                </TextListItem>
              </TextList>
            </div>
            <Title
              headingLevel={'h5'}
              size={'md'}
              className={'review-actions__heading'}
            >
              {i18nImportedHeading}
            </Title>
            <div>
              <Text
                component={TextVariants.p}
                dangerouslySetInnerHTML={{
                  __html: i18nOperationsHtmlMessage,
                }}
              />
            </div>

            {/* tagged messages */}
            {i18nOperationTagHtmlMessages && (
              <TextList className={'review-actions__tagMessageList'}>
                {i18nOperationTagHtmlMessages.map(
                  (msg: string, index: number) => (
                    <TextListItem
                      key={index}
                      dangerouslySetInnerHTML={{ __html: msg }}
                    />
                  )
                )}
              </TextList>
            )}

            {/* error messages */}
            {i18nErrorsHeading && errorMessages && (
              <Title
                data-testid={'api-provider-error-heading'}
                headingLevel={'h5'}
                size={'md'}
                className={'review-actions__heading'}
              >
                {i18nErrorsHeading}
                <Label
                  style={{ background: global_danger_color_100.value }}
                  className={'heading__label'}
                >
                  {errorMessages.length}
                </Label>
              </Title>
            )}
            <div className={'review-actions__errors'}>
              {errorMessages
                ? errorMessages.map(
                  (errorMsg: string, index: number) => (
                    <Text component={TextVariants.p} key={index}>
                      {index + 1}. {errorMsg}
                    </Text>
                  )
                )
                : null}
            </div>

            {/* warning messages */}
            {i18nWarningsHeading && warningMessages && (
              <Title
                data-testid={'api-provider-warning-heading'}
                headingLevel={'h5'}
                size={'md'}
                className={'review-actions__heading'}
              >
                {i18nWarningsHeading}
                <Label
                  style={{ background: global_warning_color_100.value }}
                  className={'heading__label'}
                >
                  {warningMessages.length}
                </Label>
              </Title>
            )}
            <div className={'review-actions__warnings'}>
              {warningMessages
                ? warningMessages.map(
                  (warningMsg: string, index: number) => (
                    <Text key={index} component={TextVariants.p}>
                      {index + 1}. {warningMsg}
                    </Text>
                  )
                )
                : null}
            </div>
          </TextContent>
        )}
        {actions && (
          <>
            <br />
            {actions}
          </>
        )}
      </StackItem>
    </Stack>
  );
}

import { Alert, Form, Stack, StackItem, Title } from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, Loader } from '../Layout';
import { ERROR, WARNING } from '../Shared';

export interface IConnectorConfigurationFormValidationResult {
  message: string;
  type: 'error' | 'success';
}

export interface IConnectorConfigurationFormProps {
  /**
   * The internationalized form title.
   */
  i18nFormTitle?: string;
  i18nValidate: string;
  isNextDisabled: boolean;
  isNextLoading: boolean;
  isValidating: boolean;
  /**
   * Form level validationResults
   */
  validationResults?: IConnectorConfigurationFormValidationResult[];
  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;
  onValidate?: (e: React.MouseEvent<any>) => void;
}

/**
 * A component to render a connector configuration form in the create connection
 * wizard. This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IConnectorConfigurationFormProps#i18nTitle}
 */
export const ConnectorConfigurationForm: React.FunctionComponent<IConnectorConfigurationFormProps> = ({
  children,
  i18nFormTitle,
  i18nValidate,
  isNextLoading,
  isNextDisabled,
  isValidating,
  validationResults,
  handleSubmit,
  onValidate,
}) => (
  <Stack gutter={'md'}>
    <StackItem>
      {i18nFormTitle && (
        <Title className="syn-card__title" headingLevel="h2" size="md">
          {i18nFormTitle}
        </Title>
      )}
    </StackItem>
    <StackItem>
      {validationResults &&
      validationResults.map((e, idx) => (
        <Alert
          title={e.message}
          key={idx}
          isInline={true}
          variant={e.type === ERROR ? WARNING : e.type}
        />
      ))}
    </StackItem>
    <StackItem>
      <Form
        isHorizontal={true}
        data-testid={'connector-configuration-form'}
        onSubmit={handleSubmit}
        style={{
          maxWidth: 600,
        }}
      >
        {children}
      </Form>
    </StackItem>
    <StackItem>
      {onValidate && (
        <ButtonLink
          data-testid={'connection-creator-layout-back-button'}
          onClick={onValidate}
          disabled={isValidating || isNextLoading || isNextDisabled}
        >
          {isValidating ? <Loader size={'xs'} inline={true} /> : null}
          {i18nValidate}
        </ButtonLink>
      )}
    </StackItem>
  </Stack>
);

import {
  Alert,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Form,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Container, Loader } from '../Layout';
import { ERROR, WARNING } from '../Shared/models';

export interface IConnectorConfigurationFormValidationResult {
  message: string;
  type: 'error' | 'success';
}

export interface IConnectorConfigurationFormProps {
  /**
   * The internationalized form title.
   */
  i18nFormTitle?: string;
  /**
   * Form level validationResults
   */
  validationResults?: IConnectorConfigurationFormValidationResult[];
  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;
  onNext: (e: React.MouseEvent<any>) => void;
  onValidate?: (e: React.MouseEvent<any>) => void;
  backHref: H.LocationDescriptor;
  isNextDisabled: boolean;
  isNextLoading: boolean;
  isValidating: boolean;
  isLastStep: boolean;
  i18nSave: string;
  i18nNext: string;
}

/**
 * A component to render a save form, to be used in the integration
 * editor. This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IConnectorConfigurationFormProps#i18nTitle}
 */
export const ConnectorConfigurationForm: React.FunctionComponent<IConnectorConfigurationFormProps> = ({
  children,
  i18nFormTitle,
  validationResults,
  handleSubmit,
  onNext,
  onValidate,
  backHref,
  isNextDisabled,
  isNextLoading,
  isValidating,
  isLastStep,
  i18nSave,
  i18nNext,
}) => (
  <Container>
    <div className="row row-cards-pf">
      <Card>
        <CardHeader className="syn-card__header--border">
          {i18nFormTitle && (
            <Title className="syn-card__title" headingLevel="h2" size="md">
              {i18nFormTitle}
            </Title>
          )}
        </CardHeader>
        <CardBody>
          {validationResults &&
            validationResults.map((e, idx) => (
              <Alert
                title={e.message}
                key={idx}
                type={e.type === ERROR ? WARNING : e.type}
              />
            ))}
          <Form
            isHorizontal={true}
            data-testid={'connector-configuration-form'}
            onSubmit={handleSubmit}
            style={{
              margin: 'auto',
              maxWidth: 600,
            }}
          >
            {children}
          </Form>
        </CardBody>
        <CardFooter className="syn-card__footer">
          <ButtonLink
            data-testid={'connection-creator-layout-back-button'}
            href={backHref}
            className={'wizard-pf-back'}
          >
            <i className="fa fa-angle-left" /> Back
          </ButtonLink>
          &nbsp;
          {onValidate && (
            <>
              <ButtonLink
                data-testid={'connection-creator-layout-back-button'}
                onClick={onValidate}
                className={'wizard-pf-back'}
                disabled={isValidating || isNextLoading || isNextDisabled}
              >
                {isValidating ? <Loader size={'xs'} inline={true} /> : null}
                Validate
              </ButtonLink>
              &nbsp;
            </>
          )}
          <ButtonLink
            data-testid={'connection-creator-layout-next-button'}
            onClick={onNext}
            as={'primary'}
            className={'wizard-pf-next'}
            disabled={isNextLoading || isNextDisabled}
          >
            {isNextLoading && (
              <Loader
                size={'xs'}
                inline={true}
                data-testid={'connection-creator-layout-loading'}
              />
            )}
            {isLastStep ? (
              <>{i18nSave}</>
            ) : (
              <>
                {i18nNext} <i className="fa fa-angle-right" />
              </>
            )}
          </ButtonLink>
        </CardFooter>
      </Card>
    </div>
  </Container>
);

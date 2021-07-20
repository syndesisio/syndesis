import {
  Alert,
  Card,
  CardBody,
  CardFooter
  ,
  Form,
  Title, CardTitle ,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Container, Loader, PageSection } from '../../Layout';

export interface IIntegrationEditorFormProps {
  /**
   * The internationalized form title.
   */
  i18nFormTitle?: string;

  i18nNext: string;
  i18nBackAction?: string;
  /**
   * The callback fired when submitting the form.
   * @param e
   */
  isBackAllowed: boolean;
  isValid: boolean;
  isLoading: boolean;
  error?: string;
  backActionHref?: H.LocationDescriptor;
  handleSubmit: (e?: any) => void;
  submitForm: (e?: any) => void;
}

/**
 * A component to render a save form, to be used in the integration
 * editor. This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IIntegrationEditorFormProps#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationEditorFormProps#i18nSubtitle}
 */
export const IntegrationEditorForm: React.FunctionComponent<IIntegrationEditorFormProps> = ({
  isValid,
  i18nNext,
  handleSubmit,
  i18nFormTitle,
  backActionHref,
  children,
  error,
  i18nBackAction,
  isBackAllowed,
  isLoading,
  submitForm,
}) => {
  return (
    <PageSection>
      <Container>
        <div className="row row-cards-pf">
          <Card>
            {i18nFormTitle && (
              <CardTitle>
                <Title className="syn-card__title" headingLevel="h2" size="md">
                  {i18nFormTitle}
                </Title>
              </CardTitle>
            )}
            <CardBody>
              <Container>
                <Form isHorizontal={true} onSubmit={handleSubmit}>
                  {error && (
                    <Alert isInline={true} variant={'warning'} title={error} />
                  )}
                  {children}
                </Form>
              </Container>
            </CardBody>
            <CardFooter className="syn-card__footer">
              {backActionHref && isBackAllowed && (
                <>
                  <ButtonLink
                    id={'integration-editor-form-back-button'}
                    href={backActionHref}
                  >
                    <i className={'fa fa-chevron-left'} /> {i18nBackAction}
                  </ButtonLink>
                  &nbsp;
                </>
              )}
              <ButtonLink
                id={'integration-editor-form-next-button'}
                onClick={submitForm}
                disabled={!isValid || isLoading}
                as={'primary'}
              >
                {i18nNext}
                {isLoading && (
                  <>
                    &nbsp;&nbsp;
                    <Loader inline={true} size={'xs'} />
                  </>
                )}
              </ButtonLink>
            </CardFooter>
          </Card>
        </div>
      </Container>
    </PageSection>
  );
};

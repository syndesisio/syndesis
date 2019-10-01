import { Card, CardHeader, CardBody, CardFooter, Form, Title } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { Alert } from 'patternfly-react';
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
export class IntegrationEditorForm extends React.Component<
  IIntegrationEditorFormProps
> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <Card>
              {this.props.i18nFormTitle && (
                <CardHeader>
                  <Title className="syn-card__title" headingLevel="h2" size="md">{this.props.i18nFormTitle}</Title>
                </CardHeader>
              )}
              {this.props.error ? (
                <Alert type={'warning'}>
                  <span>{this.props.error}</span>
                </Alert>
              ) : null}
              <CardBody>
                <Container>
                  <Form isHorizontal={true} onSubmit={this.props.handleSubmit}>
                    {this.props.children}
                  </Form>
                </Container>
              </CardBody>
              <CardFooter className="syn-card__footer">
                {this.props.backActionHref && (
                  <>
                    <ButtonLink
                      id={'integration-editor-form-back-button'}
                      href={this.props.backActionHref}
                    >
                      <i className={'fa fa-chevron-left'} />{' '}
                      {this.props.i18nBackAction}
                    </ButtonLink>
                    &nbsp;
                  </>
                )}
                <ButtonLink
                  id={'integration-editor-form-next-button'}
                  onClick={this.props.submitForm}
                  disabled={!this.props.isValid || this.props.isLoading}
                  as={'primary'}
                >
                  {this.props.i18nNext}
                  {this.props.isLoading ? (
                    <>
                      &nbsp;&nbsp;
                      <Loader inline={true} size={'xs'} />
                    </>
                  ) : null}
                </ButtonLink>
              </CardFooter>
            </Card>
          </div>
        </Container>
      </PageSection>
    );
  }
}

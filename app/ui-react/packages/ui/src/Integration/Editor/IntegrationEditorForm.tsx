import * as H from '@syndesis/history';
import { Alert } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../Layout';

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
          <form
            className="form-horizontal required-pf"
            role="form"
            onSubmit={this.props.handleSubmit}
          >
            <div className="row row-cards-pf">
              <div className="card-pf">
                {this.props.i18nFormTitle && (
                  <div className="card-pf-title">
                    {this.props.i18nFormTitle}
                  </div>
                )}
                {this.props.error ? (
                  <Alert type={'warning'}>
                    <span>{this.props.error}</span>
                  </Alert>
                ) : null}
                <div className="card-pf-body">
                  <Container>{this.props.children}</Container>
                </div>
                <div className="card-pf-footer">
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
                    disabled={!this.props.isValid}
                    as={'primary'}
                  >
                    {this.props.i18nNext}
                  </ButtonLink>
                </div>
              </div>
            </div>
          </form>
        </Container>
      </PageSection>
    );
  }
}

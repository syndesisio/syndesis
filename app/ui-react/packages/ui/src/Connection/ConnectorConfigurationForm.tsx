import { Form } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { Alert } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Container, Loader } from '../Layout';

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
export class ConnectorConfigurationForm extends React.Component<
  IConnectorConfigurationFormProps
> {
  public static defaultProps = {
    validationResults: [],
  };

  public render() {
    return (
      <Container>
        <div className="row row-cards-pf">
          <div className="card-pf">
            <div className="card-pf-heading">
              {this.props.i18nFormTitle && (
                <div className="card-pf-title">{this.props.i18nFormTitle}</div>
              )}
            </div>
            <div className="card-pf-body">
              {this.props.validationResults!.map((e, idx) => (
                <Alert key={idx} type={e.type}>
                  {e.message}
                </Alert>
              ))}
              <Form
                isHorizontal={true}
                data-testid={'connector-configuration-form'}
                onSubmit={this.props.handleSubmit}
                style={{
                  margin: 'auto',
                  maxWidth: 600,
                }}
              >
                {this.props.children}
              </Form>
            </div>
            <div className="card-pf-footer">
              <ButtonLink
                data-testid={'connection-creator-layout-back-button'}
                href={this.props.backHref}
                className={'wizard-pf-back'}
              >
                <i className="fa fa-angle-left" /> Back
              </ButtonLink>
              &nbsp;
              {this.props.onValidate && (
                <>
                  <ButtonLink
                    data-testid={'connection-creator-layout-back-button'}
                    onClick={this.props.onValidate}
                    className={'wizard-pf-back'}
                    disabled={
                      this.props.isValidating ||
                      this.props.isNextLoading ||
                      this.props.isNextDisabled
                    }
                  >
                    {this.props.isValidating ? (
                      <Loader size={'xs'} inline={true} />
                    ) : null}
                    Validate
                  </ButtonLink>
                  &nbsp;
                </>
              )}
              <ButtonLink
                data-testid={'connection-creator-layout-next-button'}
                onClick={this.props.onNext}
                as={'primary'}
                className={'wizard-pf-next'}
                disabled={this.props.isNextLoading || this.props.isNextDisabled}
              >
                {this.props.isNextLoading ? (
                  <Loader
                    size={'xs'}
                    inline={true}
                    data-testid={'connection-creator-layout-loading'}
                  />
                ) : null}
                {this.props.isLastStep ? (
                  this.props.i18nSave
                ) : (
                  <>
                    {this.props.i18nNext} <i className="fa fa-angle-right" />
                  </>
                )}
              </ButtonLink>
            </div>
          </div>
        </div>
      </Container>
    );
  }
}

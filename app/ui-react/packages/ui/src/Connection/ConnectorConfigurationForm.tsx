import { Alert } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout';

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
}

/**
 * A component to render a save form, to be used in the integration
 * editor. This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IConnectorConfigurationFormProps#i18nTitle}
 * @see [i18nSubtitle]{@link IConnectorConfigurationFormProps#i18nSubtitle}
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
        <form
          className="form-horizontal required-pf"
          role="form"
          onSubmit={this.props.handleSubmit}
        >
          <div className="row row-cards-pf">
            <div className="card-pf">
              {this.props.i18nFormTitle && (
                <div className="card-pf-title">{this.props.i18nFormTitle}</div>
              )}
              <div className="card-pf-body">
                {this.props.validationResults!.map((e, idx) => (
                  <Alert key={idx} type={e.type}>
                    {e.message}
                  </Alert>
                ))}
                <Container>{this.props.children}</Container>
              </div>
            </div>
          </div>
        </form>
      </Container>
    );
  }
}

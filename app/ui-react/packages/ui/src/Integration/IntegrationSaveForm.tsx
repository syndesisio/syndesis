import { Text, Title } from '@patternfly/react-core';
import * as React from 'react';
import { Container } from '../Layout';

export interface IIntegrationSaveFormProps {
  /**
   * The internationalized title.
   */
  i18nTitle: string;
  /**
   * The internationalized subtitle.
   */
  i18nSubtitle: string;
  /**
   * The internationalized form title.
   */
  i18nFormTitle?: string;

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
 * @see [i18nTitle]{@link IIntegrationSaveFormProps#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationSaveFormProps#i18nSubtitle}
 */
export class IntegrationSaveForm extends React.Component<
  IIntegrationSaveFormProps
> {
  public render() {
    return (
      <>
        <Container className="pf-u-my-md">
          <Title size="lg">{this.props.i18nTitle} - Choose Action</Title>
          <Text>{this.props.i18nSubtitle}</Text>
        </Container>
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
                <div className="card-pf-body">
                  <Container>{this.props.children}</Container>
                </div>
              </div>
            </div>
          </form>
        </Container>
      </>
    );
  }
}

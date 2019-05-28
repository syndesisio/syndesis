import { Text } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../Layout';

export interface IIntegrationEditorNothingToConfigureProps {
  /**
   * The internationalized alert to display.
   */
  i18nAlert: string;
  i18nNext: string;
  i18nBackAction: string;
  backActionHref: H.LocationDescriptor;
  submitForm: (e?: any) => void;
}

/**
 * A component to render an alert for unconfigurable actions.
 * @see [i18nAlert]{@link IIntegrationEditorNothingToConfigureProps#i18nAlert}
 */
export class IntegrationEditorNothingToConfigure extends React.Component<
  IIntegrationEditorNothingToConfigureProps
> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <div className="card-pf">
              <div className="card-pf-body">
                <Container>
                  <Text className="alert alert-info">
                    <span className="pficon pficon-info" />
                    {this.props.i18nAlert}
                  </Text>
                </Container>
              </div>
              <div className="card-pf-footer">
                <ButtonLink
                  data-testid={
                    'integration-editor-nothing-to-configure-back-button'
                  }
                  href={this.props.backActionHref}
                >
                  <i className={'fa fa-chevron-left'} />{' '}
                  {this.props.i18nBackAction}
                </ButtonLink>
                &nbsp;
                <ButtonLink
                  data-testid={
                    'integration-editor-nothing-to-configure-next-button'
                  }
                  onClick={this.props.submitForm}
                  as={'primary'}
                >
                  {this.props.i18nNext}
                </ButtonLink>
              </div>
            </div>
          </div>
        </Container>
      </PageSection>
    );
  }
}

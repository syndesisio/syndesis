import {
  Card,
  CardBody,
  CardFooter,
  CardTitle,
  Form,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../../Layout';

import './ChoicePageCard.css';

export interface IChoicePageCardProps {
  backHref?: H.LocationDescriptor;
  header?: JSX.Element;
  i18nBack: string;
  i18nDone: string;
  isBackAllowed: boolean;
  isValid: boolean;
  submitForm: (e?: any) => void;
}

export class ChoicePageCard extends React.Component<IChoicePageCardProps> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <Card>
              {this.props.header && <CardTitle>{this.props.header}</CardTitle>}
              <CardBody>
                <Container>
                  <Form
                    className={'conditional-flow__form'}
                    isHorizontal={true}
                  >
                    {this.props.children}
                  </Form>
                </Container>
              </CardBody>
              <CardFooter className="syn-card__footer">
                {this.props.backHref && (
                  <>
                    <ButtonLink
                      id={'integration-editor-form-back-button'}
                      disabled={!this.props.isBackAllowed}
                      href={this.props.backHref}
                    >
                      <i className={'fa fa-chevron-left'} />{' '}
                      {this.props.i18nBack}
                    </ButtonLink>
                    &nbsp;
                  </>
                )}
                <ButtonLink
                  data-testid={'editor-page-card-done-button'}
                  onClick={this.props.submitForm}
                  disabled={!this.props.isValid}
                  as={'primary'}
                >
                  {this.props.i18nDone}
                </ButtonLink>
              </CardFooter>
            </Card>
          </div>
        </Container>
      </PageSection>
    );
  }
}

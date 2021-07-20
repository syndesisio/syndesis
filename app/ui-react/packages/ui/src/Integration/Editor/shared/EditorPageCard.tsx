import { Card, CardBody, CardFooter , Form, CardTitle  } from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../../Layout';

export interface IEditorPageCardProps {
  header?: JSX.Element;
  i18nDone: string;
  isValid: boolean;
  submitForm: (e?: any) => void;
}

export class EditorPageCard extends React.Component<IEditorPageCardProps> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <Card>
              {this.props.header && (
                <CardTitle>{this.props.header}</CardTitle>
              )}
              <CardBody>
                <Container>
                  <Form isHorizontal={true}>{this.props.children}</Form>
                </Container>
              </CardBody>
              <CardFooter className="syn-card__footer">
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

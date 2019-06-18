import { Popover } from '@patternfly/react-core';
import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import { ControlLabel, FormControl, FormGroup } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../../Layout';
import { TextEditor } from '../../../Shared';

const kinds = [
  {
    label: 'Type specification not required',
    value: 'any',
  },
  {
    label: 'JSON Schema',
    value: 'json-schema',
  },
  {
    label: 'JSON Instance',
    value: 'json-instance',
  },
  {
    label: 'XML Schema',
    value: 'xml-schema',
  },
  {
    label: 'XML Instance',
    value: 'xml-instance',
  },
];

export interface IDescribeDataShapeFormProps {
  kind: string;
  definition?: string;
  name?: string;
  description?: string;
  i18nSelectType: string;
  i18nSelectTypeHelp: string;
  i18nDataTypeName: string;
  i18nDataTypeNameHelp: string;
  i18nDataTypeDescription: string;
  i18nDataTypeDescriptionHelp: string;
  i18nDefinition: string;
  i18nDefinitionHelp: string;
  i18nNext: string;
  i18nBackAction?: string;
  backActionHref?: H.LocationDescriptor;
  onNext: () => void;
  onKindChange: (kind: string) => void;
  onDefinitionChange: (text: string) => void;
  onNameChange: (name: string) => void;
  onDescriptionChange: (description: string) => void;
}

export class DescribeDataShapeForm extends React.Component<
  IDescribeDataShapeFormProps
> {
  constructor(props: IDescribeDataShapeFormProps) {
    super(props);
  }
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <div className="card-pf">
              <div className="card-pf-body">
                <form>
                  <FormGroup>
                    <ControlLabel>{this.props.i18nSelectType}</ControlLabel>
                    <Popover
                      aria-label={this.props.i18nSelectTypeHelp}
                      bodyContent={this.props.i18nSelectTypeHelp}
                    >
                      <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                    </Popover>
                    <FormControl
                      data-testid={'describe-data-shape-form-kind-input'}
                      componentClass="select"
                      value={this.props.kind}
                      onChange={(event: React.ChangeEvent<HTMLSelectElement>) =>
                        this.props.onKindChange(event.target.value)
                      }
                    >
                      {kinds.map((kind, index) => (
                        <option key={index} value={kind.value}>
                          {kind.label}
                        </option>
                      ))}
                    </FormControl>
                  </FormGroup>
                  {this.props.kind !== 'any' && (
                    <>
                      <FormGroup>
                        <ControlLabel>{this.props.i18nDefinition}</ControlLabel>
                        <Popover
                          aria-label={this.props.i18nDefinitionHelp}
                          bodyContent={this.props.i18nDefinitionHelp}
                        >
                          <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                        </Popover>
                        <TextEditor
                          id={'describe-data-shape-form-definition-editor'}
                          value={this.props.definition || ''}
                          onChange={(editor, data, text) =>
                            this.props.onDefinitionChange(text)
                          }
                          options={{
                            lineNumbers: true,
                            lineWrapping: true,
                            readOnly: false,
                            showCursorWhenSelecting: true,
                            styleActiveLine: true,
                            tabSize: 2,
                          }}
                        />
                      </FormGroup>
                      <FormGroup>
                        <ControlLabel>
                          {this.props.i18nDataTypeName}
                        </ControlLabel>
                        <Popover
                          aria-label={this.props.i18nDataTypeNameHelp}
                          bodyContent={this.props.i18nDataTypeNameHelp}
                        >
                          <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                        </Popover>
                        <FormControl
                          data-testid={'describe-data-shape-form-name-input'}
                          type="text"
                          value={this.props.name}
                          onChange={(
                            event: React.ChangeEvent<HTMLInputElement>
                          ) => this.props.onNameChange(event.target.value)}
                        />
                      </FormGroup>
                      <FormGroup>
                        <ControlLabel>
                          {this.props.i18nDataTypeDescription}
                        </ControlLabel>
                        <Popover
                          aria-label={this.props.i18nDataTypeDescriptionHelp}
                          bodyContent={this.props.i18nDataTypeDescriptionHelp}
                        >
                          <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                        </Popover>
                        <FormControl
                          type="text"
                          data-testid={
                            'describe-data-shape-form-description-input'
                          }
                          value={this.props.description}
                          onChange={(
                            event: React.ChangeEvent<HTMLInputElement>
                          ) =>
                            this.props.onDescriptionChange(event.target.value)
                          }
                        />
                      </FormGroup>
                    </>
                  )}
                </form>
              </div>
              <div className="card-pf-footer">
                {this.props.backActionHref && (
                  <>
                    <ButtonLink
                      data-testid={'describe-data-shape-form-back-button'}
                      href={this.props.backActionHref}
                    >
                      <i className={'fa fa-chevron-left'} />{' '}
                      {this.props.i18nBackAction}
                    </ButtonLink>
                    &nbsp;
                  </>
                )}
                <ButtonLink
                  data-testid={'describe-data-shape-form-next-button'}
                  onClick={this.props.onNext}
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

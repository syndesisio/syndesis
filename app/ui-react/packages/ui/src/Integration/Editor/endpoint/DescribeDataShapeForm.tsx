import * as H from '@syndesis/history';
import * as React from 'react';

import {
  Button,
  Card,
  CardBody,
  CardFooter,
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Popover,
  TextInput,
} from '@patternfly/react-core';
import { ButtonLink, Container, PageSection } from '../../../Layout';

import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
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
  {
    label: 'CSV Instance',
    value: 'csv-instance',
  },
];

const titleFor = (kind: string): string => {
  const found = kinds.find((k) => k.value === kind);
  return (found && found.label + ' Parameters') || '';
};

export interface IDescribeDataShapeFormProps {
  kind: string;
  definition?: string;
  name?: string;
  description?: string;
  parametersDialog?: React.ReactNode;
  i18nSelectType: string;
  i18nSelectTypeHelp: string;
  i18nDataTypeName: string;
  i18nDataTypeNameHelp: string;
  i18nDataTypeDescription: string;
  i18nDataTypeDescriptionHelp: string;
  i18nDataTypeParameters: string;
  i18nDataTypeParametersHelp: string;
  i18nDataTypeParametersAction: string;
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
  onShowParameters: (kind: string, parameters: []) => void;
  parametersFor: (kind: string) => any;
}

export const DescribeDataShapeForm: React.FunctionComponent<IDescribeDataShapeFormProps> =
  ({
    kind,
    definition,
    name,
    description,
    parametersDialog: parametersDialog,
    i18nDefinition,
    i18nDefinitionHelp,
    i18nSelectType,
    i18nSelectTypeHelp,
    i18nDataTypeName,
    i18nDataTypeNameHelp,
    i18nDataTypeDescription,
    i18nDataTypeDescriptionHelp,
    i18nDataTypeParameters,
    i18nDataTypeParametersHelp,
    i18nDataTypeParametersAction,
    i18nNext,
    i18nBackAction,
    backActionHref,
    onNext,
    onKindChange,
    onDefinitionChange,
    onNameChange,
    onDescriptionChange,
    onShowParameters,
    parametersFor,
  }) => (
    <PageSection>
      <Container>
        <div className="row row-cards-pf">
          <Card>
            <CardBody>
              <Form>
                <FormGroup
                  fieldId={'describe-data-shape-form-kind-input'}
                  label={
                    <>
                      {i18nSelectType}
                      <Popover
                        aria-label={i18nSelectTypeHelp}
                        bodyContent={i18nSelectTypeHelp}
                      >
                        <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                      </Popover>
                    </>
                  }
                >
                  <FormSelect
                    value={kind}
                    data-testid={'describe-data-shape-form-kind-input'}
                    id={'describe-data-shape-form-kind-input'}
                    onChange={(value) => onKindChange(value)}
                  >
                    {kinds.map((aKind, index) => (
                      <FormSelectOption
                        key={index}
                        value={aKind.value}
                        label={aKind.label}
                      />
                    ))}
                  </FormSelect>
                </FormGroup>
                {kind !== 'any' && (
                  <>
                    <FormGroup
                      fieldId={'describe-data-shape-form-definition-editor'}
                      label={
                        <>
                          {i18nDefinition}
                          <Popover
                            aria-label={i18nDefinitionHelp}
                            bodyContent={i18nDefinitionHelp}
                          >
                            <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                          </Popover>
                        </>
                      }
                    >
                      <TextEditor
                        id={'describe-data-shape-form-definition-editor'}
                        value={definition || ''}
                        onChange={(editor, data, text) =>
                          onDefinitionChange(text)
                        }
                        options={{
                          lineNumbers: true,
                          lineWrapping: true,
                          readOnly: false,
                          showCursorWhenSelecting: true,
                          tabSize: 2,
                        }}
                      />
                    </FormGroup>
                    {parametersFor(kind).length > 0 && (
                      <FormGroup
                        fieldId={'describe-data-shape-form-parameters'}
                        label={
                          <>
                            {i18nDataTypeParameters}
                            <Popover
                              aria-label={i18nDataTypeParametersHelp}
                              bodyContent={i18nDataTypeParametersHelp}
                            >
                              <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                            </Popover>
                          </>
                        }
                      >
                        <Container>
                          <Button
                            type={'button'}
                            variant={'tertiary'}
                            data-testid={
                              'describe-data-shape-form-parameters-button'
                            }
                            onClick={() =>
                              onShowParameters(
                                titleFor(kind),
                                parametersFor(kind)
                              )
                            }
                          >
                            <i className={'fa fa-ellipsis-h'} />{' '}
                            {i18nDataTypeParametersAction}
                          </Button>
                          {parametersDialog}
                        </Container>
                      </FormGroup>
                    )}
                    <FormGroup
                      fieldId={'describe-data-shape-form-name-input'}
                      label={
                        <>
                          {i18nDataTypeName}
                          <Popover
                            aria-label={i18nDataTypeNameHelp}
                            bodyContent={i18nDataTypeNameHelp}
                          >
                            <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                          </Popover>
                        </>
                      }
                    >
                      <TextInput
                        data-testid={'describe-data-shape-form-name-input'}
                        type="text"
                        value={name}
                        onChange={(value) => onNameChange(value)}
                      />
                    </FormGroup>
                    <FormGroup
                      fieldId={'describe-data-shape-form-description-input'}
                      label={
                        <>
                          {i18nDataTypeDescription}
                          <Popover
                            aria-label={i18nDataTypeDescriptionHelp}
                            bodyContent={i18nDataTypeDescriptionHelp}
                          >
                            <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
                          </Popover>
                        </>
                      }
                    >
                      <TextInput
                        type="text"
                        data-testid={
                          'describe-data-shape-form-description-input'
                        }
                        value={description}
                        onChange={(value) => onDescriptionChange(value)}
                      />
                    </FormGroup>
                  </>
                )}
              </Form>
            </CardBody>
            <CardFooter className="syn-card__footer">
              {backActionHref && (
                <>
                  <ButtonLink
                    data-testid={'describe-data-shape-form-back-button'}
                    href={backActionHref}
                  >
                    <i className={'fa fa-chevron-left'} /> {i18nBackAction}
                  </ButtonLink>
                  &nbsp;
                </>
              )}
              <ButtonLink
                data-testid={'describe-data-shape-form-next-button'}
                onClick={onNext}
                as={'primary'}
              >
                {i18nNext}
              </ButtonLink>
            </CardFooter>
          </Card>
        </div>
      </Container>
    </PageSection>
  );

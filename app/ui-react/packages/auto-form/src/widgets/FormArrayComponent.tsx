import { Button } from '@patternfly/react-core';
import * as React from 'react';
import { IFormArrayControlProps, IFormArrayDefinitionOptions } from '../models';
import { useFormBuilder } from '../useFormBuilder';
import { getNewArrayRow } from '../utils';
import { toValidHtmlId } from './helpers';

import './FormArrayComponent.css';

export const FormArrayComponent: React.FunctionComponent<IFormArrayControlProps> = props => {
  if (typeof props.property.arrayDefinition === 'undefined') {
    return (
      <div className="alert alert-warning">
        <span>No Array definition supplied for array type</span>
      </div>
    );
  }
  const { getField, getPropertiesArray, getInitialValues } = useFormBuilder();
  const definition = props.property.arrayDefinition;
  const options =
    props.property.arrayDefinitionOptions ||
    ({} as IFormArrayDefinitionOptions);
  const formGroupAttributes = options.formGroupAttributes || {};
  const fieldAttributes = options.fieldAttributes || {};
  const controlLabelAttributes = options.controlLabelAttributes || {};
  const arrayControlAttributes = options.arrayControlAttributes || {};
  const arrayRowTitleAttributes = options.arrayRowTitleAttributes || {};
  const minElements = options.minElements || 0;
  const values =
    props.form.values[props.name] || props.property.defaultValue || [];
  const myId = toValidHtmlId(props.name);
  return (
    <div
      id={myId}
      data-testid={myId}
      className="form-array-layout form-array-container"
    >
      {values.map((value: any, index: number) => {
        const fieldName = `${props.name}[${index}]`;
        const rowValue = getInitialValues(definition, value);
        const propertiesArray = getPropertiesArray(definition);
        const controlGroupName = `${fieldName}-array-controls`;
        const controlGroupId = toValidHtmlId(`${controlGroupName}-control`);
        return (
          <section
            key={fieldName}
            className="form-array-layout form-array-section"
          >
            {options.rowTitle && (
              <div {...arrayRowTitleAttributes}>
                <h5 className="form-array-section__title pf-c-title">
                  <strong>{`${index + 1}. ${options.rowTitle}`}</strong>
                </h5>
              </div>
            )}
            <div
              className="form-array-section__fields form-array-layout"
              {...formGroupAttributes}
            >
              {propertiesArray.map(property => {
                const propertyFieldName = `${fieldName}.${property.name}`;

                return getField({
                  allFieldsRequired: props.allFieldsRequired || false,
                  property: {
                    controlLabelAttributes,
                    fieldAttributes,
                    formGroupAttributes,
                    ...property,
                    disabled:
                      props.form.isSubmitting || props.property.disabled,
                    key: propertyFieldName,
                    name: propertyFieldName,
                  },
                  value: rowValue[property.name],
                });
              })}
            </div>
            <div key={controlGroupName} {...arrayControlAttributes}>
              <div
                id={controlGroupId}
                className={'form-array-control__array-controls'}
              >
                {options.showSortControls && (
                  <>
                    <Button
                      data-testid={'condition-move-up'}
                      variant={'link'}
                      onClick={() => {
                        props.move(index, index - 1);
                      }}
                      isDisabled={!(index > 0)}
                    >
                      <i className="fa fa-arrow-alt-circle-up" />
                    </Button>
                    <Button
                      data-testid={'condition-move-down'}
                      variant={'link'}
                      onClick={() => {
                        props.move(index, index + 1);
                      }}
                      isDisabled={!(index < values.length - 1)}
                    >
                      <i className="fa fa-arrow-alt-circle-down" />
                    </Button>
                  </>
                )}
                {!props.form.isSubmitting && !props.property.disabled && (
                  <Button
                    data-testid={'condition-delete'}
                    variant={'link'}
                    onClick={() => props.remove(index)}
                    isDisabled={!(values.length > minElements)}
                  >
                    <i className="fa fa-trash" />
                  </Button>
                )}
              </div>
            </div>
          </section>
        );
      })}
      {!props.form.isSubmitting && !props.property.disabled && (
        <div className={'form-array-control__array-add'}>
          <Button
            variant={'link'}
            data-testid="form-array-control-add-another-item-button"
            onClick={() => props.push(getNewArrayRow(definition))}
          >
            <>
              <i className="fa fa-plus-circle" />
              &nbsp;
              {options.i18nAddElementText || 'Add Another'}
            </>
          </Button>
        </div>
      )}
    </div>
  );
};

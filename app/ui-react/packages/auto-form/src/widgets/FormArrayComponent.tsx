import * as React from 'react';
import { IFormArrayControlProps, IFormArrayDefinitionOptions } from '../models';
import { useFormBuilder } from '../useFormBuilder';
import { getNewArrayRow } from '../utils';
import { toValidHtmlId } from './helpers';
import { TextButton } from './TextButton';

import './FormArrayComponent.css';

export interface IFormArrayComponentProps extends IFormArrayControlProps {
  customComponents: { [key: string]: any };
}

export const FormArrayComponent: React.FunctionComponent<
  IFormArrayComponentProps | any /* todo type coercion */
> = props => {
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
    <div id={myId} data-testid={myId}>
      {values.map((value: any, index: number) => {
        const fieldName = `${props.name}[${index}]`;
        const rowValue = getInitialValues(definition, value);
        const propertiesArray = getPropertiesArray(definition);
        const titleKey = `${fieldName}-title`;
        const controlGroupName = `${fieldName}-array-controls`;
        return (
          <fieldset key={fieldName}>
            <div className="form-array-fields">
              {options.rowTitle && (
                <div key={titleKey} {...arrayRowTitleAttributes}>
                  <h5 className="form-array-control__row-title">
                    <strong>{`${index + 1}. ${options.rowTitle}`}</strong>
                  </h5>
                </div>
              )}
              {propertiesArray.map(property =>
                getField({
                  allFieldsRequired: props.allFieldsRequired || false,
                  key: `${fieldName}.${property.name}`,
                  name: `${fieldName}.${property.name}`,
                  property: {
                    controlLabelAttributes,
                    fieldAttributes,
                    formGroupAttributes,
                    ...property,
                  },
                  value: rowValue[property.name],
                })
              )}
              <div
                key={controlGroupName}
                {...formGroupAttributes}
                {...arrayControlAttributes}
              >
                <label
                  htmlFor={toValidHtmlId(`${controlGroupName}-control`)}
                  className="control-label"
                  {...controlLabelAttributes}
                />
                <div id={toValidHtmlId(`${controlGroupName}-control`)}>
                  <div className="form-array-control__array-controls">
                    {options.showSortControls && (
                      <>
                        <TextButton
                          onClick={() => {
                            props.move(index, index - 1);
                          }}
                          enable={index > 0}
                        >
                          <i className="fa fa-arrow-circle-o-up" />
                        </TextButton>
                        <TextButton
                          onClick={() => {
                            props.move(index, index + 1);
                          }}
                          enable={index < values.length - 1}
                        >
                          <i className="fa fa-arrow-circle-o-down" />
                        </TextButton>
                      </>
                    )}
                    <TextButton
                      onClick={() => props.remove(index)}
                      enable={values.length > minElements}
                    >
                      <i className="fa fa-trash-o" />
                    </TextButton>
                  </div>
                </div>
              </div>
            </div>
          </fieldset>
        );
      })}
      <TextButton onClick={() => props.push(getNewArrayRow(definition))}>
        {options.i18nAddElementText || '+ Add Another'}
      </TextButton>
    </div>
  );
};

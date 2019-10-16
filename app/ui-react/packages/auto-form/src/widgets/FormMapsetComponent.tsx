import {
  DataList,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  FormGroup,
  Text,
  TextVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import {
  IFormControlProps,
  IFormDefinitionProperty,
  IMapsetKey,
  IMapsetOptions,
} from '../models';
import { useFormBuilder } from '../useFormBuilder';
import { getValidationState, toValidHtmlId } from './helpers';

import './FormMapsetComponent.css';

export const FormMapsetComponent: React.FunctionComponent<
  IFormControlProps
> = props => {
  const { getField } = useFormBuilder();
  const { value, onChange, onBlur, ...field } = props.field;
  const mapsetValue = typeof value === 'string' ? JSON.parse(value) : value;
  const id = toValidHtmlId(field.name);
  const mapsetOptions = props.property.mapsetOptions || ({} as IMapsetOptions);
  const mapsetValueDefinition = {
    ...(props.property.mapsetValueDefinition ||
      ({} as IFormDefinitionProperty)),
  };
  const mapsetKeys = props.property.mapsetKeys || ([] as IMapsetKey[]);
  return (
    <>
      <FormGroup
        {...props.property.formGroupAttributes}
        className={'form-mapset-component__form-group'}
        fieldId={id}
        isValid={getValidationState(props)}
        helperText={''}
        helperTextInvalid={props.form.errors[props.field.name]}
      />
        <DataList id={id} aria-label={field.name} className={'form-mapset-component__data-list'}>
          <DataListItem aria-labelledby={'key-label'}>
            <DataListItemRow>
              <DataListItemCells
                dataListCells={[
                  <DataListCell key={'primary content'}>
                    <Text component={TextVariants.h4} id={'key-label'}>
                      {mapsetOptions.i18nKeyColumnTitle}
                    </Text>
                  </DataListCell>,
                  <DataListCell key={'secondary content'}>
                    <Text component={TextVariants.h4}>
                      {mapsetOptions.i18nValueColumnTitle}
                    </Text>
                  </DataListCell>,
                ]}
              />
            </DataListItemRow>
          </DataListItem>
          {mapsetKeys
            .sort((a, b) => a.displayName.localeCompare(b.displayName))
            .map((mapsetKey, index) => {
              const keyId = toValidHtmlId(mapsetKey.name);
              return (
                <DataListItem
                  id={keyId}
                  key={keyId}
                  aria-labelledby={`item-label-${keyId}`}
                >
                  <DataListItemRow>
                    <DataListItemCells
                      dataListCells={[
                        <DataListCell key={'primary'}>
                          <Text
                            component={TextVariants.p}
                            id={`item-label-${keyId}`}
                          >
                            {mapsetKey.displayName}
                          </Text>
                        </DataListCell>,
                        <DataListCell key={'secondary'}>
                          <div className="form-mapset-component__data-list-cell-secondary pf-c-form">
                            {getField({
                              allFieldsRequired: false,
                              property: {
                                ...mapsetValueDefinition,
                                name: `${field.name}.${mapsetKey.name}`,
                              },
                              value: mapsetValue[mapsetKey.name],
                            })}
                          </div>
                        </DataListCell>,
                      ]}
                    />
                  </DataListItemRow>
                </DataListItem>
              );
            })}
        </DataList>
    </>
  );
};

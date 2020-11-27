import { Result, StringMap } from '@syndesis/models';
import { ConfigurationProperty } from '@syndesis/models/dist/models';
import { IConnectorConfigurationFormValidationResult } from '@syndesis/ui';
import i18n from '../../i18n';

export interface IConnectionConfiguredProperties {
  [key: string]: string;
}
export function parseValidationResult(results: Result[], name: string) {
  const badValidationResults = results
    .filter(s => s.status === 'ERROR')
    .map(
      s =>
        ({
          message: s.errors!.map(e => e.description).join(', \n'),
          type: 'error',
        } as IConnectorConfigurationFormValidationResult)
    );
  const goodValidationResults = [
    {
      message: i18n.t('connections:validationSuccessful', { name }),
      type: 'success',
    } as IConnectorConfigurationFormValidationResult,
  ];
  const unsupportedValidationResults = results
    .filter(s => s.status === 'UNSUPPORTED')
    .map(s => {
      if (
        s.errors &&
        s.errors.filter(e => e.code === 'unknown-connector').length > 0
      ) {
        return {
          message: i18n.t('connections:validationUnsupported', {
            name,
          }),
          type: 'info',
        } as IConnectorConfigurationFormValidationResult;
      } else {
        return {
          message: s.errors!.map(e => e.description).join(', \n'),
          type: 'info',
        } as IConnectorConfigurationFormValidationResult;
      }
    });
  return badValidationResults.length > 0
    ? badValidationResults
    : unsupportedValidationResults.length > 0
    ? unsupportedValidationResults
    : goodValidationResults;
}

export function parseJsonArray(
  initialValues?: IConnectionConfiguredProperties
) {
  const newObj = {};

  Object.keys(initialValues!).map(key => {
    const value = initialValues![key];
    /**
     * Syndesis's API returns a JSON encoded array,
     * so we parse it here.
     */
    try {
      newObj[key] = JSON.parse(value);
    } catch (e) {
      newObj[key] = value;
    }

    return newObj;
  });

  return newObj;
}

export function stringifyJsonArray(
  configuredProperties?: IConnectionConfiguredProperties
): IConnectionConfiguredProperties {
  const newObj = {};

  Object.keys(configuredProperties!).map(key => {
    const value = configuredProperties![key];
    /**
     * Syndesis's API doesn't handle arrays well,
     * so we'll be sending it as a JSON encoded array
     * instead.
     */
    if (Array.isArray(value)) {
      newObj[key] = JSON.stringify(value);
    } else {
      newObj[key] = value;
    }

    return newObj;
  });

  return newObj;
}

export function substituteDefaultWithConfiguredProperties(
  key: string,
  property: ConfigurationProperty,
  configuredProperties?: StringMap<string>
): ConfigurationProperty {
  if (
    !configuredProperties?.[key] ||
    configuredProperties?.[key] === property.defaultValue
  ) {
    return property;
  }

  const substituted = { ...property };
  substituted.defaultValue = configuredProperties[key];

  return substituted;
}

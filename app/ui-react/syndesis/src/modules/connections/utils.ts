import { Result } from '@syndesis/models';
import { IConnectorConfigurationFormValidationResult } from '@syndesis/ui';

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
      message: `${name} has been successfully validated`,
      type: 'success',
    } as IConnectorConfigurationFormValidationResult,
  ];

  return badValidationResults.length > 0
    ? badValidationResults
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

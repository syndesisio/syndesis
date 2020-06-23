import { Result } from '@syndesis/models';
import { IConnectorConfigurationFormValidationResult } from '@syndesis/ui';
import i18n from '../../i18n';

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

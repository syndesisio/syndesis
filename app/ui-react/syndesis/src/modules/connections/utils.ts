import { Result } from '@syndesis/models';
import { IConnectorConfigurationFormValidationResult } from '@syndesis/ui';

export function parseValidationResult(
  results: Result[],
  connectorName: string
) {
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
      message: `${connectorName} has been successfully validated`,
      type: 'success',
    } as IConnectorConfigurationFormValidationResult,
  ];

  return badValidationResults.length > 0
    ? badValidationResults
    : goodValidationResults;
}

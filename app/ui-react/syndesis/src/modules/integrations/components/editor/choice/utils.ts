import { IChoiceConfiguration, IFlowOption } from './interfaces';

/**
 * Builds a sane choice configuration object from the step's configured properties
 * @param configuredProperties
 */
export function createChoiceConfiguration(configuredProperties: {
  [key: string]: any;
}) {
  const flows = (typeof configuredProperties.flows === 'string'
    ? JSON.parse(configuredProperties.flows)
    : configuredProperties.flows || []) as IFlowOption[];
  const defaultFlowEnabled = typeof configuredProperties.default === 'string';
  const defaultFlow = configuredProperties.default;
  const routingScheme = configuredProperties.routingScheme || 'direct';
  return {
    defaultFlow,
    defaultFlowEnabled,
    flows,
    routingScheme,
  } as IChoiceConfiguration;
}

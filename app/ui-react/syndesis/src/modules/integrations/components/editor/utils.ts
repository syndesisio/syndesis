import {
  getConnectionIcon,
  getExtensionIcon,
  getStepKindIcon,
} from '@syndesis/api';
import {
  ConnectionOverview,
  Extension,
  Step,
  StepKind,
} from '@syndesis/models';
import * as H from 'history';
import { IAddStepPageProps } from './AddStepPage';
import {
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  IUIStep,
} from './interfaces';

type StepKindHrefCallback = (
  step: Step,
  p: ISelectConnectionRouteParams | IAddStepPageProps,
  s: ISelectConnectionRouteState | undefined
) => H.LocationDescriptorObject;

export function toUIStepKind(step: Step): IUIStep['uiStepKind'] {
  if ((step as ConnectionOverview).connectorId === 'api-provider') {
    return 'api-provider';
  }
  return step.stepKind;
}

export interface IGetStepHrefs {
  apiProviderHref: StepKindHrefCallback;
  connectionHref: StepKindHrefCallback;
  filterHref: StepKindHrefCallback;
  extensionHref: StepKindHrefCallback;
  mapperHref: StepKindHrefCallback;
  templateHref: StepKindHrefCallback;
  stepHref: StepKindHrefCallback;
}
export const getStepHref = (
  step: Step,
  params: ISelectConnectionRouteParams | IAddStepPageProps,
  state: ISelectConnectionRouteState | undefined,
  hrefs: IGetStepHrefs
) => {
  switch (toUIStepKind(step)) {
    case 'api-provider':
      return hrefs.apiProviderHref(step, params, state);
    case 'expressionFilter':
    case 'ruleFilter':
      return hrefs.filterHref(step, params, state);
    case 'extension':
      return hrefs.extensionHref(step, params, state);
    case 'mapper':
      return hrefs.mapperHref(step, params, state);
    case 'headers':
      throw new Error(`Can't handle stepKind ${step.stepKind}`);
    case 'template':
      return hrefs.templateHref(step, params, state);
    case 'choice':
    case 'split':
    case 'aggregate':
    case 'log':
      return hrefs.stepHref(step, params, state);
    case 'endpoint':
    case 'connector':
    default:
      return hrefs.connectionHref(step as ConnectionOverview, params, state);
  }
};

export function toStepKindCollection(
  connections: ConnectionOverview[],
  extensions: Extension[],
  steps: StepKind[]
): IUIStep[] {
  return [
    ...connections.map(
      c =>
        ({
          ...c,
          description: c.description || '',
          icon: getConnectionIcon(process.env.PUBLIC_URL, c),
          properties: undefined,
          uiStepKind:
            c.connectorId === 'api-provider' ? 'api-provider' : 'endpoint',
        } as IUIStep)
    ),
    ...extensions.reduce(
      (extentionsByAction, extension) => {
        extension.actions.forEach(a => {
          let properties = {};
          if (
            a.descriptor &&
            Array.isArray(a.descriptor.propertyDefinitionSteps)
          ) {
            properties = a.descriptor.propertyDefinitionSteps.reduce(
              (acc, current) => {
                return { ...acc, ...current.properties };
              },
              {}
            );
          }
          if (a.actionType === 'step') {
            extentionsByAction.push({
              action: a,
              configuredProperties: undefined,
              description: a.description || '',
              extension,
              icon: `${process.env.PUBLIC_URL}${getExtensionIcon(extension)}`,
              name: a.name,
              properties,
              stepKind: 'extension',
              uiStepKind: 'extension',
            });
          }
        });
        return extentionsByAction;
      },
      [] as IUIStep[]
    ),
    ...steps.map(s => ({
      ...s,
      icon: `${process.env.PUBLIC_URL}${getStepKindIcon(s.stepKind)}`,
      uiStepKind: s.stepKind,
    })),
  ]
    .filter(s => !!s.uiStepKind) // this should never happen
    .sort((a, b) => a.name.localeCompare(b.name));
}

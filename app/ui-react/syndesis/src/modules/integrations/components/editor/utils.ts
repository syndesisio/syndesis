import {
  ADVANCED_FILTER,
  AGGREGATE,
  BASIC_FILTER,
  DATA_MAPPER,
  DataShapeKinds,
  ENDPOINT,
  getExtensionIcon,
  getStepIcon,
  getStepsLastPosition,
  HIDE_FROM_STEP_SELECT,
  SPLIT,
  TEMPLATE,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  Connection,
  ConnectionOverview,
  ConnectorAction,
  DataShape,
  Extension,
  Step,
  StepKind,
} from '@syndesis/models';
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

export function getStepKind(step: Step): IUIStep['uiStepKind'] {
  if ((step as ConnectionOverview).connectorId === 'api-provider') {
    return 'api-provider';
  }
  return step.stepKind;
}

export function toUIStepKind(step: Step): IUIStep {
  const uiStepKind = getStepKind(step);
  const inputDataShape =
    step.action &&
    step.action.descriptor &&
    step.action.descriptor.inputDataShape;
  const outputDataShape =
    step.action &&
    step.action.descriptor &&
    step.action.descriptor.outputDataShape;
  switch (uiStepKind) {
    case 'extension':
      // An extension needs special mapping
      return {
        ...step,
        description:
          (step as StepKind).description ||
          (step as StepKind).extension!.description ||
          '',
        icon: getStepIcon(process.env.PUBLIC_URL, step),
        inputDataShape,
        metadata: {
          ...(step.extension!.metadata || {}),
          ...(step.metadata || {}),
        },
        name: step.name || step.extension!.name,
        outputDataShape,
        properties: step.action!.descriptor!.propertyDefinitionSteps![0]
          .properties,
        title: step.name!,
        uiStepKind,
      };
    case 'api-provider':
    case 'endpoint':
    case 'connector':
      // this step is a Connection step
      return {
        ...step,
        description:
          (step as ConnectionOverview).description ||
          step.connection!.description ||
          '',
        icon: getStepIcon(process.env.PUBLIC_URL, step),
        inputDataShape,
        metadata: {
          ...(step.connection!.metadata || {}),
          ...(step.metadata || {}),
        },
        name: step.name || step.connection!.connector!.name!,
        outputDataShape,
        properties:
          step.action &&
          step.action.descriptor &&
          step.action.descriptor.propertyDefinitionSteps &&
          step.action.descriptor.propertyDefinitionSteps.length &&
          step.action.descriptor.propertyDefinitionSteps[0].properties,
        title:
          `${(step.connection && step.connection.name) || '<unknown>'}` +
          step.action
            ? ` - ${(step.action && step.action.name) || '<unknown>'}`
            : '',
        uiStepKind,
      };
    default:
      // this is a regular built-in step
      const name = step.name || step.stepKind || 'Step';
      return {
        ...(step as StepKind),
        icon: getStepIcon(process.env.PUBLIC_URL, step),
        inputDataShape,
        metadata: step.metadata || {},
        name,
        outputDataShape,
        title: name,
        uiStepKind,
      };
  }
}

export function toUIStepKindCollection(steps: Step[]): IUIStep[] {
  return steps.map(toUIStepKind);
}

export function getDataShapeText(stepKind: string, dataShape?: DataShape) {
  if (!dataShape) {
    return undefined;
  }
  const isCollection =
    dataShape.metadata && dataShape.metadata.variant === 'collection';
  let answer: string | undefined = dataShape.name;
  if (dataShape.kind) {
    if (dataShape.kind === (DataShapeKinds.ANY as string)) {
      answer = 'ANY';
    } else if (dataShape.kind === (DataShapeKinds.NONE as string)) {
      answer = undefined;
    } else if (!dataShape.type) {
      answer = dataShape.kind;
    }
  }
  // TODO "split" currently appears to have variant set but maybe it shouldn't
  if (answer && isCollection && stepKind !== SPLIT) {
    answer = answer + ' (Collection)';
  }
  return answer;
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
  switch (getStepKind(step)) {
    case 'endpoint':
    case 'connector':
      return hrefs.connectionHref(
        typeof (step as IUIStep).uiStepKind !== 'undefined'
          ? (step as IUIStep).connection!
          : (step as ConnectionOverview),
        params,
        state
      );
    case 'api-provider':
      return hrefs.apiProviderHref(step, params, state);
    case 'ruleFilter':
      return hrefs.filterHref(step, params, state);
    case 'extension':
      return hrefs.extensionHref(step, params, state);
    case 'mapper':
      return hrefs.mapperHref(step, params, state);
    case 'template':
      return hrefs.templateHref(step, params, state);
    default:
      return hrefs.stepHref(step, params, state);
  }
};

export function mergeConnectionsSources(
  connections: ConnectionOverview[],
  extensions: Extension[],
  steps: StepKind[]
): IUIStep[] {
  return [
    ...connections.map(connection =>
      toUIStepKind({
        connection,
        name: connection.name,
        stepKind: ENDPOINT,
      } as StepKind)
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
            extentionsByAction.push(
              toUIStepKind({
                action: a,
                configuredProperties: undefined,
                description: a.description || '',
                extension,
                icon: `${process.env.PUBLIC_URL}${getExtensionIcon(extension)}`,
                metadata: (extension.metadata as { [name: string]: any }) || {},
                name: a.name,
                properties,
                stepKind: 'extension',
                title: a.name,
              } as StepKind)
            );
          }
        });
        return extentionsByAction;
      },
      [] as IUIStep[]
    ),
    ...steps.map(s => toUIStepKind(s)),
  ]
    .filter(s => !!s.uiStepKind) // this should never happen
    .sort((a, b) => a.name.localeCompare(b.name));
}

/**
 * Filters connections based on the supplied position in the step array
 * @param steps
 * @param position
 */
export function filterStepsByPosition(steps: StepKind[], position: number) {
  if (typeof position === 'undefined' || !steps) {
    // safety net
    return steps;
  }
  const atStart = position === 0;
  const atEnd = getStepsLastPosition(steps) === position;
  return steps.filter(step => {
    // Hide steps that are marked as such, and specifically the log connection
    if (
      (typeof step.connection !== 'undefined' &&
        typeof step.connection.metadata !== 'undefined' &&
        step.connection.metadata[HIDE_FROM_STEP_SELECT]) ||
      (typeof step.metadata !== 'undefined' &&
        step.metadata[HIDE_FROM_STEP_SELECT]) ||
      (step.connection || (step as Connection)).connectorId === 'log'
    ) {
      return false;
    }
    // Special handling for the beginning of a flow
    if (atStart) {
      // At the moment only endpoints can be at the start
      if (step.stepKind !== 'endpoint') {
        return false;
      }
      if ((step.connection || (step as Connection)).connector) {
        return (
          step.connection || (step as Connection)
        ).connector!.actions.some((action: ConnectorAction) => {
          return action.pattern === 'From';
        });
      }
      // it's not a connection
      return true;
    }
    // Special handling for the end of a flow
    if (atEnd) {
      // Several step kinds aren't usable at the end of a flow
      switch ((step as Step).stepKind) {
        case DATA_MAPPER:
        case BASIC_FILTER:
        case ADVANCED_FILTER:
        case SPLIT:
        case AGGREGATE:
        case TEMPLATE:
          return false;
        default:
      }
    }
    if (
      (step.connection || (step as Connection)).connectorId === 'api-provider'
    ) {
      // api provider can be used only for From actions
      return false;
    }
    // All non-connection steps can be shown, except the above
    if (step.stepKind !== ENDPOINT) {
      return true;
    }
    // Only show connections that have at least one action that accepts data
    if ((step.connection || (step as Connection)).connector) {
      return (step.connection || (step as Connection)).connector!.actions.some(
        (action: ConnectorAction) => {
          return action.pattern === 'To';
        }
      );
    }
    return true;
  });
}

/**
 * Filters connections based on the supplied position in the step array and their
 * visibility status
 * @param steps
 * @param position
 */
export function visibleStepsByPosition(
  steps: StepKind[],
  position: number,
  flowSteps: Step[]
) {
  const previousSteps = flowSteps.slice(0, position);
  const subsequentSteps = flowSteps.slice(position + 1);
  return filterStepsByPosition(steps, position).filter(s => {
    if (typeof s.visible === 'function') {
      return s.visible(position, previousSteps, subsequentSteps);
    }
    return true;
  });
}

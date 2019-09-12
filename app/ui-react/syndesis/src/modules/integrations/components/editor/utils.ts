import {
  ADVANCED_FILTER,
  AGGREGATE,
  API_PROVIDER,
  BASIC_FILTER,
  CHOICE,
  CONNECTOR,
  DATA_MAPPER,
  DataShapeKinds,
  ENDPOINT,
  EXTENSION,
  FLOW,
  FLOW_END_ACTION_ID,
  FLOW_START_ACTION_ID,
  getNextAggregateStep,
  getPreviousSteps,
  getPreviousStepWithDataShape,
  getSubsequentSteps,
  HIDE_FROM_STEP_SELECT,
  isActionOutputShapeless,
  LOG,
  SPLIT,
  TEMPLATE,
  toDataShapeKinds,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  Connection,
  ConnectionOverview,
  ConnectorAction,
  DataShape,
  ErrorKey,
  ExtendedActionDescriptor,
  Extension,
  Flow,
  IConnectionOverview,
  Step,
  StepKind,
} from '@syndesis/models';
import i18n from '../../../../i18n';
import {
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  IUIIntegrationStep,
  IUIStep,
} from './interfaces';

type StepKindHrefCallback = (
  step: StepKind,
  p: ISelectConnectionRouteParams,
  s: ISelectConnectionRouteState
) => H.LocationDescriptorObject;

export function getStepKind(step: Step): IUIStep['uiStepKind'] {
  if (
    step.connection &&
    step.connection.id === API_PROVIDER &&
    !(step.metadata || {}).configured
  ) {
    return API_PROVIDER;
  }
  return step.stepKind;
}

export function toUIStep(step: Step | StepKind): IUIStep {
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
    case EXTENSION:
      // An extension needs special mapping
      return {
        ...step,
        description:
          (step as StepKind).description ||
          (step as StepKind).extension!.description ||
          '',
        inputDataShape,
        isConfigRequired: false,
        isTechPreview: false,
        metadata: {
          ...(step.extension!.metadata || {}),
          ...(step.metadata || {}),
        },
        name: step.name || step.extension!.name,
        outputDataShape,
        properties: (step as StepKind).properties,
        title: step.name!,
        uiStepKind,
      };
    case API_PROVIDER:
    case ENDPOINT:
    case CONNECTOR:
      // this step is a Connection step
      return {
        ...step,
        description:
          (step as IConnectionOverview).description ||
          step.connection!.description ||
          '',
        inputDataShape,
        isConfigRequired: (step.connection as IConnectionOverview)
          .isConfigRequired,
        isTechPreview: (step.connection as IConnectionOverview).isTechPreview,
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
        inputDataShape,
        isConfigRequired: false,
        isTechPreview: (step as any).isTechPreview || false,
        metadata: step.metadata || {},
        name,
        outputDataShape,
        title: name,
        uiStepKind,
      };
  }
}

export function toUIStepCollection(steps: Step[]): IUIStep[] {
  return steps.map(toUIStep);
}

export function isSameDataShape(one: DataShape, other: DataShape): boolean {
  if (!one || !other) {
    return false;
  }
  return (
    one.kind === other.kind &&
    one.type === other.type &&
    one.specification === other.specification
  );
}

export function toUIIntegrationStepCollection(
  steps: IUIStep[]
): IUIIntegrationStep[] {
  return steps.map((step, position) => {
    let previousStepShouldDefineDataShape = false;
    let previousStepShouldDefineDataShapePosition: number | undefined;
    let shouldAddDataMapper = false;
    let shouldAddDefaultFlow = false;
    let restrictedDelete = false;
    if (
      step.connection &&
      (step.connection!.connectorId! === FLOW ||
        step.connection!.connectorId! === API_PROVIDER)
    ) {
      restrictedDelete = true;
    }
    const notConfigurable =
      (step.action || false) &&
      (step.action!.id === FLOW_START_ACTION_ID ||
        step.action!.id === FLOW_END_ACTION_ID);
    const isUnclosedSplit =
      step.stepKind === SPLIT &&
      getNextAggregateStep(steps, position) === undefined;

    const shape =
      position === 0
        ? getDataShapeText(step.stepKind!, step.outputDataShape)
        : getDataShapeText(step.stepKind!, step.inputDataShape);

    if (step.action && step.action.descriptor && position > 0) {
      const inputDataShape = step.action.descriptor.inputDataShape;
      if (
        inputDataShape &&
        ![DataShapeKinds.ANY, DataShapeKinds.NONE].includes(
          toDataShapeKinds(inputDataShape.kind!)
        )
      ) {
        const prev = getPreviousStepWithDataShape(steps, position);
        if (
          prev &&
          prev.stepKind !== CHOICE && // TODO: suppress this until we can also use the describe data page for a step syndesisio/syndesis#5456
          prev.action &&
          prev.action.descriptor &&
          prev.action.descriptor.outputDataShape
        ) {
          const prevOutDataShape = prev.action.descriptor.outputDataShape;
          if (DataShapeKinds.ANY === toDataShapeKinds(prevOutDataShape.kind!)) {
            previousStepShouldDefineDataShape = true;
            previousStepShouldDefineDataShapePosition = steps.findIndex(
              s => s.id === prev.id
            );
          } else if (!isSameDataShape(inputDataShape, prevOutDataShape)) {
            shouldAddDataMapper = true;
          }
        }

        // When an output shape other than ANY is specified a default flow is required.
        // This is because all alternate flows need to produce the output shape and in case no condition matches
        // the default flow is supposed to produce this output.
        if (
          step.stepKind === CHOICE &&
          !isActionOutputShapeless(step.action.descriptor)
        ) {
          shouldAddDefaultFlow = typeof step.configuredProperties!.default === 'undefined'
        }
      }
    }

    return {
      ...step,
      isUnclosedSplit,
      notConfigurable,
      previousStepShouldDefineDataShape,
      previousStepShouldDefineDataShapePosition,
      restrictedDelete,
      shape,
      shouldAddDataMapper,
      shouldAddDefaultFlow,
    };
  });
}

export function getDataShapeText(stepKind: string, dataShape?: DataShape) {
  if (!dataShape) {
    return undefined;
  }
  const isCollection =
    dataShape.metadata && dataShape.metadata.variant === 'collection';
  let answer: string | undefined = dataShape.name;
  if (dataShape.kind) {
    if (toDataShapeKinds(dataShape.kind) === DataShapeKinds.ANY) {
      answer = 'ANY';
    } else if (toDataShapeKinds(dataShape.kind) === DataShapeKinds.NONE) {
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
  choiceHref: StepKindHrefCallback;
  mapperHref: StepKindHrefCallback;
  templateHref: StepKindHrefCallback;
  stepHref: StepKindHrefCallback;
}
export const getStepHref = (
  step: Step,
  params: ISelectConnectionRouteParams,
  state: ISelectConnectionRouteState,
  hrefs: IGetStepHrefs
) => {
  switch (getStepKind(step)) {
    case API_PROVIDER:
      return hrefs.apiProviderHref(step as StepKind, params, state);
    case ENDPOINT:
    case CONNECTOR:
      return hrefs.connectionHref(
        typeof (step as IUIStep).uiStepKind !== 'undefined'
          ? ((step as IUIStep).connection! as StepKind)
          : (step as StepKind),
        params,
        state
      );
    case BASIC_FILTER:
      return hrefs.filterHref(step as StepKind, params, state);
    case CHOICE:
      return hrefs.choiceHref(step as StepKind, params, state);
    case DATA_MAPPER:
      return hrefs.mapperHref(step as StepKind, params, state);
    case TEMPLATE:
      return hrefs.templateHref(step as StepKind, params, state);
    case EXTENSION:
    default:
      return hrefs.stepHref(step as StepKind, params, state);
  }
};

export function mergeConnectionsSources(
  connections: ConnectionOverview[],
  extensions: Extension[],
  steps: StepKind[]
): IUIStep[] {
  return [
    ...connections.map(connection =>
      toUIStep({
        connection,
        // we copy over the name and description from the connection to be sure to show these instead of the connector's
        description: connection.description,
        name: connection.name,
        stepKind: ENDPOINT,
      } as StepKind)
    ),
    ...extensions.reduce(
      (extentionsByAction, extension) => {
        (extension.actions || []).forEach(a => {
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
              toUIStep({
                action: a,
                configuredProperties: undefined,
                description: a.description || '',
                extension,
                icon: extension.icon,
                metadata: (extension.metadata as { [name: string]: any }) || {},
                name: a.name,
                properties,
                stepKind: EXTENSION,
                title: a.name,
              } as StepKind)
            );
          }
        });
        return extentionsByAction;
      },
      [] as IUIStep[]
    ),
    ...steps.map(s => toUIStep(s)),
  ]
    .filter(s => !!s.uiStepKind) // this should never happen
    .sort((a, b) => a.name.localeCompare(b.name));
}

/**
 * Filters connections based on the supplied position in the step array
 * @param steps
 * @param position
 * @param atStart
 * @param atEnd
 */
export function filterStepsByPosition(
  steps: StepKind[],
  position: number,
  atStart: boolean,
  atEnd: boolean
) {
  if (typeof position === 'undefined' || !steps) {
    // safety net
    return steps;
  }
  return steps.filter(step => {
    // Hide steps that are marked as such, and specifically the log connection
    if (
      (typeof step.connection !== 'undefined' &&
        typeof step.connection.metadata !== 'undefined' &&
        step.connection.metadata[HIDE_FROM_STEP_SELECT]) ||
      (typeof step.metadata !== 'undefined' &&
        step.metadata[HIDE_FROM_STEP_SELECT]) ||
      (step.connection || (step as Connection)).connectorId === LOG
    ) {
      return false;
    }
    // Special handling for the beginning of a flow
    if (atStart) {
      // At the moment only endpoints can be at the start
      if (step.stepKind !== ENDPOINT) {
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
      (step.connection || (step as Connection)).connectorId === API_PROVIDER
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
 * @param flowSteps
 */
export function visibleStepsByPosition(
  steps: StepKind[],
  position: number,
  flowSteps: Step[]
) {
  const previousSteps = getPreviousSteps(flowSteps, position);
  const subsequentSteps = getSubsequentSteps(flowSteps, position - 1);
  return filterStepsByPosition(
    steps,
    position,
    previousSteps.length === 0,
    subsequentSteps.length === 0
  ).filter(s => {
    if (Array.isArray(s.visible) && s.visible.length > 0) {
      const matches = s.visible.map(visible =>
        visible(
          position,
          previousSteps as StepKind[],
          subsequentSteps as StepKind[]
        )
      );
      return matches.find(m => !m) === undefined;
    }
    return true;
  });
}

/**
 * Builds an array of error keys for a flow using all steps before the supplied position
 * @param flow
 * @param position
 */
export function collectErrorKeys(flow: Flow, position: number) {
  // We want all previous steps and this step
  const previousSteps = getPreviousSteps(flow.steps!, position + 1);
  // Gather up all possible standardized errors in the flow
  const collectedErrors = previousSteps
    .filter(s => typeof s.action !== 'undefined')
    .filter(s => s.action!.descriptor !== 'undefined')
    .filter(
      s =>
        typeof (s.action!.descriptor! as ExtendedActionDescriptor)
          .standardizedErrors !== 'undefined'
    )
    .map(
      s =>
        (s.action!.descriptor! as ExtendedActionDescriptor).standardizedErrors!
    );
  const standardizedErrorsWithDuplicates = [].concat(
    ...(collectedErrors as any)
  ) as ErrorKey[];
  const uniqueErrors = Array.from(
    new Set(standardizedErrorsWithDuplicates.map(err => err.name))
  );
  return uniqueErrors
    .map(uniqueError =>
      standardizedErrorsWithDuplicates.find(err => err.name === uniqueError!)
    )
    .map(err => localizeErrorKey(err!));
}

/**
 * Creates a new error key with the display name localized
 * @param key
 */
export function localizeErrorKey(key: ErrorKey) {
  return {
    displayName: i18n.t(`integrations:errorKeys:${key.displayName}`),
    name: key.name,
  };
}

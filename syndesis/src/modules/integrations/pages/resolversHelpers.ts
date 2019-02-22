import {
  Action,
  ConnectionOverview,
  Integration,
  Step,
} from '@syndesis/models';
import * as H from 'history';
import resolvers from '../resolvers';

export function getStartSelectActionHref(connection: ConnectionOverview) {
  return resolvers.create.start.selectAction({ connection });
}

export function getFinishSelectActionHref(
  startConnection: ConnectionOverview,
  startAction: Action,
  integration: Integration,
  connection: ConnectionOverview
): H.LocationDescriptor {
  return resolvers.create.finish.selectAction({
    finishConnection: connection,
    integration,
    startAction,
    startConnection,
  });
}

export function getCreateAddConnectionHref(
  integration: Integration,
  position: string
) {
  return resolvers.create.configure.addConnection.selectConnection({
    integration,
    position,
  });
}

export function getCreateAddStepHref(
  integration: Integration,
  position: string
) {
  return resolvers.create.configure.addStep.selectStep({
    integration,
    position,
  });
}

export function getCreateSelectActionHref(
  position: string,
  integration: Integration,
  connection: ConnectionOverview
) {
  return resolvers.create.configure.addConnection.selectAction({
    connection,
    integration,
    position,
  });
}

export function getCreateConfigureConnectionHrefCallback(
  integration: Integration
) {
  return (stepIdx: number, step: Step) =>
    resolvers.create.configure.editConnection.configureAction({
      actionId: step.action!.id!,
      integration,
      position: `${stepIdx}`,
    });
}

export function getCreateConfigureStepHrefCallback(integration: Integration) {
  return (stepIdx: number, step: Step) => 'TODO';
}

export function getEditAddConnectionHref(
  integration: Integration,
  position: string
) {
  return resolvers.integration.edit.addConnection.selectConnection({
    integration,
    position,
  });
}

export function getEditAddStepHref(integration: Integration, position: string) {
  return resolvers.integration.edit.addStep.selectStep({
    integration,
    position,
  });
}

export function getEditSelectActionHref(
  position: string,
  integration: Integration,
  connection: ConnectionOverview
) {
  return resolvers.integration.edit.addConnection.selectAction({
    connection,
    integration,
    position,
  });
}

export function getEditConfigureConnectionHrefCallback(
  integration: Integration
) {
  return (stepIdx: number, step: Step) =>
    resolvers.integration.edit.editConnection.configureAction({
      actionId: step.action!.id!,
      integration,
      position: `${stepIdx}`,
    });
}

export function getEditConfigureStepHrefCallback(integration: Integration) {
  return (stepIdx: number, step: Step) => 'TODO';
}

import {
  ENDPOINT,
  getSteps,
  HIDE_FROM_CONNECTION_PAGES,
  isIntegrationApiProvider,
} from '@syndesis/api';
import { Integration } from '@syndesis/models';
import {
  IntegrationStepsHorizontalItem,
  IntegrationStepsHorizontalView,
  PageSection,
} from '@syndesis/ui';
import * as React from 'react';
import { EntityIcon } from '../../../shared';
import resolvers from '../../resolvers';
import { IUIStep } from './editor/interfaces';
import { toUIStepCollection } from './editor/utils';
import './IntegrationDetailSteps.css';

export interface IIntegrationDetailStepsProps {
  integration: Integration;
}

export const IntegrationDetailSteps: React.FunctionComponent<
  IIntegrationDetailStepsProps
> = props => {
  const isApiProvider = isIntegrationApiProvider(props.integration);
  const flowId = props.integration.flows![0].id!;
  const steps = getSteps(props.integration, flowId);

  return (
    <PageSection className="integration-detail-steps">
      <IntegrationStepsHorizontalView>
        {isApiProvider ? (
          <>
            <IntegrationStepsHorizontalItem
              key={'start'}
              name={'API Provider'}
              title={'API Provider'}
              icon={<p>TODO</p>}
              isLast={false}
            />
            <IntegrationStepsHorizontalItem
              key={'end'}
              name={'3 Flows'}
              title={'3 Flows'}
              icon={<p>TODO</p>}
              isLast={true}
            />
          </>
        ) : (
          toUIStepCollection(steps).map((s: IUIStep, idx: number) => {
            const isLast = idx === steps.length - 1;
            const stepUri =
              s.stepKind === ENDPOINT && !s.metadata[HIDE_FROM_CONNECTION_PAGES]
                ? resolvers.connections.connection.details({
                    connection: s.connection!,
                  }).pathname
                : undefined;
            return (
              <React.Fragment key={idx + s.id!}>
                <IntegrationStepsHorizontalItem
                  name={s.name}
                  title={s.title}
                  icon={<EntityIcon entity={s} alt={s.name} />}
                  href={stepUri}
                  isLast={isLast}
                />
              </React.Fragment>
            );
          })
        )}
      </IntegrationStepsHorizontalView>
    </PageSection>
  );
};

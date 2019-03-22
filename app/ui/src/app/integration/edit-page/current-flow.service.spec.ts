import { TestBed, inject } from '@angular/core/testing';

import {
  Connection,
  Action,
  createIntegration,
  createStep,
  Integration,
  Step,
  Flow,
  createConnectionStep,
} from '@syndesis/ui/platform';
import { CoreModule } from '@syndesis/ui/core';
import { ApiModule } from '@syndesis/ui/api';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { TypeFactory } from '@syndesis/ui/model';
import {
  IntegrationStore,
  IntegrationService,
  StepStore,
} from '@syndesis/ui/store';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page';
import { ConfigService } from '@syndesis/ui/config.service';
import { EVENTS_SERVICE_MOCK_PROVIDER } from '@syndesis/ui/store/entity/events.service.spec';

describe('CurrentFlow', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ApiModule.forRoot(),
        CoreModule.forRoot(),
        IntegrationSupportModule,
      ],
      providers: [
        CurrentFlowService,
        IntegrationStore,
        IntegrationService,
        EVENTS_SERVICE_MOCK_PROVIDER,
        ConfigService,
        StepStore,
      ],
    });
    inject([CurrentFlowService], c => (c.flowId = 'flow1'));
  });

  function getDummyIntegration(): Integration {
    const rc = createIntegration();
    const flow: Flow = {
      id: 'flow11',
      name: 'flow11',
      description: 'flow11',
      connections: <Connection[]>[],
      steps: <Step[]>[],
      metadata: {
        excerpt: 'flow11',
      },
    };
    rc.flows = [flow];

    const step1 = createStep();
    step1.id = 'foobar';
    step1.stepKind = 'endpoint';
    step1.connection = TypeFactory.create<Connection>();
    step1.connection.connectorId = 'timer';
    step1.action = TypeFactory.create<Action>();
    flow.steps.push(step1);

    const step2 = createStep();
    step2.id = '3';
    step2.stepKind = 'endpoint';
    step2.connection = TypeFactory.create<Connection>();
    step2.action = TypeFactory.create<Action>();
    flow.steps.push(step2);

    const step3 = createStep();
    step3.stepKind = 'log';
    flow.steps.push(step3);

    const step4 = createStep();
    step4.id = '4';
    step4.stepKind = 'endpoint';
    step4.connection = TypeFactory.create<Connection>();
    step4.connection.connectorId = 'http';
    step4.action = TypeFactory.create<Action>();
    flow.steps.push(step4);
    return rc;
  }

  it('should return the previous connection', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const step = currentFlowService.getPreviousConnection(2);
      expect(step.id).toEqual('3');
    }
  ));

  it('should return the subsequent connection', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const step = currentFlowService.getSubsequentConnection(2);
      expect(step.id).toEqual('4');
    }
  ));

  it('should return all subsequent connections', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const steps = currentFlowService.getSubsequentConnections(2);
      expect(steps.length).toEqual(1);
      expect(steps[0].id).toEqual('4');
    }
  ));

  it('should return all previous connections', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const steps = currentFlowService.getPreviousConnections(2);
      expect(steps.length).toEqual(2);
      expect(steps[0].id).toEqual('foobar');
    }
  ));

  it('should return the first step in the flow', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const conn = currentFlowService.getStartStep().connection;
      expect(conn.connectorId).toEqual('timer');
    }
  ));

  it('should return the last step in the flow', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const conn = currentFlowService.getEndStep().connection;
      expect(conn.connectorId).toEqual('http');
    }
  ));

  it('should return the middle steps in the flow', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const steps: Step[] = currentFlowService.getMiddleSteps();
      expect(steps.length).toEqual(2);
      expect(steps[0].id).toEqual('3');
      expect(steps[0].stepKind).toEqual('endpoint');
    }
  ));

  it('Should return an undefined start and end connection with an empty integration', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = <Integration>{};
      expect(currentFlowService.getStartStep()).toBeUndefined();
      expect(currentFlowService.getEndStep()).toBeUndefined();
      expect(currentFlowService.getFirstPosition()).toBeUndefined();
      expect(currentFlowService.getLastPosition()).toBeUndefined();
    }
  ));

  it('Should return an 0 as start and 1 end connection with an minimal integration', inject(
    [CurrentFlowService],
    (currentFlowService: CurrentFlowService) => {
      const flow = <Flow>{};
      flow.id = 'flow1';
      flow.steps = [createConnectionStep(), createConnectionStep()];
      const integration = <Integration>{};
      integration.flows = [flow];
      currentFlowService.integration = integration;
      expect(currentFlowService.getStartStep().connection).toBeUndefined();
      expect(currentFlowService.getEndStep().connection).toBeUndefined();
      expect(currentFlowService.getFirstPosition()).toBe(0);
      expect(currentFlowService.getLastPosition()).toBe(1);
    }
  ));
});

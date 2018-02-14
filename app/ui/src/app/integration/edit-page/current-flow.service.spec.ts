
/* tslint:disable */
import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RestangularModule } from 'ngx-restangular';
import { HttpClientModule } from '@angular/common/http';

import { Connection,
  Action,
  createIntegration,
  createStep,
  Integration,
  Step,
  Steps } from '@syndesis/ui/platform';
import { CoreModule } from '@syndesis/ui/core';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { ApiModule } from '@syndesis/ui/api';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { TypeFactory } from '@syndesis/ui/model';
import { EventsService, IntegrationStore, IntegrationService } from '@syndesis/ui/store';
import { CurrentFlowService, FlowEvent } from '@syndesis/ui/integration/edit-page';
import { ConfigService } from '@syndesis/ui/config.service';

describe('CurrentFlow', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ApiModule.forRoot(),
        HttpClientModule,
        RestangularModule.forRoot(),
        CoreModule.forRoot(),
        IntegrationSupportModule,
      ],
      providers: [
        CurrentFlowService,
        IntegrationStore,
        IntegrationService,
        EventsService,
        ConfigService,
        MockBackend,
        { provide: RequestOptions, useClass: BaseRequestOptions },
        {
          provide: Http,
          useFactory: (backend, options) => {
            return new Http(backend, options);
          },
          deps: [MockBackend, RequestOptions]
        }
      ]
    });
  });

  function getDummyIntegration(): Integration {
    const rc = createIntegration();
    rc.steps = <Steps>[];

    const step1 = createStep();
    step1.id = 'foobar';
    step1.stepKind = 'endpoint';
    step1.connection = TypeFactory.create<Connection>();
    step1.connection.connectorId = 'timer';
    step1.action = TypeFactory.create<Action>();
    rc.steps.push(step1);

    const step2 = createStep();
    step2.id = '3';
    step2.stepKind = 'endpoint';
    step2.connection = TypeFactory.create<Connection>();
    step2.action = TypeFactory.create<Action>();
    rc.steps.push(step2);

    const step3 = createStep();
    step3.stepKind = 'log';
    rc.steps.push(step3);

    const step4 = createStep();
    step4.id = '4';
    step4.stepKind = 'endpoint';
    step4.connection = TypeFactory.create<Connection>();
    step4.connection.connectorId = 'http';
    step4.action = TypeFactory.create<Action>();
    rc.steps.push(step4);
    return rc;
  }

  it(
    'should return the previous connection',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const step = currentFlowService.getPreviousConnection(2);
      expect(step.id).toEqual('3');
    })
  );

  it(
    'should return the subsequent connection',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const step = currentFlowService.getSubsequentConnection(2);
      expect(step.id).toEqual('4');
    })
  );

  it(
    'should return all subsequent connections',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const steps = currentFlowService.getSubsequentConnections(2);
      expect(steps.length).toEqual(1);
      expect(steps[0].id).toEqual('4');
    })
  );

  it(
    'should return all previous connections',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const steps = currentFlowService.getPreviousConnections(2);
      expect(steps.length).toEqual(2);
      expect(steps[0].id).toEqual('foobar');
    })
  );

  it(
    'should return the first step in the flow',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const conn = currentFlowService.getStartConnection();
      expect(conn.connectorId).toEqual('timer');
    })
  );

  it(
    'should return the last step in the flow',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const conn = currentFlowService.getEndConnection();
      expect(conn.connectorId).toEqual('http');
    })
  );

  it(
    'should return the middle steps in the flow',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = getDummyIntegration();
      const steps: Step[] = currentFlowService.getMiddleSteps();
      expect(steps.length).toEqual(2);
      expect(steps[0].id).toEqual('3');
      expect(steps[0].stepKind).toEqual('endpoint');
    })
  );

  it(
    'Should return an undefined start and end connection with an empty integration',
    inject([CurrentFlowService], (currentFlowService: CurrentFlowService) => {
      currentFlowService.integration = <Integration>{};
      expect(currentFlowService.getStartConnection()).toBeUndefined();
      expect(currentFlowService.getEndConnection()).toBeUndefined();
      expect(currentFlowService.getFirstPosition()).toEqual(0);
      expect(currentFlowService.getLastPosition()).toEqual(1);
    })
  );
});

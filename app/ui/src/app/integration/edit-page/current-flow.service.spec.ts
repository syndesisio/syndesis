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
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { ApiModule } from '@syndesis/ui/api';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { TypeFactory } from '@syndesis/ui/model';
import { EventsService, IntegrationStore, IntegrationService } from '@syndesis/ui/store';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { ConfigService } from '@syndesis/ui/config.service';

describe('CurrentFlow', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ApiModule.forRoot(),
        HttpClientModule,
        RestangularModule.forRoot(),
        IntegrationSupportModule,
      ],
      providers: [
        CurrentFlow,
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
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = getDummyIntegration();
      const step = service.getPreviousConnection(2);
      expect(step.id).toEqual('3');
    })
  );

  it(
    'should return the subsequent connection',
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = getDummyIntegration();
      const step = service.getSubsequentConnection(2);
      expect(step.id).toEqual('4');
    })
  );

  it(
    'should return all subsequent connections',
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = getDummyIntegration();
      const steps = service.getSubsequentConnections(2);
      expect(steps.length).toEqual(1);
      expect(steps[0].id).toEqual('4');
    })
  );

  it(
    'should return all previous connections',
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = getDummyIntegration();
      const steps = service.getPreviousConnections(2);
      expect(steps.length).toEqual(2);
      expect(steps[0].id).toEqual('foobar');
    })
  );

  it(
    'should return the first step in the flow',
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = getDummyIntegration();
      const conn = service.getStartConnection();
      expect(conn.connectorId).toEqual('timer');
    })
  );

  it(
    'should return the last step in the flow',
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = getDummyIntegration();
      const conn = service.getEndConnection();
      expect(conn.connectorId).toEqual('http');
    })
  );

  it(
    'should return the middle steps in the flow',
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = getDummyIntegration();
      const steps: Step[] = service.getMiddleSteps();
      expect(steps.length).toEqual(2);
      expect(steps[0].id).toEqual('3');
      expect(steps[0].stepKind).toEqual('endpoint');
    })
  );

  it(
    'Should return an undefined start and end connection with an empty integration',
    inject([CurrentFlow], (service: CurrentFlow) => {
      service.integration = <Integration>{};
      expect(service.getStartConnection()).toBeUndefined();
      expect(service.getEndConnection()).toBeUndefined();
      expect(service.getFirstPosition()).toEqual(0);
      expect(service.getLastPosition()).toEqual(1);
    })
  );
});

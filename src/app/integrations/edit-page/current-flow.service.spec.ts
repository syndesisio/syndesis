/* tslint:disable */
import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RestangularModule } from 'ngx-restangular';

import { CurrentFlow, FlowEvent } from './current-flow.service';
import { IntegrationStore } from '../../store/integration/integration.store';
import { IntegrationService } from '../../store/integration/integration.service';
import { Connection, Integration, Step, Steps, Action, TypeFactory } from '../../model';
import { EventsService } from '../../store/entity/events.service';
import { SyndesisCommonModule } from '../../common/common.module';

describe('CurrentFlow', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RestangularModule.forRoot(),
        SyndesisCommonModule,
      ],
      providers: [
        CurrentFlow,
        IntegrationStore,
        IntegrationService,
        EventsService,
        MockBackend,
        { provide: RequestOptions, useClass: BaseRequestOptions },
        {
          provide: Http, useFactory: (backend, options) => {
            return new Http(backend, options);
          }, deps: [MockBackend, RequestOptions],
        }],
    });
  });

  function getDummyIntegration(): Integration {
    const rc = TypeFactory.createIntegration();
    rc.steps = <Steps> [];

    const step1 = TypeFactory.createStep();
    step1.stepKind = 'endpoint';
    step1.connection = TypeFactory.createConnection();
    step1.connection.connectorId = 'timer';
    step1.action = TypeFactory.createAction();
    rc.steps.push(step1);

    const step2 = TypeFactory.createStep();
    step2.id = '3';
    step2.stepKind = 'endpoint';
    step2.connection = TypeFactory.createConnection();
    step2.action = TypeFactory.createAction();
    rc.steps.push(step2);

    const step3 = TypeFactory.createStep();
    step3.stepKind = 'log';
    rc.steps.push(step3);

    const step4 = TypeFactory.createStep();
    step4.stepKind = 'endpoint';
    step4.connection = TypeFactory.createConnection();
    step4.connection.connectorId = 'http';
    step4.action = TypeFactory.createAction();
    rc.steps.push(step4);
    return rc;
  }

  it('should return the first step in the flow', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = getDummyIntegration();
    const conn = service.getStartConnection();
    expect(conn.connectorId).toEqual('timer');
  }));

  it('should return the last step in the flow', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = getDummyIntegration();
    const conn = service.getEndConnection();
    expect(conn.connectorId).toEqual('http');
  }));

  it('should return the middle steps in the flow', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = getDummyIntegration();
    const steps: Step[] = service.getMiddleSteps();
    expect(steps.length).toEqual(2);
    expect(steps[0].id).toEqual('3');
    expect(steps[0].stepKind).toEqual('endpoint');
  }));

  it('Should return an undefined start and end connection with an empty integration', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = <Integration> {};
    expect(service.getStartConnection()).toBeUndefined();
    expect(service.getEndConnection()).toBeUndefined();
    expect(service.getFirstPosition()).toEqual(0);
    expect(service.getLastPosition()).toEqual(1);
  }));
});

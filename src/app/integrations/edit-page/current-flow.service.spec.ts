import { TestBed, async, inject } from '@angular/core/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { RestangularModule } from 'ng2-restangular';

import { CurrentFlow, FlowEvent } from './current-flow.service';
import { IntegrationStore } from '../../store/integration/integration.store';
import { IntegrationService } from '../../store/integration/integration.service';
import { Connection, Integration, Step } from '../../model';
import { EventsService } from '../../store/entity/events.service';
import { IPaaSCommonModule } from '../../common/common.module';

describe('ConfigService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RestangularModule.forRoot(),
        IPaaSCommonModule,
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
    return <Integration>{
      connections: [
        {
          id: '1',
          kind: 'connection',
        },
        {
          id: '2',
          kind: 'connection',
        }],
      steps: [
        {
          id: '1',
          kind: 'endpoint',
        },
        {
          id: '6',
          kind: 'whatever',
        },
        {
          id: '9',
          kind: 'sumthin\' else',
        },
        {
          id: '2',
          kind: 'endpoint',
        },
      ],
    };
  }

  it('should return the first step in the flow', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = getDummyIntegration();
    const step: any = service.getStartConnection();
    expect(step['id']).toEqual('1');
    expect(step['kind']).toEqual('connection');
  }));

  it('should return the last step in the flow', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = getDummyIntegration();
    const step: any = service.getEndConnection();
    expect(step['id']).toEqual('2');
    expect(step['kind']).toEqual('connection');
  }));

  it('should return the middle steps in the flow', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = getDummyIntegration();
    const steps: any[] = service.getMiddleSteps();
    expect(steps.length).toEqual(2);
    expect(steps[0]['id']).toEqual('6');
    expect(steps[0]['kind']).toEqual('whatever');
  }));

  it('Should return an undefined start and end connection with an empty integration', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = <Integration> {};
    expect(service.getStartConnection()).toBeUndefined();
    expect(service.getEndConnection()).toBeUndefined();
    expect(service.getFirstPosition()).toEqual(0);
    expect(service.getLastPosition()).toEqual(1);
  }));

  it('Should give me an undefined end connection when I add a start connection', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = <Integration> {};
    service.connections.push(<Connection> {
      id: '1',
      kind: 'connection',
      name: 'foo',
    });
    service.steps.push(<Step>{
      id: '1',
      kind: 'endpoint',
    });
    const start = service.getStartConnection();
    expect(start.id).toBe('1');
    expect(start.name).toBe('foo');
    const end = service.getEndConnection();
    expect(end).toBeUndefined();
    expect(service.getFirstPosition()).toEqual(0);
    expect(service.getLastPosition()).toEqual(1);
  }));

  it('Should give me the right end connection when I add start and end connections', inject([CurrentFlow], (service: CurrentFlow) => {
    service.integration = <Integration>{};
    // start connection;
    service.connections.push(<Connection>{
      id: '1',
      kind: 'connection',
      name: 'foo',
    });
    service.steps.push(<Step>{
      id: '1',
      kind: 'endpoint',
    });
    // start connection;
    service.connections.push(<Connection>{
      id: '2',
      kind: 'connection',
      name: 'bar',
    });
    service.steps.push(<Step>{
      id: '2',
      kind: 'endpoint',
    });
    const start = service.getStartConnection();
    expect(start.id).toBe('1');
    expect(start.name).toBe('foo');
    const end = service.getEndConnection();
    expect(end.id).toBe('2');
    expect(end.name).toBe('bar');
    expect(service.getFirstPosition()).toEqual(0);
    expect(service.getLastPosition()).toEqual(1);
  }));
});

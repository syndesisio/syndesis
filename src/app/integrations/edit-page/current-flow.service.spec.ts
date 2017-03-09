/* tslint:disable */
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

describe('CurrentFlow', () => {
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
      "configuration": "",
      "users": [

      ],
      "tags": [

      ],
      "steps": [
        {
          "action": {
            "properties": "{\"period\": { \"kind\": \"parameter\", \"displayName\": \"Period\", \"group\": \"consumer\", \"required\": false, \"type\": \"integer\", \"javaType\": \"long\", \"deprecated\": false, \"secret\": false, \"defaultValue\": 10000, \"description\": \"Period in milli seconds when the timer runs. By default the timer runs every 10000 (10 second).\" }}",
            "description": "Set a timer that fires at intervals that you specify",
            "camelConnectorGAV": "com.redhat.ipaas:timer-connector:0.2.1",
            "camelConnectorPrefix": "periodic-timer",
            "id": "com.redhat.ipaas_timer-connector_0.2.1",
            "name": "PeriodicTimer"
          },
          "connection": {
            "connectorId": "timer",
            "configuredProperties": "{}",
            "icon": "fa-globe",
            "description": "Timer Connection",
            "position": "start",
            "id": "4",
            "name": "Timer Example"
          },
          "stepKind": "endpoint",
          "configuredProperties": "{\"period\": { \"value\": \"5000\", \"kind\": \"parameter\", \"displayName\": \"Period\", \"group\": \"consumer\", \"required\": false, \"type\": \"integer\", \"javaType\": \"long\", \"deprecated\": false, \"secret\": false, \"defaultValue\": 10000, \"description\": \"Period in milli seconds when the timer runs. By default the timer runs every 10000 (10 second).\" }}",
          "id": "3"
        },
        {
          "action": {
            "properties": "{\"httpUri\": { \"kind\": \"path\", \"displayName\": \"Http Uri\", \"group\": \"producer\", \"label\": \"producer\", \"required\": true, \"type\": \"string\", \"javaType\": \"java.net.URI\", \"deprecated\": false, \"secret\": false, \"description\": \"The url of the HTTP endpoint to call.\" }}",
            "description": "Call a service that is internal (within your company) or external (on the internet) by specifying the service's URL",
            "camelConnectorGAV": "com.redhat.ipaas:http-get-connector:0.2.1",
            "camelConnectorPrefix": "http-get",
            "id": "com.redhat.ipaas:http-get-connector:0.2.1",
            "name": "HTTP GET"
          },
          "connection": {
            "connectorId": "http",
            "configuredProperties": "{}",
            "icon": "fa-globe",
            "description": "HTTP Connection",
            "position": "any",
            "id": "3",
            "name": "HTTP Example"
          },
          "stepKind": "endpoint",
          "configuredProperties": "{\"httpUri\": { \"value\": \"http:\/\/localhost:8080\/hello\", \"kind\": \"path\", \"displayName\": \"Http Uri\", \"group\": \"producer\", \"label\": \"producer\", \"required\": true, \"type\": \"string\", \"javaType\": \"java.net.URI\", \"deprecated\": false, \"secret\": false, \"description\": \"The url of the HTTP endpoint to call.\" }}",
          "id": "3"
        },
        {
          "stepKind": "log",
          "configuredProperties": "{ \"message\": \"Hello World! ${body}\",\"loggingLevel\": \"INFO\" }",
          "id": "4"
        },
        {
          "action": {
            "properties": "{\"httpUri\": { \"kind\": \"path\", \"displayName\": \"Http Uri\", \"group\": \"producer\", \"label\": \"producer\", \"required\": true, \"type\": \"string\", \"javaType\": \"java.net.URI\", \"deprecated\": false, \"secret\": false, \"description\": \"The url of the HTTP endpoint to call.\" }}",
            "description": "Call a service that is internal (within your company) or external (on the internet) by specifying the service's URL",
            "camelConnectorGAV": "com.redhat.ipaas:http-post-connector:0.2.1",
            "camelConnectorPrefix": "http-post",
            "id": "com.redhat.ipaas:http-post-connector:0.2.1",
            "name": "HTTP POST"
          },
          "connection": {
            "connectorId": "http",
            "configuredProperties": "{}",
            "icon": "fa-globe",
            "description": "HTTP Connection",
            "position": "end",
            "id": "3",
            "name": "HTTP Example"
          },
          "stepKind": "endpoint",
          "configuredProperties": "{\"httpUri\": { \"value\": \"http:\/\/localhost:8080\/bye\", \"kind\": \"path\", \"displayName\": \"Http Uri\", \"group\": \"producer\", \"label\": \"producer\", \"required\": true, \"type\": \"string\", \"javaType\": \"java.net.URI\", \"deprecated\": false, \"secret\": false, \"description\": \"The url of the HTTP endpoint to call.\" }}",
          "id": "5"
        }
      ],
      "description": "This is an example of a Timed Pull to Post Integration",
      "id": "2",
      "name": "Timed Pull to Post Example"
    }
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

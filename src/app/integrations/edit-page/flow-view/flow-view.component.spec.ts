import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TabsModule } from 'ng2-bootstrap';
import { CollapseModule } from 'ng2-bootstrap';
import { PopoverModule } from 'ng2-bootstrap';

import { RestangularModule } from 'ng2-restangular';

import { FlowViewComponent } from './flow-view.component';
import { FlowViewStepComponent } from './flow-view-step.component';
import { IntegrationStore } from '../../../store/integration/integration.store';
import { IntegrationService } from '../../../store/integration/integration.service';
import { CurrentFlow } from '../current-flow.service';
import { IPaaSCommonModule } from '../../../common/common.module';
import { ConnectionsModule } from '../../../connections/connections.module';
import { EventsService } from '../../../store/entity/events.service';

describe('IntegrationsCreateComponent', () => {
  let component: FlowViewComponent;
  let fixture: ComponentFixture<FlowViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterTestingModule.withRoutes([]),
        RestangularModule.forRoot(),
        ConnectionsModule,
        TabsModule.forRoot(),
        PopoverModule.forRoot(),
        CollapseModule.forRoot(),
        IPaaSCommonModule,
        CollapseModule,
      ],
      declarations: [
        FlowViewComponent,
        FlowViewStepComponent,
      ],
      providers: [
        MockBackend,
        {
          provide: RequestOptions,
          useClass: BaseRequestOptions,
        },
        {
          provide: Http, useFactory: (backend, options) => {
          return new Http(backend, options);
        }, deps: [MockBackend, RequestOptions],
        },
        CurrentFlow,
        IntegrationStore,
        IntegrationService,
        EventsService,
      ],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FlowViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

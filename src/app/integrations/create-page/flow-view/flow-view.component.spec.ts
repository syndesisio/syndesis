import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TabsModule } from 'ng2-bootstrap';
import { RestangularModule } from 'ng2-restangular';

import { FlowViewComponent } from './flow-view.component';
import { CurrentFlow } from '../current-flow.service';
import { IPaaSCommonModule } from '../../../common/common.module';
import { ConnectionsModule } from '../../../connections/connections.module';
import { CollapseModule } from 'ng2-bootstrap';

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
        TabsModule,
        IPaaSCommonModule,
        CollapseModule,
      ],
      declarations: [
        FlowViewComponent,
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

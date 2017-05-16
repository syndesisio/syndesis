/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';

import { SyndesisCommonModule } from '../../common/common.module';
import { FlowViewComponent } from './flow-view/flow-view.component';
import { FlowViewStepComponent } from './flow-view/flow-view-step.component';
import { ConnectionsListComponent } from '../../connections/list/list.component';
import { ConnectionsListToolbarComponent } from '../../connections/list-toolbar/list-toolbar.component';
import { StoreModule } from '../../store/store.module';
import { CurrentFlow } from './current-flow.service';

import { ModalModule } from 'ngx-bootstrap/modal';
import { CollapseModule } from 'ngx-bootstrap';
import { PopoverModule } from 'ngx-bootstrap';
import { ToasterModule } from 'angular2-toaster';

import { IntegrationsEditPage } from './edit-page.component';

describe('IntegrationsEditComponent', () => {
  let component: IntegrationsEditPage;
  let fixture: ComponentFixture<IntegrationsEditPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CollapseModule,
        CommonModule,
        FormsModule,
        SyndesisCommonModule,
        ModalModule,
        RestangularModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        PopoverModule.forRoot(),
        CollapseModule.forRoot(),
        StoreModule,
        ToasterModule,
      ],
      declarations: [
        IntegrationsEditPage,
        ConnectionsListComponent,
        ConnectionsListToolbarComponent,
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
      ],
    })
      .compileComponents();
  }));

  // TODO: Add separate test for editing
  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationsEditPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TabsModule, CollapseModule, PopoverModule } from 'ngx-bootstrap';
import { ModalModule } from 'ngx-bootstrap/modal';

import { ApiModule } from '@syndesis/ui/api';
import { CoreModule } from '@syndesis/ui/core';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { FlowViewComponent } from './flow-view.component';
import { FlowViewStepComponent } from './flow-view-step.component';
import { EventsService, IntegrationStore, StepStore } from '@syndesis/ui/store';
import { IntegrationService } from '../../../store/integration/integration.service';
import { SyndesisCommonModule } from '../../../common/common.module';
import { ConnectionsModule } from '../../../connections/connections.module';
import {
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';

describe('FlowViewComponent', () => {
  let component: FlowViewComponent;
  let fixture: ComponentFixture<FlowViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CoreModule.forRoot(),
        ApiModule.forRoot(),
        CommonModule,
        FormsModule,
        RouterTestingModule.withRoutes([]),
        ConnectionsModule,
        ModalModule.forRoot(),
        TabsModule.forRoot(),
        PopoverModule.forRoot(),
        CollapseModule.forRoot(),
        SyndesisCommonModule.forRoot(),
        IntegrationSupportModule,
        CollapseModule
      ],
      declarations: [FlowViewComponent, FlowViewStepComponent],
      providers: [
        CurrentFlowService,
        FlowPageService,
        IntegrationStore,
        IntegrationService,
        EventsService,
        StepStore
      ]
    }).compileComponents();
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

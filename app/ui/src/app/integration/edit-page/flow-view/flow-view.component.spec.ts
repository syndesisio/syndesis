import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ApiModule } from '@syndesis/ui/api';
import { CoreModule } from '@syndesis/ui/core';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { FlowViewComponent } from '@syndesis/ui/integration/edit-page/flow-view/flow-view.component';
import { FlowViewStepComponent } from '@syndesis/ui/integration/edit-page/flow-view/flow-view-step.component';
import { FlowViewMultiFlowComponent } from '@syndesis/ui/integration/edit-page/flow-view/flow-view-multiflow.component';
import { SyndesisStoreModule } from '@syndesis/ui/store';
import { IntegrationService } from '@syndesis/ui/store/integration/integration.service';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { ConnectionsModule } from '@syndesis/ui/connections/connections.module';
import { VendorModule } from '@syndesis/ui/vendor';
import {
  CurrentFlowService,
  FlowPageService,
} from '@syndesis/ui/integration/edit-page';
import { PlatformModule } from '@syndesis/ui/platform';
import { EVENTS_SERVICE_MOCK_PROVIDER } from '@syndesis/ui/store/entity/events.service.spec';

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
        SyndesisCommonModule.forRoot(),
        IntegrationSupportModule,
        SyndesisStoreModule,
        PlatformModule.forRoot(),
        VendorModule,
      ],
      declarations: [
        FlowViewComponent,
        FlowViewStepComponent,
        FlowViewMultiFlowComponent,
      ],
      providers: [
        CurrentFlowService,
        FlowPageService,
        IntegrationService,
        EVENTS_SERVICE_MOCK_PROVIDER,
      ],
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

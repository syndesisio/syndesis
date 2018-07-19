import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';

import { CoreModule } from '@syndesis/ui/core';

import {
  PlatformModule,
  ActionDescriptor,
  ActionDescriptorStep
} from '@syndesis/ui/platform';

import { ApiModule } from '@syndesis/ui/api';
import { ConfigService } from '@syndesis/ui/config.service';
import {
  EventsService,
  IntegrationStore,
  IntegrationService,
  StepStore
} from '@syndesis/ui/store';

import {
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { IntegrationConfigureActionComponent } from '@syndesis/ui/integration/edit-page/action-configure/action-configure.component';

describe('IntegrationConfigureActionComponent', () => {
  let component: IntegrationConfigureActionComponent;
  let fixture: ComponentFixture<IntegrationConfigureActionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        IntegrationSupportModule,
        RouterTestingModule.withRoutes([]),
        FormsModule,
        ReactiveFormsModule,
        DynamicFormsCoreModule.forRoot(),
        CoreModule.forRoot(),
        ApiModule.forRoot(),
        PlatformModule.forRoot()
      ],
      declarations: [IntegrationConfigureActionComponent],
      providers: [
        ConfigService,
        FlowPageService,
        CurrentFlowService,
        IntegrationStore,
        IntegrationService,
        EventsService,
        StepStore
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationConfigureActionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
      expect(component).toBeTruthy();
  });

  // aka oscerd's use case
  xit('no properties found', () => {
      const step: ActionDescriptorStep = { configuredProperties: {} } as ActionDescriptorStep;
      const propertyDefinitionSteps: Array<ActionDescriptorStep> = [step];
      const descriptor: ActionDescriptor = { propertyDefinitionSteps } as ActionDescriptor;
      expect(component.hasNoActionPropertiesToDisplay(descriptor)).toBeFalsy();
  });

});

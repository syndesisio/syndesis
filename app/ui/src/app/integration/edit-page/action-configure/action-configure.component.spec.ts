import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';

import { CoreModule } from '@syndesis/ui/core';

import {
  PlatformModule,
  ActionDescriptor,
  ActionDescriptorStep, ConfigurationProperty, StringMap,
} from '@syndesis/ui/platform';

import { ApiModule } from '@syndesis/ui/api';
import { ConfigService } from '@syndesis/ui/config.service';
import {
  IntegrationStore,
  IntegrationService,
  StepStore,
} from '@syndesis/ui/store';

import {
  CurrentFlowService,
  FlowPageService,
} from '@syndesis/ui/integration/edit-page';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { IntegrationConfigureActionComponent } from '@syndesis/ui/integration/edit-page/action-configure/action-configure.component';
import { EVENTS_SERVICE_MOCK_PROVIDER } from '@syndesis/ui/store/entity/events.service.spec';

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
        PlatformModule.forRoot(),
      ],
      declarations: [IntegrationConfigureActionComponent],
      providers: [
        ConfigService,
        FlowPageService,
        CurrentFlowService,
        IntegrationStore,
        IntegrationService,
        EVENTS_SERVICE_MOCK_PROVIDER,
        StepStore,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
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
    const step: ActionDescriptorStep = {
      configuredProperties: {},
    } as ActionDescriptorStep;
    const propertyDefinitionSteps: Array<ActionDescriptorStep> = [step];
    const descriptor: ActionDescriptor = {
      propertyDefinitionSteps,
    } as ActionDescriptor;
    expect(component.hasNoActionPropertiesToDisplay(descriptor)).toBeFalsy();
  });

  xit('only hidden properties found', () => {
    const step: ActionDescriptorStep = {
      properties: {
        name: {
          componentProperty: false,
          deprecated: false,
          description: 'The hidden property',
          displayName: 'Some property',
          javaType: 'String',
          kind: 'parameter',
          required: true,
          secret: false,
          type: 'hidden'
        } as ConfigurationProperty
      } as StringMap<ConfigurationProperty>,
    } as ActionDescriptorStep;
    const propertyDefinitionSteps: Array<ActionDescriptorStep> = [step];
    const descriptor: ActionDescriptor = {
      propertyDefinitionSteps,
    } as ActionDescriptor;
    expect(component.hasNoActionPropertiesToDisplay(descriptor)).toBeTruthy();
  });
});

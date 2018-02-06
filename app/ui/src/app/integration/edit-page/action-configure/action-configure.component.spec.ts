/* tslint:disable:no-unused-variable */
import { TestBed, async, inject, ComponentFixture } from '@angular/core/testing';
import { IntegrationConfigureActionComponent } from './action-configure.component';
import { ApiEndpointsLazyLoaderService } from '../../../api/providers/api-endpoints-lazy-loader.service';
import {
    ApiConfigService,
    ActionDefinition,
    ActionDefinitionStep,
    DataShape,
    UserService,
    FormFactoryService,
    IntegrationSupportService
} from '@syndesis/ui/platform';

import { RouterTestingModule } from '@angular/router/testing';
import { CurrentFlow } from '@syndesis/ui/integration/edit-page';
import { ActivatedRoute } from '@angular/router';
import { IntegrationSupportModule } from '@syndesis/ui/integration/integration-support.module';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { ReactiveFormsModule, FormGroup, FormControl, FormsModule } from '@angular/forms';
import { CoreModule } from '@syndesis/ui/core';

import {
    DynamicFormsCoreModule,
    DynamicFormService,
    DynamicCheckboxModel,
    DynamicCheckboxGroupModel,
    DynamicDatePickerModel,
    DynamicEditorModel,
    DynamicFileUploadModel,
    DynamicFormArrayModel,
    DynamicFormControlModel,
    DynamicFormGroupModel,
    DynamicInputModel,
    DynamicRadioGroupModel,
    DynamicSelectModel,
    DynamicSliderModel,
    DynamicSwitchModel,
    DynamicTextAreaModel,
    DynamicTimePickerModel
} from '@ng-dynamic-forms/core';
import { ApiModule } from '@syndesis/ui/api';
import { ConfigService } from '@syndesis/ui/config.service';
import { EventsService, IntegrationStore, IntegrationService } from '@syndesis/ui/store';
import { RestangularModule } from 'ngx-restangular';

class DummyActionDefinition implements ActionDefinition {
    constructor(
        public camelConnectorGAV: string,
        public camelConnectorPrefix: string,
        public outputDataShape: DataShape,
        public inputDataShape: DataShape,
        public propertyDefinitionSteps: Array<ActionDefinitionStep>) { }

}

class DummyActionDefinitionStep implements ActionDefinitionStep {
    constructor(
        public description: string,
        public properties: {},
        public configuredProperties: {}
    ) { }
}

describe('IntegrationConfigureActionComponent', () => {
    let component: IntegrationConfigureActionComponent;
    let fixture: ComponentFixture<IntegrationConfigureActionComponent>;

    beforeEach(
        async(() => {
            TestBed.configureTestingModule({
                imports: [
                    IntegrationSupportModule,
                    RouterTestingModule.withRoutes([]),
                    FormsModule,
                    ReactiveFormsModule,
                    DynamicFormsCoreModule.forRoot(),
                    CoreModule.forRoot(),
                    ApiModule.forRoot(),
                    RestangularModule.forRoot()
                ],
                declarations: [IntegrationConfigureActionComponent],
                providers: [
                    ConfigService,
                    CurrentFlow,
                    IntegrationStore,
                    IntegrationService,
                    EventsService
                ],
                schemas: [CUSTOM_ELEMENTS_SCHEMA]
            }).compileComponents();
        })
    );

    beforeEach(() => {
        fixture = TestBed.createComponent(IntegrationConfigureActionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    // aka oscerd's use case
    it('no properties found', () => {
        const step: ActionDefinitionStep = new DummyActionDefinitionStep(null, null, {});
        const arr: Array<ActionDefinitionStep> = [step];
        const descriptor: ActionDefinition = new DummyActionDefinition(null, null, null, null, arr);

        expect(component.hasActionPropertiesToDisplay(descriptor)).toBeFalsy();
    });

});

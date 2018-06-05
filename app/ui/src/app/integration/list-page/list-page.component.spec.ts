/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ngx-bootstrap/modal';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { NotificationModule } from 'patternfly-ng';

import { SyndesisCommonModule } from '../../common/common.module';
import { PatternflyUIModule } from '../../common/ui-patternfly/ui-patternfly.module';
import { IntegrationListModule } from '../list/list.module';
import { IntegrationListPage } from './list-page.component';
import { SyndesisStoreModule } from '../../store/store.module';

xdescribe('IntegrationsListPage', () => {
  let component: IntegrationListPage;
  let fixture: ComponentFixture<IntegrationListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SyndesisCommonModule.forRoot(),
        SyndesisStoreModule,
        RouterTestingModule.withRoutes([]),
        ModalModule.forRoot(),
        TooltipModule.forRoot(),
        TabsModule.forRoot(),
        NotificationModule,
        PatternflyUIModule,
        IntegrationListModule
      ],
      declarations: [IntegrationListPage]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

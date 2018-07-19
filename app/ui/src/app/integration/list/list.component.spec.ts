/* tslint:disable:no-unused-variable */
import { CommonModule } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { ActionModule, ListModule, ToastNotificationListModule as NotificationModule } from 'patternfly-ng';

import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { IntegrationListComponent } from '@syndesis/ui/integration/list/list.component';
import { IntegrationStatusComponent } from '@syndesis/ui/integration/list/status.component';
import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';

xdescribe('IntegrationsListComponent', () => {
  let component: IntegrationListComponent;
  let fixture: ComponentFixture<IntegrationListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        SyndesisCommonModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        ModalModule.forRoot(),
        TooltipModule.forRoot(),
        BsDropdownModule.forRoot(),
        TabsModule.forRoot(),
        SyndesisStoreModule,
        ActionModule,
        ListModule,
        NotificationModule
      ],
      declarations: [IntegrationStatusComponent, IntegrationListComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

/* tslint:disable:no-unused-variable */
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { BsDropdownModule, ModalModule } from 'ngx-bootstrap';
import {
  ActionModule,
  ListModule,
  ToastNotificationListModule as NotificationModule,
} from 'patternfly-ng';

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
        RouterModule,
        FormsModule,
        SyndesisCommonModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        ModalModule.forRoot(),
        BsDropdownModule.forRoot(),
        SyndesisStoreModule,
        ActionModule,
        ListModule,
        NotificationModule,
      ],
      declarations: [IntegrationStatusComponent, IntegrationListComponent],
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

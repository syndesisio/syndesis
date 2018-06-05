import { DebugElement } from '@angular/core';
import { CommonModule } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { NotificationModule } from 'patternfly-ng';
import { SyndesisStoreModule } from '../../store/store.module';

import { SyndesisCommonModule } from '../../common/common.module';
import { ConnectionsListComponent } from './list.component';
import { TestApiModule } from '@syndesis/ui/api/testing';

describe('ConnectionsListComponent', () => {
  let component: ConnectionsListComponent;
  let fixture: ComponentFixture<ConnectionsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TestApiModule,
        CommonModule,
        SyndesisCommonModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        ModalModule.forRoot(),
        BsDropdownModule.forRoot(),
        SyndesisStoreModule,
        NotificationModule
      ],
      declarations: [ConnectionsListComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

/* tslint:disable:no-unused-variable */
import { CommonModule } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ng2-bootstrap/modal';
import { DropdownModule } from 'ng2-bootstrap/dropdown';
import { ToasterModule } from 'angular2-toaster';

import { IPaaSCommonModule } from '../../common/common.module';
import { IntegrationsListComponent } from './list.component';
import { StoreModule } from '../../store/store.module';

describe('IntegrationsListComponent', () => {
  let component: IntegrationsListComponent;
  let fixture: ComponentFixture<IntegrationsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        IPaaSCommonModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        ModalModule.forRoot(),
        DropdownModule.forRoot(),
        ToasterModule,
        StoreModule,
      ],
      declarations: [IntegrationsListComponent],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

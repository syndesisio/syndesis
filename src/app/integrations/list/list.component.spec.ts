/* tslint:disable:no-unused-variable */
import { CommonModule } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { ToasterModule } from 'angular2-toaster';
import { TooltipModule } from 'ngx-bootstrap/tooltip';

import { SyndesisCommonModule } from '../../common/common.module';
import { IntegrationsListComponent } from './list.component';
import { StoreModule } from '../../store/store.module';

describe('IntegrationsListComponent', () => {
  let component: IntegrationsListComponent;
  let fixture: ComponentFixture<IntegrationsListComponent>;

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

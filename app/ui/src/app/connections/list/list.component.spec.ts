import { DebugElement } from '@angular/core';
import { CommonModule } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';

import { CoreModule } from '@syndesis/ui/core';
import { VendorModule } from '@syndesis/ui/vendor';
import { SyndesisStoreModule } from '../../store/store.module';
import { SyndesisCommonModule } from '../../common/common.module';
import { ConnectionsListComponent } from './list.component';
import { TestApiModule } from '@syndesis/ui/api/testing';
import { PlatformModule } from '@syndesis/ui/platform';

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
        SyndesisStoreModule,
        PlatformModule.forRoot(),
        CoreModule.forRoot(),
        VendorModule
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

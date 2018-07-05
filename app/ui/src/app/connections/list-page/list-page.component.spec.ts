import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { CoreModule } from '@syndesis/ui/core';
import { VendorModule } from '@syndesis/ui/vendor';
import { TestApiModule } from '@syndesis/ui/api/testing';

import { SyndesisStoreModule } from '../../store/store.module';
import { SyndesisCommonModule } from '../../common/common.module';
import { PatternflyUIModule } from '../../common/ui-patternfly/ui-patternfly.module';
import { ConnectionsListPage } from './list-page.component';
import { ConnectionsListComponent } from '../list/list.component';
import { PlatformModule } from '@syndesis/ui/platform';

describe('ConnectionListPage', () => {
  let component: ConnectionsListPage;
  let fixture: ComponentFixture<ConnectionsListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        TestApiModule,
        SyndesisCommonModule.forRoot(),
        PlatformModule.forRoot(),
        CoreModule.forRoot(),
        SyndesisStoreModule,
        RouterTestingModule.withRoutes([]),
        VendorModule,
        PatternflyUIModule
      ],
      declarations: [ConnectionsListPage, ConnectionsListComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

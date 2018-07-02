/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { VendorModule } from '@syndesis/ui/vendor';

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
        VendorModule,
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

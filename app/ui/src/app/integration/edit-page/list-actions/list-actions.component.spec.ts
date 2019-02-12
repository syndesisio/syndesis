/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import {
  ListActionsComponent
} from '@syndesis/ui/integration/edit-page/list-actions/list-actions.component';
import { SyndesisStoreModule } from '@syndesis/ui/store/store.module';
import { VendorModule } from '@syndesis/ui/vendor';

describe('ListActionsComponent', () => {
  let component: ListActionsComponent;
  let fixture: ComponentFixture<ListActionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SyndesisCommonModule,
        VendorModule,
        RouterTestingModule.withRoutes([]),
        SyndesisStoreModule
      ],
      declarations: [ListActionsComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

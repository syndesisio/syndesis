import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

import { ApiModule } from '@syndesis/ui/api';

import { MonitorRoutingModule } from './monitor.routing';
import { monitorEndpoints } from './monitor.api';
import { monitorReducer } from './monitor.reducer';
import { MonitorEffects } from './monitor.effects';
import { MonitorService } from './monitor.service';

@NgModule({
  imports: [
    CommonModule,
    MonitorRoutingModule,
    ApiModule.forChild(monitorEndpoints),
    StoreModule.forFeature('monitorState', monitorReducer),
    EffectsModule.forFeature([MonitorEffects]),
  ],
  exports: [MonitorRoutingModule],
  providers: [MonitorService],
  declarations: [],
})
export class MonitorModule { }

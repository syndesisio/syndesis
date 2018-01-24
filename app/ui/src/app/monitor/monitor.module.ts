import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MonitorRoutingModule } from './monitor.routing';
import { MonitorService } from './monitor.service';

@NgModule({
  imports: [CommonModule, MonitorRoutingModule],
  exports: [MonitorRoutingModule],
  providers: [MonitorService],
  declarations: [],
})
export class MonitorModule { }

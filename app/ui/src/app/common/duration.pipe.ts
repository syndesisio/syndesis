import { Pipe, PipeTransform } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

import { moment } from '@syndesis/ui/vendor';

@Pipe({
  name: 'synDuration'
})
export class DurationPipe implements PipeTransform {
  transform(milliseconds: number): string {
    if (!milliseconds) {
      return 'NaN';
    }

    const durationMoment = moment.duration(milliseconds);
    const days = Math.floor(durationMoment.days());
    const hours = Math.floor(durationMoment.hours());
    const minutes = Math.floor(durationMoment.minutes());
    const seconds = Math.floor(durationMoment.seconds());
    const ms = Math.floor(durationMoment.milliseconds());

    const durationStrings = [];
    if (days > 0) {
      durationStrings.push(`${days} days`);
    }
    if (hours > 0) {
      durationStrings.push(`${hours} hours`);
    }
    if (minutes > 0) {
      durationStrings.push(`${minutes} minutes`);
    }
    if (seconds > 0) {
      durationStrings.push(`${seconds} seconds`);
    }
    if (durationStrings.length == 0) {
      if (ms > 0) {
        durationStrings.push(`${ms} ms`);
      } else if (milliseconds != 0) {
        durationStrings.push(`${milliseconds.toFixed(2)} ms`);
      }
    }
    return durationStrings.join(', ').trim();
  }
}

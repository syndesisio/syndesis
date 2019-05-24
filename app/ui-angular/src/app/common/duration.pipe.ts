import { Pipe, PipeTransform } from '@angular/core';

import { moment } from '@syndesis/ui/vendor';

@Pipe({
  name: 'synDuration'
})
export class DurationPipe implements PipeTransform {
  transform(timeDuration: number, unit: 'ms' | 'ns'): string {
    if (typeof timeDuration === 'undefined') {
      return 'NaN';
    }

    if (timeDuration <= 0) {
      return '-';
    }

    if (unit === 'ns') {
      timeDuration = timeDuration / 1000000;
    }

    const durationMoment = moment.duration(timeDuration);
    const days = Math.floor(durationMoment.days());
    const hours = Math.floor(durationMoment.hours());
    const minutes = Math.floor(durationMoment.minutes());
    const seconds = Math.floor(durationMoment.seconds());
    const milliseconds = Math.floor(durationMoment.milliseconds());

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
      if (milliseconds > 0) {
        durationStrings.push(`${milliseconds} ms`);
      } else if (timeDuration != 0) {
        durationStrings.push(`${timeDuration.toFixed(2)} ms`);
      }
    }

    return durationStrings.join(', ').trim();
  }
}

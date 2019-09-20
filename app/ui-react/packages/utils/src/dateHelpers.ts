import moment from 'moment';

export function toUptimeDurationString(
  timestamp: number,
  defaultValue = 'n/a'
): string {
  if (!timestamp) {
    return defaultValue;
  }

  const uptimeDuration = moment.duration(timestamp);
  const duration = {
    days: uptimeDuration.days(),
    hours: uptimeDuration.hours(),
    minutes: uptimeDuration.minutes(),
    seconds: uptimeDuration.seconds()
  };
  const durationString = Object.keys(duration).reduce(
    (timeSpan: string, key: string) => {
      if (duration[key] > 0) {
        if (key === 'seconds') {
          if (timeSpan.length === 0) {
            // show seconds only when overall duration is below one minute
            return `${duration[key]} ${key}`;
          } else {
            return timeSpan;
          }
        }

        return timeSpan + `${duration[key]} ${key} `;
      }

      return timeSpan;
    },
    ''
  );
  return durationString && durationString.length > 0
    ? durationString
    : defaultValue;
}

export function toDurationString(
  timeDuration: number,
  unit: 'ms' | 'ns'
): string {
  if (timeDuration === undefined) {
    return 'NaN';
  }
  if (timeDuration === 0) {
    return '0 ms';
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
  const durationStrings: string[] = [];
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
  if (durationStrings.length === 0) {
    if (milliseconds > 0) {
      durationStrings.push(`${milliseconds} ms`);
    } else if (timeDuration !== 0) {
      durationStrings.push(`${timeDuration.toFixed(2)} ms`);
    }
  }
  return durationStrings.join(', ').trim();
}

/***
 *
 * @param timestamp - Date to be formatted
 * @return string - Formatted date as, for example, Jan 1st 23:42
 */
export function toShortDateAndTimeString(timestamp: number): string {
  if (!timestamp) {
    return 'NaN';
  }

  return moment(timestamp).format('MMM Do HH:mm');
}

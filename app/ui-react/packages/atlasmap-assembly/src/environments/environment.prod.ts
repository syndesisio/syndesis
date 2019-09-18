import { NgxLoggerLevel } from 'ngx-logger';

export const environment = {
  // AtlasMap skips Maven classpath resolution if (classpath)
  classpath: ' ',
  production: true,
  xsrf: {
    headerName: 'SYNDESIS-XSRF-TOKEN',
    cookieName: 'SYNDESIS-XSRF-COOKIE',
    defaultTokenValue: 'awesome',
  },
  ngxLoggerConfig: {
    level: NgxLoggerLevel.ERROR,
    disableConsoleLogging: true
  },
};

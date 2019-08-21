#!/usr/bin/env node
const url = require('url');
const path = require('path');
const yargs = require('yargs');
const talkback = require('talkback');

// Disable self-signed certificate check
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const server = async argv => {
  const server = talkback({
    host: argv.host,
    port: argv.port,
    path: path.join(argv.recordingsPath),
    record: argv.record ? 'OVERWRITE' : 'DISABLED',
    ignoreHeaders: [
      'content-length',
      'host',
      'cookie',
      'cache-control',
      'pragma',
      'referer',
      'origin',
      'accept',
      'accept-encoding',
      'accept-language',
      'user-agent',
      'x-forwarded-access-token',
      'x-forwarded-origin',
      'upgrade-insecure-requests',
      'syndesis-xsrf-token',
      'content-type',
      'connection',
      'proxy-connection',
    ],
    silent: argv.silent,
    summary: argv.summary,
    debug: argv.debug,
    tapeNameGenerator: (tapeNumber, tape) => {
      return path.normalize(
        `${tape.req.headers[argv.sessionHeader]}/${tape.req.url}/${tapeNumber}`
      );
    },
  });
  await server.start();
  console.log(
    argv.record
      ? `Mock recording started, proxying against ${
          argv.host
        }, recording session "${argv.session}"`
      : `Replaying sessions`
  );
};

yargs
  .option('port', {
    default: 8556,
    describe: 'This server port.',
    number: true,
  })
  .option('recordingsPath', {
    default: path.join(__dirname, '..', 'tapes'),
    describe: 'Path where to load and save recorded sessions.',
    normalize: true,
  })
  .option('silent', {
    default: false,
    describe:
      'Disable requests information console messages in the middle of requests.',
  })
  .option('summary', {
    default: false,
    describe: 'Enable exit summary of new and unused tapes at exit.',
  })
  .option('debug', {
    default: false,
    describe: 'Enable verbose debug information.',
  })
  .command(
    ['$0', 'replay'],
    'Replay a previously recorded session',
    yargs => yargs,
    server
  )
  .command(
    'record <host>',
    'Proxy and record requests to <host>',
    yargs =>
      yargs
        .positional('host', {
          describe: 'The remote backend host',
        })
        .coerce('host', host => {
          const parsedUrl = url.parse(host, false, true);
          if (!parsedUrl.protocol || !parsedUrl.host) {
            throw 'Invalid host';
          }
          return `${parsedUrl.protocol}//${parsedUrl.host}`;
        })
        .option('session-header', {
          session:
            'The name of the header containing the session that should be replayed.',
          default: 'syndesis-mock-session',
        })
        .example('$0 record https://syndesis.192.168.64.1.nip.io'),
    argv =>
      server({
        ...argv,
        record: true,
      })
  )
  .help().argv;

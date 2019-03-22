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
    path: path.join(argv.recordingsPath, argv.session),
    record: argv.record,
    ignoreHeaders: [
      'content-length',
      'host',
      'cookie',
      'cache-control',
      'pragma',
    ],
    silent: argv.silent,
    summary: argv.summary,
    debug: argv.debug,
  });
  await server.start();
  console.log(
    argv.record
      ? `Mock recording started, proxing against ${
          argv.host
        }, recording session "${argv.session}"`
      : `Replaying session "${argv.session}"`
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
    ['$0 <session>', 'replay <session>'],
    'Replay the previously recorded <session>',
    yargs =>
      yargs
        .positional('session', {
          session: 'The name of the session to load and record the requests.',
        })
        .example(
          '$0 dashboard',
          'Replays a previously recorded session named dashboard'
        ),
    server
  )
  .command(
    'record <session> <host>',
    'Record a <session> proxying request to <host>. If <session> already exists, it will be updated.',
    yargs =>
      yargs
        .positional('session', {
          session: 'The name of the session to load and record the requests.',
        })
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
        .example(
          '$0 record dashboard https://syndesis.192.168.64.1.nip.io',
          'Replays a previously recorded session named dashboard'
        ),
    argv =>
      server({
        ...argv,
        record: true,
      })
  )
  .help().argv;

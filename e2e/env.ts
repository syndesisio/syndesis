import {browser} from 'protractor';

module.exports = function () {

  // see list of available hooks here
  // https://github.com/cucumber/cucumber-js/blob/master/docs/support_files/event_handlers.md

  this.setDefaultTimeout(400 * 1000);

  // todo figure out proper browser restart
  // this.BeforeFeature(function (event, callback) {
  //   console.log('restarting browser before scenario: ' + event.getPayloadItem('scenario'));
  //   browser.restart();
  //   callback();
  // });

  /**
   * create screenshot after each cucumber scenario
   */
  this.After(function (scenario, next) {
    browser.takeScreenshot().then(function (png) {
      const decodedImage = new Buffer(png, 'base64');
      scenario.attach(decodedImage, 'image/png', next);
    }, function (err) {
      next(err);
    });
  });

};

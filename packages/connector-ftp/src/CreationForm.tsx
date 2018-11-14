import * as React from 'react';

export class CreationForm extends React.Component<{}> {
  public render() {
    return (
      <div className="col-md-8 col-md-offset-2" style={{ marginTop: 20 }}>
        <div className="card-pf">
          <div className="card-pf-heading">
            <h2 className="card-pf-title">FTP Configuration</h2>
          </div>
          <div className="card-pf-body">
            <div>
              <form className="form-horizontal ng-untouched ng-pristine ng-invalid">
                <div className="form-group">
                  <div className="col-sm-12">
                    <div className="ng-untouched ng-pristine ng-invalid">
                      <div _ngcontent-c15="" className="text-right">
                        <button
                          _ngcontent-c15=""
                          className="btn btn-primary"
                          disabled
                        >
                          Validate
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
                <p className="fields-status-pf">
                  The fields marked with <span className="required-pf">*</span>{' '}
                  are required.
                </p>

                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3 required-pf"
                      htmlFor="host"
                    >
                      Host
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-invalid"
                          autoComplete="on"
                          name="host"
                          placeholder=""
                          type="text"
                          required
                          id="host"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="username"
                    >
                      User name
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-valid"
                          autoComplete="on"
                          name="username"
                          placeholder=""
                          type="text"
                          id="username"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="password"
                    >
                      Password
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-valid"
                          autoComplete="off"
                          name="password"
                          placeholder=""
                          type="password"
                          id="password"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3 required-pf"
                      htmlFor="port"
                    >
                      Port
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-valid"
                          autoComplete="on"
                          name="port"
                          placeholder=""
                          type="number"
                          required
                          id="port"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="connectTimeout"
                    >
                      Connect timeout
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-valid"
                          autoComplete="on"
                          name="connectTimeout"
                          placeholder=""
                          type="number"
                          id="connectTimeout"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="reconnectDelay"
                    >
                      Reconnect delay
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-valid"
                          autoComplete="on"
                          name="reconnectDelay"
                          placeholder=""
                          type="number"
                          id="reconnectDelay"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="maximumReconnectAttempts"
                    >
                      Maximum reconnect attempts
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-valid"
                          autoComplete="on"
                          name="maximumReconnectAttempts"
                          placeholder=""
                          type="number"
                          id="maximumReconnectAttempts"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="binary"
                    >
                      Binary file transfer mode
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <select
                        _ngcontent-c16=""
                        className="form-control ng-untouched ng-pristine ng-valid"
                        name="binary"
                        id="binary"
                      >
                        <option _ngcontent-c16="" value="0: false">
                          No
                        </option>
                        <option _ngcontent-c16="" value="1: true">
                          Yes
                        </option>
                      </select>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="passiveMode"
                    >
                      Passive connection mode
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <select
                        _ngcontent-c16=""
                        className="form-control ng-untouched ng-pristine ng-valid"
                        name="passiveMode"
                        id="passiveMode"
                      >
                        <option _ngcontent-c16="" value="0: false">
                          No
                        </option>
                        <option _ngcontent-c16="" value="1: true">
                          Yes
                        </option>
                      </select>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="disconnect"
                    >
                      Disconnect from server after use
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <select
                        _ngcontent-c16=""
                        className="form-control ng-untouched ng-pristine ng-valid"
                        id="disconnect"
                      >
                        <option _ngcontent-c16="" value="0: false">
                          No
                        </option>
                        <option _ngcontent-c16="" value="1: true">
                          Yes
                        </option>
                      </select>
                    </div>
                  </div>
                </div>
                <div>
                  <div
                    _ngcontent-c16=""
                    className="form-group ng-untouched ng-pristine ng-invalid"
                  >
                    <label
                      _ngcontent-c16=""
                      className="control-label col-sm-3"
                      htmlFor="timeout"
                    >
                      Data timeout
                      <div _ngcontent-c16="" className="inline-block hint-icon">
                        <span
                          _ngcontent-c16=""
                          className="glyphicon glyphicon-info-sign"
                        />
                      </div>
                    </label>

                    <div _ngcontent-c16="" className="col-sm-9">
                      <div _ngcontent-c16="">
                        <input
                          _ngcontent-c16=""
                          className="form-control ng-untouched ng-pristine ng-valid"
                          autoComplete="on"
                          name="timeout"
                          placeholder=""
                          type="number"
                          id="timeout"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

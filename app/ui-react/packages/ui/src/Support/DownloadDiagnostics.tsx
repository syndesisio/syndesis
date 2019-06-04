import { CardBody, Popover } from '@patternfly/react-core';
import { HelpIcon } from '@patternfly/react-icons';
import * as React from 'react';
export interface IDownloadDiagnosticsProps {
  children: React.ReactNode;
}

export const DownloadDiagnostics: React.FunctionComponent<
  IDownloadDiagnosticsProps
> = ({ children }) => (
  <>
    <CardBody>
      <div className="pf-c-content">
        <h2 className="pf-c-title pf-m-lg">
          Download Troubleshooting Diagnostics &nbsp;
          <Popover
            position="right"
            bodyContent={
              <p>
                System level and application level diagnostics will be captured
                since both are required to troubleshoot any issues. Usernames
                and passwords are not present in the downloaded zip file.
              </p>
            }
            headerContent={
              <span className="pf-c-title pf-m-lg">
                Download Troubleshooting Diagnostics
              </span>
            }
          >
            <HelpIcon />
          </Popover>
        </h2>
        {children}
      </div>
    </CardBody>
  </>
);

import { IWithConfigProps as mockIWithConfigProps } from '../WithConfig';

export const WithConfig = ({ children }: mockIWithConfigProps) => {
  return children({
    config: {
      apiBase: 'http://127.0.0.1:8556',
      apiEndpoint: '/api/v1',
      branding: {
        appName: 'Syndesis - DEVELOPMENT',
        favicon16: '/favicon-16x16.png',
        favicon32: '/favicon-32x32.png',
        logoDarkBg: 'assets/images/syndesis_logo_full_darkbkg.svg',
        logoWhiteBg: 'assets/images/syndesis_logo_full_darkbkg.svg',
        productBuild: false,
        touchIcon: '/apple-touch-icon.png',
      },
      consoleUrl: 'https://console.address/console',
      datamapper: {
        baseJSONInspectionServiceUrl: '/api/v1/atlas/json/',
        baseJavaInspectionServiceUrl: '/api/v1/atlas/java/',
        baseMappingServiceUrl: '/api/v1/atlas/',
        baseXMLInspectionServiceUrl: '/api/v1/atlas/xml/',
        disableMappingPreviewMode: false,
      },
      datavirt: {
        dvUrl: '/vdb-builder/v1/',
        enabled: 0
      },
      features: {
        logging: false,
      },
      project: 'syndesis',
      title: 'Syndesis',
    },
    error: false,
    loading: false,
  });
};

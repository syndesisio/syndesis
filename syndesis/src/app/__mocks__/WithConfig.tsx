import { IWithConfigProps as mockIWithConfigProps } from '../WithConfig';

export const WithConfig = ({ children }: mockIWithConfigProps) => {
  return children({
    config: {
      apiBase: 'https://syndesis.192.168.64.17.nip.io',
      apiEndpoint: '/api/v1',
      branding: {
        appName: 'Syndesis - DEVELOPMENT',
        favicon16: '/favicon-16x16.png',
        favicon32: '/favicon-32x32.png',
        iconDarkBg: 'assets/images/glasses_logo_square.png',
        iconWhiteBg: 'assets/images/glasses_logo_square.png',
        logoDarkBg: 'assets/images/syndesis-logo-svg-white.svg',
        logoWhiteBg: 'assets/images/syndesis-logo-svg-white.svg',
        productBuild: false,
        touchIcon: '/apple-touch-icon.png',
      },
      consoleUrl: 'https://192.168.64.17:8443/console',
      datamapper: {
        baseJSONInspectionServiceUrl: '/api/v1/atlas/json/',
        baseJavaInspectionServiceUrl: '/api/v1/atlas/java/',
        baseMappingServiceUrl: '/api/v1/atlas/',
        baseXMLInspectionServiceUrl: '/api/v1/atlas/xml/',
        disableMappingPreviewMode: false,
      },
      datavirt: {
        dvUrl: '/vdb-builder/v1/',
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

export interface IConfigFile {
  apiBase: string;
  apiEndpoint: string;
  title: string;
  consoleUrl: string;
  project: string;
  datamapper: {
    baseMappingServiceUrl: string;
    baseJavaInspectionServiceUrl: string;
    baseXMLInspectionServiceUrl: string;
    baseJSONInspectionServiceUrl: string;
    disableMappingPreviewMode: boolean;
  };
  features: {
    logging: boolean;
  };
  branding: {
    logoWhiteBg: string;
    logoDarkBg: string;
    iconWhiteBg: string;
    iconDarkBg: string;
    appName: string;
    favicon32: string;
    favicon16: string;
    touchIcon: string;
    productBuild: boolean;
  };
}

export default {
  apiBase: document!.location!.origin!,
  apiEndpoint: '/api/v1',
  title: 'Syndesis',
  consoleUrl: 'https://192.168.64.17:8443/console',
  project: 'syndesis',
  datamapper: {
    baseMappingServiceUrl: `${document!.location!.origin!}/api/v1/atlas/`,
    baseJavaInspectionServiceUrl: `${document!.location!
      .origin!}/api/v1/atlas/java/`,
    baseXMLInspectionServiceUrl: `${document!.location!
      .origin!}/api/v1/atlas/xml/`,
    baseJSONInspectionServiceUrl: `${document!.location!
      .origin!}/api/v1/atlas/json/`,
    disableMappingPreviewMode: false,
  },
  features: {
    logging: false,
  },
  branding: {
    logoWhiteBg: 'assets/images/syndesis-logo-svg-white.svg',
    logoDarkBg: 'assets/images/syndesis-logo-svg-white.svg',
    iconWhiteBg: 'assets/images/glasses_logo_square.png',
    iconDarkBg: 'assets/images/glasses_logo_square.png',
    appName: 'Syndesis',
    favicon32: '/favicon-32x32.png',
    favicon16: '/favicon-16x16.png',
    touchIcon: '/apple-touch-icon.png',
    productBuild: false,
  },
} as IConfigFile;

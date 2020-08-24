const TerserPlugin = require('terser-webpack-plugin');

module.exports = function({ env, paths }) {
  return {
    overrideCracoConfig: ({
      cracoConfig,
      pluginOptions,
      context: { env, paths },
    }) => {
      // Always return the config object.
      return cracoConfig;
    },
    webpack: {
      configure: (webpackConfig, { env, paths }) => ({
        ...webpackConfig,

        optimization: {
          ...webpackConfig.optimization,
          minimizer: [
            new TerserPlugin({
              parallel: false,
            }),
          ],
        },
      }),
    },
    jest: {
      configure: (jestConfig, { env, paths, resolve, rootDir }) => ({
        ...jestConfig,

        moduleFileExtensions: ['ts', 'tsx', 'js'],
      }),
    },
  };
};

const TerserPlugin = require('terser-webpack-plugin');

module.exports = function({ env, paths }) {
  return {
    webpack: {
      configure: (webpackConfig, { env, paths }) => ({
        ...webpackConfig,

        optimization: {
          ...webpackConfig.optimization,
          minimizer: [
            new TerserPlugin({
              parallel: 2,
            }),
          ],
        },
      }),
    },
  };
};

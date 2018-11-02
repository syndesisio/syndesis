const path = require("path");

module.exports = (baseConfig, env, defaultConfig) => {
  // Extend defaultConfig as you need.

  // For example, add typescript loader:
  defaultConfig.module.rules.push({
    test: /\.(js|mjs|jsx|ts|tsx)$/,
    include: path.resolve(__dirname, '../src'),
    loader: require.resolve('babel-loader'),
    options: {
      customize: require.resolve(
        'babel-preset-react-app/webpack-overrides'
      ),
      babelrc: false,
      configFile: false,
      presets: [require.resolve('babel-preset-react-app')],
      plugins: [
        [
          require.resolve('babel-plugin-named-asset-import'),
          {
            loaderMap: {
              svg: {
                ReactComponent: '@svgr/webpack?-prettier,-svgo![path]',
              },
            },
          },
        ],
      ],
    },
  });
  defaultConfig.resolve.extensions.push(".ts", ".tsx");

  return defaultConfig;
};
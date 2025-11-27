const path = require("path");

module.exports = {
  // Define the entry points here. They MUST have the same name as the page_id
  // defined in src/main/java/org/sonarsource/plugins/example/web/MyPluginPageDefinition.java
  entry: {
    // Using React:
    report_page: ["./src/index.jsx"],
  },

  resolve: {
    extensions: [".tsx", ".ts", ".js", ".jsx"],
    symlinks: true,
    modules: [
      path.join(__dirname, "src"),
      "node_modules"
    ]
  },
  externals: {
    // Register the Sonar* globals as packages, to simplify importing.
    // See src/main/js/common/api.js for more information on what is exposed
    // in SonarRequest.
    "sonar-helpers": "SonarHelpers",
    "sonar-request": "SonarRequest",
    // TODO: provide an example
    "sonar-measures": "SonarMeasures",
    // See src/main/js/portfolio_page/components/MeasuresHistory.js for some
    // examples using React components from SonarQube.
    "sonar-components": "SonarComponents"
  },
  // experiments: { outputModule: true },
  optimization: {
    // fix node modules not packaged into zip
    concatenateModules: false
  },
  module: {
    // Our example uses Babel to transpile our code.
    rules: [
      {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use:
        {
          loader: 'ts-loader',
          options: {
            logInfoToStdOut: true,
            logLevel: "info"
          }
        }
      },
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader'
        }
      },
      {
        test: /\.css/,
        use: [
          { loader: 'style-loader' },
          { loader: 'css-loader' },
          {
            loader: 'postcss-loader',
            options: {
              postcssOptions: {
                ident: 'postcss',
                plugins: [
                  require('autoprefixer'),
                ]
              },
            }
          }
        ],
      }
    ]
  }
};

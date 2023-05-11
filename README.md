individuals-income-received-api
========================

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The Individuals Income Received API allows a developer to create, amend, retrieve and delete data relating to:

- Employments
- Other Employment Income
- Dividends Income
- Foreign Income
- Insurance Policies Income
- Pensions Income
- Other Income
- Savings Income

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager V2](https://github.com/hmrc/sm2)

## Development Setup

Run the microservice from the console using: `sbt run` (starts on port 7794 by default)

Start the service manager profile: `sm --start MTDFB_INDIVIDUALS_INCOME_RECEIVED`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

Note: if you run into `java.lang.OutOfMemoryError` errors, add a `.sbtopts` file to the root of the project with the
following contents:

```
-J-Xmx3G
-J-XX:+UseG1GC
```

### Alternatively, run one of the test scripts

## To view the YAML

To view documentation locally, ensure the Individuals Income Received API is running, and run
api-documentation-frontend:

```
./run_local_with_dependencies.sh
```

Then go to http://localhost:9680/api-documentation/docs/preview and enter the full URL path to the YAML file with the
appropriate port and version:

```
http://localhost:7794/api/conf/1.0/application.yaml
```

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individuals-income-received-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

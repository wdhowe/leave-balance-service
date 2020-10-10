# leave-balance-service

Http service with an endpoint to calculate PTO/leave balance at the end of the year and how much will be lost if none is taken.

Dockerhub: [https://hub.docker.com/r/wdhowe/leave-balance-service](https://hub.docker.com/r/wdhowe/leave-balance-service)

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Building

Building parts of the project.

### Build Standalone Uberjar

The standalone uberjar can be built with lein and run with the java -jar command.

* Clone the project
* Build the uberjar

  ```bash
  lein uberjar
  ```

### Build Docker Image

The docker image can be built by:

* Clone the project
* Build the container

  ```bash
  docker build -t leave_service:mytag .
  ```

## Running

Different ways to run the service.

### Run With Lein or Jar

* Export environment variables for server settings if wanting to override defaults.

  Example
  
  ```bash
  # Max leave balance you can carry over each year (default: 200)
  export MAX_BAL=150
  
  # Port the http-server will listen on (default: 8080)
  export HTTP_PORT=8000

  # End of year (default: last day of current calendar year)
  # This setting may be useful if fiscal years are used instead of calendar years.
  export YEAR_END="2020-10-31"
  ```

* Run the http-server
  * With lein

    ```bash
    lein run
    ```

  * With the built uberjar

    ```bash
    java -jar target/leave-balance-service-0.2.1-standalone.jar
    ```

### Run With Docker

* Run pre-built image from dockerhub

  ```bash
  docker run -it -p 8080:8080 --name leave-service wdhowe/leave-balance-service
  ```

* Run locally built with default settings

  ```bash
  docker run -it -p 8080:8080 --rm --name leave-service leave_service:mytag
  ```

* Run locally built with new env settings (change listening port and max balance)

  ```bash
  docker run -it -p 8000:8000 --env HTTP_PORT=8000 --env MAX_BAL=150 --rm --name leave-service leave_service:mytag
  ```

## Client requests

Send client requests to the running http-server.

### Routes

Available routes are

* GET /  -> return plain text help.
* GET /help  -> return plain text help.
* GET /config  -> return json of the http-server env settings.
* GET /health  -> return json of the health check response.
* POST /calc rate=NUM bal=NUM  -> Calculate end of year balance given the rate and current balance.

### Example Requests

httpie client examples below.

GET /help

```bash
> $ http http://localhost:8000/help
HTTP/1.1 200 OK
Content-Length: 310
Content-Type: text/plain; charset=utf-8
Date: Fri, 31 Jul 2020 01:28:43 GMT
Server: http-kit

Leave/PTO Calculator API. Send requests to:
GET / or GET /help -> This help dialog.
GET /config -> Config settings.
GET /health -> Application health check.
POST /calc rate=NUM bal=NUM -> Calculate end of year balance given the rate and current balance.
  Example: http http://HOST:8000/calc rate=3.0 bal=143.5
```

GET /config

```bash
> $ http http://localhost:8000/config
HTTP/1.1 200 OK
Content-Length: 32
Content-Type: application/json; charset=utf-8
Date: Fri, 31 Jul 2020 01:30:33 GMT
Server: http-kit

{
    "http-port": 8000,
    "max-bal": 150
}
```

GET /health

```bash
> $ http http://localhost:8000/health
HTTP/1.1 200 OK
Content-Length: 16
Content-Type: application/json; charset=utf-8
Date: Fri, 31 Jul 2020 01:31:38 GMT
Server: http-kit

{
    "healthy": true
}
```

POST /calc rate=NUM bal=NUM

```bash
> $ http http://localhost:8000/calc rate=3.0 bal=143.5
HTTP/1.1 200 OK
Content-Length: 151
Content-Type: application/json; charset=utf-8
Date: Fri, 31 Jul 2020 01:33:11 GMT
Server: http-kit

{
    "bal": 143.5,
    "end-year-bal": 209.5,
    "lost-if-none-taken": 59.5,
    "max-bal": 150,
    "rate": 3.0,
    "today": "2020-07-30",
    "use-per-week": 2.7,
    "weeks-left-in-year": 22.0
}
```

## License

Copyright © 2020 Bill Howe

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
`http://www.eclipse.org/legal/epl-2.0`.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at `https://www.gnu.org/software/classpath/license.html`.

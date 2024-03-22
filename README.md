# libraries
- each child project as a module libraries
- using common gradle at root folder libraries
- using nexus on docker compose file on the same level with the be folder
# publish libraries on nexus
- cd <library folder want to deploy>
  - example: cd web-utils
- run command line: <h2><i>../gradlew clean build publishToNexusRepo</i><h2/>
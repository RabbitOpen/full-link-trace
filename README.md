mvn versions:set -DnewVersion={新的版本}



#sonar 扫描
mvn clean install sonar:sonar

#部署
mvn clean install -Pdeploy

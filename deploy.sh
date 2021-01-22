git pull
mvn clean package
sudo cp target/*.war /opt/tomcat/webapps/Checkers.war
sudo systemctl restart tomcat

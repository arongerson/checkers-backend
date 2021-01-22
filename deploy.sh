git pull
mvn clean package
sudo su
cp target/*.war /opt/tomcat/webapps/Checkers.war
sudo systemctl restart tomcat

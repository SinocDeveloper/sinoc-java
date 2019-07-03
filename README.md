# sinoc-java

Directory structure description

The main directory structure of the Sinoc code is as follows:

maxmind
pom.xml
src
start_node.bat
start_node.sh
user.conf


	maxmind: Used to store the google IP address library. You can reconfigure the path in the configuration file to display the distribution information of the linked nodes in the node console. It may not exist and does not affect the startup.
	pom.xml: maven configuration file
	src: Sinoc project 
	start_node.bat: command line for windows startup project
	start_node.sh: command line for linux startup project
	user.conf: node function configuration file

How to compile
The local SINOC is written in pure java, so to compile the project, you need to install java, the minimum version is 1.8, and the project is tested and compiled under OpenJDK and oracleJDK.

The project itself is a maven project. After properly installing java, you need to install maven. The project is successfully compiled under maven version 3.3.1. The compiled command is to run mvc install in the source code directory.

Hint: The high version of java and maven have no effect on compilation and operation, but the java version is at least 1.8.

After the code is successfully compiled, the target directory appears, where sinoc-core-shell-0.0.1-SNAPSHOT.jar is the product jar package.

Start_node
According to the current default configuration, run start_node directly. In fact, the startup process is not complicated. Switch to the directory where the product package is located and enter the command directly on the command line or linux terminal:

$ java -jar -Xms1024m -Xmx4096m -Dsinoc.conf.file="./user.conf" sinoc-core-shell-0.0.1-SNAPSHOT.jar --maxmind.file=./maxmind/GeoIP.dat
The key parameter -Dsinoc.conf.file specifies the path to the external configuration file, --maxmind.file specifies the file path to store the IP library (this optional)

Key configuration
Open the default configuration file user.conf

Configure the rpc port (note: the default is 8866) and call the IP whitelist of rpc. The default is the local call.

Configure console parameters. For node security, it is usually recommended to turn off the console.
Enable indicates whether to start the console, port is the open port, username is the console username, password console password

After launching the console, enter http://127.0.0.1 in the browser to enter the console.

Rpc call method
Rpc is compatible with some methods of web3 (or web3j), but some commonly used functions such as balance inquiry, sending transaction, and transfer interface have been implemented. Please pay attention to the configuration of IP whitelist when calling web3 interface, because there is no Upgrade to the release version, the network protocol calling web3 only supports http, the sample code is as follows:

New Web3(new Web3.providers.HttpProvider('http://192.168.1.xx:8866'))


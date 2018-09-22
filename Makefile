default:
	javac src/PlayerClient.java src/ZookeeperWatcher.java src/Player.java -cp "lib/*"
	
	java -cp "src/;lib/*" PlayerClient
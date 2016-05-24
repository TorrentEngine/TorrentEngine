mkdir -p classes

javac -cp `sh getclasspath.sh lib`:. -d classes `find src -type f -name "*.java"`


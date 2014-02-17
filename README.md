How to install opencv :
 
sudo apt-get install cmake ant
wget http://sourceforge.net/projects/opencvlibrary/files/opencv-unix/2.4.8/opencv-2.4.8.zip/download -O opencv.zip
unzip opencv.zip -d .
cd opencv-2.4.8/
mkdir release
cd release/
sudo cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local ..
sudo make -j
sudo make install
cd ../..
sudo rm -rf opencv-2.4.8/
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=/usr/local/share/OpenCV/java/opencv-248.jar -DgroupId=org.opencv -DartifactId=opencv -Dversion=2.4.8 -Dpackaging=jar
